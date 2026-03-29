'use client';

import React, { useState, useEffect } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { Inquiry, Message } from '@/types';
import toast from 'react-hot-toast';

export default function InquiriesPage() {
  const [inquiries, setInquiries] = useState<Inquiry[]>([]);
  const [selectedInquiry, setSelectedInquiry] = useState<Inquiry | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [filter, setFilter] = useState({ status: 'ALL', channel: 'ALL', search: '' });

  useEffect(() => {
    fetchInquiries();
  }, [filter]);

  useEffect(() => {
    if (selectedInquiry) {
      fetchMessages(selectedInquiry.id);
    }
  }, [selectedInquiry]);

  const fetchInquiries = async () => {
    // Mock data
    setInquiries([
      {
        id: '1',
        title: '결제 오류 문의',
        content: '결제가 안 되는데 도와주세요',
        status: 'IN_PROGRESS',
        channel: 'EMAIL',
        customerName: '김철수',
        customerEmail: 'kim@example.com',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        tags: ['결제', '긴급'],
      },
      {
        id: '2',
        title: '회원가입 문의',
        content: '회원가입이 안 됩니다',
        status: 'NEW',
        channel: 'CHAT',
        customerName: '이영희',
        customerEmail: 'lee@example.com',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        tags: ['회원가입'],
      },
    ]);
  };

  const fetchMessages = async (inquiryId: string) => {
    // Mock data
    setMessages([
      {
        id: '1',
        inquiryId,
        content: '결제가 안 되는데 도와주세요',
        sender: 'CUSTOMER',
        senderName: '김철수',
        createdAt: new Date(Date.now() - 3600000).toISOString(),
      },
      {
        id: '2',
        inquiryId,
        content: '어떤 결제 수단을 사용하셨나요?',
        sender: 'AGENT',
        senderName: '상담원',
        createdAt: new Date(Date.now() - 1800000).toISOString(),
      },
    ]);
  };

  const handleSendMessage = async () => {
    if (!newMessage.trim() || !selectedInquiry) return;

    const message: Message = {
      id: Date.now().toString(),
      inquiryId: selectedInquiry.id,
      content: newMessage,
      sender: 'AGENT',
      senderName: '상담원',
      createdAt: new Date().toISOString(),
    };

    setMessages([...messages, message]);
    setNewMessage('');
    toast.success('메시지 전송 완료');
  };

  const handleConvertToTask = () => {
    if (!selectedInquiry) return;
    toast.success('업무로 전환되었습니다');
  };

  const getStatusBadgeVariant = (status: string) => {
    switch (status) {
      case 'NEW':
        return 'info';
      case 'IN_PROGRESS':
        return 'warning';
      case 'RESOLVED':
        return 'success';
      case 'CLOSED':
        return 'default';
      default:
        return 'default';
    }
  };

  const getChannelIcon = (channel: string) => {
    switch (channel) {
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

  return (
    <AppLayout>
      <div className="h-full flex flex-col">
        {/* Header */}
        <div className="mb-4">
          <h1 className="text-2xl font-bold text-gray-900">문의 관리</h1>
        </div>

        {/* Filters */}
        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-200 mb-4">
          <div className="flex flex-wrap gap-3">
            <select
              value={filter.status}
              onChange={(e) => setFilter({ ...filter, status: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">전체 상태</option>
              <option value="NEW">신규</option>
              <option value="IN_PROGRESS">진행중</option>
              <option value="RESOLVED">해결됨</option>
              <option value="CLOSED">종료</option>
            </select>

            <select
              value={filter.channel}
              onChange={(e) => setFilter({ ...filter, channel: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">전체 채널</option>
              <option value="EMAIL">이메일</option>
              <option value="CHAT">채팅</option>
              <option value="PHONE">전화</option>
              <option value="FORM">폼</option>
            </select>

            <input
              type="text"
              placeholder="검색..."
              value={filter.search}
              onChange={(e) => setFilter({ ...filter, search: e.target.value })}
              className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        {/* Main Content */}
        <div className="flex-1 grid lg:grid-cols-3 gap-4 overflow-hidden">
          {/* Inquiry List */}
          <div className="lg:col-span-1 bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden flex flex-col">
            <div className="px-4 py-3 border-b border-gray-200">
              <h2 className="font-semibold">문의 목록 ({inquiries.length})</h2>
            </div>
            <div className="overflow-y-auto custom-scrollbar flex-1">
              {inquiries.map((inquiry) => (
                <div
                  key={inquiry.id}
                  onClick={() => setSelectedInquiry(inquiry)}
                  className={`p-4 border-b border-gray-200 cursor-pointer hover:bg-gray-50 ${
                    selectedInquiry?.id === inquiry.id ? 'bg-blue-50' : ''
                  }`}
                >
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <div className="flex items-center gap-2">
                      <span className="text-xl">{getChannelIcon(inquiry.channel)}</span>
                      <h3 className="font-medium text-gray-900">{inquiry.title}</h3>
                    </div>
                    <Badge variant={getStatusBadgeVariant(inquiry.status)}>{inquiry.status}</Badge>
                  </div>
                  <p className="text-sm text-gray-600 mb-2">{inquiry.customerName}</p>
                  <p className="text-xs text-gray-400">
                    {new Date(inquiry.createdAt).toLocaleString('ko-KR')}
                  </p>
                </div>
              ))}
            </div>
          </div>

          {/* Chat Panel */}
          <div className="lg:col-span-2 bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden flex flex-col">
            {selectedInquiry ? (
              <>
                {/* Chat Header */}
                <div className="px-4 py-3 border-b border-gray-200 flex items-center justify-between">
                  <div>
                    <h2 className="font-semibold">{selectedInquiry.title}</h2>
                    <p className="text-sm text-gray-600">{selectedInquiry.customerName}</p>
                  </div>
                  <button
                    onClick={handleConvertToTask}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"
                  >
                    업무로 전환
                  </button>
                </div>

                {/* Messages */}
                <div className="flex-1 overflow-y-auto custom-scrollbar p-4 space-y-4">
                  {messages.map((message) => (
                    <div
                      key={message.id}
                      className={`flex ${
                        message.sender === 'AGENT' ? 'justify-end' : 'justify-start'
                      }`}
                    >
                      <div
                        className={`max-w-[70%] rounded-lg p-3 ${
                          message.sender === 'AGENT'
                            ? 'bg-blue-600 text-white'
                            : 'bg-gray-100 text-gray-900'
                        }`}
                      >
                        <p className="text-sm">{message.content}</p>
                        <p
                          className={`text-xs mt-1 ${
                            message.sender === 'AGENT' ? 'text-blue-100' : 'text-gray-500'
                          }`}
                        >
                          {new Date(message.createdAt).toLocaleTimeString('ko-KR')}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Input */}
                <div className="p-4 border-t border-gray-200">
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={newMessage}
                      onChange={(e) => setNewMessage(e.target.value)}
                      onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                      placeholder="메시지를 입력하세요..."
                      className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <button
                      onClick={handleSendMessage}
                      className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                    >
                      전송
                    </button>
                  </div>
                </div>
              </>
            ) : (
              <div className="flex items-center justify-center h-full empty-state">
                문의를 선택해주세요
              </div>
            )}
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
