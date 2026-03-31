'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { ErrorMessage } from '@/components/ui/ErrorMessage';
import { EmptyState } from '@/components/ui/EmptyState';
import {
  getInquiries,
  getInquiryById,
  addInquiryMessage,
  updateInquiryStatus,
  getSimilarInquiries,
} from '@/services/inquiries';
import { exportInquiries } from '@/services/export';
import type { InquiryResponse, InquiryMessageResponse, InquiryStatus, SimilarInquiryResponse } from '@/types';
import toast from 'react-hot-toast';

export default function InquiriesPage() {
  const [inquiries, setInquiries] = useState<InquiryResponse[]>([]);
  const [selectedInquiry, setSelectedInquiry] = useState<InquiryResponse | null>(null);
  const [newMessage, setNewMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSending, setIsSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [similarInquiries, setSimilarInquiries] = useState<SimilarInquiryResponse[]>([]);
  const [isLoadingSimilar, setIsLoadingSimilar] = useState(false);

  const fetchInquiries = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const result = await getInquiries(page, 20);
      setInquiries(result.content);
      setTotalPages(result.totalPages);
    } catch (err) {
      console.error('Failed to fetch inquiries:', err);
      setError('문의 목록을 불러오지 못했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchInquiries();
  }, [fetchInquiries]);

  const handleSelectInquiry = async (inquiry: InquiryResponse) => {
    setSimilarInquiries([]);
    try {
      const detail = await getInquiryById(inquiry.id);
      setSelectedInquiry(detail);
    } catch {
      setSelectedInquiry(inquiry);
    }
    // Load similar inquiries in background
    setIsLoadingSimilar(true);
    try {
      const similar = await getSimilarInquiries(inquiry.id, 5);
      setSimilarInquiries(similar);
    } catch {
      // Non-critical
    } finally {
      setIsLoadingSimilar(false);
    }
  };

  const handleSendMessage = async () => {
    if (!newMessage.trim() || !selectedInquiry) return;

    setIsSending(true);
    try {
      const msg = await addInquiryMessage(selectedInquiry.id, {
        role: 'AGENT',
        message: newMessage,
      });
      setSelectedInquiry({
        ...selectedInquiry,
        messages: [...(selectedInquiry.messages || []), msg],
      });
      setNewMessage('');
      toast.success('메시지 전송 완료');
    } catch (err) {
      toast.error('메시지 전송에 실패했습니다.');
    } finally {
      setIsSending(false);
    }
  };

  const handleStatusChange = async (id: number, status: InquiryStatus) => {
    try {
      const updated = await updateInquiryStatus(id, { status });
      setInquiries(inquiries.map((i) => (i.id === id ? updated : i)));
      if (selectedInquiry?.id === id) setSelectedInquiry(updated);
      toast.success('상태가 변경되었습니다.');
    } catch {
      toast.error('상태 변경에 실패했습니다.');
    }
  };

  const getStatusBadgeVariant = (status: string) => {
    switch (status) {
      case 'OPEN':
        return 'info';
      case 'AI_ANSWERED':
        return 'info';
      case 'ESCALATED':
        return 'warning';
      case 'RESOLVED':
        return 'success';
      case 'CLOSED':
        return 'default';
      default:
        return 'default';
    }
  };

  const getStatusLabel = (status: string) => {
    const labels: Record<string, string> = {
      OPEN: '접수',
      AI_ANSWERED: 'AI 답변',
      ESCALATED: '에스컬레이션',
      RESOLVED: '해결됨',
      CLOSED: '종료',
    };
    return labels[status] || status;
  };

  const getChannelIcon = (channel: string) => {
    switch (channel?.toUpperCase()) {
      case 'EMAIL':
        return '📧';
      case 'CHAT':
        return '💬';
      case 'PHONE':
        return '📞';
      case 'FORM':
        return '📝';
      default:
        return '📋';
    }
  };

  const getRoleLabel = (role: string) => {
    switch (role) {
      case 'USER':
        return '고객';
      case 'AI':
        return 'AI';
      case 'AGENT':
        return '상담원';
      default:
        return role;
    }
  };

  const handleExport = async () => {
    try {
      await exportInquiries();
      toast.success('CSV 파일이 다운로드되었습니다');
    } catch (err) {
      toast.error('내보내기에 실패했습니다');
    }
  };

  return (
    <AppLayout>
      <div className="h-full flex flex-col">
        <div className="mb-4 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">문의 관리</h1>
          <button
            onClick={handleExport}
            className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors text-sm font-medium"
          >
            📥 CSV 내보내기
          </button>
        </div>

        {isLoading && <LoadingSpinner text="문의 목록을 불러오는 중..." />}
        {error && <ErrorMessage message={error} onRetry={fetchInquiries} />}

        {!isLoading && !error && (
          <div className="flex-1 grid lg:grid-cols-3 gap-4 overflow-hidden">
            {/* Inquiry List */}
            <div className="lg:col-span-1 bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden flex flex-col">
              <div className="px-4 py-3 border-b border-gray-200 dark:border-gray-700">
                <h2 className="font-semibold">문의 목록 ({inquiries.length})</h2>
              </div>
              <div className="overflow-y-auto flex-1">
                {inquiries.length === 0 ? (
                  <EmptyState icon="💬" title="문의가 없습니다" />
                ) : (
                  inquiries.map((inquiry) => (
                    <div
                      key={inquiry.id}
                      onClick={() => handleSelectInquiry(inquiry)}
                      className={`p-4 border-b border-gray-200 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 ${
                        selectedInquiry?.id === inquiry.id ? 'bg-blue-50' : ''
                      }`}
                    >
                      <div className="flex items-start justify-between gap-2 mb-2">
                        <div className="flex items-center gap-2">
                          <span className="text-xl">{getChannelIcon(inquiry.channel)}</span>
                          <h3 className="font-medium text-gray-900 line-clamp-1">
                            {inquiry.senderName}
                          </h3>
                        </div>
                        <Badge variant={getStatusBadgeVariant(inquiry.status)}>
                          {getStatusLabel(inquiry.status)}
                        </Badge>
                      </div>
                      <p className="text-sm text-gray-600 line-clamp-2">{inquiry.message}</p>
                      <p className="text-xs text-gray-400 mt-1">
                        {new Date(inquiry.createdAt).toLocaleString('ko-KR')}
                      </p>
                    </div>
                  ))
                )}
              </div>
              {/* Pagination */}
              {totalPages > 1 && (
                <div className="px-4 py-2 border-t border-gray-200 dark:border-gray-700 flex justify-between items-center">
                  <button
                    disabled={page === 0}
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    className="text-sm text-blue-600 disabled:text-gray-400"
                  >
                    ← 이전
                  </button>
                  <span className="text-xs text-gray-500">
                    {page + 1} / {totalPages}
                  </span>
                  <button
                    disabled={page >= totalPages - 1}
                    onClick={() => setPage((p) => p + 1)}
                    className="text-sm text-blue-600 disabled:text-gray-400"
                  >
                    다음 →
                  </button>
                </div>
              )}
            </div>

            {/* Chat Panel */}
            <div className="lg:col-span-2 bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden flex flex-col">
              {selectedInquiry ? (
                <>
                  {/* Header */}
                  <div className="px-4 py-3 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
                    <div>
                      <h2 className="font-semibold">{selectedInquiry.senderName}</h2>
                      <p className="text-sm text-gray-600 dark:text-gray-400">
                        {selectedInquiry.senderEmail || selectedInquiry.channel}
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      <select
                        value={selectedInquiry.status}
                        onChange={(e) =>
                          handleStatusChange(
                            selectedInquiry.id,
                            e.target.value as InquiryStatus,
                          )
                        }
                        className="px-3 py-1 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      >
                        <option value="OPEN">접수</option>
                        <option value="AI_ANSWERED">AI 답변</option>
                        <option value="ESCALATED">에스컬레이션</option>
                        <option value="RESOLVED">해결됨</option>
                        <option value="CLOSED">종료</option>
                      </select>
                    </div>
                  </div>

                  {/* AI Response */}
                  {selectedInquiry.aiResponse && (
                    <div className="px-4 py-2 bg-cyan-50 border-b border-cyan-200">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="text-sm">🤖</span>
                        <span className="text-xs font-medium text-cyan-700">
                          AI 자동 응답
                          {selectedInquiry.aiConfidence != null &&
                            ` (신뢰도: ${Math.round(selectedInquiry.aiConfidence * 100)}%)`}
                        </span>
                      </div>
                      <p className="text-sm text-cyan-800">{selectedInquiry.aiResponse}</p>
                    </div>
                  )}

                  {/* Messages */}
                  <div className="flex-1 overflow-y-auto p-4 space-y-4">
                    {/* Initial message */}
                    <div className="flex justify-start">
                      <div className="max-w-[70%] rounded-lg p-3 bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-white">
                        <p className="text-xs font-medium text-gray-500 mb-1">
                          {selectedInquiry.senderName}
                        </p>
                        <p className="text-sm">{selectedInquiry.message}</p>
                        <p className="text-xs text-gray-500 mt-1">
                          {new Date(selectedInquiry.createdAt).toLocaleTimeString('ko-KR')}
                        </p>
                      </div>
                    </div>

                    {/* Conversation messages */}
                    {selectedInquiry.messages?.map((msg) => (
                      <div
                        key={msg.id}
                        className={`flex ${
                          msg.role === 'USER' ? 'justify-start' : 'justify-end'
                        }`}
                      >
                        <div
                          className={`max-w-[70%] rounded-lg p-3 ${
                            msg.role === 'AGENT'
                              ? 'bg-blue-600 text-white'
                              : msg.role === 'AI'
                                ? 'bg-cyan-100 text-cyan-900'
                                : 'bg-gray-100 dark:bg-gray-700 text-gray-900'
                          }`}
                        >
                          <p className="text-xs font-medium mb-1 opacity-75">
                            {getRoleLabel(msg.role)}
                          </p>
                          <p className="text-sm">{msg.message}</p>
                          <p
                            className={`text-xs mt-1 ${
                              msg.role === 'AGENT' ? 'text-blue-100' : 'text-gray-500'
                            }`}
                          >
                            {new Date(msg.createdAt).toLocaleTimeString('ko-KR')}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Similar Inquiries */}
                  {(similarInquiries.length > 0 || isLoadingSimilar) && (
                    <div className="px-4 py-3 border-t border-gray-200 dark:border-gray-700 bg-gray-50">
                      <div className="flex items-center gap-2 mb-2">
                        <span className="text-sm">🔗</span>
                        <h4 className="text-sm font-semibold text-gray-700 dark:text-gray-300">유사 문의</h4>
                      </div>
                      {isLoadingSimilar ? (
                        <p className="text-xs text-gray-400">유사 문의를 검색하는 중...</p>
                      ) : (
                        <div className="space-y-1.5 max-h-32 overflow-y-auto">
                          {similarInquiries.map((sim) => (
                            <div
                              key={sim.inquiryId}
                              className="flex items-center justify-between bg-white dark:bg-gray-800 rounded px-3 py-2 border border-gray-200 dark:border-gray-700 cursor-pointer hover:bg-blue-50"
                              onClick={() => {
                                const target = inquiries.find((i) => i.id === sim.inquiryId);
                                if (target) handleSelectInquiry(target);
                              }}
                            >
                              <div className="flex-1 min-w-0">
                                <p className="text-sm text-gray-900 truncate">{sim.senderName}: {sim.message}</p>
                                <p className="text-xs text-gray-400">{new Date(sim.createdAt).toLocaleDateString('ko-KR')}</p>
                              </div>
                              <span className={`ml-2 text-xs font-bold flex-shrink-0 ${
                                sim.score >= 0.8 ? 'text-green-600' : sim.score >= 0.6 ? 'text-yellow-600' : 'text-gray-500'
                              }`}>
                                {(sim.score * 100).toFixed(0)}%
                              </span>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}

                  {/* Input */}
                  <div className="p-4 border-t border-gray-200 dark:border-gray-700">
                    <div className="flex gap-2">
                      <input
                        type="text"
                        value={newMessage}
                        onChange={(e) => setNewMessage(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && handleSendMessage()}
                        placeholder="메시지를 입력하세요..."
                        className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        disabled={isSending}
                      />
                      <button
                        onClick={handleSendMessage}
                        disabled={isSending || !newMessage.trim()}
                        className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
                      >
                        {isSending ? '...' : '전송'}
                      </button>
                    </div>
                  </div>
                </>
              ) : (
                <EmptyState icon="💬" title="문의를 선택해주세요" />
              )}
            </div>
          </div>
        )}
      </div>
    </AppLayout>
  );
}
