import api from '@/lib/api';
import type { ApiResponse } from '@/types';

export interface Team {
  id: number;
  name: string;
  description: string | null;
  members: TeamMember[];
  createdAt: string;
  updatedAt: string;
}

export interface TeamMember {
  id: number;
  userId: number;
  userName: string;
  userEmail: string;
  role: string;
  joinedAt: string;
}

export interface TeamCreateRequest {
  name: string;
  description?: string;
}

export interface TeamUpdateRequest {
  name?: string;
  description?: string;
}

export interface TeamMemberAddRequest {
  userId: number;
  role?: string;
}

export async function getTeams(): Promise<Team[]> {
  const response = await api.get<ApiResponse<Team[]>>('/teams');
  if (!response.data.success) {
    throw new Error(response.data.error || 'Failed to get teams');
  }
  return response.data.data;
}

export async function getTeamById(id: number): Promise<Team> {
  const response = await api.get<ApiResponse<Team>>(`/teams/${id}`);
  if (!response.data.success) {
    throw new Error(response.data.error || 'Failed to get team');
  }
  return response.data.data;
}

export async function createTeam(request: TeamCreateRequest): Promise<Team> {
  const response = await api.post<ApiResponse<Team>>('/teams', request);
  if (!response.data.success) {
    throw new Error(response.data.error || 'Failed to create team');
  }
  return response.data.data;
}

export async function updateTeam(id: number, request: TeamUpdateRequest): Promise<Team> {
  const response = await api.patch<ApiResponse<Team>>(`/teams/${id}`, request);
  if (!response.data.success) {
    throw new Error(response.data.error || 'Failed to update team');
  }
  return response.data.data;
}

export async function deleteTeam(id: number): Promise<void> {
  const response = await api.delete<ApiResponse<void>>(`/teams/${id}`);
  if (!response.data.success) {
    throw new Error(response.data.error || 'Failed to delete team');
  }
}

export async function addTeamMember(teamId: number, request: TeamMemberAddRequest): Promise<void> {
  const response = await api.post<ApiResponse<void>>(`/teams/${teamId}/members`, request);
  if (!response.data.success) {
    throw new Error(response.data.error || 'Failed to add team member');
  }
}

export async function removeTeamMember(teamId: number, userId: number): Promise<void> {
  const response = await api.delete<ApiResponse<void>>(`/teams/${teamId}/members/${userId}`);
  if (!response.data.success) {
    throw new Error(response.data.error || 'Failed to remove team member');
  }
}
