import { apiClient } from '@/lib/api-client';
import type { ApiResponse, InquiryResponse, InquiryMessageResponse } from '@/types';

export interface PortalInquiry {
  id: number;
  message: string;
  status: string;
  createdAt: string;
  messages: InquiryMessageResponse[];
}

export const portalService = {
  getInquiry: async (token: string): Promise<PortalInquiry> => {
    const response = await apiClient.get<ApiResponse<PortalInquiry>>(
      `/portal/inquiry/${token}`
    );
    return response.data.data;
  },

  addMessage: async (token: string, message: string): Promise<InquiryMessageResponse> => {
    const response = await apiClient.post<ApiResponse<InquiryMessageResponse>>(
      `/portal/inquiry/${token}/messages`,
      { message }
    );
    return response.data.data;
  },
};
