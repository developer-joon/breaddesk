'use client';

import React from 'react';
import type { TaskUrgency, User } from '@/types';

export interface TaskFilterOptions {
  assigneeId: number | null;
  urgency: TaskUrgency[];
  type: string[];
}

interface TaskFiltersProps {
  filters: TaskFilterOptions;
  onFiltersChange: (filters: TaskFilterOptions) => void;
  members: User[];
}

const URGENCY_OPTIONS: { value: TaskUrgency; label: string }[] = [
  { value: 'LOW', label: '낮음' },
  { value: 'NORMAL', label: '보통' },
  { value: 'HIGH', label: '높음' },
  { value: 'CRITICAL', label: '긴급' },
];

const TYPE_OPTIONS = [
  { value: 'GENERAL', label: '일반' },
  { value: 'BUG', label: '버그' },
  { value: 'FEATURE', label: '기능' },
  { value: 'INQUIRY', label: '문의' },
];

export function TaskFilters({ filters, onFiltersChange, members }: TaskFiltersProps) {
  const handleAssigneeChange = (assigneeId: number | null) => {
    onFiltersChange({ ...filters, assigneeId });
  };

  const handleUrgencyToggle = (urgency: TaskUrgency) => {
    const newUrgency = filters.urgency.includes(urgency)
      ? filters.urgency.filter((u) => u !== urgency)
      : [...filters.urgency, urgency];
    onFiltersChange({ ...filters, urgency: newUrgency });
  };

  const handleTypeToggle = (type: string) => {
    const newType = filters.type.includes(type)
      ? filters.type.filter((t) => t !== type)
      : [...filters.type, type];
    onFiltersChange({ ...filters, type: newType });
  };

  const handleClearAll = () => {
    onFiltersChange({
      assigneeId: null,
      urgency: [],
      type: [],
    });
  };

  const hasActiveFilters =
    filters.assigneeId !== null || filters.urgency.length > 0 || filters.type.length > 0;

  const activeFilterCount =
    (filters.assigneeId !== null ? 1 : 0) + filters.urgency.length + filters.type.length;

  return (
    <div className="bg-white rounded-lg shadow p-4 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-semibold text-gray-900 flex items-center gap-2">
          필터
          {activeFilterCount > 0 && (
            <span className="px-2 py-0.5 bg-blue-100 text-blue-700 text-xs rounded-full">
              {activeFilterCount}
            </span>
          )}
        </h3>
        {hasActiveFilters && (
          <button
            onClick={handleClearAll}
            className="text-sm text-red-600 hover:text-red-700"
          >
            모두 지우기
          </button>
        )}
      </div>

      {/* Assignee Filter */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">담당자</label>
        <select
          value={filters.assigneeId || ''}
          onChange={(e) =>
            handleAssigneeChange(e.target.value ? parseInt(e.target.value) : null)
          }
          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
        >
          <option value="">전체</option>
          {members.map((member) => (
            <option key={member.id} value={member.id}>
              {member.name}
            </option>
          ))}
        </select>
      </div>

      {/* Urgency Filter */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">긴급도</label>
        <div className="space-y-2">
          {URGENCY_OPTIONS.map((option) => (
            <label key={option.value} className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={filters.urgency.includes(option.value)}
                onChange={() => handleUrgencyToggle(option.value)}
                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
              <span className="text-sm text-gray-700">{option.label}</span>
            </label>
          ))}
        </div>
      </div>

      {/* Type Filter */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">유형</label>
        <div className="space-y-2">
          {TYPE_OPTIONS.map((option) => (
            <label key={option.value} className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={filters.type.includes(option.value)}
                onChange={() => handleTypeToggle(option.value)}
                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
              <span className="text-sm text-gray-700">{option.label}</span>
            </label>
          ))}
        </div>
      </div>
    </div>
  );
}

export function ActiveFiltersChips({ filters, onFilterRemove }: {
  filters: TaskFilterOptions;
  onFilterRemove: (key: keyof TaskFilterOptions, value?: string | TaskUrgency) => void;
}) {
  const urgencyLabels: Record<TaskUrgency, string> = {
    LOW: '낮음',
    NORMAL: '보통',
    HIGH: '높음',
    CRITICAL: '긴급',
  };

  const typeLabels: Record<string, string> = {
    GENERAL: '일반',
    BUG: '버그',
    FEATURE: '기능',
    INQUIRY: '문의',
  };

  const chips: Array<{ label: string; onRemove: () => void }> = [];

  if (filters.assigneeId) {
    chips.push({
      label: `담당자: #${filters.assigneeId}`,
      onRemove: () => onFilterRemove('assigneeId'),
    });
  }

  filters.urgency.forEach((u) => {
    chips.push({
      label: urgencyLabels[u],
      onRemove: () => onFilterRemove('urgency', u),
    });
  });

  filters.type.forEach((t) => {
    chips.push({
      label: typeLabels[t] || t,
      onRemove: () => onFilterRemove('type', t),
    });
  });

  if (chips.length === 0) return null;

  return (
    <div className="flex flex-wrap gap-2">
      {chips.map((chip, index) => (
        <div
          key={index}
          className="inline-flex items-center gap-1 px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-sm"
        >
          <span>{chip.label}</span>
          <button
            onClick={chip.onRemove}
            className="hover:bg-blue-200 rounded-full p-0.5"
          >
            ✕
          </button>
        </div>
      ))}
    </div>
  );
}
