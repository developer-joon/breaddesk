'use client';

import React, { useState, useEffect } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { Avatar } from '@/components/ui/Avatar';
import { SLARule, User } from '@/types';
import toast from 'react-hot-toast';

export default function SettingsPage() {
  const [activeTab, setActiveTab] = useState<'sla' | 'team'>('sla');
  const [slaRules, setSlaRules] = useState<SLARule[]>([]);
  const [teamMembers, setTeamMembers] = useState<User[]>([]);

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    // Mock data
    setSlaRules([
      {
        id: '1',
        name: '긴급 문의',
        priority: 'URGENT',
        responseTimeHours: 1,
        resolutionTimeHours: 4,
        enabled: true,
      },
      {
        id: '2',
        name: '높은 우선순위',
        priority: 'HIGH',
        responseTimeHours: 4,
        resolutionTimeHours: 24,
        enabled: true,
      },
      {
        id: '3',
        name: '일반 문의',
        priority: 'MEDIUM',
        responseTimeHours: 8,
        resolutionTimeHours: 48,
        enabled: true,
      },
    ]);

    setTeamMembers([
      {
        id: 'user1',
        email: 'kim@breaddesk.com',
        name: '김개발',
        role: 'ADMIN',
        avatar: undefined,
      },
      {
        id: 'user2',
        email: 'lee@breaddesk.com',
        name: '이디자인',
        role: 'AGENT',
        avatar: undefined,
      },
      {
        id: 'user3',
        email: 'park@breaddesk.com',
        name: '박상담',
        role: 'AGENT',
        avatar: undefined,
      },
    ]);
  };

  const toggleSlaRule = (id: string) => {
    setSlaRules(
      slaRules.map((rule) => (rule.id === id ? { ...rule, enabled: !rule.enabled } : rule))
    );
    toast.success('SLA 규칙이 업데이트되었습니다');
  };

  const getRoleBadgeVariant = (role: string) => {
    switch (role) {
      case 'ADMIN':
        return 'danger';
      case 'AGENT':
        return 'info';
      case 'VIEWER':
        return 'default';
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
      case 'VIEWER':
        return '뷰어';
      default:
        return role;
    }
  };

  return (
    <AppLayout>
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-2xl font-bold text-gray-900">설정</h1>
          <p className="text-gray-600 mt-1">시스템 설정을 관리합니다</p>
        </div>

        {/* Tabs */}
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
            {/* SLA Rules Tab */}
            {activeTab === 'sla' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h2 className="text-lg font-semibold">SLA 규칙</h2>
                    <p className="text-sm text-gray-600 mt-1">
                      우선순위별 응답 및 해결 시간을 설정합니다
                    </p>
                  </div>
                  <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
                    + 규칙 추가
                  </button>
                </div>

                <div className="space-y-3">
                  {slaRules.map((rule) => (
                    <div
                      key={rule.id}
                      className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-3 mb-2">
                            <h3 className="font-semibold text-gray-900">{rule.name}</h3>
                            <Badge>{rule.priority}</Badge>
                            <Badge variant={rule.enabled ? 'success' : 'default'}>
                              {rule.enabled ? '활성' : '비활성'}
                            </Badge>
                          </div>
                          <div className="flex items-center gap-6 text-sm text-gray-600">
                            <div>
                              <span className="font-medium">응답 시간:</span> {rule.responseTimeHours}
                              시간
                            </div>
                            <div>
                              <span className="font-medium">해결 시간:</span>{' '}
                              {rule.resolutionTimeHours}시간
                            </div>
                          </div>
                        </div>
                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => toggleSlaRule(rule.id)}
                            className={`px-4 py-2 rounded-lg text-sm transition-colors ${
                              rule.enabled
                                ? 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                                : 'bg-blue-600 text-white hover:bg-blue-700'
                            }`}
                          >
                            {rule.enabled ? '비활성화' : '활성화'}
                          </button>
                          <button className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm">
                            수정
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>

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

            {/* Team Members Tab */}
            {activeTab === 'team' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h2 className="text-lg font-semibold">팀원 관리</h2>
                    <p className="text-sm text-gray-600 mt-1">팀원을 초대하고 권한을 관리합니다</p>
                  </div>
                  <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
                    + 팀원 초대
                  </button>
                </div>

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
                        <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                          작업
                        </th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200">
                      {teamMembers.map((member) => (
                        <tr key={member.id} className="hover:bg-gray-50">
                          <td className="px-6 py-4">
                            <div className="flex items-center gap-3">
                              <Avatar name={member.name} src={member.avatar} size="sm" />
                              <span className="font-medium text-gray-900">{member.name}</span>
                            </div>
                          </td>
                          <td className="px-6 py-4 text-sm text-gray-600">{member.email}</td>
                          <td className="px-6 py-4">
                            <Badge variant={getRoleBadgeVariant(member.role)}>
                              {getRoleLabel(member.role)}
                            </Badge>
                          </td>
                          <td className="px-6 py-4 text-right">
                            <button className="text-blue-600 hover:text-blue-700 text-sm mr-3">
                              수정
                            </button>
                            <button className="text-red-600 hover:text-red-700 text-sm">
                              삭제
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mt-6">
                  <div className="flex items-start gap-3">
                    <span className="text-xl">⚠️</span>
                    <div className="text-sm text-yellow-800">
                      <p className="font-semibold mb-1">Phase 1 안내</p>
                      <p>
                        현재 버전에서는 팀원 관리 UI만 제공됩니다. 실제 초대 및 권한 관리 기능은
                        추후 업데이트됩니다.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
