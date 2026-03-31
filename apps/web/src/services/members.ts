import api from '@/lib/api';
import type { ApiResponse, User, MemberRequest } from '@/types';

// Note: backend returns MemberResponse which maps to User type

export async function getMembers(): Promise<User[]> {
  const { data } = await api.get<ApiResponse<User[]>>('/members');
  return data.data;
}

export async function getMemberById(id: number): Promise<User> {
  const { data } = await api.get<ApiResponse<User>>(`/members/${id}`);
  return data.data;
}

export async function createMember(req: MemberRequest): Promise<User> {
  const { data } = await api.post<ApiResponse<User>>('/members', req);
  return data.data;
}

export async function updateMember(id: number, req: MemberRequest): Promise<User> {
  const { data } = await api.put<ApiResponse<User>>(`/members/${id}`, req);
  return data.data;
}

export async function deleteMember(id: number): Promise<void> {
  await api.delete(`/members/${id}`);
}
