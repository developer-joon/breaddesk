import api from '@/lib/api';
import type { ApiResponse, Page, NotificationResponse } from '@/types';

export async function getNotifications(
  page = 0,
  size = 20,
  unreadOnly = false,
): Promise<Page<NotificationResponse>> {
  const { data } = await api.get<ApiResponse<Page<NotificationResponse>>>('/notifications', {
    params: { page, size, unreadOnly },
  });
  return data.data;
}

export async function getUnreadCount(): Promise<number> {
  const { data } = await api.get<ApiResponse<number>>('/notifications/unread-count');
  return data.data;
}

export async function markAsRead(id: number): Promise<void> {
  await api.patch(`/notifications/${id}/read`);
}

export async function markAllAsRead(): Promise<void> {
  await api.patch('/notifications/read-all');
}

export async function deleteNotification(id: number): Promise<void> {
  await api.delete(`/notifications/${id}`);
}
