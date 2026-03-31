'use client';

import React, { useEffect, useState } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { ErrorMessage } from '@/components/ui/ErrorMessage';
import { fetchDashboardStats } from '@/services/dashboard';
import { getSlaStats } from '@/services/sla';
import type { DashboardStats, SlaStatsResponse } from '@/types';

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [slaStats, setSlaStats] = useState<SlaStatsResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadDashboard = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [dashData, slaData] = await Promise.allSettled([
        fetchDashboardStats(),
        getSlaStats(),
      ]);
      if (dashData.status === 'fulfilled') setStats(dashData.value);
      if (slaData.status === 'fulfilled') setSlaStats(slaData.value);
      if (dashData.status === 'rejected') throw dashData.reason;
    } catch (err) {
      console.error('Failed to fetch dashboard:', err);
      setError('대시보드 데이터를 불러오지 못했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  const getStatusLabel = (status: string) => {
    const labels: Record<string, string> = {
      OPEN: '접수', AI_ANSWERED: 'AI 답변', ESCALATED: '에스컬레이션',
      RESOLVED: '해결됨', CLOSED: '종료', WAITING: '대기',
      IN_PROGRESS: '진행중', PENDING: '보류', REVIEW: '검토', DONE: '완료',
    };
    return labels[status] || status;
  };

  const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
      OPEN: 'bg-blue-500', AI_ANSWERED: 'bg-cyan-500', ESCALATED: 'bg-orange-500',
      RESOLVED: 'bg-green-500', CLOSED: 'bg-gray-500', WAITING: 'bg-gray-500',
      IN_PROGRESS: 'bg-blue-500', PENDING: 'bg-yellow-500', REVIEW: 'bg-purple-500', DONE: 'bg-green-500',
    };
    return colors[status] || 'bg-gray-500';
  };

  const getComplianceColor = (rate: number) => {
    if (rate >= 90) return 'text-green-600 dark:text-green-400';
    if (rate >= 70) return 'text-yellow-600 dark:text-yellow-400';
    return 'text-red-600 dark:text-red-400';
  };

  return (
    <AppLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">팀 현황 대시보드</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">실시간 문의 및 업무 현황을 확인하세요</p>
        </div>

        {isLoading && <LoadingSpinner text="대시보드를 불러오는 중..." />}
        {error && <ErrorMessage message={error} onRetry={loadDashboard} />}

        {stats && (
          <>
            {/* Stats Cards */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm border border-gray-200 dark:border-gray-700">
                <div className="text-3xl mb-2">📊</div>
                <div className="text-2xl font-bold text-gray-900 dark:text-white">{stats.totalInquiries}</div>
                <div className="text-sm text-gray-600 dark:text-gray-400 mt-1">총 문의</div>
              </div>
              <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm border border-gray-200 dark:border-gray-700">
                <div className="text-3xl mb-2">⏳</div>
                <div className="text-2xl font-bold text-orange-600 dark:text-orange-400">{stats.unresolvedInquiries}</div>
                <div className="text-sm text-gray-600 dark:text-gray-400 mt-1">미해결</div>
              </div>
              <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm border border-gray-200 dark:border-gray-700">
                <div className="text-3xl mb-2">📥</div>
                <div className="text-2xl font-bold text-blue-600 dark:text-blue-400">{stats.todayInquiries}</div>
                <div className="text-sm text-gray-600 dark:text-gray-400 mt-1">오늘 접수</div>
              </div>
              <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm border border-gray-200 dark:border-gray-700">
                <div className="text-3xl mb-2">🤖</div>
                <div className="text-2xl font-bold text-green-600 dark:text-green-400">{Math.round(stats.aiResolutionRate)}%</div>
                <div className="text-sm text-gray-600 dark:text-gray-400 mt-1">AI 해결률</div>
              </div>
            </div>

            {/* SLA Stats */}
            {slaStats && (
              <div>
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">📋 SLA 현황</h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                  <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm border border-gray-200 dark:border-gray-700">
                    <div className="text-3xl mb-2">✅</div>
                    <div className={`text-2xl font-bold ${getComplianceColor(slaStats.overallResponseComplianceRate)}`}>{Math.round(slaStats.overallResponseComplianceRate)}%</div>
                    <div className="text-sm text-gray-600 mt-1">응답 SLA 준수율</div>
                  </div>
                  <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm border border-gray-200 dark:border-gray-700">
                    <div className="text-3xl mb-2">📅</div>
                    <div className={`text-2xl font-bold ${getComplianceColor(slaStats.overallResolveComplianceRate)}`}>{Math.round(slaStats.overallResolveComplianceRate)}%</div>
                    <div className="text-sm text-gray-600 mt-1">해결 SLA 준수율</div>
                  </div>
                  <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm border border-gray-200 dark:border-gray-700">
                    <div className="text-3xl mb-2">🚨</div>
                    <div className="text-2xl font-bold text-red-600 dark:text-red-400">{slaStats.totalResponseBreaches + slaStats.totalResolveBreaches}</div>
                    <div className="text-sm text-gray-600 mt-1">SLA 위반 건수</div>
                  </div>
                  <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm border border-gray-200 dark:border-gray-700">
                    <div className="text-3xl mb-2">⏱️</div>
                    <div className="text-2xl font-bold text-gray-900 dark:text-white">
                      {slaStats.avgResponseMinutes !== null ? (slaStats.avgResponseMinutes / 60).toFixed(1) : '-'}h
                    </div>
                    <div className="text-sm text-gray-600 mt-1">평균 응답 시간</div>
                    <div className="text-xs text-gray-400 mt-0.5">
                      해결: {slaStats.avgResolveMinutes !== null ? (slaStats.avgResolveMinutes / 60).toFixed(1) : '-'}h
                    </div>
                  </div>
                </div>

                <div className="mt-4 bg-white rounded-lg p-4 shadow-sm border border-gray-200">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm font-medium text-gray-700 dark:text-gray-300">SLA 준수/위반 비율</span>
                    <span className="text-sm text-gray-500">응답: {slaStats.totalResponseBreaches}건 위반 / 해결: {slaStats.totalResolveBreaches}건 위반</span>
                  </div>
                  <div className="flex gap-1 h-4 rounded-full overflow-hidden bg-gray-200">
                    <div className="bg-green-500 rounded-l-full" style={{ width: `${slaStats.overallResponseComplianceRate}%` }} />
                    <div className="bg-red-500 rounded-r-full" style={{ width: `${100 - slaStats.overallResponseComplianceRate}%` }} />
                  </div>
                  <div className="flex justify-between mt-1 text-xs text-gray-500">
                    <span>응답 준수: {Math.round(slaStats.overallResponseComplianceRate)}%</span>
                    <span>해결 준수: {Math.round(slaStats.overallResolveComplianceRate)}%</span>
                  </div>
                </div>
              </div>
            )}

            {/* Status Breakdown */}
            <div className="grid lg:grid-cols-2 gap-6">
              {stats.inquiriesByStatus && Object.keys(stats.inquiriesByStatus).length > 0 && (
                <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                  <div className="px-6 py-4 border-b border-gray-200"><h2 className="text-lg font-semibold">문의 현황</h2></div>
                  <div className="p-6 space-y-4">
                    {Object.entries(stats.inquiriesByStatus).map(([status, count]) => {
                      const total = Object.values(stats.inquiriesByStatus!).reduce((a, b) => a + b, 0);
                      return (
                        <div key={status}>
                          <div className="flex items-center justify-between mb-2">
                            <span className="text-sm font-medium text-gray-700 dark:text-gray-300">{getStatusLabel(status)}</span>
                            <span className="text-sm font-bold text-gray-900 dark:text-white">{count}</span>
                          </div>
                          <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                            <div className={`h-2 rounded-full ${getStatusColor(status)}`} style={{ width: `${total > 0 ? (count / total) * 100 : 0}%` }} />
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {stats.tasksByStatus && Object.keys(stats.tasksByStatus).length > 0 && (
                <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                  <div className="px-6 py-4 border-b border-gray-200"><h2 className="text-lg font-semibold">업무 현황</h2></div>
                  <div className="p-6 space-y-4">
                    {Object.entries(stats.tasksByStatus).map(([status, count]) => {
                      const total = Object.values(stats.tasksByStatus!).reduce((a, b) => a + b, 0);
                      return (
                        <div key={status}>
                          <div className="flex items-center justify-between mb-2">
                            <span className="text-sm font-medium text-gray-700 dark:text-gray-300">{getStatusLabel(status)}</span>
                            <span className="text-sm font-bold text-gray-900 dark:text-white">{count}</span>
                          </div>
                          <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                            <div className={`h-2 rounded-full ${getStatusColor(status)}`} style={{ width: `${total > 0 ? (count / total) * 100 : 0}%` }} />
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {(!stats.inquiriesByStatus || Object.keys(stats.inquiriesByStatus).length === 0) &&
                (!stats.tasksByStatus || Object.keys(stats.tasksByStatus).length === 0) && (
                  <div className="lg:col-span-2 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-6 text-center">
                    <p className="text-blue-800 dark:text-blue-200">상세 현황 데이터가 없습니다. 문의와 업무가 쌓이면 여기에 표시됩니다.</p>
                  </div>
                )}
            </div>
          </>
        )}
      </div>
    </AppLayout>
  );
}
