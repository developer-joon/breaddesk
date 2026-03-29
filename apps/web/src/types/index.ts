// 문의 (Inquiry)
export type InquiryStatus = 'OPEN' | 'AI_ANSWERED' | 'ESCALATED' | 'RESOLVED' | 'CLOSED';
export type MessageRole = 'USER' | 'AI' | 'AGENT';
export type ResolvedBy = 'AI' | 'HUMAN';

export interface InquiryMessage {
  id: number;
  inquiryId: number;
  role: MessageRole;
  message: string;
  createdAt: string;
}

export interface Inquiry {
  id: number;
  channel: string;
  channelMeta?: Record<string, any>;
  senderName: string;
  senderEmail: string;
  message: string;
  aiResponse?: string;
  aiConfidence?: number;
  status: InquiryStatus;
  taskId?: number;
  resolvedBy?: ResolvedBy;
  createdAt: string;
  resolvedAt?: string;
  messages?: InquiryMessage[];
}

// 업무 (Task)
export type TaskType = 
  | 'DEVELOPMENT' 
  | 'ACCESS' 
  | 'INFRA' 
  | 'FIREWALL' 
  | 'DEPLOY' 
  | 'INCIDENT' 
  | 'GENERAL';

export type TaskUrgency = 'LOW' | 'NORMAL' | 'HIGH' | 'CRITICAL';
export type TaskStatus = 'WAITING' | 'IN_PROGRESS' | 'REVIEW' | 'DONE';

export interface TaskChecklist {
  id: number;
  taskId: number;
  itemText: string;
  isDone: boolean;
  sortOrder: number;
}

export interface TaskComment {
  id: number;
  taskId: number;
  authorId: number;
  authorName?: string;
  content: string;
  isInternal: boolean;
  createdAt: string;
}

export interface TaskLog {
  id: number;
  taskId: number;
  action: string;
  actorId: number;
  actorName?: string;
  details?: Record<string, any>;
  createdAt: string;
}

export interface Task {
  id: number;
  title: string;
  description?: string;
  type: TaskType;
  urgency: TaskUrgency;
  status: TaskStatus;
  requesterName: string;
  requesterEmail: string;
  assigneeId?: number;
  assigneeName?: string;
  inquiryId?: number;
  aiSummary?: string;
  dueDate?: string;
  estimatedHours?: number;
  actualHours?: number;
  slaResponseDeadline?: string;
  slaResolveDeadline?: string;
  slaRespondedAt?: string;
  slaResponseBreached: boolean;
  slaResolveBreached: boolean;
  createdAt: string;
  startedAt?: string;
  completedAt?: string;
  checklists?: TaskChecklist[];
  comments?: TaskComment[];
  logs?: TaskLog[];
  tags?: string[];
}

// 팀원 (Member)
export type MemberRole = 'AGENT' | 'ADMIN';

export interface Member {
  id: number;
  name: string;
  email: string;
  role: MemberRole;
  skills?: Record<string, number>;
  isActive: boolean;
}

// 답변 템플릿 (Template)
export interface ReplyTemplate {
  id: number;
  title: string;
  category: string;
  content: string;
  usageCount: number;
  createdBy: number;
  createdByName?: string;
  createdAt: string;
  updatedAt: string;
}

// 지식베이스
export interface KnowledgeDocument {
  id: number;
  source: string;
  sourceId: string;
  title: string;
  content: string;
  url?: string;
  tags?: string[];
  syncedAt?: string;
  createdAt: string;
}

export interface KnowledgeConnector {
  id: number;
  sourceType: string;
  config: Record<string, any>;
  syncIntervalMin: number;
  lastSyncedAt?: string;
  isActive: boolean;
}

// 대시보드 통계
export interface DashboardStats {
  totalInquiries: number;
  unresolvedInquiries: number;
  todayInquiries: number;
  resolvedRate: number;
  tasksByStatus: Record<TaskStatus, number>;
  recentInquiries: Inquiry[];
}

// API 응답 타입 (백엔드와 일치)
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}
