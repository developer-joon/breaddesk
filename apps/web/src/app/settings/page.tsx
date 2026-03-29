'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { Avatar } from '@/components/ui/Avatar';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { ErrorMessage } from '@/components/ui/ErrorMessage';
import { getMembers } from '@/services/members';
import api from '@/lib/api';
import type { User, ApiResponse } from '@/types';
import toast from 'react-hot-toast';

interface SlaRuleResponse {
  id: number;
  urgency: string;
  responseTimeHours: number;
  resolveTimeHours: number;
  enabled: boolean;
}

export default function SettingsPage() {
  const [activeTab, setActiveTab] = useState<'sla' | 'team'>('sla');
  const [slaRules, setSlaRules] = useState<SlaRuleResponse[]>([]);
  const [teamMembers, setTeamMembers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadSettings = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [membersResult, slaResult] = await Promise.allSettled([
        getMembers(),
        api.get<ApiResponse<SlaRuleResponse[]>>('/sla-rules'),
      ]);

      if (membersResult.status === 'fulfilled') {
        setTeamMembers(membersResult.value);
      }
      if (slaResult.status === 'fulfilled' && slaResult.value.data.success) {
        setSlaRules(slaResult.value.data.data);
      }
    } catch (err) {
      console.error('Failed to load settings:', err);
      setError('설정을 불러오지 못했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSettings();
  }, [loadSettings]);

  const getRoleBadgeVariant = (role: string) => {
    switch (role) {
      case 'ADMIN':
        return 'danger';
      case 'AGENT':
        return 'info';
      default:
        return 'default';
    }
  };

  const getRoleLabel = (role: string) => {
    switch (role) {
      case 'ADMIN':
        return '관리자';
      case 'AGENT':
        return '상담원';
      default:
        return role;
    }
  };

  const getUrgencyLabel = (urgency: string) => {
    const labels: Record<string, string> = {
      CRITICAL: '긴급',
      HIGH: '높음',
      NORMAL: '보통',
      LOW: '낮음',
    };
    return labels[urgency] || urgency;
  };

  return (
    <AppLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">설정</h1>
          <p className="text-gray-600 mt-1">시스템 설정을 관리합니다</p>
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          <div className="border-b border-gray-200">
            <div className="flex">
              <button
                onClick={() => setActiveTab('sla')}
                className={`px-6 py-3 font-medium transition-colors ${
                  activeTab === 'sla'
                    ? 'border-b-2 border-blue-600 text-blue-600'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                SLA 규칙
              </button>
              <button
                onClick={() => setActiveTab('team')}
                className={`px-6 py-3 font-medium transition-colors ${
                  activeTab === 'team'
                    ? 'border-b-2 border-blue-600 text-blue-600'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                팀원 관리
              </button>
            </div>
          </div>

          <div className="p-6">
            {isLoading && <LoadingSpinner text="설정을 불러오는 중..." />}
            {error && <ErrorMessage message={error} onRetry={loadSettings} />}

            {!isLoading && !error && activeTab === 'sla' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h2 className="text-lg font-semibold">SLA 규칙</h2>
                    <p className="text-sm text-gray-600 mt-1">
                      긴급도별 응답 및 해결 시간을 설정합니다
                    </p>
                  </div>
                </div>

                {slaRules.length === 0 ? (
                  <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                    <p className="text-sm text-yellow-800">
                      SLA 규칙이 아직 설정되지 않았습니다. 백엔드 API에서 규칙을 추가해주세요.
                    </p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {slaRules.map((rule) => (
                      <div
                        key={rule.id}
                        className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50"
                      >
                        <div className="flex items-center justify-between">
                          <div className="flex-1">
                            <div className="flex items-center gap-3 mb-2">
                              <Badge>{getUrgencyLabel(rule.urgency)}</Badge>
                              <Badge variant={rule.enabled ? 'success' : 'default'}>
                                {rule.enabled ? '활성' : '비활성'}
                              </Badge>
                            </div>
                            <div className="flex items-center gap-6 text-sm text-gray-600">
                              <div>
                                <span className="font-medium">응답 시간:</span>{' '}
                                {rule.responseTimeHours}시간
                              </div>
                              <div>
                                <span className="font-medium">해결 시간:</span>{' '}
                                {rule.resolveTimeHours}시간
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mt-6">
                  <div className="flex items-start gap-3">
                    <span className="text-xl">ℹ️</span>
                    <div className="text-sm text-blue-800">
                      <p className="font-semibold mb-1">SLA란?</p>
                      <p>
                        Service Level Agreement의 약자로, 서비스 제공자와 고객 간의 서비스 수준에
                        대한 약속입니다. 응답 시간과 해결 시간을 설정하여 팀의 목표를 관리하세요.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {!isLoading && !error && activeTab === 'team' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h2 className="text-lg font-semibold">팀원 관리</h2>
                    <p className="text-sm text-gray-600 mt-1">팀원을 관리하고 권한을 설정합니다</p>
                  </div>
                </div>

                {teamMembers.length === 0 ? (
                  <div className="bg-gray-50 rounded-lg p-8 text-center">
                    <p className="text-gray-500">등록된 팀원이 없습니다.</p>
                  </div>
                ) : (
                  <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
                    <table className="w-full">
                      <thead className="bg-gray-50 border-b border-gray-200">
                        <tr>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                            사용자
                          </th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                            이메일
                          </th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                            역할
                          </th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                            상태
                          </th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-200">
                        {teamMembers.map((member) => (
                          <tr key={member.id} className="hover:bg-gray-50">
                            <td className="px-6 py-4">
                              <div className="flex items-center gap-3">
                                <Avatar name={member.name} size="sm" />
                                <span className="font-medium text-gray-900">{member.name}</span>
                              </div>
                            </td>
                            <td className="px-6 py-4 text-sm text-gray-600">{member.email}</td>
                            <td className="px-6 py-4">
                              <Badge variant={getRoleBadgeVariant(member.role)}>
                                {getRoleLabel(member.role)}
                              </Badge>
                            </td>
                            <td className="px-6 py-4">
                              <Badge variant={member.active !== false ? 'success' : 'default'}>
                                {member.active !== false ? '활성' : '비활성'}
                              </Badge>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
