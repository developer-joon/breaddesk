import api from '@/lib/api';
import type { ApiResponse } from '@/types';

export interface ChannelConfigResponse {
  id: number;
  channelType: string;
  webhookUrl?: string;
  isActive: boolean;
  config?: string;
  hasAuthToken: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface ChannelConfigRequest {
  channelType: string;
  webhookUrl?: string;
  authToken?: string;
  isActive?: boolean;
  config?: string;
}

export async function getChannels(): Promise<ChannelConfigResponse[]> {
  const { data } = await api.get<ApiResponse<ChannelConfigResponse[]>>('/channels');
  return data.data;
}

export async function getActiveChannels(): Promise<ChannelConfigResponse[]> {
  const { data } = await api.get<ApiResponse<ChannelConfigResponse[]>>('/channels/active');
  return data.data;
}

export async function getChannel(id: number): Promise<ChannelConfigResponse> {
  const { data } = await api.get<ApiResponse<ChannelConfigResponse>>(`/channels/${id}`);
  return data.data;
}

export async function createChannel(request: ChannelConfigRequest): Promise<ChannelConfigResponse> {
  const { data } = await api.post<ApiResponse<ChannelConfigResponse>>('/channels', request);
  return data.data;
}

export async function updateChannel(
  id: number,
  request: ChannelConfigRequest,
): Promise<ChannelConfigResponse> {
  const { data } = await api.put<ApiResponse<ChannelConfigResponse>>(`/channels/${id}`, request);
  return data.data;
}

export async function deleteChannel(id: number): Promise<void> {
  await api.delete(`/channels/${id}`);
}

export async function testChannel(id: number): Promise<string> {
  const { data } = await api.post<ApiResponse<string>>(`/channels/${id}/test`);
  return data.data;
}
