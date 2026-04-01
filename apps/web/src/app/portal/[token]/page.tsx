'use client';

import React, { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { Badge } from '@/components/ui/Badge';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { portalService } from '@/services/portal';
import type { PortalInquiry } from '@/services/portal';
import toast from 'react-hot-toast';

export default function CustomerPortalPage() {
  const params = useParams();
  const token = params.token as string;
  
  const [inquiry, setInquiry] = useState<PortalInquiry | null>(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [sending, setSending] = useState(false);

  useEffect(() => {
    loadInquiry();
  }, [token]);

  const loadInquiry = async () => {
    try {
      setLoading(true);
      const data = await portalService.getInquiry(token);
      setInquiry(data);
    } catch (error) {
      console.error('Failed to load inquiry:', error);
      toast.error('문의를 불러올 수 없습니다');
    } finally {
      setLoading(false);
    }
  };

  const handleSendMessage = async () => {
    if (!message.trim()) {
      toast.error('메시지를 입력해주세요');
      return;
    }

    try {
      setSending(true);
      const newMsg = await portalService.addMessage(token, message);
      if (inquiry) {
        setInquiry({
          ...inquiry,
          messages: [...inquiry.messages, newMsg],
        });
      }
      setMessage('');
      toast.success('메시지가 전송되었습니다');
    } catch (error) {
      console.error('Failed to send message:', error);
      toast.error('메시지 전송에 실패했습니다');
    } finally {
      setSending(false);
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
    return <Badge variant={variants[status] || 'default'}>{status}</Badge>;
  };

  const getRoleIcon = (role: string) => {
    switch (role) {
      case 'USER':
        return '👤';
      case 'AI':
        return '🤖';
      case 'AGENT':
        return '👨‍💼';
      default:
        return '💬';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!inquiry) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-lg shadow-lg p-8 max-w-md text-center">
          <div className="text-6xl mb-4">❌</div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">문의를 찾을 수 없습니다</h1>
          <p className="text-gray-600">유효하지 않거나 만료된 링크입니다.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h1 className="text-2xl font-bold text-gray-900">문의 상세</h1>
            {getStatusBadge(inquiry.status)}
          </div>
          <div className="text-sm text-gray-600">
            문의 번호: <span className="font-mono font-semibold">INQ-{inquiry.id}</span>
          </div>
          <div className="text-sm text-gray-600 mt-1">
            접수일: {new Date(inquiry.createdAt).toLocaleString('ko-KR')}
          </div>
        </div>

        {/* Initial Message */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <h2 className="font-semibold text-gray-900 mb-3">문의 내용</h2>
          <p className="text-gray-700 whitespace-pre-wrap">{inquiry.message}</p>
        </div>

        {/* Conversation */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <h2 className="font-semibold text-gray-900 mb-4">대화 내역</h2>
          <div className="space-y-4">
            {inquiry.messages.length === 0 ? (
              <p className="text-gray-500 text-center py-8">아직 답변이 없습니다</p>
            ) : (
              inquiry.messages.map((msg) => (
                <div
                  key={msg.id}
                  className={`flex gap-3 ${msg.role === 'USER' ? 'flex-row-reverse' : ''}`}
                >
                  <div className="flex-shrink-0 text-2xl">{getRoleIcon(msg.role)}</div>
                  <div className={`flex-1 ${msg.role === 'USER' ? 'text-right' : ''}`}>
                    <div className="text-xs text-gray-500 mb-1">
                      {msg.role === 'AI' ? 'AI 어시스턴트' : msg.role === 'AGENT' ? '담당자' : '나'}
                      {' · '}
                      {new Date(msg.createdAt).toLocaleString('ko-KR')}
                    </div>
                    <div
                      className={`inline-block px-4 py-3 rounded-lg ${
                        msg.role === 'USER'
                          ? 'bg-blue-600 text-white'
                          : 'bg-gray-100 text-gray-900'
                      }`}
                    >
                      <p className="whitespace-pre-wrap">{msg.message}</p>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Reply Section */}
        {inquiry.status !== 'CLOSED' && (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="font-semibold text-gray-900 mb-4">추가 메시지</h2>
            <div className="space-y-3">
              <textarea
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="추가 질문이나 정보를 입력하세요..."
                rows={4}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <button
                onClick={handleSendMessage}
                disabled={sending || !message.trim()}
                className="w-full px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
              >
                {sending ? '전송 중...' : '메시지 전송'}
              </button>
            </div>
          </div>
        )}

        {/* Footer */}
        <div className="text-center text-sm text-gray-500 mt-8">
          <p>궁금하신 사항이 있으시면 위 메시지 입력란을 통해 추가 질문해주세요.</p>
        </div>
      </div>
    </div>
  );
}
