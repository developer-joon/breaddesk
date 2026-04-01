import api from '@/lib/api';
import type {
  ApiResponse,
  Page,
  TaskResponse,
  TaskRequest,
  TaskStatusUpdateRequest,
  TaskKanbanResponse,
  TaskChecklistRequest,
  TaskChecklistResponse,
  TaskCommentRequest,
  TaskCommentResponse,
  TaskTagRequest,
  TaskTagResponse,
  TaskLogResponse,
  TaskHoldRequest,
  TaskTransferRequest,
  TaskRelationResponse,
  TaskRelationRequest,
  TaskWatchResponse,
  AssigneeRecommendation,
} from '@/types';

export interface TaskFilterParams {
  status?: string;
  assigneeId?: number;
  priority?: string;
  teamId?: number;
  dueDateFrom?: string;
  dueDateTo?: string;
  page?: number;
  size?: number;
}

export async function getTasks(filters?: TaskFilterParams): Promise<Page<TaskResponse>> {
  const { data } = await api.get<ApiResponse<Page<TaskResponse>>>('/tasks', {
    params: {
      page: filters?.page || 0,
      size: filters?.size || 20,
      ...(filters?.status && { status: filters.status }),
      ...(filters?.assigneeId && { assigneeId: filters.assigneeId }),
      ...(filters?.priority && { priority: filters.priority }),
      ...(filters?.teamId && { teamId: filters.teamId }),
      ...(filters?.dueDateFrom && { dueDateFrom: filters.dueDateFrom }),
      ...(filters?.dueDateTo && { dueDateTo: filters.dueDateTo }),
    },
  });
  return data.data;
}

export async function getTaskById(id: number): Promise<TaskResponse> {
  const { data } = await api.get<ApiResponse<TaskResponse>>(`/tasks/${id}`);
  return data.data;
}

export async function createTask(req: TaskRequest): Promise<TaskResponse> {
  const { data } = await api.post<ApiResponse<TaskResponse>>('/tasks', req);
  return data.data;
}

export async function updateTask(id: number, req: TaskRequest): Promise<TaskResponse> {
  const { data } = await api.put<ApiResponse<TaskResponse>>(`/tasks/${id}`, req);
  return data.data;
}

export async function updateTaskStatus(
  id: number,
  req: TaskStatusUpdateRequest,
): Promise<TaskResponse> {
  const { data } = await api.patch<ApiResponse<TaskResponse>>(`/tasks/${id}/status`, req);
  return data.data;
}

export async function deleteTask(id: number): Promise<void> {
  await api.delete(`/tasks/${id}`);
}

export interface KanbanMap {
  waiting: TaskResponse[];
  inProgress: TaskResponse[];
  pending: TaskResponse[];
  review: TaskResponse[];
  done: TaskResponse[];
}

export async function getKanbanView(): Promise<KanbanMap> {
  const { data } = await api.get<ApiResponse<KanbanMap>>('/tasks/kanban');
  return data.data;
}

// ── Checklists ──
export async function addChecklist(
  taskId: number,
  req: TaskChecklistRequest,
): Promise<TaskChecklistResponse> {
  const { data } = await api.post<ApiResponse<TaskChecklistResponse>>(
    `/tasks/${taskId}/checklists`,
    req,
  );
  return data.data;
}

export async function updateChecklist(
  taskId: number,
  checklistId: number,
  req: TaskChecklistRequest,
): Promise<TaskChecklistResponse> {
  const { data } = await api.put<ApiResponse<TaskChecklistResponse>>(
    `/tasks/${taskId}/checklists/${checklistId}`,
    req,
  );
  return data.data;
}

export async function deleteChecklist(taskId: number, checklistId: number): Promise<void> {
  await api.delete(`/tasks/${taskId}/checklists/${checklistId}`);
}

// ── Tags ──
export async function addTag(taskId: number, req: TaskTagRequest): Promise<TaskTagResponse> {
  const { data } = await api.post<ApiResponse<TaskTagResponse>>(`/tasks/${taskId}/tags`, req);
  return data.data;
}

export async function deleteTag(taskId: number, tag: string): Promise<void> {
  await api.delete(`/tasks/${taskId}/tags/${tag}`);
}

// ── Comments ──
export async function addComment(
  taskId: number,
  req: TaskCommentRequest,
): Promise<TaskCommentResponse> {
  const { data } = await api.post<ApiResponse<TaskCommentResponse>>(
    `/tasks/${taskId}/comments`,
    req,
  );
  return data.data;
}

export async function getComments(taskId: number): Promise<TaskCommentResponse[]> {
  const { data } = await api.get<ApiResponse<TaskCommentResponse[]>>(`/tasks/${taskId}/comments`);
  return data.data;
}

// ── Logs ──
export async function getLogs(taskId: number): Promise<TaskLogResponse[]> {
  const { data } = await api.get<ApiResponse<TaskLogResponse[]>>(`/tasks/${taskId}/logs`);
  return data.data;
}

// ── Hold / Resume / Transfer ──
export async function holdTask(taskId: number, req: TaskHoldRequest): Promise<void> {
  await api.post(`/tasks/${taskId}/hold`, req);
}

export async function resumeTask(taskId: number): Promise<void> {
  await api.post(`/tasks/${taskId}/resume`);
}

export async function transferTask(taskId: number, req: TaskTransferRequest): Promise<void> {
  await api.post(`/tasks/${taskId}/transfer`, req);
}

// ── Task Relations ──
export async function getTaskRelations(taskId: number): Promise<TaskRelationResponse[]> {
  const { data } = await api.get<ApiResponse<TaskRelationResponse[]>>(
    `/tasks/${taskId}/relations`,
  );
  return data.data;
}

export async function addTaskRelation(
  taskId: number,
  req: TaskRelationRequest,
): Promise<TaskRelationResponse> {
  const { data } = await api.post<ApiResponse<TaskRelationResponse>>(
    `/tasks/${taskId}/relations`,
    req,
  );
  return data.data;
}

export async function deleteTaskRelation(taskId: number, relationId: number): Promise<void> {
  await api.delete(`/tasks/${taskId}/relations/${relationId}`);
}

// ── Task Watching ──
export async function watchTask(taskId: number): Promise<void> {
  await api.post(`/tasks/${taskId}/watch`);
}

export async function unwatchTask(taskId: number): Promise<void> {
  await api.delete(`/tasks/${taskId}/watch`);
}

export async function isWatching(taskId: number): Promise<boolean> {
  const { data } = await api.get<ApiResponse<boolean>>(`/tasks/${taskId}/watching`);
  return data.data;
}

// ── Assignee Recommendation ──
export async function getAssigneeRecommendations(
  taskId: number,
): Promise<AssigneeRecommendation[]> {
  const { data } = await api.get<ApiResponse<AssigneeRecommendation[]>>(
    `/tasks/${taskId}/recommend-assignee`,
  );
  return data.data;
}

// ── Internal Comments ──
export async function getInternalComments(taskId: number): Promise<TaskCommentResponse[]> {
  const { data } = await api.get<ApiResponse<TaskCommentResponse[]>>(
    `/tasks/${taskId}/comments/internal`,
  );
  return data.data;
}
