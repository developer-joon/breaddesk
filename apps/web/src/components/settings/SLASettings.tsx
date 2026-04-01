'use client';

import React, { useState, useEffect } from 'react';
import { getSlaRules, updateSlaRule } from '@/services/sla';
import type { SlaRuleResponse, TaskUrgency } from '@/types';
import toast from 'react-hot-toast';

export function SLASettings() {
  const [rules, setRules] = useState<SlaRuleResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadRules();
  }, []);

  const loadRules = async () => {
    try {
      setLoading(true);
      const data = await getSlaRules();
      setRules(data);
    } catch (error) {
      console.error('Failed to load SLA rules:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async (urgency: TaskUrgency, field: string, value: any) => {
    const rule = rules.find((r) => r.urgency === urgency);
    if (!rule) return;

    try {
      await updateSlaRule(urgency, {
        responseTimeHours: field === 'responseTimeHours' ? Number(value) : rule.responseTimeHours,
        resolveTimeHours: field === 'resolveTimeHours' ? Number(value) : rule.resolveTimeHours,
        enabled: field === 'enabled' ? value : rule.enabled,
      });
      toast.success('SLA 규칙이 수정되었습니다');
      loadRules();
    } catch (error) {
      console.error('Failed to update SLA rule:', error);
      toast.error('SLA 규칙 수정에 실패했습니다');
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

  const getUrgencyColor = (urgency: string) => {
    const colors: Record<string, string> = {
      CRITICAL: 'bg-red-100 text-red-800',
      HIGH: 'bg-orange-100 text-orange-800',
      NORMAL: 'bg-blue-100 text-blue-800',
      LOW: 'bg-gray-100 text-gray-800',
    };
    return colors[urgency] || 'bg-gray-100 text-gray-800';
  };

  return (
    <div>
      <div className="mb-6">
        <h2 className="text-xl font-semibold">SLA 규칙</h2>
        <p className="text-sm text-gray-600 mt-1">
          긴급도별 응답 시간 및 해결 시간 목표를 설정합니다
        </p>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                긴급도
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                응답 시간 (시간)
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                해결 시간 (시간)
              </th>
              <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">
                활성화
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {rules.map((rule) => (
              <tr key={rule.urgency} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <span
                    className={`px-3 py-1 rounded-full text-sm font-medium ${getUrgencyColor(
                      rule.urgency
                    )}`}
                  >
                    {getUrgencyLabel(rule.urgency)}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <input
                    type="number"
                    value={rule.responseTimeHours}
                    onChange={(e) =>
                      handleUpdate(rule.urgency, 'responseTimeHours', e.target.value)
                    }
                    className="w-24 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    min="0"
                    step="0.5"
                  />
                </td>
                <td className="px-6 py-4">
                  <input
                    type="number"
                    value={rule.resolveTimeHours}
                    onChange={(e) =>
                      handleUpdate(rule.urgency, 'resolveTimeHours', e.target.value)
                    }
                    className="w-24 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    min="0"
                    step="0.5"
                  />
                </td>
                <td className="px-6 py-4 text-center">
                  <input
                    type="checkbox"
                    checked={rule.enabled}
                    onChange={(e) =>
                      handleUpdate(rule.urgency, 'enabled', e.target.checked)
                    }
                    className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 className="font-semibold text-blue-900 mb-2">💡 SLA 안내</h3>
        <ul className="text-sm text-blue-800 space-y-1">
          <li>• 응답 시간: 문의 접수 후 첫 답변까지의 목표 시간</li>
          <li>• 해결 시간: 문의 접수 후 완전 해결까지의 목표 시간</li>
          <li>• 목표 시간의 80%가 경과하면 경고 알림이 발송됩니다</li>
          <li>• 목표 시간 초과 시 SLA 위반으로 기록됩니다</li>
        </ul>
      </div>
    </div>
  );
}
