'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { Avatar } from '@/components/ui/Avatar';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { ErrorMessage } from '@/components/ui/ErrorMessage';
import { Modal } from '@/components/ui/Modal';
import { useFeaturesStore } from '@/stores/features';
import { getMembers, createMember, updateMember, deleteMember } from '@/services/members';
import {
  getChannels,
  updateChannel,
  testChannel,
  type ChannelConfigResponse,
} from '@/services/channels';
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

const CHANNEL_ICONS: Record<string, string> = {
  slack: '💬',
  teams: '👥',
  email: '📧',
};

const CHANNEL_LABELS: Record<string, string> = {
  slack: 'Slack',
  teams: 'Microsoft Teams',
  email: 'Email',
};

// Features List Component
function FeaturesList() {
  const { features, isLoading } = useFeaturesStore();

  if (isLoading) {
    return <LoadingSpinner text="기능 목록을 불러오는 중..." />;
  }

  if (!features) {
    return <p className="text-gray-500">기능 목록을 불러올 수 없습니다.</p>;
  }

  const featureLabels: Record<keyof typeof features, string> = {
    kanbanTasks: '칸반/업무 관리',
    internalNotes: '내부 메모',
    aiAssignment: 'AI 담당자 추천',
    jiraIntegration: 'Jira 연동',
  };

  return (
    <div className="space-y-3">
      {Object.entries(features).map(([key, enabled]) => (
        <div
          key={key}
          className="flex items-center justify-between p-4 border border-gray-200 rounded-lg"
        >
          <div>
            <h3 className="font-medium text-gray-900">
              {featureLabels[key as keyof typeof features] || key}
            </h3>
            <p className="text-sm text-gray-500 mt-1">
              {key === 'kanbanTasks' && '칸반 보드 기반 업무 관리 기능'}
              {key === 'internalNotes' && '문의에 대한 내부 메모 작성 기능'}
              {key === 'aiAssignment' && 'AI 기반 담당자 자동 추천 (칸반 기능 의존)'}
              {key === 'jiraIntegration' && 'Jira 이슈 생성 및 동기화 연동'}
            </p>
          </div>
          <Badge variant={enabled ? 'success' : 'default'}>
            {enabled ? 'ON' : 'OFF'}
          </Badge>
        </div>
      ))}

      <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mt-4">
        <p className="text-sm text-yellow-800">
          <strong>⚠️ 알림:</strong> 기능 플래그는 현재 서버 설정 파일(application.yml)에서만 변경 가능합니다.
          런타임 변경 기능은 향후 추가될 예정입니다.
        </p>
      </div>
    </div>
  );
}

