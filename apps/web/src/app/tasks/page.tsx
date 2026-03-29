'use client';

import React, { useState, useEffect } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { Avatar } from '@/components/ui/Avatar';
import { Modal } from '@/components/ui/Modal';
import { Task, TaskStatus, Priority } from '@/types';
import toast from 'react-hot-toast';

const COLUMNS: { status: TaskStatus; label: string; color: string }[] = [
  { status: 'TODO', label: '대기', color: 'bg-gray-100' },
  { status: 'IN_PROGRESS', label: '진행중', color: 'bg-blue-100' },
  { status: 'REVIEW', label: '검토', color: 'bg-yellow-100' },
  { status: 'DONE', label: '완료', color: 'bg-green-100' },
];

export default function TasksPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [filter, setFilter] = useState({ type: 'ALL', priority: 'ALL', assignee: 'ALL' });

  useEffect(() => {
    fetchTasks();
  }, [filter]);

  const fetchTasks = async () => {
    // Mock data
    const mockTasks: Task[] = [
      {
        id: '1',
        title: '결제 시스템 버그 수정',
        description: '특정 카드로 결제 시 오류 발생',
        status: 'IN_PROGRESS',
        type: 'BUG',
        priority: 'HIGH',
        assigneeId: 'user1',
        assigneeName: '김개발',
        tags: ['결제', 'backend'],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        slaDeadline: new Date(Date.now() + 86400000).toISOString(),
      },
      {
        id: '2',
        title: '회원가입 화면 개선',
        description: 'UI/UX 개선 작업',
        status: 'TODO',
        type: 'FEATURE',
        priority: 'MEDIUM',
        assigneeId: 'user2',
        assigneeName: '이디자인',
        tags: ['UI', 'frontend'],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
      {
        id: '3',
        title: '고객 문의 답변',
        description: '로그인 관련 문의',
        status: 'REVIEW',
        type: 'INQUIRY',
        priority: 'LOW',
        assigneeId: 'user3',
        assigneeName: '박상담',
        tags: ['문의'],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    ];
    setTasks(mockTasks);
  };

  const getTasksByStatus = (status: TaskStatus) => {
    return tasks.filter((task) => task.status === status);
  };

  const getPriorityBadgeVariant = (priority: Priority) => {
    switch (priority) {
      case 'URGENT':
        return 'danger';
      case 'HIGH':
        return 'warning';
      case 'MEDIUM':
        return 'info';
      case 'LOW':
        return 'default';
      default:
        return 'default';
    }
  };

  const getPriorityLabel = (priority: Priority) => {
    const labels: Record<Priority, string> = {
      URGENT: '긴급',
      HIGH: '높음',
      MEDIUM: '보통',
      LOW: '낮음',
    };
    return labels[priority];
  };

  const handleCreateTask = () => {
    toast.success('업무가 생성되었습니다');
    setIsCreateModalOpen(false);
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

        {/* Filters */}
        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-200 mb-4">
          <div className="flex flex-wrap gap-3">
            <select
              value={filter.type}
              onChange={(e) => setFilter({ ...filter, type: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">전체 유형</option>
              <option value="GENERAL">일반</option>
              <option value="BUG">버그</option>
              <option value="FEATURE">기능</option>
              <option value="INQUIRY">문의</option>
            </select>

            <select
              value={filter.priority}
              onChange={(e) => setFilter({ ...filter, priority: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">전체 긴급도</option>
              <option value="URGENT">긴급</option>
              <option value="HIGH">높음</option>
              <option value="MEDIUM">보통</option>
              <option value="LOW">낮음</option>
            </select>

            <select
              value={filter.assignee}
              onChange={(e) => setFilter({ ...filter, assignee: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">전체 담당자</option>
              <option value="me">내 업무</option>
            </select>
          </div>
        </div>

        {/* Kanban Board */}
        <div className="flex-1 overflow-x-auto">
          <div className="flex gap-4 h-full" style={{ minWidth: '800px' }}>
            {COLUMNS.map((column) => {
              const columnTasks = getTasksByStatus(column.status);
              return (
                <div
                  key={column.status}
                  className="flex-1 bg-gray-50 rounded-lg p-4 flex flex-col min-w-[250px]"
                >
                  {/* Column Header */}
                  <div className="mb-4">
                    <div className="flex items-center justify-between">
                      <h3 className="font-semibold text-gray-900">{column.label}</h3>
                      <span className="px-2 py-1 bg-white rounded-full text-sm font-medium">
                        {columnTasks.length}
                      </span>
                    </div>
                  </div>

                  {/* Tasks */}
                  <div className="space-y-3 overflow-y-auto custom-scrollbar flex-1">
                    {columnTasks.map((task) => (
                      <div
                        key={task.id}
                        onClick={() => setSelectedTask(task)}
                        className="task-card bg-white rounded-lg p-4 shadow-sm border border-gray-200"
                      >
                        {/* Priority & Type */}
                        <div className="flex items-center gap-2 mb-2">
                          <Badge variant={getPriorityBadgeVariant(task.priority)}>
                            {getPriorityLabel(task.priority)}
                          </Badge>
                          <span className="text-xs text-gray-500">{task.type}</span>
                        </div>

                        {/* Title */}
                        <h4 className="font-medium text-gray-900 mb-2">{task.title}</h4>

                        {/* Tags */}
                        {task.tags.length > 0 && (
                          <div className="flex flex-wrap gap-1 mb-3">
                            {task.tags.map((tag) => (
                              <span
                                key={tag}
                                className="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded"
                              >
                                {tag}
                              </span>
                            ))}
                          </div>
                        )}

                        {/* Footer */}
                        <div className="flex items-center justify-between">
                          {task.assigneeName && (
                            <Avatar name={task.assigneeName} size="sm" />
                          )}
                          {task.slaDeadline && (
                            <span className="text-xs text-gray-500">
                              ⏰ {new Date(task.slaDeadline).toLocaleDateString('ko-KR')}
                            </span>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

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
                <div className="flex items-center gap-2 mb-4">
                  <Badge variant={getPriorityBadgeVariant(selectedTask.priority)}>
                    {getPriorityLabel(selectedTask.priority)}
                  </Badge>
                  <Badge>{selectedTask.type}</Badge>
                  <Badge variant="info">{selectedTask.status}</Badge>
                </div>
              </div>

              <div>
                <h3 className="font-semibold mb-2">설명</h3>
                <p className="text-gray-700">{selectedTask.description}</p>
              </div>

              {selectedTask.assigneeName && (
                <div>
                  <h3 className="font-semibold mb-2">담당자</h3>
                  <div className="flex items-center gap-2">
                    <Avatar name={selectedTask.assigneeName} size="sm" />
                    <span>{selectedTask.assigneeName}</span>
                  </div>
                </div>
              )}

              {selectedTask.tags.length > 0 && (
                <div>
                  <h3 className="font-semibold mb-2">태그</h3>
                  <div className="flex flex-wrap gap-2">
                    {selectedTask.tags.map((tag) => (
                      <Badge key={tag}>{tag}</Badge>
                    ))}
                  </div>
                </div>
              )}

              <div className="flex gap-2">
                <button className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
                  상태 변경
                </button>
                <button className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50">
                  코멘트 추가
                </button>
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
              <label className="block text-sm font-medium text-gray-700 mb-1">제목</label>
              <input
                type="text"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="업무 제목을 입력하세요"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">설명</label>
              <textarea
                rows={4}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="업무 설명을 입력하세요"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">유형</label>
                <select className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="GENERAL">일반</option>
                  <option value="BUG">버그</option>
                  <option value="FEATURE">기능</option>
                  <option value="INQUIRY">문의</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">긴급도</label>
                <select className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="LOW">낮음</option>
                  <option value="MEDIUM">보통</option>
                  <option value="HIGH">높음</option>
                  <option value="URGENT">긴급</option>
                </select>
              </div>
            </div>

            <div className="flex gap-2">
              <button
                onClick={handleCreateTask}
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                생성
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
