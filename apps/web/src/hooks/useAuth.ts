'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/auth';

/**
 * Hook that ensures the user is authenticated.
 * Redirects to /login if not authenticated after checking.
 */
export function useAuth({ redirectTo = '/login' } = {}) {
  const { user, isAuthenticated, isLoading, checkAuth, logout } = useAuthStore();
  const router = useRouter();

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  useEffect(() => {
    if (!isLoading && !isAuthenticated && redirectTo) {
      router.replace(redirectTo);
    }
  }, [isLoading, isAuthenticated, redirectTo, router]);

  return { user, isAuthenticated, isLoading, logout };
}
