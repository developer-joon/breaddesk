'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { ErrorMessage } from '@/components/ui/ErrorMessage';
import { EmptyState } from '@/components/ui/EmptyState';
import { TaskListView } from '@/components/tasks/TaskListView';
import { TeamSelector } from '@/components/layout/TeamSelector';
import { useCurrentTeam } from '@/components/layout/Header';
import {
  getKanbanView,
  createTask,
  updateTaskStatus,
  addComment,
} from '@/services/tasks';
import type {
  TaskResponse,
  TaskStatus,
  TaskUrgency,
  TaskKanbanResponse,
  TaskCommentResponse,
} from '@/types';
import toast from 'react-hot-toast';

type ViewMode = 'kanban' | 'list';

const COLUMNS: { status: TaskStatus; label: string }[] = [
  { status: 'WAITING', label: '대기' },
  { status: 'IN_PROGRESS', label: '진행중' },
  { status: 'PENDING', label: '보류' },
  { status: 'REVIEW', label: '검토' },
  { status: 'DONE', label: '완료' },
];

export default function TasksPage() {
  const [kanbanData, setKanbanData] = useState<TaskKanbanResponse[]>([]);
  const [selectedTask, setSelectedTask] = useState<TaskResponse | null>(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [viewMode, setViewMode] = useState<ViewMode>('kanban');
  const { teamId } = useCurrentTeam();

  // Filters
  const [filterAssignee, setFilterAssignee] = useState<number | undefined>();
  const [filterUrgency, setFilterUrgency] = useState<TaskUrgency[]>([]);
  const [filterType, setFilterType] = useState<string>('');

  // Create form state
  const [newTitle, setNewTitle] = useState('');
  const [newDescription, setNewDescription] = useState('');
  const [newType, setNewType] = useState('GENERAL');
  const [newUrgency, setNewUrgency] = useState<TaskUrgency>('NORMAL');
  const [isCreating, setIsCreating] = useState(false);

  // Comment state
  const [newComment, setNewComment] = useState('');

  // Load view mode from localStorage
  useEffect(() => {
    const saved = localStorage.getItem('taskViewMode') as ViewMode;
    if (saved) setViewMode(saved);
  }, []);

  const handleViewModeChange = (mode: ViewMode) => {
    setViewMode(mode);
    localStorage.setItem('taskViewMode', mode);
  };

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

  const filterTasks = (tasks: TaskResponse[]): TaskResponse[] => {
    return tasks.filter((task) => {
      if (teamId && task.teamId !== teamId) return false;
      if (filterAssignee && task.assigneeId !== filterAssignee) return false;
      if (filterUrgency.length > 0 && !filterUrgency.includes(task.urgency)) return false;
      if (filterType && task.type !== filterType) return false;
      return true;
    });
  };

  const getTasksByStatus = (status: TaskStatus): TaskResponse[] => {
    const column = kanbanData.find((k) => k.status === status);
    const tasks = column?.tasks || [];
    return filterTasks(tasks);
  };

  const getAllTasks = (): TaskResponse[] => {
    const allTasks = kanbanData.flatMap((k) => k.tasks);
    return filterTasks(allTasks);
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
        <div className="mb-4">
          <div className="flex items-center justify-between mb-3">
            <h1 className="text-2xl font-bold text-gray-900">업무 관리</h1>
            <div className="flex items-center gap-2">
              {/* View Toggle */}
              <div className="flex border border-gray-300 rounded-lg overflow-hidden">
                <button
                  onClick={() => handleViewModeChange('kanban')}
                  className={`px-4 py-2 text-sm ${
                    viewMode === 'kanban'
                      ? 'bg-blue-600 text-white'
                      : 'bg-white text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  📊 칸반
                </button>
                <button
                  onClick={() => handleViewModeChange('list')}
                  className={`px-4 py-2 text-sm border-l border-gray-300 ${
                    viewMode === 'list'
                      ? 'bg-blue-600 text-white'
                      : 'bg-white text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  📋 목록
                </button>
              </div>
              <button
                onClick={() => setIsCreateModalOpen(true)}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                + 새 업무
              </button>
            </div>
          </div>

          {/* Filters */}
          <div className="flex items-center gap-3 flex-wrap">
            <TeamSelector value={teamId} onChange={() => {}} className="w-40" />
            
            <select
              value={filterAssignee || ''}
              onChange={(e) =>
                setFilterAssignee(e.target.value ? Number(e.target.value) : undefined)
              }
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">전체 담당자</option>
              {/* TODO: Load members dynamically */}
            </select>

            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">전체 유형</option>
              <option value="GENERAL">일반</option>
              <option value="TECHNICAL">기술</option>
              <option value="ACCOUNT">계정</option>
            </select>

            {/* Urgency multi-select (simplified) */}
            <div className="flex items-center gap-2">
              {['LOW', 'NORMAL', 'HIGH', 'CRITICAL'].map((urg) => (
                <label key={urg} className="flex items-center gap-1 text-sm">
                  <input
                    type="checkbox"
                    checked={filterUrgency.includes(urg as TaskUrgency)}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setFilterUrgency([...filterUrgency, urg as TaskUrgency]);
                      } else {
                        setFilterUrgency(filterUrgency.filter((u) => u !== urg));
                      }
                    }}
                    className="rounded"
                  />
                  <span className="text-xs">{getUrgencyLabel(urg as TaskUrgency)}</span>
                </label>
              ))}
            </div>

            {(teamId || filterAssignee || filterType || filterUrgency.length > 0) && (
              <button
                onClick={() => {
                  setFilterAssignee(undefined);
                  setFilterType('');
                  setFilterUrgency([]);
                }}
                className="text-sm text-blue-600 hover:underline"
              >
                필터 초기화
              </button>
            )}
          </div>
        </div>

        {isLoading && <LoadingSpinner text="업무 목록을 불러오는 중..." />}
        {error && <ErrorMessage message={error} onRetry={loadKanban} />}

        {!isLoading && !error && viewMode === 'list' && (
          <TaskListView tasks={getAllTasks()} onTaskClick={setSelectedTask} />
        )}

        {!isLoading && !error && viewMode === 'kanban' && (
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
