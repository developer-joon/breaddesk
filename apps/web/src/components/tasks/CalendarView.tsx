'use client';

import React, { useState } from 'react';
import { Badge } from '@/components/ui/Badge';
import type { TaskResponse, TaskUrgency } from '@/types';

interface CalendarViewProps {
  tasks: TaskResponse[];
  onTaskClick?: (task: TaskResponse) => void;
}

export function CalendarView({ tasks, onTaskClick }: CalendarViewProps) {
  const [currentDate, setCurrentDate] = useState(new Date());

  const getMonthDays = () => {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay();

    const days: Array<{ date: number; tasks: TaskResponse[] }> = [];

    // Previous month's days
    for (let i = 0; i < startingDayOfWeek; i++) {
      days.push({ date: 0, tasks: [] });
    }

    // Current month's days
    for (let day = 1; day <= daysInMonth; day++) {
      const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const dayTasks = tasks.filter((task) => task.dueDate?.startsWith(dateStr));
      days.push({ date: day, tasks: dayTasks });
    }

    return days;
  };

  const navigateMonth = (direction: number) => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + direction, 1));
  };

  const getUrgencyColor = (urgency: TaskUrgency) => {
    const colors: Record<TaskUrgency, string> = {
      CRITICAL: 'bg-red-500',
      HIGH: 'bg-orange-500',
      NORMAL: 'bg-blue-500',
      LOW: 'bg-gray-500',
    };
    return colors[urgency] || 'bg-gray-500';
  };

  const monthDays = getMonthDays();
  const weekDays = ['일', '월', '화', '수', '목', '금', '토'];

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-semibold">
          {currentDate.getFullYear()}년 {currentDate.getMonth() + 1}월
        </h2>
        <div className="flex gap-2">
          <button
            onClick={() => navigateMonth(-1)}
            className="px-3 py-1 border border-gray-300 rounded-lg hover:bg-gray-50"
          >
            ◀
          </button>
          <button
            onClick={() => setCurrentDate(new Date())}
            className="px-3 py-1 border border-gray-300 rounded-lg hover:bg-gray-50"
          >
            오늘
          </button>
          <button
            onClick={() => navigateMonth(1)}
            className="px-3 py-1 border border-gray-300 rounded-lg hover:bg-gray-50"
          >
            ▶
          </button>
        </div>
      </div>

      {/* Calendar Grid */}
      <div className="grid grid-cols-7 gap-2">
        {/* Week day headers */}
        {weekDays.map((day, idx) => (
          <div
            key={idx}
            className={`text-center font-semibold text-sm py-2 ${
              idx === 0 ? 'text-red-600' : idx === 6 ? 'text-blue-600' : 'text-gray-700'
            }`}
          >
            {day}
          </div>
        ))}

        {/* Days */}
        {monthDays.map((day, idx) => (
          <div
            key={idx}
            className={`min-h-24 border border-gray-200 rounded-lg p-2 ${
              day.date === 0 ? 'bg-gray-50' : 'bg-white hover:bg-gray-50'
            } transition-colors`}
          >
            {day.date > 0 && (
              <>
                <div className="text-sm font-medium text-gray-700 mb-1">{day.date}</div>
                <div className="space-y-1">
                  {day.tasks.slice(0, 3).map((task) => (
                    <div
                      key={task.id}
                      onClick={() => onTaskClick?.(task)}
                      className="text-xs p-1 rounded bg-gray-100 hover:bg-gray-200 cursor-pointer truncate border-l-2"
                      style={{ borderLeftColor: getUrgencyColor(task.urgency) }}
                      title={task.title}
                    >
                      {task.title}
                    </div>
                  ))}
                  {day.tasks.length > 3 && (
                    <div className="text-xs text-gray-500 pl-1">
                      +{day.tasks.length - 3}개 더
                    </div>
                  )}
                </div>
              </>
            )}
          </div>
        ))}
      </div>

      {/* Legend */}
      <div className="mt-6 flex gap-4 justify-center text-xs">
        <div className="flex items-center gap-1">
          <div className="w-3 h-3 bg-red-500 rounded"></div>
          <span>긴급</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-3 h-3 bg-orange-500 rounded"></div>
          <span>높음</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-3 h-3 bg-blue-500 rounded"></div>
          <span>보통</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-3 h-3 bg-gray-500 rounded"></div>
          <span>낮음</span>
        </div>
      </div>
    </div>
  );
}
