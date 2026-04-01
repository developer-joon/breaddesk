'use client';

import React, { useState, useEffect } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { analyticsService } from '@/services/analytics';
import type { AIPerformance, AgentProductivity } from '@/services/analytics';
import type { WeeklyReport } from '@/types';
import toast from 'react-hot-toast';

export default function AnalyticsPage() {
  const [aiPerformance, setAiPerformance] = useState<AIPerformance | null>(null);
  const [agentProductivity, setAgentProductivity] = useState<AgentProductivity | null>(null);
  const [weeklyReport, setWeeklyReport] = useState<WeeklyReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [dateRange, setDateRange] = useState({
    start: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    end: new Date().toISOString().split('T')[0],
  });

  useEffect(() => {
    loadAnalytics();
  }, [dateRange]);

  const loadAnalytics = async () => {
    try {
      setLoading(true);
      const [aiData, agentData, report] = await Promise.all([
        analyticsService.getAIPerformance(dateRange.start, dateRange.end),
        analyticsService.getAgentProductivity(dateRange.start, dateRange.end),
        analyticsService.getWeeklyReport(),
      ]);
      setAiPerformance(aiData);
      setAgentProductivity(agentData);
      setWeeklyReport(report);
    } catch (error) {
      console.error('Failed to load analytics:', error);
      toast.error('분석 데이터 로딩에 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  const exportData = async (type: 'inquiries' | 'tasks') => {
    try {
      const blob = await analyticsService.exportCSV(type, dateRange.start, dateRange.end);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${type}_${dateRange.start}_${dateRange.end}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      toast.success('CSV 다운로드 완료');
    } catch (error) {
      console.error('Export failed:', error);
      toast.error('내보내기에 실패했습니다');
    }
  };

  if (loading) {
    return (
      <AppLayout>
        <div className="flex justify-center items-center h-64">
          <LoadingSpinner size="lg" />
        </div>
      </AppLayout>
    );
  }

  return (
    <AppLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">분석 대시보드</h1>
            <p className="text-gray-600 mt-1">AI 성능 및 팀 생산성 분석</p>
          </div>
          <div className="flex gap-4 items-center">
            <div className="flex gap-2 items-center">
              <label className="text-sm text-gray-600">기간:</label>
              <input
                type="date"
                value={dateRange.start}
                onChange={(e) => setDateRange({ ...dateRange, start: e.target.value })}
                className="px-3 py-2 border border-gray-300 rounded-lg text-sm"
              />
              <span className="text-gray-600">~</span>
              <input
                type="date"
                value={dateRange.end}
                onChange={(e) => setDateRange({ ...dateRange, end: e.target.value })}
                className="px-3 py-2 border border-gray-300 rounded-lg text-sm"
              />
            </div>
          </div>
        </div>

        {/* AI Performance */}
        <div>
          <h2 className="text-xl font-semibold mb-4">AI 성능</h2>
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <div className="text-sm text-gray-600 mb-1">자동 해결률</div>
              <div className="text-3xl font-bold text-blue-600">
                {aiPerformance?.autoResolveRate.toFixed(1)}%
              </div>
            </div>
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <div className="text-sm text-gray-600 mb-1">평균 신뢰도</div>
              <div className="text-3xl font-bold text-green-600">
                {((aiPerformance?.avgConfidence || 0) * 100).toFixed(1)}%
              </div>
            </div>
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <div className="text-sm text-gray-600 mb-1">총 자동 처리</div>
              <div className="text-3xl font-bold text-purple-600">
                {aiPerformance?.dailyAutoResolve.reduce((sum, d) => sum + d.count, 0) || 0}건
              </div>
            </div>
          </div>

          {/* Auto-resolve trend chart (simple SVG bars) */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
            <h3 className="font-semibold mb-4">일별 자동 해결 추이</h3>
            <div className="h-64 flex items-end gap-2">
              {aiPerformance?.dailyAutoResolve.map((day, idx) => {
                const maxCount = Math.max(...(aiPerformance?.dailyAutoResolve.map((d) => d.count) || [1]));
                const height = (day.count / maxCount) * 100;
                return (
                  <div key={idx} className="flex-1 flex flex-col items-center">
                    <div
                      className="w-full bg-blue-500 rounded-t"
                      style={{ height: `${height}%` }}
                      title={`${day.date}: ${day.count}건`}
                    />
                    <div className="text-xs text-gray-600 mt-2 rotate-45 origin-top-left">
                      {new Date(day.date).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Confidence distribution */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
            <h3 className="font-semibold mb-4">신뢰도 분포</h3>
            <div className="space-y-3">
              {Object.entries(aiPerformance?.confidenceDistribution || {}).map(([range, count]) => {
                const total = Object.values(aiPerformance?.confidenceDistribution || {}).reduce((sum, c) => sum + c, 0);
                const percentage = (count / total) * 100;
                return (
                  <div key={range} className="flex items-center gap-3">
                    <div className="w-32 text-sm text-gray-700">{range}</div>
                    <div className="flex-1 bg-gray-200 rounded-full h-6 overflow-hidden">
                      <div
                        className="bg-green-500 h-full rounded-full flex items-center justify-end px-2"
                        style={{ width: `${percentage}%` }}
                      >
                        <span className="text-xs text-white font-medium">{count}건</span>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Top categories */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h3 className="font-semibold mb-4">자동 해결 상위 카테고리</h3>
            <div className="space-y-3">
              {aiPerformance?.topResolvedCategories.slice(0, 5).map((cat, idx) => (
                <div key={idx} className="flex items-center justify-between">
                  <span className="text-gray-700">{cat.category}</span>
                  <span className="font-semibold text-blue-600">{cat.count}건</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Agent Productivity */}
        <div>
          <h2 className="text-xl font-semibold mb-4">팀 생산성</h2>
          <div className="grid md:grid-cols-2 gap-4 mb-6">
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <div className="text-sm text-gray-600 mb-1">평균 해결 시간</div>
              <div className="text-3xl font-bold text-orange-600">
                {agentProductivity?.avgResolutionTimeMinutes.toFixed(0)}분
              </div>
            </div>
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <div className="text-sm text-gray-600 mb-1">현재 워크로드</div>
              <div className="text-3xl font-bold text-red-600">
                {agentProductivity?.currentWorkload.reduce((sum, w) => sum + w.activeCount, 0)}건
              </div>
            </div>
          </div>

          {/* Tasks completed by agent */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
            <h3 className="font-semibold mb-4">담당자별 완료 업무</h3>
            <div className="h-64 flex items-end gap-3">
              {agentProductivity?.tasksCompletedByAgent.map((agent, idx) => {
                const maxCount = Math.max(...(agentProductivity?.tasksCompletedByAgent.map((a) => a.count) || [1]));
                const height = (agent.count / maxCount) * 100;
                return (
                  <div key={idx} className="flex-1 flex flex-col items-center">
                    <div className="text-sm font-semibold text-gray-700 mb-1">{agent.count}</div>
                    <div
                      className="w-full bg-purple-500 rounded-t"
                      style={{ height: `${height}%` }}
                    />
                    <div className="text-xs text-gray-600 mt-2">{agent.agentName}</div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Current workload */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h3 className="font-semibold mb-4">현재 업무 분배</h3>
            <div className="space-y-3">
              {agentProductivity?.currentWorkload.map((agent, idx) => (
                <div key={idx} className="flex items-center justify-between">
                  <span className="text-gray-700">{agent.agentName}</span>
                  <span className={`font-semibold ${agent.activeCount > 10 ? 'text-red-600' : 'text-gray-600'}`}>
                    {agent.activeCount}건
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Weekly Report */}
        {weeklyReport && (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold">주간 리포트</h2>
              <button
                onClick={() => exportData('inquiries')}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"
              >
                CSV 다운로드
              </button>
            </div>
            <div className="grid md:grid-cols-4 gap-4">
              <div>
                <div className="text-sm text-gray-600">신규 문의</div>
                <div className="text-2xl font-bold text-gray-900">{weeklyReport.newInquiries}</div>
              </div>
              <div>
                <div className="text-sm text-gray-600">해결 문의</div>
                <div className="text-2xl font-bold text-green-600">{weeklyReport.resolvedInquiries}</div>
              </div>
              <div>
                <div className="text-sm text-gray-600">AI 자동 해결률</div>
                <div className="text-2xl font-bold text-blue-600">
                  {weeklyReport.aiResolutionRate.toFixed(1)}%
                </div>
              </div>
              <div>
                <div className="text-sm text-gray-600">SLA 준수율</div>
                <div className="text-2xl font-bold text-purple-600">
                  {weeklyReport.slaComplianceRate.toFixed(1)}%
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Export Section */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 className="font-semibold text-blue-900 mb-3">데이터 내보내기</h3>
          <div className="flex gap-3">
            <button
              onClick={() => exportData('inquiries')}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              문의 데이터 CSV
            </button>
            <button
              onClick={() => exportData('tasks')}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              업무 데이터 CSV
            </button>
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
