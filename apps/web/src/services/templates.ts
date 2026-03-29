import api from '@/lib/api';
import type { ApiResponse, Page, ReplyTemplateResponse, ReplyTemplateRequest } from '@/types';

export async function getTemplates(
  page = 0,
  size = 20,
  category?: string,
): Promise<Page<ReplyTemplateResponse>> {
  const { data } = await api.get<ApiResponse<Page<ReplyTemplateResponse>>>('/reply-templates', {
    params: { page, size, ...(category ? { category } : {}) },
  });
  return data.data;
}

export async function getTemplateById(id: number): Promise<ReplyTemplateResponse> {
  const { data } = await api.get<ApiResponse<ReplyTemplateResponse>>(`/reply-templates/${id}`);
  return data.data;
}

export async function createTemplate(req: ReplyTemplateRequest): Promise<ReplyTemplateResponse> {
  const { data } = await api.post<ApiResponse<ReplyTemplateResponse>>('/reply-templates', req);
  return data.data;
}

export async function updateTemplate(
  id: number,
  req: ReplyTemplateRequest,
): Promise<ReplyTemplateResponse> {
  const { data } = await api.put<ApiResponse<ReplyTemplateResponse>>(`/reply-templates/${id}`, req);
  return data.data;
}

export async function applyTemplate(
  id: number,
  variables?: Record<string, string>,
): Promise<string> {
  const { data } = await api.post<ApiResponse<string>>(`/reply-templates/${id}/apply`, variables);
  return data.data;
}

export async function deleteTemplate(id: number): Promise<void> {
  await api.delete(`/reply-templates/${id}`);
}
