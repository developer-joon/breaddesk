import api from '@/lib/api';
import type { ApiResponse, DashboardStats } from '@/types';

export async function fetchDashboardStats(): Promise<DashboardStats> {
  const { data } = await api.get<ApiResponse<DashboardStats>>('/dashboard');
  return data.data;
}
