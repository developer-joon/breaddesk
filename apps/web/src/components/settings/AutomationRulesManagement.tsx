'use client';

import React, { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { Badge } from '@/components/ui/Badge';
import api from '@/lib/api';
import type { ApiResponse } from '@/types';
import toast from 'react-hot-toast';

interface AutomationRule {
  id: number;
  name: string;
  active: boolean;
  triggerType: 'INQUIRY_CREATED' | 'INQUIRY_STATUS_CHANGED';
  conditionJson: string;
  actionJson: string;
  createdAt: string;
}

interface AutomationRuleForm {
  name: string;
  triggerType: 'INQUIRY_CREATED' | 'INQUIRY_STATUS_CHANGED';
  conditionField: string;
  conditionOperator: string;
  conditionValue: string;
  actionType: string;
  actionValue: string;
}

export function AutomationRulesManagement() {
  const [rules, setRules] = useState<AutomationRule[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [form, setForm] = useState<AutomationRuleForm>({
    name: '',
    triggerType: 'INQUIRY_CREATED',
    conditionField: 'channel',
    conditionOperator: 'equals',
    conditionValue: '',
    actionType: 'SET_STATUS',
    actionValue: 'ESCALATED',
  });

  const fetchRules = async () => {
    setIsLoading(true);
    try {
      const { data } = await api.get<ApiResponse<AutomationRule[]>>('/automation-rules');
      setRules(data.data);
    } catch (err) {
      toast.error('자동화 규칙을 불러오지 못했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const handleToggle = async (id: number, active: boolean) => {
    try {
      await api.patch(`/automation-rules/${id}/toggle?active=${!active}`);
      toast.success(active ? '규칙이 비활성화되었습니다.' : '규칙이 활성화되었습니다.');
      fetchRules();
    } catch (err) {
      toast.error('규칙 상태 변경에 실패했습니다.');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('이 규칙을 삭제하시겠습니까?')) return;
    
    try {
      await api.delete(`/automation-rules/${id}`);
      toast.success('규칙이 삭제되었습니다.');
      fetchRules();
    } catch (err) {
      toast.error('규칙 삭제에 실패했습니다.');
    }
  };

  const handleCreate = async () => {
    if (!form.name.trim() || !form.conditionValue.trim()) {
      toast.error('이름과 조건 값을 입력하세요.');
      return;
    }

    setIsCreating(true);
    try {
      const conditionJson = JSON.stringify({
        field: form.conditionField,
        operator: form.conditionOperator,
        value: form.conditionValue,
      });

      const actionJson = JSON.stringify({
        type: form.actionType,
        value: form.actionValue,
      });

      await api.post('/automation-rules', {
        name: form.name,
        active: true,
        triggerType: form.triggerType,
        conditionJson,
        actionJson,
      });

      toast.success('자동화 규칙이 생성되었습니다.');
      setShowCreateModal(false);
      setForm({
        name: '',
        triggerType: 'INQUIRY_CREATED',
        conditionField: 'channel',
        conditionOperator: 'equals',
        conditionValue: '',
        actionType: 'SET_STATUS',
        actionValue: 'ESCALATED',
      });
      fetchRules();
    } catch (err) {
      toast.error('규칙 생성에 실패했습니다.');
    } finally {
      setIsCreating(false);
    }
  };

  const getTriggerLabel = (type: string) => {
    return type === 'INQUIRY_CREATED' ? '문의 생성 시' : '문의 상태 변경 시';
  };

  const parseCondition = (json: string) => {
    try {
      const obj = JSON.parse(json);
      return `${obj.field} ${obj.operator} "${obj.value}"`;
    } catch {
      return json;
    }
  };

  const parseAction = (json: string) => {
    try {
      const obj = JSON.parse(json);
      if (obj.type === 'SET_STATUS') {
        return `상태 변경 → ${obj.value}`;
      } else if (obj.type === 'ASSIGN') {
        return `담당자 할당 → #${obj.memberId}`;
      }
      return obj.type;
    } catch {
      return json;
    }
  };

  if (isLoading) {
    return <LoadingSpinner text="자동화 규칙을 불러오는 중..." />;
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-xl font-semibold">⚡ 자동화 규칙</h2>
          <p className="text-sm text-gray-500 mt-1">
            문의 접수 시 조건에 따라 자동으로 처리합니다.
          </p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          + 규칙 추가
        </button>
      </div>

      {rules.length === 0 ? (
        <div className="text-center py-12 text-gray-500">
          <p>자동화 규칙이 없습니다.</p>
          <button
            onClick={() => setShowCreateModal(true)}
            className="mt-4 text-blue-600 hover:underline"
          >
            첫 규칙 만들기
          </button>
        </div>
      ) : (
        <div className="space-y-3">
          {rules.map((rule) => (
            <div
              key={rule.id}
              className="p-4 border border-gray-200 dark:border-gray-700 rounded-lg hover:shadow-md transition-shadow"
            >
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <h3 className="font-medium text-gray-900 dark:text-white">
                      {rule.name}
                    </h3>
                    <Badge variant={rule.active ? 'success' : 'default'}>
                      {rule.active ? '활성' : '비활성'}
                    </Badge>
                  </div>
                  <div className="text-sm text-gray-600 dark:text-gray-400 space-y-1">
                    <p>
                      <span className="font-medium">트리거:</span> {getTriggerLabel(rule.triggerType)}
                    </p>
                    <p>
                      <span className="font-medium">조건:</span> {parseCondition(rule.conditionJson)}
                    </p>
                    <p>
                      <span className="font-medium">액션:</span> {parseAction(rule.actionJson)}
                    </p>
                  </div>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => handleToggle(rule.id, rule.active)}
                    className="px-3 py-1 text-sm border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
                  >
                    {rule.active ? '비활성화' : '활성화'}
                  </button>
                  <button
                    onClick={() => handleDelete(rule.id)}
                    className="px-3 py-1 text-sm text-red-600 border border-red-300 rounded hover:bg-red-50"
                  >
                    삭제
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        title="자동화 규칙 추가"
        size="lg"
      >
        <div className="p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">규칙 이름</label>
            <input
              type="text"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              className="w-full px-3 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
              placeholder="예: 이메일 문의 자동 에스컬레이션"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">트리거</label>
            <select
              value={form.triggerType}
              onChange={(e) =>
                setForm({ ...form, triggerType: e.target.value as any })
              }
              className="w-full px-3 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
            >
              <option value="INQUIRY_CREATED">문의 생성 시</option>
              <option value="INQUIRY_STATUS_CHANGED">문의 상태 변경 시</option>
            </select>
          </div>

          <div className="grid grid-cols-3 gap-2">
            <div>
              <label className="block text-sm font-medium mb-1">조건 필드</label>
              <select
                value={form.conditionField}
                onChange={(e) =>
                  setForm({ ...form, conditionField: e.target.value })
                }
                className="w-full px-3 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
              >
                <option value="channel">채널</option>
                <option value="message">메시지</option>
                <option value="senderName">발신자 이름</option>
                <option value="senderEmail">발신자 이메일</option>
                <option value="status">상태</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">연산자</label>
              <select
                value={form.conditionOperator}
                onChange={(e) =>
                  setForm({ ...form, conditionOperator: e.target.value })
                }
                className="w-full px-3 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
              >
                <option value="equals">일치</option>
                <option value="contains">포함</option>
                <option value="startsWith">시작</option>
                <option value="endsWith">끝</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">값</label>
              <input
                type="text"
                value={form.conditionValue}
                onChange={(e) =>
                  setForm({ ...form, conditionValue: e.target.value })
                }
                className="w-full px-3 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                placeholder="email"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="block text-sm font-medium mb-1">액션 타입</label>
              <select
                value={form.actionType}
                onChange={(e) => setForm({ ...form, actionType: e.target.value })}
                className="w-full px-3 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
              >
                <option value="SET_STATUS">상태 변경</option>
                <option value="ASSIGN">담당자 할당</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">액션 값</label>
              {form.actionType === 'SET_STATUS' ? (
                <select
                  value={form.actionValue}
                  onChange={(e) => setForm({ ...form, actionValue: e.target.value })}
                  className="w-full px-3 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                >
                  <option value="OPEN">접수</option>
                  <option value="ESCALATED">에스컬레이션</option>
                  <option value="RESOLVED">해결됨</option>
                  <option value="CLOSED">종료</option>
                </select>
              ) : (
                <input
                  type="number"
                  value={form.actionValue}
                  onChange={(e) => setForm({ ...form, actionValue: e.target.value })}
                  className="w-full px-3 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                  placeholder="담당자 ID"
                />
              )}
            </div>
          </div>

          <div className="flex gap-2 pt-4">
            <button
              onClick={handleCreate}
              disabled={isCreating}
              className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
            >
              {isCreating ? '생성 중...' : '생성'}
            </button>
            <button
              onClick={() => setShowCreateModal(false)}
              className="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              취소
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
