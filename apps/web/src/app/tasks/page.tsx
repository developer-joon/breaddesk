'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { ErrorMessage } from '@/components/ui/ErrorMessage';
import { EmptyState } from '@/components/ui/EmptyState';
import { TaskDetailModal } from '@/components/tasks/TaskDetailModal';
import { KanbanBoard } from '@/components/tasks/KanbanBoard';
import { useFeaturesStore } from '@/stores/features';
import {
  getKanbanView,
  createTask,
  updateTaskStatus,
  addComment,
} from '@/services/tasks';
import { exportTasks } from '@/services/export';
import type { KanbanMap } from '@/services/tasks';
import type {
  TaskResponse,
  TaskStatus,
  TaskUrgency,
  TaskCommentResponse,
} from '@/types';
import toast from 'react-hot-toast';

function SlaCountdown({ deadline, isPaused }: { deadline: string; isPaused?: boolean }) {
  const [remaining, setRemaining] = useState('');

  useEffect(() => {
    if (isPaused) { setRemaining('일시정지'); return; }
    const update = () => {
      const diff = new Date(deadline).getTime() - Date.now();
      if (diff <= 0) { setRemaining('만료됨'); return; }
      const hours = Math.floor(diff / (1000 * 60 * 60));
      const mins = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      setRemaining(`${hours}시간 ${mins}분 남음`);
    };
    update();
    const timer = setInterval(update, 60_000);
    return () => clearInterval(timer);
  }, [deadline, isPaused]);

  const diff = new Date(deadline).getTime() - Date.now();
  const isUrgent = diff > 0 && diff < 2 * 60 * 60 * 1000;

  return (
    <span className={`text-xs font-medium ${isPaused ? 'text-yellow-600' : isUrgent ? 'text-red-600' : 'text-green-600'}`}>
      ⏱ {remaining}
    </span>
  );
}

const COLUMNS: { status: TaskStatus; label: string }[] = [
  { status: 'WAITING', label: '대기' },
  { status: 'IN_PROGRESS', label: '진행중' },
  { status: 'PENDING', label: '보류' },
  { status: 'REVIEW', label: '검토' },
  { status: 'DONE', label: '완료' },
];

