'use client';

import React from 'react';
import { Badge } from '@/components/ui/Badge';
import type { TaskResponse, TaskUrgency, TaskStatus } from '@/types';

interface TaskListViewProps {
  tasks: TaskResponse[];
  onTaskClick: (task: TaskResponse) => void;
}

export function TaskListView({ tasks, onTaskClick }: TaskListViewProps) {
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

  const getStatusBadgeVariant = (status: TaskStatus) => {
    switch (status) {
      case 'DONE':
        return 'success';
      case 'IN_PROGRESS':
        return 'info';
      case 'REVIEW':
        return 'warning';
      case 'PENDING':
        return 'default';
      case 'WAITING':
        return 'default';
      default:
        return 'default';
    }
  };

  const getStatusLabel = (status: TaskStatus) => {
    const labels: Record<TaskStatus, string> = {
      WAITING: '대기',
      IN_PROGRESS: '진행중',
      PENDING: '보류',
      REVIEW: '검토',
      DONE: '완료',
    };
    return labels[status] || status;
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                ID
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                제목
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                상태
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                긴급도
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                유형
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                담당자
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                생성일
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {tasks.map((task) => (
              <tr
                key={task.id}
                onClick={() => onTaskClick(task)}
                className="hover:bg-gray-50 cursor-pointer"
              >
                <td className="px-4 py-3 text-sm text-gray-900">#{task.id}</td>
                <td className="px-4 py-3">
                  <div className="text-sm font-medium text-gray-900 line-clamp-1">
                    {task.title}
                  </div>
                  {task.description && (
                    <div className="text-xs text-gray-500 line-clamp-1">
                      {task.description}
                    </div>
                  )}
                </td>
                <td className="px-4 py-3">
                  <Badge variant={getStatusBadgeVariant(task.status)}>
                    {getStatusLabel(task.status)}
                  </Badge>
                </td>
                <td className="px-4 py-3">
                  <Badge variant={getUrgencyBadgeVariant(task.urgency)}>
                    {getUrgencyLabel(task.urgency)}
                  </Badge>
                </td>
                <td className="px-4 py-3 text-sm text-gray-600">{task.type}</td>
                <td className="px-4 py-3 text-sm text-gray-600">
                  {task.assigneeId ? `#${task.assigneeId}` : '-'}
                </td>
                <td className="px-4 py-3 text-sm text-gray-500">
                  {new Date(task.createdAt).toLocaleDateString('ko-KR')}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {tasks.length === 0 && (
        <div className="text-center py-12 text-gray-500">
          <p>태스크가 없습니다</p>
        </div>
      )}
    </div>
  );
}
