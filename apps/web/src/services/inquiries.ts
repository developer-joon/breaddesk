import api from '@/lib/api';
import type {
  ApiResponse,
  Page,
  InquiryResponse,
  InquiryRequest,
  InquiryStatusUpdateRequest,
  InquiryMessageRequest,
  InquiryMessageResponse,
  ConvertToTaskRequest,
} from '@/types';

export async function getInquiries(page = 0, size = 20): Promise<Page<InquiryResponse>> {
  const { data } = await api.get<ApiResponse<Page<InquiryResponse>>>('/inquiries', {
    params: { page, size },
  });
  return data.data;
}

export async function getInquiryById(id: number): Promise<InquiryResponse> {
  const { data } = await api.get<ApiResponse<InquiryResponse>>(`/inquiries/${id}`);
  return data.data;
}

export async function createInquiry(req: InquiryRequest): Promise<InquiryResponse> {
  const { data } = await api.post<ApiResponse<InquiryResponse>>('/inquiries', req);
  return data.data;
}

export async function updateInquiryStatus(
  id: number,
  req: InquiryStatusUpdateRequest,
): Promise<InquiryResponse> {
  const { data } = await api.patch<ApiResponse<InquiryResponse>>(`/inquiries/${id}/status`, req);
  return data.data;
}

export async function addInquiryMessage(
  id: number,
  req: InquiryMessageRequest,
): Promise<InquiryMessageResponse> {
  const { data } = await api.post<ApiResponse<InquiryMessageResponse>>(
    `/inquiries/${id}/messages`,
    req,
  );
  return data.data;
}

export async function convertInquiryToTask(
  id: number,
  req: ConvertToTaskRequest,
): Promise<InquiryResponse> {
  const { data } = await api.post<ApiResponse<InquiryResponse>>(
    `/inquiries/${id}/convert-to-task`,
    req,
  );
  return data.data;
}

export async function deleteInquiry(id: number): Promise<void> {
  await api.delete(`/inquiries/${id}`);
}

export async function getSimilarInquiries(id: number, limit = 5): Promise<any[]> {
  const { data } = await api.get<ApiResponse<any[]>>(`/inquiries/${id}/similar`, {
    params: { limit },
  });
  return data.data;
}
