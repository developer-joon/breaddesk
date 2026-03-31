import { create } from 'zustand';

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
  startPolling: () => void;
  stopPolling: () => void;
}

let pollingInterval: NodeJS.Timeout | null = null;

// Mock notifications for now - 나중에 실제 API로 교체
const mockNotifications: Notification[] = [
  {
    id: 1,
    title: '새 문의가 도착했습니다',
    message: '고객님이 제품 문의를 남겼습니다.',
    time: '5분 전',
    unread: true,
    type: 'info',
  },
  {
    id: 2,
    title: 'SLA 위반 경고',
    message: '문의 #1234의 응답 시간이 임박했습니다.',
    time: '1시간 전',
    unread: true,
    type: 'warning',
  },
  {
    id: 3,
    title: '업무가 완료되었습니다',
    message: '김담당자가 업무 #5678을 완료했습니다.',
    time: '2시간 전',
    unread: false,
    type: 'success',
  },
];

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

  markAsRead: (id) => {
    set((state) => ({
      notifications: state.notifications.map((n) =>
        n.id === id ? { ...n, unread: false } : n
      ),
      unreadCount: Math.max(0, state.unreadCount - 1),
    }));
  },

  markAllAsRead: () => {
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

  startPolling: () => {
    const state = get();
    if (state.isPolling) return;

    set({ isPolling: true });

    // Initial fetch
    get().setNotifications(mockNotifications);

    // Poll every 30 seconds
    pollingInterval = setInterval(() => {
      // 실제 환경에서는 여기서 API 호출
      // const response = await fetch('/api/notifications');
      // const data = await response.json();
      // get().setNotifications(data);
      
      // For now, just simulate with mock data
      console.log('[NotificationStore] Polling notifications...');
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
