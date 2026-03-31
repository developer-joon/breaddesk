'use client';

import React, { useState, useEffect } from 'react';
import { useAuthStore } from '@/stores/auth';
import { useThemeStore } from '@/stores/theme';
import { useNotificationStore } from '@/stores/notification';
import { Avatar } from '@/components/ui/Avatar';
import { NotificationDropdown } from '@/components/ui/NotificationDropdown';

interface HeaderProps {
  onMenuClick: () => void;
}

export function Header({ onMenuClick }: HeaderProps) {
  const { user, logout } = useAuthStore();
  const { theme, setTheme, resolvedTheme } = useThemeStore();
  const { unreadCount, startPolling, stopPolling } = useNotificationStore();
  const [showNotifications, setShowNotifications] = useState(false);

  useEffect(() => {
    // Start polling when component mounts
    startPolling();
    return () => {
      // Stop polling when component unmounts
      stopPolling();
    };
  }, [startPolling, stopPolling]);

  const cycleTheme = () => {
    const nextTheme = theme === 'light' ? 'dark' : theme === 'dark' ? 'system' : 'light';
    setTheme(nextTheme);
  };

  const getThemeIcon = () => {
    if (theme === 'system') return '💻';
    return resolvedTheme === 'dark' ? '🌙' : '☀️';
  };

  return (
    <header className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 px-4 py-3 flex items-center justify-between safe-top transition-colors">
      {/* Mobile Menu Button */}
      <button
        onClick={onMenuClick}
        className="lg:hidden text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white text-2xl"
      >
        ☰
      </button>

      {/* Search Bar */}
      <div className="flex-1 max-w-xl mx-4 hidden md:block">
        <div className="relative">
          <input
            type="text"
            placeholder="검색..."
            className="w-full px-4 py-2 pl-10 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
          />
          <span className="absolute left-3 top-2.5 text-gray-400 dark:text-gray-500">🔍</span>
        </div>
      </div>

      {/* Right Section */}
      <div className="flex items-center gap-4">
        {/* Theme Toggle */}
        <button
          onClick={cycleTheme}
          className="text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white text-xl transition-colors"
          title={`현재: ${theme === 'system' ? '시스템 설정' : theme === 'dark' ? '다크 모드' : '라이트 모드'}`}
        >
          {getThemeIcon()}
        </button>

        {/* Notifications */}
        <div className="relative">
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            className="relative text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white text-xl transition-colors"
          >
            🔔
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
                {unreadCount}
              </span>
            )}
          </button>
          {showNotifications && (
            <NotificationDropdown onClose={() => setShowNotifications(false)} />
          )}
        </div>

        {/* User Menu */}
        <div className="flex items-center gap-2">
          {user && <Avatar name={user.name} src={user.avatar} size="sm" />}
          <button
            onClick={logout}
            className="hidden md:block text-sm text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white transition-colors"
          >
            로그아웃
          </button>
        </div>
      </div>
    </header>
  );
}
