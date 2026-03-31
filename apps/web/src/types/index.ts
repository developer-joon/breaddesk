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

// ─── Knowledge (Phase 2) ──────────────────────────────────
export type ConnectorType = 'NOTION' | 'CONFLUENCE' | 'GOOGLE_DRIVE' | 'WEB_CRAWL' | 'LOCAL';
export type ConnectorStatus = 'CONNECTED' | 'DISCONNECTED' | 'SYNCING' | 'ERROR';

export interface KnowledgeConnectorResponse {
  id: number;
  name: string;
  type: ConnectorType;
  config?: Record<string, string>;
  status: ConnectorStatus;
  lastSyncAt?: string;
  documentCount: number;
  errorMessage?: string;
  createdAt: string;
  updatedAt: string;
}

export interface KnowledgeConnectorRequest {
  name: string;
  type: ConnectorType;
  config?: Record<string, string>;
}

export interface KnowledgeDocumentResponse {
  id: number;
  connectorId: number;
  connectorName?: string;
  title: string;
  content?: string;
  sourceUrl?: string;
  tags?: string[];
  metadata?: Record<string, string>;
  lastSyncAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface VectorSearchResult {
  documentId: number;
  title: string;
  content: string;
  sourceUrl?: string;
  score: number;
  connectorName?: string;
  tags?: string[];
}

export interface SimilarInquiryResponse {
  inquiryId: number;
  senderName: string;
  message: string;
  status: InquiryStatus;
  score: number;
  createdAt: string;
}

// ─── SLA (Phase 2) ────────────────────────────────────────
export interface SlaRuleResponse {
  id: number;
  urgency: TaskUrgency;
  responseTimeHours: number;
  resolveTimeHours: number;
  enabled: boolean;
}

export interface SlaRuleUpdateRequest {
  responseTimeHours: number;
  resolveTimeHours: number;
  enabled: boolean;
}

export interface SlaStatsResponse {
  overallResponseComplianceRate: number;
  overallResolveComplianceRate: number;
  responseComplianceByUrgency: Record<string, number>;
  resolveComplianceByUrgency: Record<string, number>;
  avgResponseMinutes: number | null;
  avgResolveMinutes: number | null;
  totalResponseBreaches: number;
  totalResolveBreaches: number;
}

// ─── Search ───────────────────────────────────────────────
export interface SearchResult {
  inquiries: InquiryResponse[];
  tasks: TaskResponse[];
  knowledge: KnowledgeDocumentResponse[];
}

// ─── Attachments ──────────────────────────────────────────
export interface AttachmentResponse {
  id: number;
  filename: string;
  fileSize: number;
  contentType: string;
  uploadedBy: number;
  uploadedAt: string;
}

// ─── Stats ────────────────────────────────────────────────
export interface StatsOverview {
  totalInquiries: number;
  totalTasks: number;
  totalMembers: number;
  aiResolutionRate: number;
  avgResponseTime: number;
  avgResolveTime: number;
}

export interface AIStats {
  totalAIResponses: number;
  aiAcceptRate: number;
  avgConfidence: number;
  topResolvedCategories: Array<{ category: string; count: number }>;
}

export interface TeamStats {
  members: Array<{
    memberId: number;
    memberName: string;
    assignedTasks: number;
    completedTasks: number;
    avgCompletionHours: number;
  }>;
}

export interface WeeklyReport {
  weekStart: string;
  weekEnd: string;
  totalInquiries: number;
  totalTasks: number;
  completedTasks: number;
  slaComplianceRate: number;
  topIssues: Array<{ issue: string; count: number }>;
}

// ─── Task Relations ───────────────────────────────────────
export interface TaskRelationResponse {
  id: number;
  taskId: number;
  relatedTaskId: number;
  relatedTaskTitle: string;
  relationType: string;
  createdAt: string;
}

export interface TaskRelationRequest {
  relatedTaskId: number;
  relationType?: string;
}

// ─── Task Watching ────────────────────────────────────────
export interface TaskWatchResponse {
  memberId: number;
  taskId: number;
  createdAt: string;
}

// ─── Task Assignee Recommendation ─────────────────────────
export interface AssigneeRecommendation {
  memberId: number;
  memberName: string;
  score: number;
  reason: string;
}

// ─── Channel ──────────────────────────────────────────────
export interface ChannelResponse {
  id: number;
  name: string;
  type: string;
  config?: Record<string, string>;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ChannelTestResult {
  success: boolean;
  message: string;
}
