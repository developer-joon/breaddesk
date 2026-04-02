import { api } from '@/lib/api';
import type { ApiResponse, AIStats, TeamStats, WeeklyReport } from '@/types';

export interface AIPerformance {
  autoResolveRate: number;
  avgConfidence: number;
  confidenceDistribution: Record<string, number>;
  topResolvedCategories: Array<{ category: string; count: number }>;
  dailyAutoResolve: Array<{ date: string; count: number }>;
}

export interface AgentProductivity {
  tasksCompletedByAgent: Array<{ agentName: string; count: number }>;
  avgResolutionTimeMinutes: number;
  currentWorkload: Array<{ agentName: string; activeCount: number }>;
}

export interface AnalyticsData {
  aiPerformance: AIPerformance;
  agentProductivity: AgentProductivity;
}

export const analyticsService = {
  getAIPerformance: async (
    startDate?: string,
    endDate?: string
  ): Promise<AIPerformance> => {
    const params = new URLSearchParams();
    if (startDate) params.set('startDate', startDate);
    if (endDate) params.set('endDate', endDate);
    
    const response = await api.get<ApiResponse<AIPerformance>>(
      `/analytics/ai-performance?${params}`
    );
    return response.data.data;
  },

  getAgentProductivity: async (
    startDate?: string,
    endDate?: string
  ): Promise<AgentProductivity> => {
    const params = new URLSearchParams();
    if (startDate) params.set('startDate', startDate);
    if (endDate) params.set('endDate', endDate);
    
    const response = await api.get<ApiResponse<AgentProductivity>>(
      `/analytics/agent-productivity?${params}`
    );
    return response.data.data;
  },

  getWeeklyReport: async (weekStart?: string): Promise<WeeklyReport> => {
    const params = weekStart ? `?weekStart=${weekStart}` : '';
    const response = await api.get<ApiResponse<WeeklyReport>>(
      `/stats/weekly-report${params}`
    );
    return response.data.data;
  },

  exportCSV: async (type: 'inquiries' | 'tasks', startDate?: string, endDate?: string): Promise<Blob> => {
    const params = new URLSearchParams();
    if (startDate) params.set('startDate', startDate);
    if (endDate) params.set('endDate', endDate);
    
    const response = await api.get(
      `/analytics/export/${type}?${params}`,
      { responseType: 'blob' }
    );
    return response.data;
  },
};
