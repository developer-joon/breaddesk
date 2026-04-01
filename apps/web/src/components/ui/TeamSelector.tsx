'use client';

import React, { useState, useRef, useEffect } from 'react';
import type { TeamResponse } from '@/types';
import { useTeam } from '@/contexts/TeamContext';

export function TeamSelector() {
  const { currentTeam, teams, setCurrentTeam, isLoading } = useTeam();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [isOpen]);

  if (isLoading) {
    return (
      <div className="px-3 py-2 bg-gray-100 rounded-lg text-sm text-gray-500">
        팀 로딩중...
      </div>
    );
  }

  if (teams.length === 0) {
    return null;
  }

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 px-3 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
      >
        <span className="text-xl">👥</span>
        <span className="text-sm font-medium text-gray-700 hidden md:inline">
          {currentTeam?.name || '팀 선택'}
        </span>
        <span className={`text-gray-400 transition-transform ${isOpen ? 'rotate-180' : ''}`}>
          ▼
        </span>
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-64 bg-white border border-gray-200 rounded-lg shadow-lg z-50">
          <div className="p-2">
            <div className="text-xs font-semibold text-gray-500 uppercase px-3 py-2">
              팀 선택
            </div>
            <div className="max-h-60 overflow-y-auto">
              {teams.map((team) => (
                <button
                  key={team.id}
                  onClick={() => {
                    setCurrentTeam(team);
                    setIsOpen(false);
                  }}
                  className={`w-full text-left px-3 py-2 rounded-md transition-colors ${
                    currentTeam?.id === team.id
                      ? 'bg-blue-50 text-blue-700 font-medium'
                      : 'text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <div className="text-sm">{team.name}</div>
                      {team.description && (
                        <div className="text-xs text-gray-500 mt-0.5 line-clamp-1">
                          {team.description}
                        </div>
                      )}
                    </div>
                    {currentTeam?.id === team.id && (
                      <span className="text-blue-600">✓</span>
                    )}
                  </div>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
