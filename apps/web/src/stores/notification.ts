import { create } from 'zustand';
import { getNotifications, getUnreadCount, markAsRead as apiMarkAsRead, markAllAsRead as apiMarkAllAsRead } from '@/services/notifications';

export interface Notification {
  id: number | string;
  title: string;
  message?: string;
  time: string;
  unread: boolean;
  type?: 'info' | 'warning' | 'error' | 'success';
}

interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  isPolling: boolean;
  lastFetchTime: number | null;
  
  addNotification: (notification: Omit<Notification, 'id' | 'time' | 'unread'>) => void;
  markAsRead: (id: number | string) => void;
  markAllAsRead: () => void;
  removeNotification: (id: number | string) => void;
  setNotifications: (notifications: Notification[]) => void;
  fetchNotifications: () => Promise<void>;
  startPolling: () => void;
  stopPolling: () => void;
}

let pollingInterval: NodeJS.Timeout | null = null;

function formatTime(dateStr: string): string {
  try {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMin = Math.floor(diffMs / 60000);
    if (diffMin < 1) return '방금 전';
    if (diffMin < 60) return `${diffMin}분 전`;
    const diffHour = Math.floor(diffMin / 60);
    if (diffHour < 24) return `${diffHour}시간 전`;
    const diffDay = Math.floor(diffHour / 24);
    return `${diffDay}일 전`;
  } catch {
    return dateStr;
  }
}

export const useNotificationStore = create<NotificationState>((set, get) => ({
  notifications: [],
  unreadCount: 0,
  isPolling: false,
  lastFetchTime: null,

  addNotification: (notification) => {
    const newNotif: Notification = {
      ...notification,
      id: Date.now(),
      time: '방금 전',
      unread: true,
    };
    
    set((state) => ({
      notifications: [newNotif, ...state.notifications],
      unreadCount: state.unreadCount + 1,
    }));
  },

  markAsRead: async (id) => {
    try {
      await apiMarkAsRead(Number(id));
    } catch {
      // API 실패해도 UI는 업데이트
    }
    set((state) => ({
      notifications: state.notifications.map((n) =>
        n.id === id ? { ...n, unread: false } : n
      ),
      unreadCount: Math.max(0, state.unreadCount - 1),
    }));
  },

  markAllAsRead: async () => {
    try {
      await apiMarkAllAsRead();
    } catch {
      // API 실패해도 UI는 업데이트
    }
    set((state) => ({
      notifications: state.notifications.map((n) => ({ ...n, unread: false })),
      unreadCount: 0,
    }));
  },

  removeNotification: (id) => {
    set((state) => {
      const notification = state.notifications.find((n) => n.id === id);
      return {
        notifications: state.notifications.filter((n) => n.id !== id),
        unreadCount: notification?.unread
          ? Math.max(0, state.unreadCount - 1)
          : state.unreadCount,
      };
    });
  },

  setNotifications: (notifications) => {
    const unreadCount = notifications.filter((n) => n.unread).length;
    set({
      notifications,
      unreadCount,
      lastFetchTime: Date.now(),
    });
  },

  fetchNotifications: async () => {
    try {
      const [pageResult, count] = await Promise.allSettled([
        getNotifications(0, 10),
        getUnreadCount(),
      ]);

      if (pageResult.status === 'fulfilled' && pageResult.value?.content) {
        const mapped: Notification[] = pageResult.value.content.map((n: any) => ({
          id: n.id,
          title: n.title ?? n.message ?? '알림',
          message: n.message,
          time: formatTime(n.createdAt),
          unread: !n.read,
          type: n.type?.toLowerCase() ?? 'info',
        }));
        set({ notifications: mapped, lastFetchTime: Date.now() });
      }

      if (count.status === 'fulfilled') {
        set({ unreadCount: count.value ?? 0 });
      }
    } catch {
      // 실패 시 기존 상태 유지
    }
  },

  startPolling: () => {
    const state = get();
    if (state.isPolling) return;

    set({ isPolling: true });

    // Initial fetch from API
    get().fetchNotifications();

    // Poll every 30 seconds
    pollingInterval = setInterval(() => {
      get().fetchNotifications();
    }, 30000);
  },

  stopPolling: () => {
    if (pollingInterval) {
      clearInterval(pollingInterval);
      pollingInterval = null;
    }
    set({ isPolling: false });
  },
}));
