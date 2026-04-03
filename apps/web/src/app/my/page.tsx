'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { ErrorMessage } from '@/components/ui/ErrorMessage';
import { EmptyState } from '@/components/ui/EmptyState';
import { getTasks } from '@/services/tasks';
import { getNotifications } from '@/services/notifications';
import { changePassword } from '@/services/auth';
import api from '@/lib/api';
import type { TaskResponse, NotificationResponse, ApiResponse, PersonalNoteResponse, User } from '@/types';
import toast from 'react-hot-toast';

export default function MyPage() {
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [notes, setNotes] = useState<PersonalNoteResponse[]>([]);
  const [newNote, setNewNote] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  
  // Password change form
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  const loadData = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [tasksResult, notifResult] = await Promise.allSettled([
        getTasks({ page: 0, size: 50 }),
        getNotifications(0, 10, true),
      ]);

      if (tasksResult.status === 'fulfilled') {
        setTasks(tasksResult.value?.content ?? []);
      }
      if (notifResult.status === 'fulfilled') {
        setNotifications(notifResult.value?.content ?? []);
      }

      // Try fetching personal notes
      try {
        const { data } = await api.get<ApiResponse<PersonalNoteResponse[]>>('/personal-notes');
        if (data.success) setNotes(data.data);
      } catch {
        // Personal notes endpoint might not exist yet
      }
    } catch (err) {
      console.error('Failed to load my page data:', err);
      setError('데이터를 불러오지 못했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
    // Load current user info
    if (typeof window !== 'undefined') {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        try {
          setCurrentUser(JSON.parse(userStr));
        } catch {
          // Invalid user data
        }
      }
    }
  }, [loadData]);

  const handleAddNote = async () => {
    if (!newNote.trim()) return;
    try {
      const { data } = await api.post<ApiResponse<PersonalNoteResponse>>('/personal-notes', {
        content: newNote,
      });
      if (data.success) {
        setNotes([data.data, ...notes]);
      }
      setNewNote('');
      toast.success('메모가 추가되었습니다');
    } catch {
      toast.error('메모 추가에 실패했습니다.');
    }
  };

  const handleDeleteNote = async (id: number) => {
    try {
      await api.delete(`/personal-notes/${id}`);
      setNotes(notes.filter((n) => n.id !== id));
      toast.success('메모가 삭제되었습니다');
    } catch {
      toast.error('메모 삭제에 실패했습니다.');
    }
  };

  const handleChangePassword = async () => {
    if (!currentPassword || !newPassword || !confirmPassword) {
      toast.error('모든 필드를 입력해주세요');
      return;
    }
    if (newPassword !== confirmPassword) {
      toast.error('새 비밀번호가 일치하지 않습니다');
      return;
    }
    if (newPassword.length < 8) {
      toast.error('비밀번호는 최소 8자 이상이어야 합니다');
      return;
    }
    
    setIsChangingPassword(true);
    try {
      await changePassword({ currentPassword, newPassword });
      toast.success('비밀번호가 변경되었습니다');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err: unknown) {
      const error = err as { response?: { data?: { error?: string } } };
      if (error.response?.data?.error?.includes('incorrect')) {
        toast.error('현재 비밀번호가 올바르지 않습니다');
      } else {
        toast.error('비밀번호 변경에 실패했습니다');
      }
    } finally {
      setIsChangingPassword(false);
    }
  };

  const getUrgencyBadgeVariant = (urgency: string) => {
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

  const getStatusLabel = (status: string) => {
    const labels: Record<string, string> = {
      WAITING: '대기',
      IN_PROGRESS: '진행중',
      PENDING: '보류',
      REVIEW: '검토',
      DONE: '완료',
    };
    return labels[status] || status;
  };

  // Stats from tasks
  const totalTasks = tasks.length;
  const doneTasks = tasks.filter((t) => t.status === 'DONE').length;
  const inProgressTasks = tasks.filter((t) => t.status === 'IN_PROGRESS').length;
  const activeTasks = tasks.filter((t) => t.status !== 'DONE');

  const getRoleLabel = (role: string) => {
    switch (role) {
      case 'ADMIN':
        return '관리자';
      case 'AGENT':
        return '상담원';
      default:
        return role;
    }
  };

  return (
    <AppLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">내 업무</h1>
          <p className="text-gray-600 mt-1">나에게 할당된 업무와 현황을 확인하세요</p>
        </div>

        {isLoading && <LoadingSpinner text="데이터를 불러오는 중..." />}
        {error && <ErrorMessage message={error} onRetry={loadData} />}

        {!isLoading && !error && (
          <>
            {/* My Profile & Password Change */}
            <div className="grid lg:grid-cols-2 gap-6">
              <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                <div className="px-6 py-4 border-b border-gray-200">
                  <h2 className="text-lg font-semibold">내 프로필</h2>
                </div>
                <div className="p-6 space-y-4">
                  {currentUser ? (
                    <>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          이름
                        </label>
                        <div className="text-gray-900">{currentUser.name}</div>
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          이메일
                        </label>
                        <div className="text-gray-900">{currentUser.email}</div>
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          역할
                        </label>
                        <Badge>{getRoleLabel(currentUser.role)}</Badge>
                      </div>
                    </>
                  ) : (
                    <p className="text-gray-500 text-center py-4">
                      사용자 정보를 불러올 수 없습니다
                    </p>
                  )}
                </div>
              </div>

              <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                <div className="px-6 py-4 border-b border-gray-200">
                  <h2 className="text-lg font-semibold">비밀번호 변경</h2>
                </div>
                <div className="p-6 space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      현재 비밀번호
                    </label>
                    <input
                      type="password"
                      value={currentPassword}
                      onChange={(e) => setCurrentPassword(e.target.value)}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="현재 비밀번호를 입력하세요"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      새 비밀번호
                    </label>
                    <input
                      type="password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="새 비밀번호 (최소 8자)"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      새 비밀번호 확인
                    </label>
                    <input
                      type="password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="새 비밀번호를 다시 입력하세요"
                    />
                  </div>
                  <button
                    onClick={handleChangePassword}
                    disabled={isChangingPassword}
                    className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {isChangingPassword ? '변경 중...' : '비밀번호 변경'}
                  </button>
                </div>
              </div>
            </div>

            {/* KPI Cards */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
                <div className="text-3xl mb-2">📋</div>
                <div className="text-2xl font-bold text-gray-900">{totalTasks}</div>
                <div className="text-sm text-gray-600 mt-1">전체 업무</div>
              </div>
              <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
                <div className="text-3xl mb-2">🔄</div>
                <div className="text-2xl font-bold text-blue-600">{inProgressTasks}</div>
                <div className="text-sm text-gray-600 mt-1">진행 중</div>
              </div>
              <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
                <div className="text-3xl mb-2">✅</div>
                <div className="text-2xl font-bold text-green-600">{doneTasks}</div>
                <div className="text-sm text-gray-600 mt-1">완료</div>
              </div>
              <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
                <div className="text-3xl mb-2">📈</div>
                <div className="text-2xl font-bold text-purple-600">
                  {totalTasks > 0 ? Math.round((doneTasks / totalTasks) * 100) : 0}%
                </div>
                <div className="text-sm text-gray-600 mt-1">완료율</div>
              </div>
            </div>

            <div className="grid lg:grid-cols-2 gap-6">
              {/* Active Tasks */}
              <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                <div className="px-6 py-4 border-b border-gray-200">
                  <h2 className="text-lg font-semibold">진행 중인 업무 ({activeTasks.length})</h2>
                </div>
                <div className="p-6">
                  {activeTasks.length === 0 ? (
                    <EmptyState icon="🎉" title="진행 중인 업무가 없습니다" />
                  ) : (
                    <div className="space-y-4">
                      {activeTasks.slice(0, 10).map((task) => (
                        <div
                          key={task.id}
                          className="p-4 border border-gray-200 rounded-lg hover:bg-gray-50"
                        >
                          <div className="flex items-start justify-between mb-2">
                            <h3 className="font-medium text-gray-900 line-clamp-1">
                              {task.title}
                            </h3>
                            <Badge variant={getUrgencyBadgeVariant(task.urgency)}>
                              {task.urgency}
                            </Badge>
                          </div>
                          {task.description && (
                            <p className="text-sm text-gray-600 mb-2 line-clamp-1">
                              {task.description}
                            </p>
                          )}
                          <div className="flex items-center justify-between">
                            <Badge>{getStatusLabel(task.status)}</Badge>
                            {task.dueDate && (
                              <span className="text-xs text-gray-500">⏰ {task.dueDate}</span>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              {/* Personal Notes + Notifications */}
              <div className="space-y-6">
                {/* Notifications */}
                {notifications.length > 0 && (
                  <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                    <div className="px-6 py-4 border-b border-gray-200">
                      <h2 className="text-lg font-semibold">
                        읽지 않은 알림 ({notifications.length})
                      </h2>
                    </div>
                    <div className="p-6 space-y-3">
                      {notifications.slice(0, 5).map((notif) => (
                        <div
                          key={notif.id}
                          className="p-3 bg-blue-50 border border-blue-200 rounded-lg"
                        >
                          <h4 className="text-sm font-medium text-blue-900">{notif.title}</h4>
                          <p className="text-xs text-blue-700 mt-1">{notif.message}</p>
                          <p className="text-xs text-blue-500 mt-1">
                            {new Date(notif.createdAt).toLocaleString('ko-KR')}
                          </p>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Personal Notes */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                  <div className="px-6 py-4 border-b border-gray-200">
                    <h2 className="text-lg font-semibold">개인 메모</h2>
                  </div>
                  <div className="p-6">
                    <div className="mb-4 flex gap-2">
                      <input
                        type="text"
                        value={newNote}
                        onChange={(e) => setNewNote(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleAddNote()}
                        placeholder="메모를 입력하세요..."
                        className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                      <button
                        onClick={handleAddNote}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                      >
                        추가
                      </button>
                    </div>
                    {notes.length === 0 ? (
                      <p className="text-center text-sm text-gray-400 py-4">메모가 없습니다</p>
                    ) : (
                      <div className="space-y-3">
                        {notes.map((note) => (
                          <div
                            key={note.id}
                            className="p-3 border border-gray-200 rounded-lg hover:bg-gray-50"
                          >
                            <div className="flex items-start justify-between">
                              <p className="text-sm text-gray-900 flex-1">{note.content}</p>
                              <button
                                onClick={() => handleDeleteNote(note.id)}
                                className="text-gray-400 hover:text-red-600 text-sm ml-2"
                              >
                                ×
                              </button>
                            </div>
                            <p className="text-xs text-gray-400 mt-2">
                              {new Date(note.createdAt).toLocaleString('ko-KR')}
                            </p>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </>
        )}
      </div>
    </AppLayout>
  );
}
