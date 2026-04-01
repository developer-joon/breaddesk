import type {
  TeamResponse,
  TeamRequest,
  TeamMemberResponse,
  AddTeamMemberRequest,
  ApiResponse,
} from '@/types';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

async function fetchAPI<T>(
  endpoint: string,
  options?: RequestInit,
): Promise<T> {
  const token =
    typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;

  const res = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options?.headers,
    },
  });

  if (!res.ok) {
    throw new Error(`API error: ${res.status}`);
  }

  const json: ApiResponse<T> = await res.json();
  return json.data;
}

// ─── Team CRUD ─────────────────────────────────────────────

export async function getTeams(): Promise<TeamResponse[]> {
  return fetchAPI<TeamResponse[]>('/api/v1/teams');
}

export async function getTeamById(id: number): Promise<TeamResponse> {
  return fetchAPI<TeamResponse>(`/api/v1/teams/${id}`);
}

export async function createTeam(
  req: TeamRequest,
): Promise<TeamResponse> {
  return fetchAPI<TeamResponse>('/api/v1/teams', {
    method: 'POST',
    body: JSON.stringify(req),
  });
}

export async function updateTeam(
  id: number,
  req: TeamRequest,
): Promise<TeamResponse> {
  return fetchAPI<TeamResponse>(`/api/v1/teams/${id}`, {
    method: 'PUT',
    body: JSON.stringify(req),
  });
}

export async function deleteTeam(id: number): Promise<void> {
  await fetchAPI<void>(`/api/v1/teams/${id}`, {
    method: 'DELETE',
  });
}

// ─── Team Members ──────────────────────────────────────────

export async function getTeamMembers(
  teamId: number,
): Promise<TeamMemberResponse[]> {
  return fetchAPI<TeamMemberResponse[]>(`/api/v1/teams/${teamId}/members`);
}

export async function addTeamMember(
  teamId: number,
  req: AddTeamMemberRequest,
): Promise<TeamMemberResponse> {
  return fetchAPI<TeamMemberResponse>(`/api/v1/teams/${teamId}/members`, {
    method: 'POST',
    body: JSON.stringify(req),
  });
}

export async function removeTeamMember(
  teamId: number,
  memberId: number,
): Promise<void> {
  await fetchAPI<void>(`/api/v1/teams/${teamId}/members/${memberId}`, {
    method: 'DELETE',
  });
}
