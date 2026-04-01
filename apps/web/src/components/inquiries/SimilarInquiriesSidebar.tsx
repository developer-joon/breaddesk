'use client';

import React, { useState, useEffect } from 'react';
import { Badge } from '@/components/ui/Badge';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import api from '@/lib/api';
import type { ApiResponse, SimilarInquiryResponse } from '@/types';
import toast from 'react-hot-toast';

interface SimilarInquiriesSidebarProps {
  inquiryId: number;
  onNavigate?: (inquiryId: number) => void;
}

export function SimilarInquiriesSidebar({ inquiryId, onNavigate }: SimilarInquiriesSidebarProps) {
  const [similar, setSimilar] = useState<SimilarInquiryResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSimilar();
  }, [inquiryId]);

  const loadSimilar = async () => {
    try {
      setLoading(true);
      const { data } = await api.get<ApiResponse<SimilarInquiryResponse[]>>(
        `/inquiries/${inquiryId}/similar`
      );
      setSimilar(data.data);
    } catch (error) {
      console.error('Failed to load similar inquiries:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    const variants: Record<string, any> = {
      OPEN: 'default',
      AI_ANSWERED: 'primary',
      ESCALATED: 'warning',
      RESOLVED: 'success',
      CLOSED: 'default',
    };
    return variants[status] || 'default';
  };

  const getSimilarityColor = (score: number) => {
    if (score >= 0.9) return 'text-green-600';
    if (score >= 0.8) return 'text-blue-600';
    if (score >= 0.7) return 'text-yellow-600';
    return 'text-gray-600';
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
      <h3 className="font-semibold text-gray-900 mb-4">유사 문의</h3>

      {loading ? (
        <div className="flex justify-center py-8">
          <LoadingSpinner size="sm" />
        </div>
      ) : similar.length === 0 ? (
        <p className="text-sm text-gray-500 text-center py-8">
          유사한 문의가 없습니다
        </p>
      ) : (
        <div className="space-y-3">
          {similar.map((item) => (
            <div
              key={item.inquiryId}
              className="border border-gray-200 rounded-lg p-3 hover:bg-gray-50 cursor-pointer transition-colors"
              onClick={() => onNavigate?.(item.inquiryId)}
            >
              <div className="flex items-start justify-between mb-2">
                <span className="text-xs text-gray-500">
                  INQ-{item.inquiryId}
                </span>
                <div className="flex items-center gap-2">
                  <span className={`text-xs font-semibold ${getSimilarityColor(item.score)}`}>
                    {(item.score * 100).toFixed(0)}% 유사
                  </span>
                  <Badge variant={getStatusBadge(item.status)} size="sm">
                    {item.status}
                  </Badge>
                </div>
              </div>

              <p className="text-sm text-gray-700 line-clamp-2 mb-2">
                {item.message}
              </p>

              <div className="flex items-center justify-between text-xs text-gray-500">
                <span>{item.senderName}</span>
                <span>{new Date(item.createdAt).toLocaleDateString('ko-KR')}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="mt-4 text-xs text-gray-500 text-center">
        💡 벡터 유사도 기반 자동 탐지
      </div>
    </div>
  );
}
