'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import StatCard from '@/components/StatCard';
import { api } from '@/lib/api';
import type { DashboardStats, TaskStatus } from '@/types';

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, []);

  async function loadStats() {
    setLoading(true);
    const response = await api.getDashboardStats();
    if (response.success && response.data) {
      setStats(response.data);
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

  if (!stats) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">데이터를 불러올 수 없습니다.</div>
      </div>
    );
  }

  const resolvedRate = stats.totalInquiries > 0 
    ? Math.round((stats.resolvedRate) * 100) 
    : 0;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">대시보드</h1>
        <p className="text-gray-600 mt-1">전체 현황을 한눈에 확인하세요</p>
      </div>

      {/* 현황 카드 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          title="총 문의"
          value={stats.totalInquiries}
          icon="💬"
          color="blue"
        />
        <StatCard
          title="미해결 문의"
          value={stats.unresolvedInquiries}
          icon="⚠️"
          color="yellow"
        />
        <StatCard
          title="오늘 접수"
          value={stats.todayInquiries}
          icon="📥"
          color="green"
        />
        <StatCard
          title="처리율"
          value={`${resolvedRate}%`}
          icon="✅"
          color="green"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 최근 문의 */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">최근 문의</h2>
            <Link 
              href="/inquiries"
              className="text-sm text-primary-600 hover:text-primary-700"
            >
              전체 보기 →
            </Link>
          </div>
          <div className="divide-y divide-gray-200">
            {stats.recentInquiries.length === 0 ? (
              <div className="px-6 py-8 text-center text-gray-500">
                문의가 없습니다
              </div>
            ) : (
              stats.recentInquiries.map((inquiry) => (
                <Link
                  key={inquiry.id}
                  href={`/inquiries/${inquiry.id}`}
                  className="block px-6 py-4 hover:bg-gray-50 transition-colors"
                >
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-gray-900 truncate">
                        {inquiry.senderName}
                      </p>
                      <p className="text-xs text-gray-500">{inquiry.senderEmail}</p>
                    </div>
                    <span className={`
                      px-2 py-1 text-xs font-medium rounded-full
                      ${inquiry.status === 'RESOLVED' ? 'bg-green-100 text-green-800' :
                        inquiry.status === 'ESCALATED' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-blue-100 text-blue-800'}
                    `}>
                      {inquiry.status}
                    </span>
                  </div>
                  <p className="text-sm text-gray-600 line-clamp-2">
                    {inquiry.message}
                  </p>
                  <p className="text-xs text-gray-400 mt-2">
                    {new Date(inquiry.createdAt).toLocaleString('ko-KR')}
                  </p>
                </Link>
              ))
            )}
          </div>
        </div>

        {/* 업무 상태별 요약 */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">업무 현황</h2>
            <Link 
              href="/tasks"
              className="text-sm text-primary-600 hover:text-primary-700"
            >
              전체 보기 →
            </Link>
          </div>
          <div className="p-6">
            <div className="space-y-4">
              {Object.entries(stats.tasksByStatus).map(([status, count]) => {
                const statusLabels: Record<TaskStatus, string> = {
                  WAITING: '대기',
                  IN_PROGRESS: '진행중',
                  REVIEW: '리뷰',
                  DONE: '완료',
                };

                const statusColors: Record<TaskStatus, string> = {
                  WAITING: 'bg-gray-200',
                  IN_PROGRESS: 'bg-blue-500',
                  REVIEW: 'bg-yellow-500',
                  DONE: 'bg-green-500',
                };

                return (
                  <div key={status} className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <div className={`w-3 h-3 rounded-full ${statusColors[status as TaskStatus]}`} />
                      <span className="text-sm text-gray-700">
                        {statusLabels[status as TaskStatus]}
                      </span>
                    </div>
                    <span className="text-lg font-semibold text-gray-900">{count}</span>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
