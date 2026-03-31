import api from '@/lib/api';

export interface FeatureFlags {
  kanbanTasks: boolean;
  internalNotes: boolean;
  aiAssignment: boolean;
  jiraIntegration: boolean;
}

export const featuresService = {
  async getFeatures(): Promise<FeatureFlags> {
    const response = await api.get<FeatureFlags>('/features');
    return response.data;
  },
};
