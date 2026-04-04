'use client';

import React, { useState, useEffect } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import api from '@/lib/api';
import toast from 'react-hot-toast';

interface AuditLog {
  id: number;
  memberId: number | null;
  memberName: string;
  action: string;
  entityType: string;
  entityId: number | null;
  details: string;
  createdAt: string;
}

export default function AuditLogsPage() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadLogs();
  }, [page]);

  const loadLogs = async () => {
    setIsLoading(true);
    try {
      const response = await api.get(`/audit-logs?page=${page}&size=50`);
      setLogs(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('Failed to load audit logs:', error);
      toast.error('감사 로그를 불러올 수 없습니다');
    } finally {
      setIsLoading(false);
    }
  };

  const getActionLabel = (action: string) => {
    const labels: Record<string, string> = {
      INQUIRY_CREATED: '문의 생성',
      INQUIRY_STATUS_CHANGED: '문의 상태 변경',
      TASK_CREATED: '업무 생성',
      TASK_ASSIGNED: '업무 배정',
      SETTINGS_CHANGED: '설정 변경',
    };
    return labels[action] || action;
  };

  const getActionColor = (action: string) => {
    if (action.includes('CREATED')) return 'text-green-600';
    if (action.includes('CHANGED') || action.includes('ASSIGNED')) return 'text-blue-600';
    if (action.includes('DELETED')) return 'text-red-600';
    return 'text-gray-600';
  };

  return (
    <AppLayout>
      <div className="max-w-7xl mx-auto px-4 py-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">📋 감사 로그</h1>
          <p className="text-sm text-gray-600 mt-1">시스템의 주요 작업 이력을 추적합니다</p>
        </div>

        {isLoading ? (
          <div className="flex justify-center py-20">
            <LoadingSpinner size="lg" />
          </div>
        ) : (
          <>
            <div className="bg-white rounded-lg shadow border border-gray-200 overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        시간
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        작업자
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        작업
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        대상
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        상세
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {logs.length === 0 ? (
                      <tr>
                        <td colSpan={5} className="px-4 py-8 text-center text-gray-500">
                          감사 로그가 없습니다
                        </td>
                      </tr>
                    ) : (
                      logs.map((log) => (
                        <tr key={log.id} className="hover:bg-gray-50">
                          <td className="px-4 py-3 text-sm text-gray-900 whitespace-nowrap">
                            {new Date(log.createdAt).toLocaleString('ko-KR')}
                          </td>
                          <td className="px-4 py-3 text-sm text-gray-900">
                            {log.memberName}
                          </td>
                          <td className={`px-4 py-3 text-sm font-medium ${getActionColor(log.action)}`}>
                            {getActionLabel(log.action)}
                          </td>
                          <td className="px-4 py-3 text-sm text-gray-600">
                            {log.entityType} #{log.entityId}
                          </td>
                          <td className="px-4 py-3 text-sm text-gray-600">
                            {log.details}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            {totalPages > 1 && (
              <div className="mt-6 flex items-center justify-center gap-2">
                <button
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="px-4 py-2 bg-white border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  이전
                </button>
                <span className="text-sm text-gray-600">
                  {page + 1} / {totalPages}
                </span>
                <button
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                  disabled={page >= totalPages - 1}
                  className="px-4 py-2 bg-white border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  다음
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </AppLayout>
  );
}
