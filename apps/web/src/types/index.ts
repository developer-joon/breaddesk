// ─── Common ───────────────────────────────────────────────
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: string;
  timestamp: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // 0-based page number
  first: boolean;
  last: boolean;
  empty: boolean;
}

// ─── Auth ─────────────────────────────────────────────────
export interface LoginRequest {
  email: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

export type AuthResponse = TokenResponse;

// ─── Member ───────────────────────────────────────────────
export type MemberRole = 'AGENT' | 'ADMIN';

export interface User {
  id: string;
  name: string;
  email: string;
  role: MemberRole;
  skills?: string;
  active?: boolean;
  avatar?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface MemberRequest {
  name: string;
  email: string;
  password?: string;
  role: MemberRole;
  skills?: string;
  active?: boolean;
}

// ─── Inquiry ──────────────────────────────────────────────
export type InquiryStatus = 'OPEN' | 'AI_ANSWERED' | 'ESCALATED' | 'RESOLVED' | 'CLOSED';
export type InquiryMessageRole = 'USER' | 'AI' | 'AGENT';

export interface InquiryMessageResponse {
  id: number;
  inquiryId: number;
  role: InquiryMessageRole;
  message: string;
  createdAt: string;
}

export interface InquiryResponse {
  id: number;
  channel: string;
  channelMeta?: string;
  senderName: string;
  senderEmail?: string;
  message: string;
  aiResponse?: string;
  aiConfidence?: number;
  status: InquiryStatus;
  taskId?: number;
  resolvedBy?: string;
  createdAt: string;
  resolvedAt?: string;
  messages?: InquiryMessageResponse[];
}

export interface InquiryRequest {
  channel: string;
  channelMeta?: string;
  senderName: string;
  senderEmail?: string;
  message: string;
}

export interface InquiryMessageRequest {
  role: InquiryMessageRole;
  message: string;
}

export interface InquiryStatusUpdateRequest {
  status: InquiryStatus;
}

export interface ConvertToTaskRequest {
  title: string;
  description?: string;
  type?: string;
  urgency?: TaskUrgency;
  assigneeId?: number;
}

// Legacy types used in existing pages (kept for compat)
export interface Inquiry {
  id: string;
  title: string;
  content: string;
  status: string;
  channel: string;
  customerName: string;
  customerEmail: string;
  createdAt: string;
  updatedAt: string;
  tags: string[];
}

export interface Message {
  id: string;
  inquiryId: string;
  content: string;
  sender: string;
  senderName: string;
  createdAt: string;
}

// ─── Task ─────────────────────────────────────────────────
export type TaskStatus = 'WAITING' | 'IN_PROGRESS' | 'PENDING' | 'REVIEW' | 'DONE';
export type TaskUrgency = 'LOW' | 'NORMAL' | 'HIGH' | 'CRITICAL';
// Legacy alias used in existing pages
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface TaskChecklistResponse {
  id: number;
  itemText: string;
  done: boolean;
  sortOrder: number;
}

export interface TaskTagResponse {
  id: number;
  tag: string;
}

export interface TaskCommentResponse {
  id: number;
  authorId: number;
  content: string;
  internal: boolean;
  createdAt: string;
}

export interface TaskLogResponse {
  id: number;
  action: string;
  actorId: number;
  details?: string;
  createdAt: string;
}

export interface TaskResponse {
  id: number;
  title: string;
  description?: string;
  type: string;
  urgency: TaskUrgency;
  status: TaskStatus;
  requesterName?: string;
  requesterEmail?: string;
  assigneeId?: number;
  aiSummary?: string;
  dueDate?: string;
  estimatedHours?: number;
  actualHours?: number;
  slaResponseDeadline?: string;
  slaResolveDeadline?: string;
  slaResponseBreached: boolean;
  slaResolveBreached: boolean;
  transferCount: number;
  createdAt: string;
  startedAt?: string;
  completedAt?: string;
  checklists?: TaskChecklistResponse[];
  tags?: TaskTagResponse[];
  comments?: TaskCommentResponse[];
  logs?: TaskLogResponse[];
}

export interface TaskRequest {
  title: string;
  description?: string;
  type?: string;
  urgency: TaskUrgency;
  status?: TaskStatus;
  requesterName?: string;
  requesterEmail?: string;
  assigneeId?: number;
  dueDate?: string;
  estimatedHours?: number;
  actualHours?: number;
}

export interface TaskStatusUpdateRequest {
  status: TaskStatus;
}

export interface TaskChecklistRequest {
  itemText: string;
  done?: boolean;
  sortOrder?: number;
}

export interface TaskCommentRequest {
  content: string;
  internal?: boolean;
}

export interface TaskTagRequest {
  tag: string;
}

export interface TaskHoldRequest {
  reason: string;
}

export interface TaskTransferRequest {
  toMemberId: number;
  reason: string;
}

export interface TaskKanbanResponse {
  status: TaskStatus;
  tasks: TaskResponse[];
}

// Legacy task type used in existing pages
export interface Task {
  id: string;
  title: string;
  description: string;
  status: string;
  type: string;
  priority: string;
  assigneeId: string;
  assigneeName: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
  slaDeadline?: string;
}

// ─── Template ─────────────────────────────────────────────
export interface ReplyTemplateResponse {
  id: number;
  title: string;
  category?: string;
  content: string;
  usageCount: number;
  createdBy?: number;
  createdAt: string;
  updatedAt: string;
}

export interface ReplyTemplateRequest {
  title: string;
  category?: string;
  content: string;
}

// Legacy type used in existing pages
export interface ReplyTemplate {
  id: string;
  title: string;
  category: string;
  content: string;
  variables: string[];
  createdAt: string;
  updatedAt: string;
}

// ─── Dashboard ────────────────────────────────────────────
export interface DashboardStats {
  totalInquiries: number;
  unresolvedInquiries: number;
  todayInquiries: number;
  aiResolutionRate: number;
  inquiriesByStatus?: Record<string, number>;
  tasksByStatus?: Record<string, number>;
}

export interface RecentInquiry {
  id: string;
  title: string;
  status: string;
  channel: string;
  createdAt: string;
  customerName: string;
}

export interface TaskStatusCount {
  status: string;
  count: number;
}

// ─── Notification ─────────────────────────────────────────
export interface NotificationResponse {
  id: number;
  memberId: number;
  type: string;
  title: string;
  message: string;
  link?: string;
  read: boolean;
  createdAt: string;
}

// ─── My / Personal ───────────────────────────────────────
export interface MyKPI {
  processedCount: number;
  averageTimeMinutes: number;
  resolvedCount: number;
  period: string;
}

export interface PersonalNote {
  id: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface PersonalNoteRequest {
  content: string;
}

export interface PersonalNoteResponse {
  id: number;
  memberId: number;
  content: string;
  createdAt: string;
  updatedAt: string;
}

// ─── Knowledge ────────────────────────────────────────────
export interface KnowledgeConnector {
  id: string;
  name: string;
  type: string;
  status: string;
  lastSyncAt?: string;
  documentCount: number;
}

export interface KnowledgeDocument {
  id: string;
  title: string;
  connectorId: string;
  connectorName: string;
  url?: string;
  lastUpdated: string;
}

// ─── SLA ──────────────────────────────────────────────────
export interface SLARule {
  id: string;
  name: string;
  priority: string;
  responseTimeHours: number;
  resolutionTimeHours: number;
  enabled: boolean;
}
