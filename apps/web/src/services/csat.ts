import { api } from '@/lib/api';
import type { ApiResponse } from '@/types';

export interface CsatSurvey {
  id: number;
  inquiryId: number;
  token: string;
  expiresAt: string;
  submittedAt?: string;
  rating?: number;
  feedback?: string;
}

export interface CsatResponse {
  rating: number;
  feedback?: string;
}

export interface CsatStats {
  totalResponses: number;
  avgRating: number;
  ratingDistribution: Record<string, number>;
  recentFeedback: Array<{
    rating: number;
    feedback: string;
    createdAt: string;
  }>;
}

export const csatService = {
  getSurvey: async (token: string): Promise<CsatSurvey> => {
    const response = await api.get<ApiResponse<CsatSurvey>>(
      `/csat/survey/${token}`
    );
    return response.data.data;
  },

  submitSurvey: async (token: string, data: CsatResponse): Promise<void> => {
    await api.post(`/csat/survey/${token}`, data);
  },

  getStats: async (startDate?: string, endDate?: string): Promise<CsatStats> => {
    const params = new URLSearchParams();
    if (startDate) params.set('startDate', startDate);
    if (endDate) params.set('endDate', endDate);
    
    const response = await api.get<ApiResponse<CsatStats>>(
      `/csat/stats?${params}`
    );
    return response.data.data;
  },
};
