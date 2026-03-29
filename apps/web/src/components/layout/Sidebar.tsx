'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import React from 'react';
import { useAuthStore } from '@/stores/auth';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

const navItems = [
  { href: '/dashboard', icon: '📊', label: '팀 현황' },
  { href: '/inquiries', icon: '💬', label: '문의' },
  { href: '/tasks', icon: '✅', label: '업무' },
  { href: '/templates', icon: '📝', label: '템플릿' },
  { href: '/knowledge', icon: '📚', label: '지식' },
  { href: '/my', icon: '👤', label: '내 업무' },
  { href: '/settings', icon: '⚙️', label: '설정' },
];

export function Sidebar({ isOpen, onClose }: SidebarProps) {
  const pathname = usePathname();
  const { user } = useAuthStore();

  return (
    <>
      {/* Overlay (mobile) */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`w-60 bg-[#1E293B] text-white flex-shrink-0 transition-transform duration-300 fixed lg:static inset-y-0 left-0 z-50 overflow-y-auto safe-top ${
          isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        <div className="p-4">
          <Link href="/dashboard" className="flex items-center gap-2 mb-8">
            <span className="text-3xl">🍞</span>
            <h1 className="text-xl font-bold">BreadDesk</h1>
          </Link>

          <nav className="space-y-1">
            {navItems.map((item) => {
              const isActive = pathname === item.href;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={() => onClose()}
                  className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                    isActive
                      ? 'bg-[#334155] text-white'
                      : 'hover:bg-[#334155] text-gray-300'
                  }`}
                >
                  <span>{item.icon}</span>
                  <span>{item.label}</span>
                </Link>
              );
            })}
          </nav>
        </div>

        {/* User Info */}
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-700 safe-bottom">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-sm font-medium">
              {user?.name?.charAt(0)?.toUpperCase() || 'U'}
            </div>
            <div className="flex-1">
              <div className="text-sm font-medium">{user?.name || '사용자'}</div>
              <div className="text-xs text-gray-400">{user?.email || 'user@example.com'}</div>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
}
