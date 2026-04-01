'use client';

import React, { useState } from 'react';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import api from '@/lib/api';
import type { ApiResponse } from '@/types';
import toast from 'react-hot-toast';

interface AISummaryProps {
  inquiryId: number;
}

export function AISummary({ inquiryId }: AISummaryProps) {
  const [expanded, setExpanded] = useState(false);
  const [summary, setSummary] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const loadSummary = async () => {
    if (summary) {
      setExpanded(!expanded);
      return;
    }

    try {
      setLoading(true);
      const { data } = await api.post<ApiResponse<{ summary: string }>>(
        `/inquiries/${inquiryId}/summarize`
      );
      setSummary(data.data.summary);
      setExpanded(true);
    } catch (error) {
      console.error('Failed to generate summary:', error);
      toast.error('요약 생성에 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-blue-50 border border-blue-200 rounded-lg overflow-hidden">
      <button
        onClick={loadSummary}
        disabled={loading}
        className="w-full px-4 py-3 flex items-center justify-between hover:bg-blue-100 transition-colors disabled:cursor-not-allowed"
      >
        <div className="flex items-center gap-2">
          <span className="text-xl">🤖</span>
          <span className="font-semibold text-blue-900">AI 요약</span>
        </div>
        <div className="flex items-center gap-2">
          {loading && <LoadingSpinner size="sm" />}
          <span className="text-blue-700">
            {expanded ? '▲' : '▼'}
          </span>
        </div>
      </button>

      {expanded && summary && (
        <div className="px-4 py-3 border-t border-blue-200 bg-white">
          <p className="text-gray-700 whitespace-pre-wrap">{summary}</p>
          <div className="mt-3 text-xs text-gray-500">
            💡 AI가 대화 내용을 요약했습니다
          </div>
        </div>
      )}
    </div>
  );
}
