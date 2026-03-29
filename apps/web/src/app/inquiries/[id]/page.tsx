'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { api } from '@/lib/api';
import type { Inquiry, InquiryMessage, MessageRole } from '@/types';

const ROLE_LABELS: Record<MessageRole, string> = {
  USER: '사용자',
  AI: 'AI',
  AGENT: '담당자',
};

const ROLE_COLORS: Record<MessageRole, string> = {
  USER: 'bg-gray-100',
  AI: 'bg-blue-100',
  AGENT: 'bg-green-100',
};

export default function InquiryDetailPage() {
  const params = useParams();
  const inquiryId = Number(params.id);

  const [inquiry, setInquiry] = useState<Inquiry | null>(null);
  const [loading, setLoading] = useState(true);
  const [replyText, setReplyText] = useState('');
  const [sending, setSending] = useState(false);

  useEffect(() => {
    loadInquiry();
  }, [inquiryId]);

  async function loadInquiry() {
    setLoading(true);
    const response = await api.getInquiry(inquiryId);
    if (response.success && response.data) {
      setInquiry(response.data);
    }
    setLoading(false);
  }

  async function handleReply() {
    if (!replyText.trim()) return;

    setSending(true);
    const response = await api.replyToInquiry(inquiryId, replyText);
    if (response.success) {
      setReplyText('');
      await loadInquiry();
    }
    setSending(false);
  }

  async function handleEscalate() {
    // 에스컬레이션: 업무 생성 페이지로 이동 (문의 ID 전달)
    window.location.href = `/tasks/new?inquiryId=${inquiryId}`;
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">로딩 중...</div>
      </div>
    );
  }

  if (!inquiry) {
    return (
      <div className="flex flex-col items-center justify-center h-64 space-y-4">
        <div className="text-gray-500">문의를 찾을 수 없습니다</div>
        <Link href="/inquiries" className="text-primary-600 hover:text-primary-700">
          목록으로 돌아가기
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* 헤더 */}
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <Link href="/inquiries" className="text-sm text-gray-600 hover:text-gray-900 mb-2 inline-block">
            ← 목록으로
          </Link>
          <h1 className="text-2xl font-bold text-gray-900">문의 #{inquiry.id}</h1>
          <div className="flex items-center space-x-4 mt-2 text-sm text-gray-600">
            <span>📱 {inquiry.channel}</span>
            <span>📅 {new Date(inquiry.createdAt).toLocaleString('ko-KR')}</span>
            {inquiry.aiConfidence !== undefined && (
              <span>🤖 신뢰도: {Math.round(inquiry.aiConfidence * 100)}%</span>
            )}
          </div>
        </div>

        {inquiry.status !== 'ESCALATED' && inquiry.status !== 'CLOSED' && (
          <button
            onClick={handleEscalate}
            className="px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 transition-colors"
          >
            업무로 전환
          </button>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 대화 이력 */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-lg shadow">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold text-gray-900">대화 이력</h2>
            </div>
            
            <div className="p-6 space-y-4 max-h-[600px] overflow-y-auto">
              {/* 초기 문의 */}
              <div className="flex items-start space-x-3">
                <div className="w-10 h-10 bg-gray-500 rounded-full flex items-center justify-center text-white font-bold flex-shrink-0">
                  {inquiry.senderName[0]}
                </div>
                <div className="flex-1">
                  <div className="flex items-center space-x-2 mb-1">
                    <span className="font-medium text-gray-900">{inquiry.senderName}</span>
                    <span className="text-xs text-gray-500">사용자</span>
                  </div>
                  <div className="bg-gray-100 rounded-lg p-4">
                    <p className="text-gray-800 whitespace-pre-wrap">{inquiry.message}</p>
                  </div>
                </div>
              </div>

              {/* AI 답변 */}
              {inquiry.aiResponse && (
                <div className="flex items-start space-x-3">
                  <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center text-white text-xl flex-shrink-0">
                    🤖
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center space-x-2 mb-1">
                      <span className="font-medium text-gray-900">AI Assistant</span>
                      <span className="text-xs text-gray-500">자동 답변</span>
                    </div>
                    <div className="bg-blue-50 rounded-lg p-4 border border-blue-200">
                      <p className="text-gray-800 whitespace-pre-wrap">{inquiry.aiResponse}</p>
                    </div>
                  </div>
                </div>
              )}

              {/* 추가 대화 메시지 */}
              {inquiry.messages?.map((message) => {
                const isUser = message.role === 'USER';
                const isAI = message.role === 'AI';
                const isAgent = message.role === 'AGENT';

                return (
                  <div key={message.id} className="flex items-start space-x-3">
                    <div className={`
                      w-10 h-10 rounded-full flex items-center justify-center text-white font-bold flex-shrink-0
                      ${isUser ? 'bg-gray-500' : isAI ? 'bg-blue-500' : 'bg-green-500'}
                    `}>
                      {isUser ? inquiry.senderName[0] : isAI ? '🤖' : '👤'}
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center space-x-2 mb-1">
                        <span className="font-medium text-gray-900">
                          {isUser ? inquiry.senderName : isAI ? 'AI Assistant' : '담당자'}
                        </span>
                        <span className="text-xs text-gray-500">
                          {ROLE_LABELS[message.role]}
                        </span>
                        <span className="text-xs text-gray-400">
                          {new Date(message.createdAt).toLocaleString('ko-KR')}
                        </span>
                      </div>
                      <div className={`
                        rounded-lg p-4
                        ${isUser ? 'bg-gray-100' : isAI ? 'bg-blue-50 border border-blue-200' : 'bg-green-50 border border-green-200'}
                      `}>
                        <p className="text-gray-800 whitespace-pre-wrap">{message.message}</p>
                      </div>
                    </div>
                  </div>
                );
              })}

              {(!inquiry.messages || inquiry.messages.length === 0) && !inquiry.aiResponse && (
                <div className="text-center py-8 text-gray-500">
                  아직 답변이 없습니다
                </div>
              )}
            </div>
          </div>

          {/* 답변 입력 */}
          {inquiry.status !== 'CLOSED' && (
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">답변 작성</h2>
              <textarea
                value={replyText}
                onChange={(e) => setReplyText(e.target.value)}
                placeholder="답변을 입력하세요..."
                rows={4}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none"
              />
              <div className="flex justify-end mt-4">
                <button
                  onClick={handleReply}
                  disabled={sending || !replyText.trim()}
                  className="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                >
                  {sending ? '전송 중...' : '답변 전송'}
                </button>
              </div>
            </div>
          )}
        </div>

        {/* 사이드바 */}
        <div className="space-y-6">
          {/* 상태 */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-sm font-semibold text-gray-900 mb-3">상태</h3>
            <span className={`
              inline-block px-3 py-1 text-sm font-medium rounded-full
              ${inquiry.status === 'RESOLVED' ? 'bg-green-100 text-green-800' :
                inquiry.status === 'ESCALATED' ? 'bg-yellow-100 text-yellow-800' :
                inquiry.status === 'AI_ANSWERED' ? 'bg-purple-100 text-purple-800' :
                inquiry.status === 'CLOSED' ? 'bg-gray-100 text-gray-800' :
                'bg-blue-100 text-blue-800'}
            `}>
              {inquiry.status}
            </span>
          </div>

          {/* 요청자 정보 */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-sm font-semibold text-gray-900 mb-3">요청자</h3>
            <div className="space-y-2">
              <div>
                <span className="text-sm text-gray-600">이름:</span>
                <p className="text-gray-900">{inquiry.senderName}</p>
              </div>
              <div>
                <span className="text-sm text-gray-600">이메일:</span>
                <p className="text-gray-900 text-sm">{inquiry.senderEmail}</p>
              </div>
            </div>
          </div>

          {/* 연결된 업무 */}
          {inquiry.taskId && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-sm font-semibold text-gray-900 mb-3">연결된 업무</h3>
              <Link
                href={`/tasks/${inquiry.taskId}`}
                className="flex items-center justify-between px-4 py-3 bg-primary-50 rounded-lg hover:bg-primary-100 transition-colors"
              >
                <span className="text-primary-700 font-medium">업무 #{inquiry.taskId}</span>
                <span className="text-primary-600">→</span>
              </Link>
            </div>
          )}

          {/* 해결 정보 */}
          {inquiry.resolvedAt && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-sm font-semibold text-gray-900 mb-3">해결 정보</h3>
              <div className="space-y-2 text-sm">
                <div>
                  <span className="text-gray-600">해결 방법:</span>
                  <span className="ml-2 text-gray-900">
                    {inquiry.resolvedBy === 'AI' ? '🤖 AI 자동 해결' : '👤 담당자 처리'}
                  </span>
                </div>
                <div>
                  <span className="text-gray-600">해결 시간:</span>
                  <span className="ml-2 text-gray-900">
                    {new Date(inquiry.resolvedAt).toLocaleString('ko-KR')}
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
