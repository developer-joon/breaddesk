'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { ErrorMessage } from '@/components/ui/ErrorMessage';
import { EmptyState } from '@/components/ui/EmptyState';
import {
  getTemplates,
  createTemplate,
  deleteTemplate,
  applyTemplate,
} from '@/services/templates';
import type { ReplyTemplateResponse } from '@/types';
import toast from 'react-hot-toast';

export default function TemplatesPage() {
  const [templates, setTemplates] = useState<ReplyTemplateResponse[]>([]);
  const [selectedTemplate, setSelectedTemplate] = useState<ReplyTemplateResponse | null>(null);
  const [appliedContent, setAppliedContent] = useState<string | null>(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterCategory, setFilterCategory] = useState<string | undefined>(undefined);

  // Create form
  const [newTitle, setNewTitle] = useState('');
  const [newCategory, setNewCategory] = useState('');
  const [newContent, setNewContent] = useState('');
  const [isCreating, setIsCreating] = useState(false);

  const fetchTemplateList = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const result = await getTemplates(0, 50, filterCategory);
      setTemplates(result.content);
    } catch (err) {
      console.error('Failed to fetch templates:', err);
      setError('템플릿 목록을 불러오지 못했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, [filterCategory]);

  useEffect(() => {
    fetchTemplateList();
  }, [fetchTemplateList]);

  const categories = Array.from(new Set(templates.map((t) => t.category).filter(Boolean)));

  const handleCreateTemplate = async () => {
    if (!newTitle.trim() || !newContent.trim()) {
      toast.error('제목과 내용을 입력해주세요.');
      return;
    }
    setIsCreating(true);
    try {
      await createTemplate({
        title: newTitle,
        category: newCategory || undefined,
        content: newContent,
      });
      toast.success('템플릿이 생성되었습니다');
      setIsCreateModalOpen(false);
      setNewTitle('');
      setNewCategory('');
      setNewContent('');
      await fetchTemplateList();
    } catch {
      toast.error('템플릿 생성에 실패했습니다.');
    } finally {
      setIsCreating(false);
    }
  };

  const handleDeleteTemplate = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    try {
      await deleteTemplate(id);
      setTemplates(templates.filter((t) => t.id !== id));
      toast.success('템플릿이 삭제되었습니다');
    } catch {
      toast.error('템플릿 삭제에 실패했습니다.');
    }
  };

  const handleApplyTemplate = async (id: number, variables?: Record<string, string>) => {
    try {
      const content = await applyTemplate(id, variables);
      setAppliedContent(content);
      toast.success('템플릿이 적용되었습니다');
    } catch {
      toast.error('템플릿 적용에 실패했습니다.');
    }
  };

  // Extract variables from template content ({{varName}} pattern)
  const extractVariables = (content: string): string[] => {
    const matches = content.match(/\{\{(\w+)\}\}/g);
    if (!matches) return [];
    return [...new Set(matches.map((m) => m.replace(/\{\{|\}\}/g, '')))];
  };

  return (
    <AppLayout>
      <div className="space-y-6">
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

        {/* Template Variables Guide */}
        <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
          <div className="flex items-start gap-3">
            <span className="text-2xl">💡</span>
            <div>
              <h3 className="font-semibold text-purple-900 mb-1">템플릿 변수 사용법</h3>
              <p className="text-sm text-purple-800 mb-2">
                템플릿에 <code className="bg-purple-100 px-1 rounded">{'{{변수명}}'}</code> 형식으로 변수를 넣으면, 사용 시 실제 값으로 대체됩니다.
              </p>
              <p className="text-sm text-purple-800">
                <strong>예시:</strong> &quot;안녕하세요 {'{{고객명}}'} 님, 문의하신 {'{{제품명}}'} 관련 답변입니다.&quot;
              </p>
            </div>
          </div>
        </div>

        {/* Category Filter */}
        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-200">
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => setFilterCategory(undefined)}
              className={`px-4 py-2 rounded-lg transition-colors ${
                !filterCategory
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              전체
            </button>
            {categories.map((category) => (
              <button
                key={category}
                onClick={() => setFilterCategory(category!)}
                className={`px-4 py-2 rounded-lg transition-colors ${
                  filterCategory === category
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                {category}
              </button>
            ))}
          </div>
        </div>

        {isLoading && <LoadingSpinner text="템플릿을 불러오는 중..." />}
        {error && <ErrorMessage message={error} onRetry={fetchTemplateList} />}

        {!isLoading && !error && (
          <>
            {templates.length === 0 ? (
              <EmptyState
                icon="📝"
                title="템플릿이 없습니다"
                description="자주 사용하는 답변을 템플릿으로 만들어보세요."
                action={{ label: '새 템플릿 만들기', onClick: () => setIsCreateModalOpen(true) }}
              />
            ) : (
              <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
                {templates.map((template) => {
                  const variables = extractVariables(template.content);
                  return (
                    <div
                      key={template.id}
                      className="bg-white rounded-lg p-6 shadow-sm border border-gray-200 hover:shadow-md transition-shadow"
                    >
                      <div className="flex items-start justify-between mb-3">
                        <h3 className="font-semibold text-gray-900">{template.title}</h3>
                        {template.category && <Badge>{template.category}</Badge>}
                      </div>

                      <p className="text-sm text-gray-600 mb-4 line-clamp-3">{template.content}</p>

                      {variables.length > 0 && (
                        <div className="mb-4">
                          <p className="text-xs text-gray-500 mb-2">변수:</p>
                          <div className="flex flex-wrap gap-1">
                            {variables.map((v) => (
                              <span
                                key={v}
                                className="px-2 py-1 bg-purple-100 text-purple-700 text-xs rounded"
                              >
                                {`{{${v}}}`}
                              </span>
                            ))}
                          </div>
                        </div>
                      )}

                      <div className="flex items-center justify-between">
                        <span className="text-xs text-gray-400">사용 {template.usageCount}회</span>
                        <div className="flex gap-2">
                          <button
                            onClick={() => setSelectedTemplate(template)}
                            className="px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"
                            title="미리보기 및 변수 입력"
                          >
                            📝 미리보기
                          </button>
                          <button
                            onClick={() => handleDeleteTemplate(template.id)}
                            className="px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm"
                          >
                            삭제
                          </button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </>
        )}

        {/* Template Preview/Apply Modal */}
        {selectedTemplate && (
          <Modal
            isOpen={!!selectedTemplate}
            onClose={() => {
              setSelectedTemplate(null);
              setAppliedContent(null);
            }}
            title="템플릿 미리보기"
            size="md"
          >
            <div className="p-6 space-y-4">
              <div>
                <h3 className="font-semibold mb-2">{selectedTemplate.title}</h3>
                {selectedTemplate.category && <Badge>{selectedTemplate.category}</Badge>}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  {appliedContent ? '적용된 내용' : '원본 내용'}
                </label>
                <div className="p-4 bg-gray-50 rounded-lg whitespace-pre-wrap text-sm">
                  {appliedContent || selectedTemplate.content}
                </div>
              </div>

              {appliedContent ? (
                <button
                  onClick={() => {
                    navigator.clipboard.writeText(appliedContent);
                    toast.success('클립보드에 복사되었습니다');
                  }}
                  className="w-full px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
                >
                  📋 복사하기
                </button>
              ) : (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                  <p className="text-xs text-blue-800">
                    💡 <strong>사용 방법:</strong> 아래 &quot;적용&quot; 버튼을 클릭하면 변수가 채워진 최종 답변을 복사할 수 있습니다.
                  </p>
                </div>
              )}

              <div className="flex gap-2">
                <button
                  onClick={() => handleApplyTemplate(selectedTemplate.id)}
                  className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  적용
                </button>
                <button
                  onClick={() => {
                    setSelectedTemplate(null);
                    setAppliedContent(null);
                  }}
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
              <label className="block text-sm font-medium text-gray-700 mb-1">제목 *</label>
              <input
                type="text"
                value={newTitle}
                onChange={(e) => setNewTitle(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="템플릿 제목을 입력하세요"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">카테고리</label>
              <input
                type="text"
                value={newCategory}
                onChange={(e) => setNewCategory(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="예: 회원가입, 결제, 배송 등"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">내용 *</label>
              <textarea
                rows={6}
                value={newContent}
                onChange={(e) => setNewContent(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="템플릿 내용을 입력하세요. 변수는 {{변수명}} 형식으로 입력합니다."
              />
            </div>

            <div className="flex gap-2">
              <button
                onClick={handleCreateTemplate}
                disabled={isCreating}
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
              >
                {isCreating ? '생성 중...' : '생성'}
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
