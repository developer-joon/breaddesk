'use client';

import { useState, useEffect, useCallback } from 'react';
import api from '@/lib/api';
import type { ApiResponse } from '@/types';

interface UseFetchOptions {
  /** Skip the initial fetch (useful when you want to trigger manually) */
  skip?: boolean;
}

interface UseFetchReturn<T> {
  data: T | null;
  error: string | null;
  isLoading: boolean;
  refetch: () => Promise<void>;
}

/**
 * Simple data-fetching hook wrapping the API client.
 * Expects the backend to return ApiResponse<T>.
 */
export function useFetch<T>(url: string, options?: UseFetchOptions): UseFetchReturn<T> {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(!options?.skip);

  const fetchData = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await api.get<ApiResponse<T>>(url);
      if (response.data.success) {
        setData(response.data.data);
      } else {
        setError(response.data.error || 'Unknown error');
      }
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : 'An error occurred';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, [url]);

  useEffect(() => {
    if (!options?.skip) {
      fetchData();
    }
  }, [fetchData, options?.skip]);

  return { data, error, isLoading, refetch: fetchData };
}