export default function TasksPage() {
  const router = useRouter();
  const { isFeatureEnabled } = useFeaturesStore();
  const [kanbanData, setKanbanData] = useState<KanbanMap>({ waiting: [], inProgress: [], pending: [], review: [], done: [] });
  const [selectedTask, setSelectedTask] = useState<TaskResponse | null>(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Feature Flag 체크: kanban-tasks가 OFF면 dashboard로 리다이렉트
  useEffect(() => {
    if (!isFeatureEnabled('kanbanTasks')) {
      toast.error('칸반 업무 기능이 비활성화되어 있습니다.');
      router.push('/dashboard');
    }
  }, [isFeatureEnabled, router]);

  // Create form state
  const [newTitle, setNewTitle] = useState('');
  const [newDescription, setNewDescription] = useState('');
  const [newType, setNewType] = useState('GENERAL');
  const [newUrgency, setNewUrgency] = useState<TaskUrgency>('NORMAL');
  const [isCreating, setIsCreating] = useState(false);

  // Comment state
  const [newComment, setNewComment] = useState('');

  const loadKanban = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await getKanbanView();
      setKanbanData(data);
    } catch (err) {
      console.error('Failed to fetch kanban:', err);
      setError('업무 칸반을 불러오지 못했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadKanban();
  }, [loadKanban]);

  const statusToKey: Record<TaskStatus, keyof KanbanMap> = {
    WAITING: 'waiting',
    IN_PROGRESS: 'inProgress',
    PENDING: 'pending',
    REVIEW: 'review',
    DONE: 'done',
  };

  const getTasksByStatus = (status: TaskStatus): TaskResponse[] => {
    const key = statusToKey[status];
    return kanbanData[key] || [];
  };

  const getUrgencyBadgeVariant = (urgency: TaskUrgency) => {
    switch (urgency) {
      case 'CRITICAL':
        return 'danger';
      case 'HIGH':
        return 'warning';
      case 'NORMAL':
        return 'info';
      case 'LOW':
        return 'default';
      default:
        return 'default';
    }
  };

  const getUrgencyLabel = (urgency: TaskUrgency) => {
    const labels: Record<TaskUrgency, string> = {
      CRITICAL: '긴급',
      HIGH: '높음',
      NORMAL: '보통',
      LOW: '낮음',
    };
    return labels[urgency] || urgency;
  };

  const handleCreateTask = async () => {
    if (!newTitle.trim()) {
      toast.error('제목을 입력해주세요.');
      return;
    }

    setIsCreating(true);
    try {
      await createTask({
        title: newTitle,
        description: newDescription,
        type: newType,
        urgency: newUrgency,
      });
      toast.success('업무가 생성되었습니다');
      setIsCreateModalOpen(false);
      setNewTitle('');
      setNewDescription('');
      setNewType('GENERAL');
      setNewUrgency('NORMAL');
      await loadKanban();
    } catch {
      toast.error('업무 생성에 실패했습니다.');
    } finally {
      setIsCreating(false);
    }
  };

  const handleStatusChange = async (taskId: number, newStatus: TaskStatus) => {
    try {
      await updateTaskStatus(taskId, { status: newStatus });
      toast.success('상태가 변경되었습니다.');
      await loadKanban();
      if (selectedTask?.id === taskId) {
        setSelectedTask({ ...selectedTask, status: newStatus });
      }
    } catch {
      toast.error('상태 변경에 실패했습니다.');
    }
  };

  const handleAddComment = async () => {
    if (!newComment.trim() || !selectedTask) return;
    try {
      const comment = await addComment(selectedTask.id, { content: newComment });
      setSelectedTask({
        ...selectedTask,
        comments: [...(selectedTask.comments || []), comment],
      });
      setNewComment('');
      toast.success('코멘트가 추가되었습니다.');
    } catch {
      toast.error('코멘트 추가에 실패했습니다.');
    }
  };

  const handleExport = async () => {
    try {
      await exportTasks();
      toast.success('CSV 파일이 다운로드되었습니다');
    } catch (err) {
      toast.error('내보내기에 실패했습니다');
    }
  };

  return (
    <AppLayout>
      <div className="h-full flex flex-col">
        {/* Header */}
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">업무 칸반</h1>
          </div>
          <div className="flex gap-2">
            <button
              onClick={handleExport}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors text-sm font-medium"
            >
              📥 CSV 내보내기
            </button>
            <button
              onClick={() => setIsCreateModalOpen(true)}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              + 새 업무
            </button>
          </div>
        </div>

        {isLoading && <LoadingSpinner text="업무 칸반을 불러오는 중..." />}
        {error && <ErrorMessage message={error} onRetry={loadKanban} />}

        {!isLoading && !error && (
          <div className="flex-1 overflow-x-auto overflow-y-hidden">
            <KanbanBoard
              kanbanData={kanbanData}
              onTaskClick={setSelectedTask}
              onStatusChange={handleStatusChange}
            />
          </div>
        )}

        {/* Task Detail Modal */}
        {selectedTask && (
          <TaskDetailModal
            task={selectedTask}
            onClose={() => setSelectedTask(null)}
            onUpdate={loadKanban}
          />
        )}

        {/* Create Task Modal */}
        <Modal
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
          title="새 업무 생성"
          size="md"
        >
          <div className="p-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">제목 *</label>
              <input
                type="text"
                value={newTitle}
                onChange={(e) => setNewTitle(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="업무 제목을 입력하세요"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">설명</label>
              <textarea
                rows={4}
                value={newDescription}
                onChange={(e) => setNewDescription(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="업무 설명을 입력하세요"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">유형</label>
                <select
                  value={newType}
                  onChange={(e) => setNewType(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="GENERAL">일반</option>
                  <option value="BUG">버그</option>
                  <option value="FEATURE">기능</option>
                  <option value="INQUIRY">문의</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">긴급도 *</label>
                <select
                  value={newUrgency}
                  onChange={(e) => setNewUrgency(e.target.value as TaskUrgency)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="LOW">낮음</option>
                  <option value="NORMAL">보통</option>
                  <option value="HIGH">높음</option>
                  <option value="CRITICAL">긴급</option>
                </select>
              </div>
            </div>

            <div className="flex gap-2">
              <button
                onClick={handleCreateTask}
                disabled={isCreating}
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
              >
                {isCreating ? '생성 중...' : '생성'}
              </button>
              <button
                onClick={() => setIsCreateModalOpen(false)}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
              >
                취소
              </button>
            </div>
          </div>
        </Modal>
      </div>
    </AppLayout>
  );
}
