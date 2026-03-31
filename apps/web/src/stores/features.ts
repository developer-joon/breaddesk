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
      set({ error: (error as Error).message, isLoading: false });
    }
  },

  isFeatureEnabled: (feature: keyof FeatureFlags) => {
    const { features } = get();
    return features?.[feature] ?? false;
  },
}));
