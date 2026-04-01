'use client';

import React, { useState, useEffect } from 'react';
import { useAuthStore } from '@/stores/auth';
import { Avatar } from '@/components/ui/Avatar';
import { TeamSelector } from './TeamSelector';

interface HeaderProps {
  onMenuClick: () => void;
}

// Global team context (can be upgraded to React Context later)
let currentTeamId: number | undefined;
const teamChangeListeners: Set<(teamId: number | undefined) => void> = new Set();

export function useCurrentTeam() {
  const [teamId, setTeamId] = useState(currentTeamId);

  useEffect(() => {
    const listener = (id: number | undefined) => setTeamId(id);
    teamChangeListeners.add(listener);
    return () => {
      teamChangeListeners.delete(listener);
    };
  }, []);

  const setCurrentTeam = (id: number | undefined) => {
    currentTeamId = id;
    if (typeof window !== 'undefined') {
      if (id) {
        localStorage.setItem('currentTeamId', String(id));
      } else {
        localStorage.removeItem('currentTeamId');
      }
    }
    teamChangeListeners.forEach((listener) => listener(id));
  };

  useEffect(() => {
    // Load from localStorage on mount
    if (typeof window !== 'undefined' && !currentTeamId) {
      const stored = localStorage.getItem('currentTeamId');
      if (stored) {
        const id = Number(stored);
        currentTeamId = id;
        setTeamId(id);
      }
    }
  }, []);

  return { teamId, setCurrentTeam };
}

export function Header({ onMenuClick }: HeaderProps) {
  const { user, logout } = useAuthStore();
  const { teamId, setCurrentTeam } = useCurrentTeam();

  return (
    <header className="bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between safe-top">
      {/* Mobile Menu Button */}
      <button
        onClick={onMenuClick}
        className="lg:hidden text-gray-600 hover:text-gray-900 text-2xl"
      >
        ☰
      </button>

      {/* Team Selector */}
      <div className="hidden sm:block ml-4">
        <TeamSelector value={teamId} onChange={setCurrentTeam} />
      </div>

      {/* Search Bar */}
      <div className="flex-1 max-w-xl mx-4 hidden md:block">
        <div className="relative">
          <input
            type="text"
            placeholder="검색..."
            className="w-full px-4 py-2 pl-10 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <span className="absolute left-3 top-2.5 text-gray-400">🔍</span>
        </div>
      </div>

      {/* Right Section */}
      <div className="flex items-center gap-4">
        {/* Notifications */}
        <button className="relative text-gray-600 hover:text-gray-900 text-xl">
          🔔
          <span className="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
            3
          </span>
        </button>

        {/* User Menu */}
        <div className="flex items-center gap-2">
          {user && <Avatar name={user.name} src={user.avatar} size="sm" />}
          <button
            onClick={logout}
            className="hidden md:block text-sm text-gray-600 hover:text-gray-900"
          >
            로그아웃
          </button>
        </div>
      </div>
    </header>
  );
}
