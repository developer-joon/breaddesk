import type {
  ApiResponse,
  PaginatedResponse,
  Inquiry,
  Task,
  Member,
  ReplyTemplate,
  KnowledgeConnector,
  KnowledgeDocument,
  DashboardStats,
} from '@/types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1';

class ApiClient {
  private baseURL: string;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> {
    const url = `${this.baseURL}${endpoint}`;
    
    try {
      const response = await fetch(url, {
        ...options,
        headers: {
          'Content-Type': 'application/json',
          ...options.headers,
        },
      });

      if (!response.ok) {
        const error = await response.json().catch(() => ({ message: response.statusText }));
        return {
          success: false,
          error: error.message || `HTTP ${response.status}`,
        };
      }

      const data = await response.json();
      return {
        success: true,
        data,
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
      };
    }
  }

  // ========== Inquiry API ==========
  async getInquiries(params?: {
    status?: string;
    page?: number;
    pageSize?: number;
  }): Promise<ApiResponse<PaginatedResponse<Inquiry>>> {
    const searchParams = new URLSearchParams();
    if (params?.status) searchParams.set('status', params.status);
    if (params?.page) searchParams.set('page', params.page.toString());
    if (params?.pageSize) searchParams.set('pageSize', params.pageSize.toString());

    const query = searchParams.toString();
    return this.request<PaginatedResponse<Inquiry>>(
      `/inquiries${query ? `?${query}` : ''}`
    );
  }

  async getInquiry(id: number): Promise<ApiResponse<Inquiry>> {
    return this.request<Inquiry>(`/inquiries/${id}`);
  }

  async replyToInquiry(id: number, message: string): Promise<ApiResponse<void>> {
    return this.request<void>(`/inquiries/${id}/reply`, {
      method: 'POST',
      body: JSON.stringify({ message }),
    });
  }

  async submitInquiryFeedback(
    id: number,
    resolved: boolean
  ): Promise<ApiResponse<void>> {
    return this.request<void>(`/inquiries/${id}/feedback`, {
      method: 'POST',
      body: JSON.stringify({ resolved }),
    });
  }

  // ========== Task API ==========
  async getTasks(params?: {
    status?: string;
    assigneeId?: number;
    type?: string;
    page?: number;
    pageSize?: number;
  }): Promise<ApiResponse<PaginatedResponse<Task>>> {
    const searchParams = new URLSearchParams();
    if (params?.status) searchParams.set('status', params.status);
    if (params?.assigneeId) searchParams.set('assigneeId', params.assigneeId.toString());
    if (params?.type) searchParams.set('type', params.type);
    if (params?.page) searchParams.set('page', params.page.toString());
    if (params?.pageSize) searchParams.set('pageSize', params.pageSize.toString());

    const query = searchParams.toString();
    return this.request<PaginatedResponse<Task>>(`/tasks${query ? `?${query}` : ''}`);
  }

  async getTask(id: number): Promise<ApiResponse<Task>> {
    return this.request<Task>(`/tasks/${id}`);
  }

  async createTask(task: Partial<Task>): Promise<ApiResponse<Task>> {
    return this.request<Task>('/tasks', {
      method: 'POST',
      body: JSON.stringify(task),
    });
  }

  async updateTask(id: number, updates: Partial<Task>): Promise<ApiResponse<Task>> {
    return this.request<Task>(`/tasks/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(updates),
    });
  }

  async deleteTask(id: number): Promise<ApiResponse<void>> {
    return this.request<void>(`/tasks/${id}`, {
      method: 'DELETE',
    });
  }

  async assignTask(id: number, assigneeId: number): Promise<ApiResponse<void>> {
    return this.request<void>(`/tasks/${id}/assign`, {
      method: 'POST',
      body: JSON.stringify({ assigneeId }),
    });
  }

  async getKanbanTasks(): Promise<ApiResponse<Record<string, Task[]>>> {
    return this.request<Record<string, Task[]>>('/tasks/kanban');
  }

  // ========== Template API ==========
  async getTemplates(): Promise<ApiResponse<ReplyTemplate[]>> {
    return this.request<ReplyTemplate[]>('/templates');
  }

  async getTemplate(id: number): Promise<ApiResponse<ReplyTemplate>> {
    return this.request<ReplyTemplate>(`/templates/${id}`);
  }

  async createTemplate(template: Partial<ReplyTemplate>): Promise<ApiResponse<ReplyTemplate>> {
    return this.request<ReplyTemplate>('/templates', {
      method: 'POST',
      body: JSON.stringify(template),
    });
  }

  async updateTemplate(id: number, updates: Partial<ReplyTemplate>): Promise<ApiResponse<ReplyTemplate>> {
    return this.request<ReplyTemplate>(`/templates/${id}`, {
      method: 'PUT',
      body: JSON.stringify(updates),
    });
  }

  async deleteTemplate(id: number): Promise<ApiResponse<void>> {
    return this.request<void>(`/templates/${id}`, {
      method: 'DELETE',
    });
  }

  // ========== Dashboard API ==========
  async getDashboardStats(): Promise<ApiResponse<DashboardStats>> {
    return this.request<DashboardStats>('/stats/overview');
  }

  // ========== Knowledge API ==========
  async getKnowledgeConnectors(): Promise<ApiResponse<KnowledgeConnector[]>> {
    return this.request<KnowledgeConnector[]>('/knowledge/connectors');
  }

  async createKnowledgeConnector(connector: Partial<KnowledgeConnector>): Promise<ApiResponse<KnowledgeConnector>> {
    return this.request<KnowledgeConnector>('/knowledge/connectors', {
      method: 'POST',
      body: JSON.stringify(connector),
    });
  }

  async syncConnector(id: number): Promise<ApiResponse<void>> {
    return this.request<void>(`/knowledge/connectors/${id}/sync`, {
      method: 'POST',
    });
  }

  async searchKnowledge(query: string): Promise<ApiResponse<KnowledgeDocument[]>> {
    return this.request<KnowledgeDocument[]>(`/knowledge/search?q=${encodeURIComponent(query)}`);
  }

  // ========== Member API ==========
  async getMembers(): Promise<ApiResponse<Member[]>> {
    return this.request<Member[]>('/members');
  }
}

export const api = new ApiClient(API_BASE_URL);
