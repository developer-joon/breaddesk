import api from '@/lib/api';
import type { ApiResponse, AttachmentResponse } from '@/types';

export async function uploadAttachment(
  file: File,
  entityType: 'inquiry' | 'task',
  entityId: number,
): Promise<AttachmentResponse> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('entityType', entityType);
  formData.append('entityId', entityId.toString());

  const { data } = await api.post<ApiResponse<AttachmentResponse>>(
    '/attachments',
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
    },
  );
  return data.data;
}

export async function downloadAttachment(id: number): Promise<void> {
  const { data, headers } = await api.get(`/attachments/${id}/download`, {
    responseType: 'blob',
  });

  // Extract filename from Content-Disposition header if available
  const contentDisposition = headers['content-disposition'];
  let filename = `attachment-${id}`;
  if (contentDisposition) {
    const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
    if (filenameMatch?.[1]) {
      filename = filenameMatch[1].replace(/['"]/g, '');
    }
  }

  const blob = new Blob([data]);
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

export async function deleteAttachment(id: number): Promise<void> {
  await api.delete(`/attachments/${id}`);
}

export async function getAttachments(
  entityType: 'inquiry' | 'task',
  entityId: number,
): Promise<AttachmentResponse[]> {
  const { data } = await api.get<ApiResponse<AttachmentResponse[]>>('/attachments', {
    params: { entityType, entityId },
  });
  return data.data;
}