// Jira Integration Card Component
function JiraIntegrationCard() {
  const [jiraUrl, setJiraUrl] = useState('');
  const [jiraEmail, setJiraEmail] = useState('');
  const [jiraToken, setJiraToken] = useState('');
  const [projectKey, setProjectKey] = useState('');
  const [isTestingConnection, setIsTestingConnection] = useState(false);

  const handleTestConnection = async () => {
    if (!jiraUrl || !jiraEmail || !jiraToken || !projectKey) {
      toast.error('모든 필드를 입력해주세요.');
      return;
    }

    setIsTestingConnection(true);
    try {
      // TODO: API 구현 후 실제 호출로 교체
      await new Promise((resolve) => setTimeout(resolve, 1500));
      toast.success('연결 테스트 성공 (준비 중)');
    } catch (error) {
      toast.error('연결 테스트에 실패했습니다.');
    } finally {
      setIsTestingConnection(false);
    }
  };

  const handleSave = async () => {
    if (!jiraUrl || !jiraEmail || !jiraToken || !projectKey) {
      toast.error('모든 필드를 입력해주세요.');
      return;
    }

    try {
      // TODO: API 구현 후 실제 호출로 교체
      toast('Jira 연동 기능은 아직 구현 중입니다.', { icon: 'ℹ️' });
    } catch (error) {
      toast.error('저장에 실패했습니다.');
    }
  };

  return (
    <div className="border border-gray-200 rounded-lg p-6 space-y-4">
      <div className="flex items-center gap-3 mb-4">
        <span className="text-3xl">🔗</span>
        <div>
          <h3 className="text-lg font-semibold">Jira 연동</h3>
          <p className="text-sm text-gray-600">문의를 Jira 이슈로 에스컬레이션하고 상태를 동기화합니다.</p>
        </div>
      </div>

      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Jira Cloud URL <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            value={jiraUrl}
            onChange={(e) => setJiraUrl(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="https://your-domain.atlassian.net"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Service Account Email <span className="text-red-500">*</span>
          </label>
          <input
            type="email"
            value={jiraEmail}
            onChange={(e) => setJiraEmail(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="service-account@example.com"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            API Token <span className="text-red-500">*</span>
          </label>
          <input
            type="password"
            value={jiraToken}
            onChange={(e) => setJiraToken(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="••••••••••••••••"
          />
          <p className="text-xs text-gray-500 mt-1">
            Jira API Token은{' '}
            <a
              href="https://id.atlassian.com/manage-profile/security/api-tokens"
              target="_blank"
              rel="noopener noreferrer"
              className="text-blue-600 hover:underline"
            >
              여기
            </a>
            에서 생성할 수 있습니다.
          </p>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            프로젝트 키 <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            value={projectKey}
            onChange={(e) => setProjectKey(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="SUPPORT"
          />
          <p className="text-xs text-gray-500 mt-1">
            Jira 프로젝트 키 (예: SUPPORT, DEV 등)
          </p>
        </div>

        <div className="flex gap-2 pt-2">
          <button
            onClick={handleTestConnection}
            disabled={isTestingConnection}
            className="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isTestingConnection ? '테스트 중...' : '🔍 연결 테스트'}
          </button>
          <button
            onClick={handleSave}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            💾 저장
          </button>
        </div>
      </div>

      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mt-4">
        <p className="text-sm text-blue-800">
          <strong>ℹ️ 알림:</strong> Jira 연동 기능은 현재 스펙 문서가 작성되었으며, 실제 구현은 향후 진행될 예정입니다.
          자세한 스펙은 <code className="bg-blue-100 px-1 rounded">specs/features/jira-integration.md</code>를 참고하세요.
        </p>
      </div>
    </div>
  );
}

export default function SettingsPage() {
  const [activeTab, setActiveTab] = useState<'sla' | 'team' | 'channels' | 'features' | 'integrations' | 'teams'>('sla');
  const [slaRules, setSlaRules] = useState<SlaRuleResponse[]>([]);
  const [teamMembers, setTeamMembers] = useState<User[]>([]);
  const [channels, setChannels] = useState<ChannelConfigResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Channel edit modal state
  const [editingChannel, setEditingChannel] = useState<ChannelConfigResponse | null>(null);
  const [editWebhookUrl, setEditWebhookUrl] = useState('');
  const [editAuthToken, setEditAuthToken] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [isTesting, setIsTesting] = useState<number | null>(null);

  // Member modal state
  const [showMemberModal, setShowMemberModal] = useState(false);
  const [editingMember, setEditingMember] = useState<User | null>(null);
  const [memberForm, setMemberForm] = useState({
    name: '',
    email: '',
    password: '',
    role: 'AGENT' as 'ADMIN' | 'AGENT',
    skills: '',
    active: true,
  });

  const loadSettings = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [membersResult, slaResult, channelsResult] = await Promise.allSettled([
        getMembers(),
        api.get<ApiResponse<SlaRuleResponse[]>>('/sla/rules'),
        getChannels(),
      ]);

      if (membersResult.status === 'fulfilled') {
        setTeamMembers(membersResult.value);
      }
      if (slaResult.status === 'fulfilled' && slaResult.value.data.success) {
        setSlaRules(slaResult.value.data.data);
      }
      if (channelsResult.status === 'fulfilled') {
        setChannels(channelsResult.value);
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

  const handleToggleChannel = async (channel: ChannelConfigResponse) => {
    try {
      await updateChannel(channel.id, {
        channelType: channel.channelType,
        isActive: !channel.isActive,
      });
      setChannels((prev) =>
        prev.map((c) => (c.id === channel.id ? { ...c, isActive: !c.isActive } : c)),
      );
      toast.success(`${CHANNEL_LABELS[channel.channelType] || channel.channelType} ${!channel.isActive ? '활성화' : '비활성화'}됨`);
    } catch {
      toast.error('채널 상태 변경에 실패했습니다.');
    }
  };

  const handleEditChannel = (channel: ChannelConfigResponse) => {
    setEditingChannel(channel);
    setEditWebhookUrl(channel.webhookUrl || '');
    setEditAuthToken('');
  };

  const handleSaveChannel = async () => {
    if (!editingChannel) return;
    setIsSaving(true);
    try {
      const request: Record<string, unknown> = {
        channelType: editingChannel.channelType,
        webhookUrl: editWebhookUrl || null,
      };
      if (editAuthToken) {
        request.authToken = editAuthToken;
      }
      const updated = await updateChannel(editingChannel.id, request as never);
      setChannels((prev) => prev.map((c) => (c.id === editingChannel.id ? updated : c)));
      setEditingChannel(null);
      toast.success('채널 설정이 저장되었습니다.');
    } catch {
      toast.error('채널 설정 저장에 실패했습니다.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleTestWebhook = async (channel: ChannelConfigResponse) => {
    setIsTesting(channel.id);
    try {
      const result = await testChannel(channel.id);
      toast.success(result || '웹훅 테스트 성공');
    } catch {
      toast.error('웹훅 테스트에 실패했습니다.');
    } finally {
      setIsTesting(null);
    }
  };

  const handleAddMember = () => {
    setEditingMember(null);
    setMemberForm({
      name: '',
      email: '',
      password: '',
      role: 'AGENT',
      skills: '',
      active: true,
    });
    setShowMemberModal(true);
  };

  const handleEditMember = (member: User) => {
    setEditingMember(member);
    setMemberForm({
      name: member.name,
      email: member.email,
      password: '',
      role: member.role,
      skills: member.skills || '',
      active: member.active !== false,
    });
    setShowMemberModal(true);
  };

  const handleSaveMember = async () => {
    if (!memberForm.name || !memberForm.email) {
      toast.error('이름과 이메일은 필수입니다');
      return;
    }
    if (!editingMember && !memberForm.password) {
      toast.error('새 팀원은 비밀번호가 필요합니다');
      return;
    }
    if (memberForm.password && memberForm.password.length < 8) {
      toast.error('비밀번호는 최소 8자 이상이어야 합니다');
      return;
    }

    setIsSaving(true);
    try {
      if (editingMember) {
        // Update
        const payload: Record<string, unknown> = {
          name: memberForm.name,
          email: memberForm.email,
          role: memberForm.role,
          skills: memberForm.skills || undefined,
          active: memberForm.active,
        };
        if (memberForm.password) {
          payload.password = memberForm.password;
        }
        await updateMember(Number(editingMember.id), payload as never);
        toast.success('팀원 정보가 수정되었습니다');
      } else {
        // Create
        await createMember({
          name: memberForm.name,
          email: memberForm.email,
          password: memberForm.password,
          role: memberForm.role,
          skills: memberForm.skills || undefined,
          active: memberForm.active,
        });
        toast.success('팀원이 추가되었습니다');
      }
      setShowMemberModal(false);
      loadSettings();
    } catch {
      toast.error('팀원 저장에 실패했습니다');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteMember = async (member: User) => {
    if (!confirm(`정말로 ${member.name}을(를) 삭제하시겠습니까?`)) {
      return;
    }
    try {
      await deleteMember(Number(member.id));
      toast.success('팀원이 삭제되었습니다');
      loadSettings();
    } catch {
      toast.error('팀원 삭제에 실패했습니다');
    }
  };

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
              <button
                onClick={() => setActiveTab('channels')}
                className={`px-6 py-3 font-medium transition-colors ${
                  activeTab === 'channels'
                    ? 'border-b-2 border-blue-600 text-blue-600'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                채널 연동
              </button>
              <button
                onClick={() => setActiveTab('features')}
                className={`px-6 py-3 font-medium transition-colors ${
                  activeTab === 'features'
                    ? 'border-b-2 border-blue-600 text-blue-600'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                기능 관리
              </button>
              <button
                onClick={() => setActiveTab('integrations')}
                className={`px-6 py-3 font-medium transition-colors ${
                  activeTab === 'integrations'
                    ? 'border-b-2 border-blue-600 text-blue-600'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                🔗 연동
              </button>
              <button
                onClick={() => setActiveTab('teams')}
                className={`px-6 py-3 font-medium transition-colors ${
                  activeTab === 'teams'
                    ? 'border-b-2 border-blue-600 text-blue-600'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                🏢 팀 관리
              </button>
            </div>
          </div>

          <div className="p-6">
            {isLoading && <LoadingSpinner text="설정을 불러오는 중..." />}
            {error && <ErrorMessage message={error} onRetry={loadSettings} />}

            {/* SLA Tab */}
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
                      <p className="mb-2">
                        Service Level Agreement의 약자로, 서비스 제공자와 고객 간의 서비스 수준에
                        대한 약속입니다. 응답 시간과 해결 시간을 설정하여 팀의 목표를 관리하세요.
                      </p>
                      <p className="font-semibold mb-1">SLA 동작 방식:</p>
                      <ul className="list-disc list-inside space-y-1">
                        <li>업무 생성 시 긴급도에 따라 SLA 마감시간이 자동 설정됩니다</li>
                        <li>마감시간을 넘기면 <code className="bg-blue-100 px-1 rounded">sla_response_breached</code> 또는 <code className="bg-blue-100 px-1 rounded">sla_resolve_breached</code> 플래그가 설정됩니다</li>
                        <li>통계 페이지에서 SLA 준수율을 확인할 수 있습니다</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Team Tab */}
            {!isLoading && !error && activeTab === 'team' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h2 className="text-lg font-semibold">팀원 관리</h2>
                    <p className="text-sm text-gray-600 mt-1">팀원을 관리하고 권한을 설정합니다</p>
                  </div>
                  <button
                    onClick={handleAddMember}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                  >
                    + 팀원 추가
                  </button>
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
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                            작업
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
                            <td className="px-6 py-4">
                              <div className="flex items-center gap-2">
                                <button
                                  onClick={() => handleEditMember(member)}
                                  className="px-3 py-1 text-sm text-blue-600 border border-blue-300 rounded hover:bg-blue-50"
                                >
                                  수정
                                </button>
                                <button
                                  onClick={() => handleDeleteMember(member)}
                                  className="px-3 py-1 text-sm text-red-600 border border-red-300 rounded hover:bg-red-50"
                                >
                                  삭제
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            )}

            {/* Channels Tab */}
            {!isLoading && !error && activeTab === 'channels' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h2 className="text-lg font-semibold">채널 연동</h2>
                    <p className="text-sm text-gray-600 mt-1">
                      외부 채널(Slack, Teams, Email)과의 웹훅 연동을 설정합니다
                    </p>
                  </div>
                </div>

                {channels.length === 0 ? (
                  <div className="bg-gray-50 rounded-lg p-8 text-center">
                    <p className="text-gray-500">등록된 채널이 없습니다.</p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {channels.map((channel) => (
                      <div
                        key={channel.id}
                        className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50"
                      >
                        <div className="flex items-center justify-between">
                          <div className="flex-1">
                            <div className="flex items-center gap-3 mb-2">
                              <span className="text-2xl">
                                {CHANNEL_ICONS[channel.channelType] || '🔗'}
                              </span>
                              <span className="text-lg font-medium">
                                {CHANNEL_LABELS[channel.channelType] || channel.channelType}
                              </span>
                              <Badge variant={channel.isActive ? 'success' : 'default'}>
                                {channel.isActive ? '활성' : '비활성'}
                              </Badge>
                              {channel.hasAuthToken && (
                                <Badge variant="info">🔑 토큰 설정됨</Badge>
                              )}
                            </div>
                            <div className="text-sm text-gray-600 ml-9">
                              {channel.webhookUrl ? (
                                <span>
                                  웹훅 URL: <code className="bg-gray-100 px-1 rounded text-xs">{channel.webhookUrl}</code>
                                </span>
                              ) : (
                                <span className="text-yellow-600">웹훅 URL 미설정</span>
                              )}
                            </div>
                          </div>

                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => handleTestWebhook(channel)}
                              disabled={!channel.webhookUrl || isTesting === channel.id}
                              className="px-3 py-1.5 text-sm border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                              {isTesting === channel.id ? '테스트 중...' : '🔔 테스트'}
                            </button>
                            <button
                              onClick={() => handleEditChannel(channel)}
                              className="px-3 py-1.5 text-sm border border-blue-300 text-blue-600 rounded-md hover:bg-blue-50"
                            >
                              ✏️ 설정
                            </button>
                            <button
                              onClick={() => handleToggleChannel(channel)}
                              className={`px-3 py-1.5 text-sm rounded-md ${
                                channel.isActive
                                  ? 'bg-red-50 text-red-600 border border-red-300 hover:bg-red-100'
                                  : 'bg-green-50 text-green-600 border border-green-300 hover:bg-green-100'
                              }`}
                            >
                              {channel.isActive ? '비활성화' : '활성화'}
                            </button>
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
                      <p className="font-semibold mb-1">채널 연동이란?</p>
                      <p>
                        n8n 등의 자동화 도구를 통해 Slack, Teams, Email로 들어오는 문의를
                        BreadDesk에서 자동으로 수신하고, AI 답변을 원래 채널로 되돌려 보냅니다.
                      </p>
                      <p className="mt-2">
                        <strong>수신:</strong> 외부 → <code className="bg-blue-100 px-1 rounded">POST /api/v1/webhooks/incoming</code> → BreadDesk
                      </p>
                      <p>
                        <strong>발신:</strong> BreadDesk → 설정된 웹훅 URL → n8n → 외부 채널
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Features Tab */}
            {!isLoading && !error && activeTab === 'features' && (
              <div className="space-y-4">
                <div className="mb-4">
                  <h2 className="text-lg font-semibold">기능 관리</h2>
                  <p className="text-sm text-gray-600 mt-1">
                    각 기능의 활성화 여부를 확인합니다. (현재는 조회만 가능하며, 변경은 서버 설정 파일에서 수정해야 합니다.)
                  </p>
                </div>

                <FeaturesList />
              </div>
            )}

            {/* Integrations Tab */}
            {!isLoading && !error && activeTab === 'integrations' && (
              <div className="space-y-4">
                <div className="mb-4">
                  <h2 className="text-lg font-semibold">🔗 외부 연동</h2>
                  <p className="text-sm text-gray-600 mt-1">
                    Jira, Slack, Teams 등 외부 시스템과의 연동을 관리합니다.
                  </p>
                </div>

                <JiraIntegrationCard />
              </div>
            )}

            {/* Teams Tab */}
            {!isLoading && !error && activeTab === 'teams' && (
              <div className="space-y-4">
                <div className="mb-4">
                  <h2 className="text-lg font-semibold">🏢 팀 관리 (멀티 테넌트)</h2>
                  <p className="text-sm text-gray-600 mt-1">
                    여러 팀/테넌트를 관리합니다. (Phase 2 기능 예정)
                  </p>
                </div>

                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-6">
                  <div className="flex items-start gap-3">
                    <span className="text-3xl">🚧</span>
                    <div>
                      <h3 className="font-semibold text-yellow-900 mb-2">Phase 2 기능 안내</h3>
                      <p className="text-sm text-yellow-800 mb-3">
                        멀티 테넌트/팀 기능은 Phase 2에서 구현될 예정입니다. 다음 기능이 포함됩니다:
                      </p>
                      <ul className="list-disc list-inside text-sm text-yellow-800 space-y-1">
                        <li><code className="bg-yellow-100 px-1 rounded">teams</code> 테이블 추가 (team_id, team_name, settings)</li>
                        <li>멤버, 문의, 업무에 <code className="bg-yellow-100 px-1 rounded">team_id</code> 컬럼 추가</li>
                        <li>팀별 데이터 격리 (Row-Level Security)</li>
                        <li>팀 전환 UI (헤더 드롭다운)</li>
                        <li>팀별 통계 및 설정 관리</li>
                      </ul>
                      <p className="text-sm text-yellow-800 mt-3">
                        현재는 단일 팀(테넌트)만 지원하며, 모든 데이터는 기본 팀에 속합니다.
                      </p>
                    </div>
                  </div>
                </div>

                <div className="border border-gray-200 rounded-lg p-6">
                  <h3 className="text-lg font-semibold mb-4">현재 팀 정보</h3>
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-gray-600">팀 이름:</span>
                      <span className="font-medium">기본 팀 (Default)</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">팀원 수:</span>
                      <span className="font-medium">{teamMembers.length}명</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">테넌트 ID:</span>
                      <span className="font-mono text-xs bg-gray-100 px-2 py-1 rounded">default-tenant-001</span>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Member Add/Edit Modal */}
      {showMemberModal && (
        <Modal
          isOpen={showMemberModal}
          title={editingMember ? '팀원 정보 수정' : '팀원 추가'}
          onClose={() => setShowMemberModal(false)}
        >
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                이름 <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={memberForm.name}
                onChange={(e) => setMemberForm({ ...memberForm, name: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="홍길동"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                이메일 <span className="text-red-500">*</span>
              </label>
              <input
                type="email"
                value={memberForm.email}
                onChange={(e) => setMemberForm({ ...memberForm, email: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="user@example.com"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                비밀번호 {!editingMember && <span className="text-red-500">*</span>}
              </label>
              <input
                type="password"
                value={memberForm.password}
                onChange={(e) => setMemberForm({ ...memberForm, password: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder={editingMember ? '(변경하지 않으려면 비워두세요)' : '최소 8자'}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                역할 <span className="text-red-500">*</span>
              </label>
              <select
                value={memberForm.role}
                onChange={(e) =>
                  setMemberForm({ ...memberForm, role: e.target.value as 'ADMIN' | 'AGENT' })
                }
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="AGENT">상담원</option>
                <option value="ADMIN">관리자</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">스킬</label>
              <input
                type="text"
                value={memberForm.skills}
                onChange={(e) => setMemberForm({ ...memberForm, skills: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="결제, 배송, 환불"
              />
            </div>

            <div className="flex items-center">
              <input
                type="checkbox"
                id="member-active"
                checked={memberForm.active}
                onChange={(e) => setMemberForm({ ...memberForm, active: e.target.checked })}
                className="mr-2"
              />
              <label htmlFor="member-active" className="text-sm text-gray-700">
                활성 상태
              </label>
            </div>

            <div className="flex justify-end gap-2 pt-4 border-t border-gray-200">
              <button
                onClick={() => setShowMemberModal(false)}
                className="px-4 py-2 text-sm text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
              >
                취소
              </button>
              <button
                onClick={handleSaveMember}
                disabled={isSaving}
                className="px-4 py-2 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                {isSaving ? '저장 중...' : editingMember ? '수정' : '추가'}
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* Channel Edit Modal */}
      {editingChannel && (
        <Modal
          isOpen={!!editingChannel}
          title={`${CHANNEL_LABELS[editingChannel.channelType] || editingChannel.channelType} 설정`}
          onClose={() => setEditingChannel(null)}
        >
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                웹훅 URL (발신)
              </label>
              <input
                type="url"
                value={editWebhookUrl}
                onChange={(e) => setEditWebhookUrl(e.target.value)}
                placeholder="https://n8n.example.com/webhook/..."
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <p className="text-xs text-gray-500 mt-1">
                AI/담당자 답변을 이 URL로 전송합니다 (n8n 웹훅 URL)
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                인증 토큰 (수신)
              </label>
              <input
                type="password"
                value={editAuthToken}
                onChange={(e) => setEditAuthToken(e.target.value)}
                placeholder={editingChannel.hasAuthToken ? '(기존 토큰 유지)' : '인증 토큰 입력'}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <p className="text-xs text-gray-500 mt-1">
                외부에서 웹훅 수신 시 X-Webhook-Token 헤더로 검증합니다
              </p>
            </div>

            <div className="flex justify-end gap-2 pt-4 border-t border-gray-200">
              <button
                onClick={() => setEditingChannel(null)}
                className="px-4 py-2 text-sm text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
              >
                취소
              </button>
              <button
                onClick={handleSaveChannel}
                disabled={isSaving}
                className="px-4 py-2 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                {isSaving ? '저장 중...' : '저장'}
              </button>
            </div>
          </div>
        </Modal>
      )}
    </AppLayout>
  );
}
