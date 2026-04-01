'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import type { TeamResponse } from '@/types';
import { getTeams } from '@/services/teams';

interface TeamContextValue {
  currentTeam: TeamResponse | null;
  teams: TeamResponse[];
  setCurrentTeam: (team: TeamResponse | null) => void;
  isLoading: boolean;
  refreshTeams: () => Promise<void>;
}

const TeamContext = createContext<TeamContextValue | undefined>(undefined);

interface TeamProviderProps {
  children: ReactNode;
}

export function TeamProvider({ children }: TeamProviderProps) {
  const [currentTeam, setCurrentTeamState] = useState<TeamResponse | null>(null);
  const [teams, setTeams] = useState<TeamResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const loadTeams = async () => {
    try {
      const fetchedTeams = await getTeams();
      setTeams(fetchedTeams);

      // Restore saved team from localStorage
      if (typeof window !== 'undefined') {
        const savedTeamId = localStorage.getItem('currentTeamId');
        if (savedTeamId) {
          const savedTeam = fetchedTeams.find((t) => t.id === parseInt(savedTeamId));
          if (savedTeam) {
            setCurrentTeamState(savedTeam);
            return;
          }
        }
      }

      // If no saved team, default to first team
      if (fetchedTeams.length > 0) {
        setCurrentTeamState(fetchedTeams[0]);
      }
    } catch (error) {
      console.error('Failed to load teams:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadTeams();
  }, []);

  const setCurrentTeam = (team: TeamResponse | null) => {
    setCurrentTeamState(team);
    if (typeof window !== 'undefined') {
      if (team) {
        localStorage.setItem('currentTeamId', team.id.toString());
      } else {
        localStorage.removeItem('currentTeamId');
      }
    }
  };

  const refreshTeams = async () => {
    await loadTeams();
  };

  return (
    <TeamContext.Provider
      value={{ currentTeam, teams, setCurrentTeam, isLoading, refreshTeams }}
    >
      {children}
    </TeamContext.Provider>
  );
}

export function useTeam() {
  const context = useContext(TeamContext);
  if (context === undefined) {
    throw new Error('useTeam must be used within a TeamProvider');
  }
  return context;
}
