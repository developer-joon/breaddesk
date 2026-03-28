/**
 * BreadDesk Shared Types
 * 프론트엔드/백엔드 공유 타입 정의
 */

// 문의 상태
export type InquiryStatus = 'OPEN' | 'AI_ANSWERED' | 'ESCALATED' | 'RESOLVED' | 'CLOSED';

// 업무 상태
export type TaskStatus = 'WAITING' | 'IN_PROGRESS' | 'REVIEW' | 'DONE';

// 긴급도
export type Urgency = 'LOW' | 'NORMAL' | 'HIGH' | 'CRITICAL';

// 업무 유형
export type TaskType = 
  | 'DEVELOPMENT' 
  | 'ACCESS' 
  | 'INFRA' 
  | 'FIREWALL' 
  | 'DEPLOY' 
  | 'INCIDENT' 
  | 'GENERAL';

// 역할
export type MemberRole = 'AGENT' | 'ADMIN';
