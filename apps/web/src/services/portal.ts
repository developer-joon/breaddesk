import { api } from '@/lib/api';
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
    const response = await fetch(`/api/v1/portal/${token}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    });
    if (!response.ok) {
      throw new Error('Failed to load inquiry');
    }
    return response.json();
  },

  addMessage: async (token: string, message: string): Promise<InquiryMessageResponse> => {
    const response = await fetch(`/api/v1/portal/${token}/messages`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message }),
    });
    if (!response.ok) {
      throw new Error('Failed to send message');
    }
    return response.json();
  },
};
