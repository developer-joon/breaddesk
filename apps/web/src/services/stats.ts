import api from '@/lib/api';
import type { ApiResponse, StatsOverview, AIStats, TeamStats, WeeklyReport } from '@/types';

export async function getStatsOverview(): Promise<StatsOverview> {
  const { data } = await api.get<ApiResponse<StatsOverview>>('/stats/overview');
  return data.data;
}

export async function getAIStats(): Promise<AIStats> {
  const { data } = await api.get<ApiResponse<AIStats>>('/stats/ai');
  return data.data;
}

export async function getTeamStats(): Promise<TeamStats> {
  // TeamStats is now directly an array, not wrapped in an object
  const { data } = await api.get<ApiResponse<TeamStats>>('/stats/team');
  return data.data;
}

export async function getWeeklyReport(): Promise<WeeklyReport> {
  const { data } = await api.get<ApiResponse<WeeklyReport>>('/stats/weekly-report');
  return data.data;
}
