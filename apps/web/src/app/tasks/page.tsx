'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { ErrorMessage } from '@/components/ui/ErrorMessage';
import { EmptyState } from '@/components/ui/EmptyState';
import {
  getKanbanView,
  createTask,
  updateTaskStatus,
  addComment,
} from '@/services/tasks';
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
  const [kanbanData, setKanbanData] = useState<KanbanMap>({ waiting: [], inProgress: [], pending: [], review: [], done: [] });
  const [selectedTask, setSelectedTask] = useState<TaskResponse | null>(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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

  return (
    <AppLayout>
      <div className="h-full flex flex-col">
        {/* Header */}
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">업무 칸반</h1>
          </div>
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            + 새 업무
          </button>
        </div>

        {isLoading && <LoadingSpinner text="업무 칸반을 불러오는 중..." />}
        {error && <ErrorMessage message={error} onRetry={loadKanban} />}

        {!isLoading && !error && (
          <div className="flex-1 overflow-x-auto">
            <div className="flex gap-4 h-full" style={{ minWidth: '1000px' }}>
              {COLUMNS.map((column) => {
                const columnTasks = getTasksByStatus(column.status);
                return (
                  <div
                    key={column.status}
                    className="flex-1 bg-gray-50 rounded-lg p-4 flex flex-col min-w-[200px]"
                  >
                    <div className="mb-4">
                      <div className="flex items-center justify-between">
                        <h3 className="font-semibold text-gray-900">{column.label}</h3>
                        <span className="px-2 py-1 bg-white rounded-full text-sm font-medium">
                          {columnTasks.length}
                        </span>
                      </div>
                    </div>

                    <div className="space-y-3 overflow-y-auto flex-1">
                      {columnTasks.length === 0 ? (
                        <p className="text-center text-sm text-gray-400 py-4">없음</p>
                      ) : (
                        columnTasks.map((task) => (
                          <div
                            key={task.id}
                            onClick={() => setSelectedTask(task)}
                            className="bg-white rounded-lg p-4 shadow-sm border border-gray-200 cursor-pointer hover:shadow-md transition-shadow"
                          >
                            <div className="flex items-center gap-2 mb-2">
                              <Badge variant={getUrgencyBadgeVariant(task.urgency)}>
                                {getUrgencyLabel(task.urgency)}
                              </Badge>
                              <span className="text-xs text-gray-500">{task.type}</span>
                            </div>
                            <h4 className="font-medium text-gray-900 mb-2 line-clamp-2">
                              {task.title}
                            </h4>
                            {task.tags && task.tags.length > 0 && (
                              <div className="flex flex-wrap gap-1 mb-3">
                                {task.tags.map((t) => (
                                  <span
                                    key={t.id}
                                    className="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded"
                                  >
                                    {t.tag}
                                  </span>
                                ))}
                              </div>
                            )}
                            <div className="flex items-center justify-between text-xs text-gray-500">
                              {task.assigneeId && <span>👤 #{task.assigneeId}</span>}
                              {task.dueDate && <span>⏰ {task.dueDate}</span>}
                            </div>
                            {(task.slaResponseBreached || task.slaResolveBreached) && (
                              <div className="mt-2">
                                <Badge variant="danger">SLA 위반</Badge>
                              </div>
                            )}
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Task Detail Modal */}
        {selectedTask && (
          <Modal
            isOpen={!!selectedTask}
            onClose={() => setSelectedTask(null)}
            title="업무 상세"
            size="lg"
          >
            <div className="p-6 space-y-6">
              <div>
                <h2 className="text-xl font-bold mb-2">{selectedTask.title}</h2>
                <div className="flex items-center gap-2 mb-4 flex-wrap">
                  <Badge variant={getUrgencyBadgeVariant(selectedTask.urgency)}>
                    {getUrgencyLabel(selectedTask.urgency)}
                  </Badge>
                  <Badge>{selectedTask.type}</Badge>
                  <Badge variant="info">{selectedTask.status}</Badge>
                  {selectedTask.slaResponseBreached && (
                    <Badge variant="danger">응답 SLA 위반</Badge>
                  )}
                  {selectedTask.slaResolveBreached && (
                    <Badge variant="danger">해결 SLA 위반</Badge>
                  )}
                </div>
              </div>

              {selectedTask.description && (
                <div>
                  <h3 className="font-semibold mb-2">설명</h3>
                  <p className="text-gray-700 whitespace-pre-wrap">
                    {selectedTask.description}
                  </p>
                </div>
              )}

              {/* SLA Information */}
              {(selectedTask.slaResponseDeadline || selectedTask.slaResolveDeadline) && (
                <div className={`p-4 rounded-lg border ${
                  selectedTask.slaResponseBreached || selectedTask.slaResolveBreached
                    ? 'bg-red-50 border-red-200'
                    : selectedTask.status === 'PENDING'
                      ? 'bg-yellow-50 border-yellow-200'
                      : 'bg-green-50 border-green-200'
                }`}>
                  <div className="flex items-center gap-2 mb-3">
                    <span className="text-lg">📋</span>
                    <h3 className="font-semibold">SLA 정보</h3>
                    {selectedTask.status === 'PENDING' && <Badge variant="warning">SLA 일시정지됨</Badge>}
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    {selectedTask.slaResponseDeadline && (
                      <div>
                        <p className="text-gray-500 mb-1">응답 데드라인</p>
                        <p className="font-medium">{new Date(selectedTask.slaResponseDeadline).toLocaleString('ko-KR')}</p>
                        {selectedTask.slaResponseBreached
                          ? <Badge variant="danger">위반</Badge>
                          : <SlaCountdown deadline={selectedTask.slaResponseDeadline} isPaused={selectedTask.status === 'PENDING'} />}
                      </div>
                    )}
                    {selectedTask.slaResolveDeadline && (
                      <div>
                        <p className="text-gray-500 mb-1">해결 데드라인</p>
                        <p className="font-medium">{new Date(selectedTask.slaResolveDeadline).toLocaleString('ko-KR')}</p>
                        {selectedTask.slaResolveBreached
                          ? <Badge variant="danger">위반</Badge>
                          : <SlaCountdown deadline={selectedTask.slaResolveDeadline} isPaused={selectedTask.status === 'PENDING'} />}
                      </div>
                    )}
                  </div>
                </div>
              )}

              {selectedTask.aiSummary && (
                <div className="bg-cyan-50 p-4 rounded-lg">
                  <h3 className="font-semibold mb-2 text-cyan-800">🤖 AI 요약</h3>
                  <p className="text-sm text-cyan-700">{selectedTask.aiSummary}</p>
                </div>
              )}

              {/* Checklists */}
              {selectedTask.checklists && selectedTask.checklists.length > 0 && (
                <div>
                  <h3 className="font-semibold mb-2">체크리스트</h3>
                  <div className="space-y-2">
                    {selectedTask.checklists.map((cl) => (
                      <div key={cl.id} className="flex items-center gap-2">
                        <input type="checkbox" checked={cl.done} readOnly className="rounded" />
                        <span className={cl.done ? 'line-through text-gray-400' : ''}>
                          {cl.itemText}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Comments */}
              <div>
                <h3 className="font-semibold mb-2">코멘트</h3>
                {selectedTask.comments && selectedTask.comments.length > 0 ? (
                  <div className="space-y-3 mb-4">
                    {selectedTask.comments.map((c) => (
                      <div
                        key={c.id}
                        className={`p-3 rounded-lg ${
                          c.internal ? 'bg-yellow-50 border border-yellow-200' : 'bg-gray-50'
                        }`}
                      >
                        <div className="flex items-center justify-between mb-1">
                          <span className="text-xs font-medium text-gray-500">
                            #{c.authorId} {c.internal && '(내부)'}
                          </span>
                          <span className="text-xs text-gray-400">
                            {new Date(c.createdAt).toLocaleString('ko-KR')}
                          </span>
                        </div>
                        <p className="text-sm">{c.content}</p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-gray-400 mb-4">코멘트가 없습니다</p>
                )}
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleAddComment()}
                    placeholder="코멘트를 입력하세요..."
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                  />
                  <button
                    onClick={handleAddComment}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"
                  >
                    추가
                  </button>
                </div>
              </div>

              {/* Status Change */}
              <div className="flex gap-2 pt-4 border-t">
                <select
                  value={selectedTask.status}
                  onChange={(e) =>
                    handleStatusChange(selectedTask.id, e.target.value as TaskStatus)
                  }
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  {COLUMNS.map((col) => (
                    <option key={col.status} value={col.status}>
                      {col.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </Modal>
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
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
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
