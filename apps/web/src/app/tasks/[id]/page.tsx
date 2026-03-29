'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { api } from '@/lib/api';
import type { Task, TaskStatus, Member } from '@/types';

const STATUS_LABELS: Record<TaskStatus, string> = {
  WAITING: '대기',
  IN_PROGRESS: '진행중',
  REVIEW: '리뷰',
  DONE: '완료',
};

export default function TaskDetailPage() {
  const params = useParams();
  const router = useRouter();
  const taskId = Number(params.id);

  const [task, setTask] = useState<Task | null>(null);
  const [members, setMembers] = useState<Member[]>([]);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    loadTask();
    loadMembers();
  }, [taskId]);

  async function loadTask() {
    const response = await api.getTask(taskId);
    if (response.success && response.data) {
      setTask(response.data);
    }
    setLoading(false);
  }

  async function loadMembers() {
    const response = await api.getMembers();
    if (response.success && response.data) {
      setMembers(response.data);
    }
  }

  async function updateTaskStatus(newStatus: TaskStatus) {
    if (!task) return;
    
    setUpdating(true);
    const response = await api.updateTask(taskId, { status: newStatus });
    if (response.success && response.data) {
      setTask(response.data);
    }
    setUpdating(false);
  }

  async function assignToMember(memberId: number) {
    setUpdating(true);
    const response = await api.assignTask(taskId, memberId);
    if (response.success) {
      await loadTask();
    }
    setUpdating(false);
  }

  async function toggleChecklist(checklistId: number) {
    if (!task) return;

    const checklist = task.checklists?.find(c => c.id === checklistId);
    if (!checklist) return;

    // 백엔드 API 필요 - 임시로 로컬 상태만 업데이트
    setTask({
      ...task,
      checklists: task.checklists?.map(c =>
        c.id === checklistId ? { ...c, isDone: !c.isDone } : c
      ),
    });
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">로딩 중...</div>
      </div>
    );
  }

  if (!task) {
    return (
      <div className="flex flex-col items-center justify-center h-64 space-y-4">
        <div className="text-gray-500">업무를 찾을 수 없습니다</div>
        <Link href="/tasks" className="text-primary-600 hover:text-primary-700">
          목록으로 돌아가기
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* 헤더 */}
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <Link href="/tasks" className="text-sm text-gray-600 hover:text-gray-900 mb-2 inline-block">
            ← 목록으로
          </Link>
          <h1 className="text-2xl font-bold text-gray-900">{task.title}</h1>
          <div className="flex items-center space-x-2 mt-2">
            <span className="px-3 py-1 bg-blue-100 text-blue-700 text-sm font-medium rounded">
              {task.type}
            </span>
            <span className={`
              px-3 py-1 text-sm font-medium rounded
              ${task.urgency === 'CRITICAL' ? 'bg-red-100 text-red-700' :
                task.urgency === 'HIGH' ? 'bg-yellow-100 text-yellow-700' :
                task.urgency === 'NORMAL' ? 'bg-blue-100 text-blue-700' :
                'bg-gray-100 text-gray-700'}
            `}>
              {task.urgency}
            </span>
          </div>
        </div>

        <button
          onClick={() => router.push(`/tasks/${taskId}/edit`)}
          className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
        >
          수정
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 메인 정보 */}
        <div className="lg:col-span-2 space-y-6">
          {/* 설명 */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">설명</h2>
            <p className="text-gray-700 whitespace-pre-wrap">
              {task.description || '설명이 없습니다.'}
            </p>
          </div>

          {/* AI 요약 */}
          {task.aiSummary && (
            <div className="bg-blue-50 rounded-lg p-6">
              <div className="flex items-center space-x-2 mb-2">
                <span className="text-xl">🤖</span>
                <h2 className="text-lg font-semibold text-gray-900">AI 요약</h2>
              </div>
              <p className="text-gray-700">{task.aiSummary}</p>
            </div>
          )}

          {/* 체크리스트 */}
          {task.checklists && task.checklists.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">체크리스트</h2>
              <div className="space-y-2">
                {task.checklists
                  .sort((a, b) => a.sortOrder - b.sortOrder)
                  .map((item) => (
                    <label
                      key={item.id}
                      className="flex items-center space-x-3 p-2 hover:bg-gray-50 rounded cursor-pointer"
                    >
                      <input
                        type="checkbox"
                        checked={item.isDone}
                        onChange={() => toggleChecklist(item.id)}
                        className="w-5 h-5 text-primary-600 rounded"
                      />
                      <span className={`flex-1 ${item.isDone ? 'line-through text-gray-400' : 'text-gray-700'}`}>
                        {item.itemText}
                      </span>
                    </label>
                  ))}
              </div>
            </div>
          )}

          {/* 태그 */}
          {task.tags && task.tags.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">태그</h2>
              <div className="flex flex-wrap gap-2">
                {task.tags.map((tag) => (
                  <span
                    key={tag}
                    className="px-3 py-1 bg-gray-100 text-gray-700 text-sm rounded-full"
                  >
                    #{tag}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* 코멘트 */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">코멘트</h2>
            {task.comments && task.comments.length > 0 ? (
              <div className="space-y-4">
                {task.comments.map((comment) => (
                  <div key={comment.id} className="border-l-2 border-gray-200 pl-4">
                    <div className="flex items-center space-x-2 mb-1">
                      <span className="font-medium text-gray-900">{comment.authorName}</span>
                      <span className="text-xs text-gray-500">
                        {new Date(comment.createdAt).toLocaleString('ko-KR')}
                      </span>
                      {comment.isInternal && (
                        <span className="px-2 py-0.5 bg-yellow-100 text-yellow-700 text-xs rounded">
                          내부
                        </span>
                      )}
                    </div>
                    <p className="text-gray-700">{comment.content}</p>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500">코멘트가 없습니다.</p>
            )}
          </div>
        </div>

        {/* 사이드바 */}
        <div className="space-y-6">
          {/* 상태 */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-sm font-semibold text-gray-900 mb-3">상태</h3>
            <select
              value={task.status}
              onChange={(e) => updateTaskStatus(e.target.value as TaskStatus)}
              disabled={updating}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            >
              {Object.entries(STATUS_LABELS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          {/* 담당자 */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-sm font-semibold text-gray-900 mb-3">담당자</h3>
            {task.assigneeName ? (
              <div className="flex items-center space-x-2">
                <div className="w-8 h-8 bg-primary-500 rounded-full flex items-center justify-center text-white font-bold">
                  {task.assigneeName[0]}
                </div>
                <span className="text-gray-900">{task.assigneeName}</span>
              </div>
            ) : (
              <p className="text-gray-500 text-sm mb-3">할당되지 않음</p>
            )}
            <select
              onChange={(e) => {
                const memberId = Number(e.target.value);
                if (memberId) assignToMember(memberId);
              }}
              disabled={updating}
              className="w-full mt-2 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              defaultValue=""
            >
              <option value="">담당자 변경...</option>
              {members.map((member) => (
                <option key={member.id} value={member.id}>
                  {member.name}
                </option>
              ))}
            </select>
          </div>

          {/* 요청자 */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-sm font-semibold text-gray-900 mb-3">요청자</h3>
            <p className="text-gray-900">{task.requesterName}</p>
            <p className="text-sm text-gray-500">{task.requesterEmail}</p>
          </div>

          {/* 일정 */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-sm font-semibold text-gray-900 mb-3">일정</h3>
            <div className="space-y-2 text-sm">
              {task.dueDate && (
                <div>
                  <span className="text-gray-600">마감일:</span>
                  <span className="ml-2 text-gray-900">
                    {new Date(task.dueDate).toLocaleDateString('ko-KR')}
                  </span>
                </div>
              )}
              <div>
                <span className="text-gray-600">생성:</span>
                <span className="ml-2 text-gray-900">
                  {new Date(task.createdAt).toLocaleDateString('ko-KR')}
                </span>
              </div>
              {task.startedAt && (
                <div>
                  <span className="text-gray-600">시작:</span>
                  <span className="ml-2 text-gray-900">
                    {new Date(task.startedAt).toLocaleDateString('ko-KR')}
                  </span>
                </div>
              )}
              {task.completedAt && (
                <div>
                  <span className="text-gray-600">완료:</span>
                  <span className="ml-2 text-gray-900">
                    {new Date(task.completedAt).toLocaleDateString('ko-KR')}
                  </span>
                </div>
              )}
            </div>
          </div>

          {/* SLA */}
          {task.slaResolveDeadline && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-sm font-semibold text-gray-900 mb-3">SLA</h3>
              <div className="space-y-2 text-sm">
                <div className={task.slaResponseBreached ? 'text-red-600' : 'text-gray-700'}>
                  응답 기한: {task.slaRespondedAt ? '✓ 준수' : '⚠️ 초과'}
                </div>
                <div className={task.slaResolveBreached ? 'text-red-600' : 'text-gray-700'}>
                  해결 기한: {new Date(task.slaResolveDeadline).toLocaleString('ko-KR')}
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
