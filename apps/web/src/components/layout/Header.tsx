'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/auth';
import { useThemeStore } from '@/stores/theme';
import { useNotificationStore } from '@/stores/notification';
import { Avatar } from '@/components/ui/Avatar';
import { NotificationDropdown } from '@/components/ui/NotificationDropdown';
import { search } from '@/services/search';
import type { SearchResult } from '@/types';

interface HeaderProps {
  onMenuClick: () => void;
}

export function Header({ onMenuClick }: HeaderProps) {
  const router = useRouter();
  const { user, logout } = useAuthStore();
  const { theme, setTheme, resolvedTheme } = useThemeStore();
  const { unreadCount, startPolling, stopPolling } = useNotificationStore();
  const [showNotifications, setShowNotifications] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchResults, setSearchResults] = useState<SearchResult | null>(null);
  const [showSearchResults, setShowSearchResults] = useState(false);
  const [isSearching, setIsSearching] = useState(false);

  useEffect(() => {
    // Start polling when component mounts
    startPolling();
    return () => {
      // Stop polling when component unmounts
      stopPolling();
    };
  }, [startPolling, stopPolling]);

  // Debounced search
  useEffect(() => {
    if (!searchKeyword.trim()) {
      setSearchResults(null);
      setShowSearchResults(false);
      return;
    }

    const timer = setTimeout(async () => {
      try {
        setIsSearching(true);
        const results = await search(searchKeyword);
        setSearchResults(results);
        setShowSearchResults(true);
      } catch (error) {
        console.error('Search failed:', error);
      } finally {
        setIsSearching(false);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [searchKeyword]);

  // Close search results on outside click
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest('.search-container')) {
        setShowSearchResults(false);
      }
    };

    if (showSearchResults) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [showSearchResults]);

  const handleSearchResultClick = (type: 'inquiry' | 'task' | 'knowledge', id: number) => {
    setShowSearchResults(false);
    setSearchKeyword('');
    if (type === 'inquiry') {
      router.push(`/inquiries?id=${id}`);
    } else if (type === 'task') {
      router.push(`/tasks?id=${id}`);
    } else if (type === 'knowledge') {
      router.push(`/knowledge?id=${id}`);
    }
  };

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
        <div className="relative search-container">
          <input
            type="text"
            placeholder="검색..."
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            onFocus={() => searchResults && setShowSearchResults(true)}
            className="w-full px-4 py-2 pl-10 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
          />
          <span className="absolute left-3 top-2.5 text-gray-400 dark:text-gray-500">
            {isSearching ? '⏳' : '🔍'}
          </span>

          {/* Search Results Dropdown */}
          {showSearchResults && searchResults && (
            <div className="absolute top-full mt-2 w-full bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg max-h-96 overflow-y-auto z-50">
              {searchResults.inquiries.length > 0 && (
                <div className="p-2">
                  <div className="text-xs font-semibold text-gray-500 dark:text-gray-400 px-2 py-1">
                    문의
                  </div>
                  {searchResults.inquiries.map((inquiry) => (
                    <button
                      key={inquiry.id}
                      onClick={() => handleSearchResultClick('inquiry', inquiry.id)}
                      className="w-full text-left px-3 py-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded"
                    >
                      <div className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {inquiry.senderName}
                      </div>
                      <div className="text-xs text-gray-600 dark:text-gray-400 truncate">
                        {inquiry.message}
                      </div>
                    </button>
                  ))}
                </div>
              )}

              {searchResults.tasks.length > 0 && (
                <div className="p-2 border-t border-gray-200 dark:border-gray-700">
                  <div className="text-xs font-semibold text-gray-500 dark:text-gray-400 px-2 py-1">
                    업무
                  </div>
                  {searchResults.tasks.map((task) => (
                    <button
                      key={task.id}
                      onClick={() => handleSearchResultClick('task', task.id)}
                      className="w-full text-left px-3 py-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded"
                    >
                      <div className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {task.title}
                      </div>
                      <div className="text-xs text-gray-600 dark:text-gray-400 truncate">
                        {task.description}
                      </div>
                    </button>
                  ))}
                </div>
              )}

              {searchResults.knowledge.length > 0 && (
                <div className="p-2 border-t border-gray-200 dark:border-gray-700">
                  <div className="text-xs font-semibold text-gray-500 dark:text-gray-400 px-2 py-1">
                    지식
                  </div>
                  {searchResults.knowledge.map((doc) => (
                    <button
                      key={doc.id}
                      onClick={() => handleSearchResultClick('knowledge', doc.id)}
                      className="w-full text-left px-3 py-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded"
                    >
                      <div className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {doc.title}
                      </div>
                      {doc.sourceUrl && (
                        <div className="text-xs text-gray-600 dark:text-gray-400 truncate">
                          {doc.sourceUrl}
                        </div>
                      )}
                    </button>
                  ))}
                </div>
              )}

              {searchResults.inquiries.length === 0 &&
                searchResults.tasks.length === 0 &&
                searchResults.knowledge.length === 0 && (
                  <div className="p-4 text-center text-sm text-gray-500 dark:text-gray-400">
                    검색 결과가 없습니다
                  </div>
                )}
            </div>
          )}
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
