'use client';

import { useEffect, useState } from 'react';
import { getStatsOverview, getAIStats, getTeamStats, getWeeklyReport } from '@/services/stats';
import type { StatsOverview, AIStats, TeamStats, WeeklyReport } from '@/types';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { ErrorMessage } from '@/components/ui/ErrorMessage';

export default function StatsPage() {
  const [overview, setOverview] = useState<StatsOverview | null>(null);
  const [aiStats, setAiStats] = useState<AIStats | null>(null);
  const [teamStats, setTeamStats] = useState<TeamStats | null>(null);
  const [weeklyReport, setWeeklyReport] = useState<WeeklyReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadAllStats();
  }, []);

  const loadAllStats = async () => {
    try {
      setLoading(true);
      setError(null);
      const [overviewData, aiData, teamData, weeklyData] = await Promise.all([
        getStatsOverview(),
        getAIStats(),
        getTeamStats(),
        getWeeklyReport(),
      ]);
      setOverview(overviewData);
      setAiStats(aiData);
      setTeamStats(teamData);
      setWeeklyReport(weeklyData);
    } catch (err) {
      setError(err instanceof Error ? err.message : '통계 로드 실패');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-96">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error) {
    return <ErrorMessage message={error} onRetry={loadAllStats} />;
  }

  return (
    <div className="p-6 space-y-6">
      <h1 className="text-3xl font-bold text-gray-900 dark:text-white">📈 통계</h1>

      {/* Overview */}
      {overview && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <StatCard title="전체 문의" value={overview.totalInquiries} icon="📨" />
          <StatCard title="전체 업무" value={overview.totalTasks} icon="📋" />
          <StatCard title="팀원 수" value={overview.totalMembers} icon="👥" />
          <StatCard
            title="AI 해결률"
            value={`${(overview.aiResolutionRate * 100).toFixed(1)}%`}
            icon="🤖"
          />
          <StatCard
            title="평균 응답 시간"
            value={`${overview.avgResponseTime.toFixed(1)}분`}
            icon="⏱️"
          />
          <StatCard
            title="평균 해결 시간"
            value={`${overview.avgResolveTime.toFixed(1)}분`}
            icon="✅"
          />
        </div>
      )}

      {/* AI Stats */}
      {aiStats && (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">🤖 AI 성과</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div>
              <div className="text-sm text-gray-600 dark:text-gray-400">총 AI 응답</div>
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {aiStats.totalAIResponses}
              </div>
            </div>
            <div>
              <div className="text-sm text-gray-600 dark:text-gray-400">AI 수용률</div>
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {(aiStats.aiAcceptRate * 100).toFixed(1)}%
              </div>
            </div>
            <div>
              <div className="text-sm text-gray-600 dark:text-gray-400">평균 신뢰도</div>
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {(aiStats.avgConfidence * 100).toFixed(1)}%
              </div>
            </div>
          </div>

          {aiStats.topResolvedCategories.length > 0 && (
            <div>
              <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">
                주요 해결 카테고리
              </h3>
              <div className="space-y-2">
                {aiStats.topResolvedCategories.map((cat) => (
                  <div key={cat.category} className="flex items-center gap-2">
                    <div className="flex-1 bg-gray-200 dark:bg-gray-700 rounded h-6 overflow-hidden">
                      <div
                        className="bg-blue-500 h-full flex items-center px-2 text-xs text-white font-medium"
                        style={{
                          width: `${
                            (cat.count / aiStats.topResolvedCategories[0].count) * 100
                          }%`,
                        }}
                      >
                        {cat.category}
                      </div>
                    </div>
                    <div className="text-sm font-medium text-gray-900 dark:text-white w-12 text-right">
                      {cat.count}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Team Stats */}
      {teamStats && (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">👥 팀별 성과</h2>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-200 dark:border-gray-700">
                  <th className="text-left py-2 px-4 text-sm font-semibold text-gray-700 dark:text-gray-300">
                    팀원
                  </th>
                  <th className="text-right py-2 px-4 text-sm font-semibold text-gray-700 dark:text-gray-300">
                    할당된 업무
                  </th>
                  <th className="text-right py-2 px-4 text-sm font-semibold text-gray-700 dark:text-gray-300">
                    완료 업무
                  </th>
                  <th className="text-right py-2 px-4 text-sm font-semibold text-gray-700 dark:text-gray-300">
                    평균 완료 시간
                  </th>
                </tr>
              </thead>
              <tbody>
                {teamStats.members.map((member) => (
                  <tr
                    key={member.memberId}
                    className="border-b border-gray-100 dark:border-gray-700"
                  >
                    <td className="py-3 px-4 text-sm text-gray-900 dark:text-white">
                      {member.memberName}
                    </td>
                    <td className="py-3 px-4 text-sm text-gray-900 dark:text-white text-right">
                      {member.assignedTasks}
                    </td>
                    <td className="py-3 px-4 text-sm text-gray-900 dark:text-white text-right">
                      {member.completedTasks}
                    </td>
                    <td className="py-3 px-4 text-sm text-gray-600 dark:text-gray-400 text-right">
                      {member.avgCompletionHours.toFixed(1)}h
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Weekly Report */}
      {weeklyReport && (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">
            📅 주간 리포트
          </h2>
          <div className="text-sm text-gray-600 dark:text-gray-400 mb-4">
            {weeklyReport.weekStart} ~ {weeklyReport.weekEnd}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            <div>
              <div className="text-sm text-gray-600 dark:text-gray-400">총 문의</div>
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {weeklyReport.totalInquiries}
              </div>
            </div>
            <div>
              <div className="text-sm text-gray-600 dark:text-gray-400">총 업무</div>
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {weeklyReport.totalTasks}
              </div>
            </div>
            <div>
              <div className="text-sm text-gray-600 dark:text-gray-400">완료 업무</div>
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {weeklyReport.completedTasks}
              </div>
            </div>
            <div>
              <div className="text-sm text-gray-600 dark:text-gray-400">SLA 준수율</div>
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {(weeklyReport.slaComplianceRate * 100).toFixed(1)}%
              </div>
            </div>
          </div>

          {weeklyReport.topIssues.length > 0 && (
            <div>
              <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">
                주요 이슈
              </h3>
              <div className="space-y-2">
                {weeklyReport.topIssues.map((issue, idx) => (
                  <div
                    key={idx}
                    className="flex justify-between items-center p-2 bg-gray-50 dark:bg-gray-700 rounded"
                  >
                    <div className="text-sm text-gray-900 dark:text-white">{issue.issue}</div>
                    <div className="text-sm font-medium text-gray-600 dark:text-gray-400">
                      {issue.count}건
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function StatCard({
  title,
  value,
  icon,
}: {
  title: string;
  value: string | number;
  icon: string;
}) {
  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-4">
      <div className="flex items-center gap-3">
        <div className="text-3xl">{icon}</div>
        <div className="flex-1">
          <div className="text-sm text-gray-600 dark:text-gray-400">{title}</div>
          <div className="text-2xl font-bold text-gray-900 dark:text-white">{value}</div>
        </div>
      </div>
    </div>
  );
}
