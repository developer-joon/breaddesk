// ─── Team ─────────────────────────────────────────────────
export type TeamMemberRole = 'LEADER' | 'MEMBER';

export interface TeamResponse {
  id: number;
  name: string;
  description?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TeamRequest {
  name: string;
  description?: string;
  active?: boolean;
}

export interface TeamMemberResponse {
  id: number;
  teamId: number;
  memberId: number;
  memberName?: string;
  memberEmail?: string;
  role: TeamMemberRole;
  joinedAt: string;
}

export interface AddTeamMemberRequest {
  memberId: number;
  role: TeamMemberRole;
}
