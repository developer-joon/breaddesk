'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { api } from '@/lib/api';
import type { Inquiry, InquiryStatus } from '@/types';

const STATUS_LABELS: Record<InquiryStatus, string> = {
  OPEN: '열림',
  AI_ANSWERED: 'AI 답변',
  ESCALATED: '에스컬레이션',
  RESOLVED: '해결됨',
  CLOSED: '종료',
};

const STATUS_COLORS: Record<InquiryStatus, string> = {
  OPEN: 'bg-blue-100 text-blue-800',
  AI_ANSWERED: 'bg-purple-100 text-purple-800',
  ESCALATED: 'bg-yellow-100 text-yellow-800',
  RESOLVED: 'bg-green-100 text-green-800',
  CLOSED: 'bg-gray-100 text-gray-800',
};

export default function InquiriesPage() {
  const [inquiries, setInquiries] = useState<Inquiry[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<InquiryStatus | 'ALL'>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    loadInquiries();
  }, [filter]);

  async function loadInquiries() {
    setLoading(true);
    const response = await api.getInquiries({
      status: filter === 'ALL' ? undefined : filter,
    });
    if (response.success && response.data) {
      setInquiries(response.data.items);
    }
    setLoading(false);
  }

  const filteredInquiries = inquiries.filter((inquiry) => {
    if (!searchQuery) return true;
    const query = searchQuery.toLowerCase();
    return (
      inquiry.senderName.toLowerCase().includes(query) ||
      inquiry.senderEmail.toLowerCase().includes(query) ||
      inquiry.message.toLowerCase().includes(query)
    );
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">문의 관리</h1>
        <p className="text-gray-600 mt-1">사용자 문의를 확인하고 답변하세요</p>
      </div>

      {/* 필터 & 검색 */}
      <div className="bg-white rounded-lg shadow p-4">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <input
              type="text"
              placeholder="이름, 이메일, 내용으로 검색..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
          </div>
          <div>
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value as InquiryStatus | 'ALL')}
              className="w-full md:w-48 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            >
              <option value="ALL">전체</option>
              {Object.entries(STATUS_LABELS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* 문의 목록 */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center h-64">
            <div className="text-gray-500">로딩 중...</div>
          </div>
        ) : filteredInquiries.length === 0 ? (
          <div className="flex items-center justify-center h-64">
            <div className="text-gray-500">문의가 없습니다</div>
          </div>
        ) : (
          <div className="divide-y divide-gray-200">
            {filteredInquiries.map((inquiry) => (
              <Link
                key={inquiry.id}
                href={`/inquiries/${inquiry.id}`}
                className="block p-6 hover:bg-gray-50 transition-colors"
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center space-x-3 mb-1">
                      <h3 className="text-lg font-medium text-gray-900">
                        {inquiry.senderName}
                      </h3>
                      <span className={`px-2 py-1 text-xs font-medium rounded-full ${STATUS_COLORS[inquiry.status]}`}>
                        {STATUS_LABELS[inquiry.status]}
                      </span>
                    </div>
                    <p className="text-sm text-gray-500">{inquiry.senderEmail}</p>
                  </div>
                  <div className="text-sm text-gray-500">
                    {new Date(inquiry.createdAt).toLocaleString('ko-KR')}
                  </div>
                </div>

                <p className="text-gray-700 mb-2 line-clamp-2">{inquiry.message}</p>

                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center space-x-4 text-gray-500">
                    <span>📱 {inquiry.channel}</span>
                    {inquiry.aiConfidence !== undefined && (
                      <span>
                        🤖 신뢰도: {Math.round(inquiry.aiConfidence * 100)}%
                      </span>
                    )}
                    {inquiry.taskId && (
                      <span className="text-primary-600">
                        → 업무 #{inquiry.taskId}
                      </span>
                    )}
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
