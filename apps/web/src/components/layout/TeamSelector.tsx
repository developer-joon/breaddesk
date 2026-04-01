'use client';

import React, { useState, useEffect } from 'react';
import { getTeams } from '@/services/teams';
import type { TeamResponse } from '@/types';

interface TeamSelectorProps {
  value?: number;
  onChange: (teamId: number | undefined) => void;
  className?: string;
}

export function TeamSelector({ value, onChange, className = '' }: TeamSelectorProps) {
  const [teams, setTeams] = useState<TeamResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadTeams();
  }, []);

  const loadTeams = async () => {
    try {
      const data = await getTeams();
      setTeams(data);
      
      // Auto-select first team if none selected
      if (!value && data.length > 0) {
        onChange(data[0].id);
      }
    } catch (error) {
      console.error('Failed to load teams:', error);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className={`px-3 py-2 text-sm text-gray-500 ${className}`}>
        로딩 중...
      </div>
    );
  }

  if (teams.length === 0) {
    return null;
  }

  return (
    <select
      value={value || ''}
      onChange={(e) => onChange(e.target.value ? Number(e.target.value) : undefined)}
      className={`px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm ${className}`}
    >
      <option value="">전체 팀</option>
      {teams.map((team) => (
        <option key={team.id} value={team.id}>
          {team.name}
        </option>
      ))}
    </select>
  );
}
