'use client';

import React, { useState, useEffect } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { ReplyTemplate } from '@/types';
import toast from 'react-hot-toast';

export default function TemplatesPage() {
  const [templates, setTemplates] = useState<ReplyTemplate[]>([]);
  const [selectedTemplate, setSelectedTemplate] = useState<ReplyTemplate | null>(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    fetchTemplates();
  }, []);

  const fetchTemplates = async () => {
    // Mock data
    const mockTemplates: ReplyTemplate[] = [
      {
        id: '1',
        title: '회원가입 완료 안내',
        category: '회원가입',
        content: '안녕하세요 {{customerName}}님,\n\n회원가입이 완료되었습니다.\n\n감사합니다.',
        variables: ['customerName'],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
      {
        id: '2',
        title: '결제 오류 안내',
        category: '결제',
        content: '{{customerName}}님의 결제 건에 대해 확인 중입니다.\n\n결제 금액: {{amount}}원\n\n빠른 시일 내에 처리하겠습니다.',
        variables: ['customerName', 'amount'],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
      {
        id: '3',
        title: '배송 지연 안내',
        category: '배송',
        content: '주문하신 상품의 배송이 {{reason}} 사유로 지연되고 있습니다.\n\n예상 배송일: {{expectedDate}}\n\n불편을 드려 죄송합니다.',
        variables: ['reason', 'expectedDate'],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    ];
    setTemplates(mockTemplates);
  };

  const categories = Array.from(new Set(templates.map((t) => t.category)));
  const filteredTemplates =
    filter === 'ALL' ? templates : templates.filter((t) => t.category === filter);

  const handleCreateTemplate = () => {
    toast.success('템플릿이 생성되었습니다');
    setIsCreateModalOpen(false);
  };

  const handleDeleteTemplate = (id: string) => {
    if (confirm('정말 삭제하시겠습니까?')) {
      setTemplates(templates.filter((t) => t.id !== id));
      toast.success('템플릿이 삭제되었습니다');
    }
  };

  return (
    <AppLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">답변 템플릿</h1>
            <p className="text-gray-600 mt-1">자주 사용하는 답변을 템플릿으로 관리하세요</p>
          </div>
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            + 새 템플릿
          </button>
        </div>

        {/* Category Filter */}
        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-200">
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => setFilter('ALL')}
              className={`px-4 py-2 rounded-lg transition-colors ${
                filter === 'ALL'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              전체
            </button>
            {categories.map((category) => (
              <button
                key={category}
                onClick={() => setFilter(category)}
                className={`px-4 py-2 rounded-lg transition-colors ${
                  filter === category
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                {category}
              </button>
            ))}
          </div>
        </div>

        {/* Templates Grid */}
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredTemplates.map((template) => (
            <div
              key={template.id}
              className="bg-white rounded-lg p-6 shadow-sm border border-gray-200 hover-lift"
            >
              <div className="flex items-start justify-between mb-3">
                <h3 className="font-semibold text-gray-900">{template.title}</h3>
                <Badge>{template.category}</Badge>
              </div>

              <p className="text-sm text-gray-600 mb-4 line-clamp-3">{template.content}</p>

              {/* Variables */}
              {template.variables.length > 0 && (
                <div className="mb-4">
                  <p className="text-xs text-gray-500 mb-2">변수:</p>
                  <div className="flex flex-wrap gap-1">
                    {template.variables.map((variable) => (
                      <span
                        key={variable}
                        className="px-2 py-1 bg-purple-100 text-purple-700 text-xs rounded"
                      >
                        {`{{${variable}}}`}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {/* Actions */}
              <div className="flex gap-2">
                <button
                  onClick={() => setSelectedTemplate(template)}
                  className="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"
                >
                  사용
                </button>
                <button
                  onClick={() => handleDeleteTemplate(template.id)}
                  className="px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm"
                >
                  삭제
                </button>
              </div>
            </div>
          ))}
        </div>

        {filteredTemplates.length === 0 && (
          <div className="empty-state bg-white rounded-lg shadow-sm border border-gray-200">
            템플릿이 없습니다
          </div>
        )}

        {/* Template Preview Modal */}
        {selectedTemplate && (
          <Modal
            isOpen={!!selectedTemplate}
            onClose={() => setSelectedTemplate(null)}
            title="템플릿 미리보기"
            size="md"
          >
            <div className="p-6 space-y-4">
              <div>
                <h3 className="font-semibold mb-2">{selectedTemplate.title}</h3>
                <Badge>{selectedTemplate.category}</Badge>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">내용</label>
                <div className="p-4 bg-gray-50 rounded-lg whitespace-pre-wrap text-sm">
                  {selectedTemplate.content}
                </div>
              </div>

              {/* Variable Inputs */}
              {selectedTemplate.variables.length > 0 && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">변수 입력</label>
                  <div className="space-y-2">
                    {selectedTemplate.variables.map((variable) => (
                      <div key={variable}>
                        <label className="block text-xs text-gray-600 mb-1">{variable}</label>
                        <input
                          type="text"
                          placeholder={`{{${variable}}}`}
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div className="flex gap-2">
                <button className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
                  적용
                </button>
                <button
                  onClick={() => setSelectedTemplate(null)}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  닫기
                </button>
              </div>
            </div>
          </Modal>
        )}

        {/* Create Template Modal */}
        <Modal
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
          title="새 템플릿 생성"
          size="md"
        >
          <div className="p-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">제목</label>
              <input
                type="text"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="템플릿 제목을 입력하세요"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">카테고리</label>
              <input
                type="text"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="예: 회원가입, 결제, 배송 등"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">내용</label>
              <textarea
                rows={6}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="템플릿 내용을 입력하세요. 변수는 {{변수명}} 형식으로 입력합니다."
              />
              <p className="text-xs text-gray-500 mt-1">
                예: 안녕하세요 {`{{customerName}}`}님, {`{{orderId}}`} 주문이 접수되었습니다.
              </p>
            </div>

            <div className="flex gap-2">
              <button
                onClick={handleCreateTemplate}
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                생성
              </button>
              <button
                onClick={() => setIsCreateModalOpen(false)}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
              >
                취소
              </button>
            </div>
          </div>
        </Modal>
      </div>
    </AppLayout>
  );
}
