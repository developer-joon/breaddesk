import api from '@/lib/api';
import type { ApiResponse, PasswordChangeRequest } from '@/types';

export async function changePassword(req: PasswordChangeRequest): Promise<void> {
  await api.put<ApiResponse<void>>('/auth/password', req);
}
