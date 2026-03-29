'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { api } from '@/lib/api';
import type { Task, TaskStatus, TaskUrgency } from '@/types';

const STATUS_LABELS: Record<TaskStatus, string> = {
  WAITING: '대기',
  IN_PROGRESS: '진행중',
  REVIEW: '리뷰',
  DONE: '완료',
};

const URGENCY_COLORS: Record<TaskUrgency, string> = {
  LOW: 'border-gray-300',
  NORMAL: 'border-blue-400',
  HIGH: 'border-yellow-400',
  CRITICAL: 'border-red-500',
};

export default function TasksPage() {
  const [tasksByStatus, setTasksByStatus] = useState<Record<TaskStatus, Task[]>>({
    WAITING: [],
    IN_PROGRESS: [],
    REVIEW: [],
    DONE: [],
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadTasks();
  }, []);

  async function loadTasks() {
    setLoading(true);
    const response = await api.getKanbanTasks();
    if (response.success && response.data) {
      setTasksByStatus(response.data as Record<TaskStatus, Task[]>);
    }
    setLoading(false);
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">업무 관리</h1>
          <p className="text-gray-600 mt-1">칸반 보드로 업무를 관리하세요</p>
        </div>
        <Link
          href="/tasks/new"
          className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
        >
          + 업무 생성
        </Link>
      </div>

      {/* 칸반 보드 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {(Object.entries(tasksByStatus) as [TaskStatus, Task[]][]).map(([status, tasks]) => (
          <div key={status} className="bg-gray-100 rounded-lg p-4">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-semibold text-gray-900">
                {STATUS_LABELS[status]}
              </h2>
              <span className="px-2 py-1 bg-gray-200 text-gray-700 text-xs font-medium rounded-full">
                {tasks.length}
              </span>
            </div>

            <div className="space-y-3">
              {tasks.map((task) => (
                <Link
                  key={task.id}
                  href={`/tasks/${task.id}`}
                  className={`
                    block bg-white rounded-lg p-4 shadow-sm 
                    hover:shadow-md transition-shadow
                    border-l-4 ${URGENCY_COLORS[task.urgency]}
                  `}
                >
                  <h3 className="font-medium text-gray-900 mb-2 line-clamp-2">
                    {task.title}
                  </h3>

                  <div className="flex items-center space-x-2 mb-2">
                    <span className="px-2 py-1 bg-blue-100 text-blue-700 text-xs font-medium rounded">
                      {task.type}
                    </span>
                    <span className={`
                      px-2 py-1 text-xs font-medium rounded
                      ${task.urgency === 'CRITICAL' ? 'bg-red-100 text-red-700' :
                        task.urgency === 'HIGH' ? 'bg-yellow-100 text-yellow-700' :
                        task.urgency === 'NORMAL' ? 'bg-blue-100 text-blue-700' :
                        'bg-gray-100 text-gray-700'}
                    `}>
                      {task.urgency}
                    </span>
                  </div>

                  {task.assigneeName && (
                    <div className="flex items-center space-x-2 text-sm text-gray-600">
                      <div className="w-6 h-6 bg-primary-500 rounded-full flex items-center justify-center text-white text-xs font-bold">
                        {task.assigneeName[0]}
                      </div>
                      <span>{task.assigneeName}</span>
                    </div>
                  )}

                  {task.dueDate && (
                    <div className="mt-2 text-xs text-gray-500">
                      📅 {new Date(task.dueDate).toLocaleDateString('ko-KR')}
                    </div>
                  )}
                </Link>
              ))}

              {tasks.length === 0 && (
                <div className="text-center py-8 text-gray-400 text-sm">
                  업무가 없습니다
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
