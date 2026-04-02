import api from '@/lib/api';
import type { ApiResponse, ChannelResponse } from '@/types';

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

// Map backend response to frontend type
function mapToChannelResponse(config: ChannelConfigResponse): ChannelResponse {
  return {
    id: config.id,
    name: config.channelType,
    type: config.channelType,
    enabled: config.isActive,
    webhookUrl: config.webhookUrl,
    hasAuthToken: config.hasAuthToken,
    config: config.config ? JSON.parse(config.config) : undefined,
    createdAt: config.createdAt,
    updatedAt: config.updatedAt || config.createdAt,
  };
}

export interface ChannelConfigRequest {
  channelType: string;
  webhookUrl?: string;
  authToken?: string;
  isActive?: boolean;
  config?: string;
}

export async function getChannels(): Promise<ChannelResponse[]> {
  const { data } = await api.get<ApiResponse<ChannelConfigResponse[]>>('/channels');
  return data.data.map(mapToChannelResponse);
}

export async function getActiveChannels(): Promise<ChannelResponse[]> {
  const { data } = await api.get<ApiResponse<ChannelConfigResponse[]>>('/channels/active');
  return data.data.map(mapToChannelResponse);
}

export async function getChannel(id: number): Promise<ChannelResponse> {
  const { data } = await api.get<ApiResponse<ChannelConfigResponse>>(`/channels/${id}`);
  return mapToChannelResponse(data.data);
}

export async function createChannel(request: ChannelConfigRequest | any): Promise<ChannelResponse> {
  // Map frontend names to backend names if needed
  const backendRequest: ChannelConfigRequest = {
    channelType: request.type || request.channelType,
    webhookUrl: request.webhookUrl,
    authToken: request.authToken,
    isActive: request.enabled !== undefined ? request.enabled : request.isActive,
    config: request.config ? JSON.stringify(request.config) : undefined,
  };
  const { data } = await api.post<ApiResponse<ChannelConfigResponse>>('/channels', backendRequest);
  return mapToChannelResponse(data.data);
}

export async function updateChannel(
  id: number,
  request: ChannelConfigRequest | any,
): Promise<ChannelResponse> {
  // Map frontend names to backend names if needed
  const backendRequest: ChannelConfigRequest = {
    channelType: request.type || request.channelType,
    webhookUrl: request.webhookUrl,
    authToken: request.authToken,
    isActive: request.enabled !== undefined ? request.enabled : request.isActive,
    config: request.config ? JSON.stringify(request.config) : undefined,
  };
  const { data } = await api.put<ApiResponse<ChannelConfigResponse>>(`/channels/${id}`, backendRequest);
  return mapToChannelResponse(data.data);
}

export async function deleteChannel(id: number): Promise<void> {
  await api.delete(`/channels/${id}`);
}

export async function testChannel(id: number): Promise<string> {
  const { data } = await api.post<ApiResponse<string>>(`/channels/${id}/test`);
  return data.data;
}

export const channelService = {
  list: getChannels,
  getChannels,
  getActiveChannels,
  getChannel,
  create: createChannel,
  createChannel,
  update: updateChannel,
  updateChannel,
  delete: deleteChannel,
  deleteChannel,
  test: testChannel,
  testChannel,
};
