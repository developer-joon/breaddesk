import { create } from 'zustand';
import api from '@/lib/api';
import type { User, LoginRequest, ApiResponse, TokenResponse } from '@/types';

interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (credentials: RegisterRequest) => Promise<void>;
  logout: () => void;
  checkAuth: () => void;
}

/**
 * Decode JWT payload (base64url) without a library.
 * Returns null if decoding fails.
 */
function decodeJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  } catch {
    return null;
  }
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: true,

  login: async (credentials: LoginRequest) => {
    const response = await api.post<ApiResponse<TokenResponse>>('/auth/login', credentials);
    const { accessToken, refreshToken } = response.data.data;

    // Persist tokens
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);

    // Derive basic user info from JWT claims
    const payload = decodeJwtPayload(accessToken);
    const user: User = {
      id: (payload?.sub as string) ?? credentials.email,
      email: (payload?.sub as string) ?? credentials.email,
      name: (payload?.name as string) ?? credentials.email.split('@')[0],
      role: ((payload?.role as string) ?? 'AGENT') as User['role'],
    };

    localStorage.setItem('user', JSON.stringify(user));
    set({ user, isAuthenticated: true, isLoading: false });
  },

  register: async (credentials: RegisterRequest) => {
    const response = await api.post<ApiResponse<TokenResponse>>('/auth/register', credentials);
    const { accessToken, refreshToken } = response.data.data;

    // Persist tokens
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);

    // Derive basic user info from JWT claims
    const payload = decodeJwtPayload(accessToken);
    const user: User = {
      id: (payload?.sub as string) ?? credentials.email,
      email: (payload?.sub as string) ?? credentials.email,
      name: (payload?.name as string) ?? credentials.name,
      role: ((payload?.role as string) ?? 'AGENT') as User['role'],
    };

    localStorage.setItem('user', JSON.stringify(user));
    set({ user, isAuthenticated: true, isLoading: false });
  },

  logout: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    set({ user: null, isAuthenticated: false });
    if (typeof window !== 'undefined') {
      window.location.href = '/login';
    }
  },

  checkAuth: () => {
    if (typeof window === 'undefined') {
      set({ isLoading: false });
      return;
    }

    const token = localStorage.getItem('accessToken');
    const userStr = localStorage.getItem('user');

    if (token && userStr) {
      try {
        const user = JSON.parse(userStr) as User;
        set({ user, isAuthenticated: true, isLoading: false });
      } catch {
        set({ user: null, isAuthenticated: false, isLoading: false });
      }
    } else {
      set({ user: null, isAuthenticated: false, isLoading: false });
    }
  },
}));
