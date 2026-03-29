'use client';

import React, { useEffect, useState } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { api } from '@/lib/api';
import { DashboardStats, RecentInquiry, TaskStatusCount } from '@/types';

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats>({
    totalInquiries: 0,
    unresolvedInquiries: 0,
    todayInquiries: 0,
    aiResolutionRate: 0,
  });
  const [recentInquiries, setRecentInquiries] = useState<RecentInquiry[]>([]);
  const [taskCounts, setTaskCounts] = useState<TaskStatusCount[]>([]);

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      // Mock data for now
      setStats({
        totalInquiries: 1248,
        unresolvedInquiries: 23,
        todayInquiries: 47,
        aiResolutionRate: 68,
      });

      setRecentInquiries([
        {
          id: '1',
          title: '결제 오류 문의',
          status: 'IN_PROGRESS',
          channel: 'EMAIL',
          createdAt: new Date().toISOString(),
          customerName: '김철수',
        },
        {
          id: '2',
          title: '회원가입 관련 질문',
          status: 'NEW',
          channel: 'CHAT',
          createdAt: new Date().toISOString(),
          customerName: '이영희',
        },
      ]);

      setTaskCounts([
        { status: 'TODO', count: 12 },
        { status: 'IN_PROGRESS', count: 8 },
        { status: 'REVIEW', count: 5 },
        { status: 'DONE', count: 156 },
      ]);

      // Real API call (uncomment when backend is ready)
      // const response = await api.get('/dashboard');
      // setStats(response.data.stats);
      // setRecentInquiries(response.data.recentInquiries);
      // setTaskCounts(response.data.taskCounts);
    } catch (error) {
      console.error('Failed to fetch dashboard:', error);
    }
  };

  const getStatusBadgeVariant = (status: string) => {
    switch (status) {
      case 'NEW':
        return 'info';
      case 'IN_PROGRESS':
        return 'warning';
      case 'RESOLVED':
        return 'success';
      case 'CLOSED':
        return 'default';
      default:
        return 'default';
    }
  };

  const getChannelIcon = (channel: string) => {
    switch (channel) {
      case 'EMAIL':
        return '📧';
      case 'CHAT':
        return '💬';
      case 'PHONE':
        return '📞';
      case 'FORM':
        return '📝';
      default:
        return '📋';
    }
  };

  return (
    <AppLayout>
      <div className="space-y-6">
        {/* Page Title */}
        <div>
          <h1 className="text-2xl font-bold text-gray-900">팀 현황 대시보드</h1>
          <p className="text-gray-600 mt-1">실시간 문의 및 업무 현황을 확인하세요</p>
        </div>

        {/* Stats Cards (2x2 grid on mobile) */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
            <div className="text-3xl mb-2">📊</div>
            <div className="text-2xl font-bold text-gray-900">{stats.totalInquiries}</div>
            <div className="text-sm text-gray-600 mt-1">총 문의</div>
          </div>

          <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
            <div className="text-3xl mb-2">⏳</div>
            <div className="text-2xl font-bold text-orange-600">{stats.unresolvedInquiries}</div>
            <div className="text-sm text-gray-600 mt-1">미해결</div>
          </div>

          <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
            <div className="text-3xl mb-2">📥</div>
            <div className="text-2xl font-bold text-blue-600">{stats.todayInquiries}</div>
            <div className="text-sm text-gray-600 mt-1">오늘 접수</div>
          </div>

          <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
            <div className="text-3xl mb-2">🤖</div>
            <div className="text-2xl font-bold text-green-600">{stats.aiResolutionRate}%</div>
            <div className="text-sm text-gray-600 mt-1">AI 해결률</div>
          </div>
        </div>

        {/* Recent Inquiries & Task Status */}
        <div className="grid lg:grid-cols-2 gap-6">
          {/* Recent Inquiries */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold">최근 문의</h2>
            </div>
            <div className="p-6">
              {recentInquiries.length === 0 ? (
                <div className="empty-state">문의가 없습니다</div>
              ) : (
                <div className="space-y-4">
                  {recentInquiries.map((inquiry) => (
                    <div
                      key={inquiry.id}
                      className="flex items-start gap-3 p-3 rounded-lg hover:bg-gray-50 cursor-pointer"
                    >
                      <span className="text-2xl">{getChannelIcon(inquiry.channel)}</span>
                      <div className="flex-1">
                        <div className="flex items-start justify-between gap-2">
                          <h3 className="font-medium text-gray-900">{inquiry.title}</h3>
                          <Badge variant={getStatusBadgeVariant(inquiry.status)}>
                            {inquiry.status}
                          </Badge>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">{inquiry.customerName}</p>
                        <p className="text-xs text-gray-400 mt-1">
                          {new Date(inquiry.createdAt).toLocaleString('ko-KR')}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Task Status Chart */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold">업무 현황</h2>
            </div>
            <div className="p-6">
              <div className="space-y-4">
                {taskCounts.map((item) => {
                  const statusLabels: Record<string, string> = {
                    TODO: '대기',
                    IN_PROGRESS: '진행중',
                    REVIEW: '검토',
                    DONE: '완료',
                  };

                  const statusColors: Record<string, string> = {
                    TODO: 'bg-gray-500',
                    IN_PROGRESS: 'bg-blue-500',
                    REVIEW: 'bg-yellow-500',
                    DONE: 'bg-green-500',
                  };

                  return (
                    <div key={item.status}>
                      <div className="flex items-center justify-between mb-2">
                        <span className="text-sm font-medium text-gray-700">
                          {statusLabels[item.status]}
                        </span>
                        <span className="text-sm font-bold text-gray-900">{item.count}</span>
                      </div>
                      <div className="w-full bg-gray-200 rounded-full h-2">
                        <div
                          className={`h-2 rounded-full ${statusColors[item.status]}`}
                          style={{ width: `${(item.count / 200) * 100}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
