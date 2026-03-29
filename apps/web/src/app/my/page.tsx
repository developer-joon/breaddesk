'use client';

import React, { useState, useEffect } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { Avatar } from '@/components/ui/Avatar';
import { Task, MyKPI, PersonalNote } from '@/types';
import toast from 'react-hot-toast';

export default function MyPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [kpi, setKpi] = useState<MyKPI | null>(null);
  const [notes, setNotes] = useState<PersonalNote[]>([]);
  const [newNote, setNewNote] = useState('');

  useEffect(() => {
    fetchMyData();
  }, []);

  const fetchMyData = async () => {
    // Mock data
    setTasks([
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
    ]);

    setKpi({
      processedCount: 28,
      averageTimeMinutes: 45,
      resolvedCount: 24,
      period: '이번 주',
    });

    setNotes([
      {
        id: '1',
        content: '내일 회의 준비하기',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
      {
        id: '2',
        content: '고객 A 문의 재확인 필요',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    ]);
  };

  const handleAddNote = async () => {
    if (!newNote.trim()) return;

    const note: PersonalNote = {
      id: Date.now().toString(),
      content: newNote,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    setNotes([note, ...notes]);
    setNewNote('');
    toast.success('메모가 추가되었습니다');
  };

  const handleDeleteNote = (id: string) => {
    setNotes(notes.filter((note) => note.id !== id));
    toast.success('메모가 삭제되었습니다');
  };

  const getPriorityBadgeVariant = (priority: string) => {
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

  return (
    <AppLayout>
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-2xl font-bold text-gray-900">내 업무</h1>
          <p className="text-gray-600 mt-1">나에게 할당된 업무와 성과를 확인하세요</p>
        </div>

        {/* KPI Cards */}
        {kpi && (
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
              <div className="text-3xl mb-2">✅</div>
              <div className="text-2xl font-bold text-gray-900">{kpi.processedCount}</div>
              <div className="text-sm text-gray-600 mt-1">처리 건수</div>
              <div className="text-xs text-gray-400 mt-1">{kpi.period}</div>
            </div>

            <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
              <div className="text-3xl mb-2">⏱️</div>
              <div className="text-2xl font-bold text-blue-600">{kpi.averageTimeMinutes}분</div>
              <div className="text-sm text-gray-600 mt-1">평균 처리 시간</div>
              <div className="text-xs text-gray-400 mt-1">{kpi.period}</div>
            </div>

            <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
              <div className="text-3xl mb-2">🎯</div>
              <div className="text-2xl font-bold text-green-600">{kpi.resolvedCount}</div>
              <div className="text-sm text-gray-600 mt-1">해결 건수</div>
              <div className="text-xs text-gray-400 mt-1">{kpi.period}</div>
            </div>

            <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
              <div className="text-3xl mb-2">📈</div>
              <div className="text-2xl font-bold text-purple-600">
                {Math.round((kpi.resolvedCount / kpi.processedCount) * 100)}%
              </div>
              <div className="text-sm text-gray-600 mt-1">해결률</div>
              <div className="text-xs text-gray-400 mt-1">{kpi.period}</div>
            </div>
          </div>
        )}

        {/* Tasks & Notes */}
        <div className="grid lg:grid-cols-2 gap-6">
          {/* My Tasks */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold">내가 담당한 업무 ({tasks.length})</h2>
            </div>
            <div className="p-6">
              {tasks.length === 0 ? (
                <div className="empty-state">담당한 업무가 없습니다</div>
              ) : (
                <div className="space-y-4">
                  {tasks.map((task) => (
                    <div
                      key={task.id}
                      className="p-4 border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer"
                    >
                      <div className="flex items-start justify-between mb-2">
                        <h3 className="font-medium text-gray-900">{task.title}</h3>
                        <Badge variant={getPriorityBadgeVariant(task.priority)}>
                          {task.priority}
                        </Badge>
                      </div>
                      <p className="text-sm text-gray-600 mb-2">{task.description}</p>
                      <div className="flex items-center justify-between">
                        <Badge>{task.status}</Badge>
                        {task.slaDeadline && (
                          <span className="text-xs text-gray-500">
                            ⏰ {new Date(task.slaDeadline).toLocaleString('ko-KR')}
                          </span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Personal Notes */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold">개인 메모</h2>
            </div>
            <div className="p-6">
              {/* Add Note */}
              <div className="mb-4">
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={newNote}
                    onChange={(e) => setNewNote(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && handleAddNote()}
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
              </div>

              {/* Notes List */}
              {notes.length === 0 ? (
                <div className="empty-state">메모가 없습니다</div>
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

        {/* Quick Links */}
        <div className="bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg p-6 text-white">
          <h2 className="text-xl font-semibold mb-4">빠른 작업</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            <button className="bg-white/20 hover:bg-white/30 rounded-lg p-4 text-center transition-colors">
              <div className="text-2xl mb-2">📥</div>
              <div className="text-sm font-medium">새 문의 확인</div>
            </button>
            <button className="bg-white/20 hover:bg-white/30 rounded-lg p-4 text-center transition-colors">
              <div className="text-2xl mb-2">✅</div>
              <div className="text-sm font-medium">업무 생성</div>
            </button>
            <button className="bg-white/20 hover:bg-white/30 rounded-lg p-4 text-center transition-colors">
              <div className="text-2xl mb-2">📝</div>
              <div className="text-sm font-medium">템플릿 사용</div>
            </button>
            <button className="bg-white/20 hover:bg-white/30 rounded-lg p-4 text-center transition-colors">
              <div className="text-2xl mb-2">📊</div>
              <div className="text-sm font-medium">통계 보기</div>
            </button>
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
