'use client';

import React, { useState } from 'react';
import {
  DndContext,
  DragEndEvent,
  DragOverlay,
  DragStartEvent,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import { Badge } from '@/components/ui/Badge';
import type { TaskResponse, TaskStatus, TaskUrgency } from '@/types';
import type { KanbanMap } from '@/services/tasks';

const COLUMNS: { status: TaskStatus; label: string }[] = [
  { status: 'WAITING', label: '대기' },
  { status: 'IN_PROGRESS', label: '진행중' },
  { status: 'PENDING', label: '보류' },
  { status: 'REVIEW', label: '검토' },
  { status: 'DONE', label: '완료' },
];

interface KanbanBoardProps {
  kanbanData: KanbanMap;
  onTaskClick: (task: TaskResponse) => void;
  onStatusChange: (taskId: number, newStatus: TaskStatus) => Promise<void>;
}

export function KanbanBoard({ kanbanData, onTaskClick, onStatusChange }: KanbanBoardProps) {
  const [activeTask, setActiveTask] = useState<TaskResponse | null>(null);
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8, // 8px 이상 드래그해야 시작 (클릭과 구분)
      },
    })
  );

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

  const getUrgencyBadgeVariant = (urgency: TaskUrgency): 'default' | 'success' | 'warning' | 'danger' | 'info' => {
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

  const handleDragStart = (event: DragStartEvent) => {
    const { active } = event;
    const taskId = active.id as number;
    // 모든 컬럼에서 찾기
    for (const column of COLUMNS) {
      const task = getTasksByStatus(column.status).find((t) => t.id === taskId);
      if (task) {
        setActiveTask(task);
        break;
      }
    }
  };

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveTask(null);

    if (!over) return;

    const taskId = active.id as number;
    const newStatus = over.id as TaskStatus;

    // 상태 변경
    await onStatusChange(taskId, newStatus);
  };

  return (
    <DndContext sensors={sensors} onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
      <div className="flex gap-4 h-full pb-4 overflow-x-auto snap-x snap-mandatory" style={{ minWidth: 'max-content' }}>
        {COLUMNS.map((column) => {
          const columnTasks = getTasksByStatus(column.status);
          return (
            <div
              key={column.status}
              id={column.status}
              className="flex-1 bg-gray-50 dark:bg-gray-800/50 rounded-lg p-4 flex flex-col min-w-[280px] snap-start"
            >
              <div className="mb-4">
                <div className="flex items-center justify-between">
                  <h3 className="font-semibold text-gray-900 dark:text-white">{column.label}</h3>
                  <span className="px-2 py-1 bg-white dark:bg-gray-800 rounded-full text-sm font-medium">
                    {columnTasks.length}
                  </span>
                </div>
              </div>

              <DroppableColumn status={column.status}>
                <div className="space-y-3 overflow-y-auto flex-1">
                  {columnTasks.length === 0 ? (
                    <p className="text-center text-sm text-gray-400 py-4">없음</p>
                  ) : (
                    columnTasks.map((task) => (
                      <DraggableTaskCard
                        key={task.id}
                        task={task}
                        onClick={() => onTaskClick(task)}
                        getUrgencyBadgeVariant={getUrgencyBadgeVariant}
                        getUrgencyLabel={getUrgencyLabel}
                      />
                    ))
                  )}
                </div>
              </DroppableColumn>
            </div>
          );
        })}
      </div>

      <DragOverlay>
        {activeTask && (
          <div className="bg-white dark:bg-gray-800 rounded-lg p-4 shadow-2xl border border-blue-500 opacity-90 w-64">
            <div className="flex items-center gap-2 mb-2">
              <Badge variant={getUrgencyBadgeVariant(activeTask.urgency)}>
                {getUrgencyLabel(activeTask.urgency)}
              </Badge>
              <span className="text-xs text-gray-500">{activeTask.type}</span>
            </div>
            <h4 className="font-medium text-gray-900 dark:text-white mb-2 line-clamp-2">
              {activeTask.title}
            </h4>
          </div>
        )}
      </DragOverlay>
    </DndContext>
  );
}

// 드롭 영역 (상태별 컬럼)
function DroppableColumn({ status, children }: { status: TaskStatus; children: React.ReactNode }) {
  const { setNodeRef } = useDroppable({ id: status });
  return (
    <div ref={setNodeRef} className="flex-1 flex flex-col">
      {children}
    </div>
  );
}

// 드래그 가능한 태스크 카드
function DraggableTaskCard({
  task,
  onClick,
  getUrgencyBadgeVariant,
  getUrgencyLabel,
}: {
  task: TaskResponse;
  onClick: () => void;
  getUrgencyBadgeVariant: (urgency: TaskUrgency) => 'default' | 'success' | 'warning' | 'danger' | 'info';
  getUrgencyLabel: (urgency: TaskUrgency) => string;
}) {
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
    id: task.id,
  });

  const style = transform
    ? {
        transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
        opacity: isDragging ? 0.5 : 1,
        cursor: isDragging ? 'grabbing' : 'grab',
      }
    : { cursor: 'grab' };

  const handleClick = (e: React.MouseEvent) => {
    // 드래그 중이 아닐 때만 클릭 이벤트 처리
    if (!isDragging) {
      onClick();
    }
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      onClick={handleClick}
      className="bg-white dark:bg-gray-800 rounded-lg p-4 shadow-sm border border-gray-200 dark:border-gray-700 hover:shadow-md transition-shadow cursor-pointer"
    >
      <div className="flex items-center gap-2 mb-2">
        <span {...listeners} className="cursor-grab active:cursor-grabbing text-gray-400 hover:text-gray-600" title="드래그하여 이동">⠿</span>
        <Badge variant={getUrgencyBadgeVariant(task.urgency)}>
          {getUrgencyLabel(task.urgency)}
        </Badge>
        <span className="text-xs text-gray-500">{task.type}</span>
      </div>
      <h4 className="font-medium text-gray-900 dark:text-white mb-2 line-clamp-2">
        {task.title}
      </h4>
      {task.tags && task.tags.length > 0 && (
        <div className="flex flex-wrap gap-1 mb-3">
          {task.tags.map((t) => (
            <span
              key={t.id}
              className="px-2 py-0.5 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 text-xs rounded"
            >
              {t.tag}
            </span>
          ))}
        </div>
      )}
      <div className="flex items-center justify-between text-xs text-gray-500">
        <div className="flex items-center gap-1">
          {task.assigneeId && (
            <div className="flex items-center gap-1 bg-blue-50 dark:bg-blue-900/20 px-2 py-1 rounded">
              <span>👤</span>
              <span className="font-medium text-blue-700 dark:text-blue-300">담당자 #{task.assigneeId}</span>
            </div>
          )}
        </div>
        {task.dueDate && <span>⏰ {task.dueDate}</span>}
      </div>
      {(task.slaResponseBreached || task.slaResolveBreached) && (
        <div className="mt-2">
          <Badge variant="danger">SLA 위반</Badge>
        </div>
      )}
    </div>
  );
}

// useDroppable 훅
import { useDroppable } from '@dnd-kit/core';

// useDraggable 훅
import { useDraggable } from '@dnd-kit/core';
