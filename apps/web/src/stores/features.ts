import { create } from 'zustand';
import { featuresService, FeatureFlags } from '@/services/features';

interface FeaturesState {
  features: FeatureFlags | null;
  isLoading: boolean;
  error: string | null;
  fetchFeatures: () => Promise<void>;
  isFeatureEnabled: (feature: keyof FeatureFlags) => boolean;
}

export const useFeaturesStore = create<FeaturesState>((set, get) => ({
  features: null,
  isLoading: false,
  error: null,

  fetchFeatures: async () => {
    set({ isLoading: true, error: null });
    try {
      const features = await featuresService.getFeatures();
      set({ features, isLoading: false });
    } catch (error) {
      // Feature flag 로딩 실패 시 기본값 사용 (앱 크래시 방지)
      console.warn('Feature flags load failed, using defaults:', error);
      set({ 
        features: { kanbanTasks: true, internalNotes: true, aiAssignment: false, jiraIntegration: false },
        error: (error as Error).message, 
        isLoading: false 
      });
    }
  },

  isFeatureEnabled: (feature: keyof FeatureFlags) => {
    const { features } = get();
    return features?.[feature] ?? false;
  },
}));
