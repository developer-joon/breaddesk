'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import type { ReplyTemplate } from '@/types';

export default function TemplatesPage() {
  const [templates, setTemplates] = useState<ReplyTemplate[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingTemplate, setEditingTemplate] = useState<ReplyTemplate | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [formData, setFormData] = useState({
    title: '',
    category: '',
    content: '',
  });

  useEffect(() => {
    loadTemplates();
  }, []);

  async function loadTemplates() {
    setLoading(true);
    const response = await api.getTemplates();
    if (response.success && response.data) {
      setTemplates(response.data);
    }
    setLoading(false);
  }

  function handleEdit(template: ReplyTemplate) {
    setEditingTemplate(template);
    setFormData({
      title: template.title,
      category: template.category,
      content: template.content,
    });
    setIsCreating(false);
  }

  function handleCreate() {
    setEditingTemplate(null);
    setFormData({ title: '', category: '', content: '' });
    setIsCreating(true);
  }

  async function handleSave() {
    if (!formData.title || !formData.content) {
      alert('제목과 내용을 입력해주세요.');
      return;
    }

    if (editingTemplate) {
      // 수정
      const response = await api.updateTemplate(editingTemplate.id, formData);
      if (response.success) {
        await loadTemplates();
        setEditingTemplate(null);
        setFormData({ title: '', category: '', content: '' });
      }
    } else {
      // 생성
      const response = await api.createTemplate(formData);
      if (response.success) {
        await loadTemplates();
        setIsCreating(false);
        setFormData({ title: '', category: '', content: '' });
      }
    }
  }

  async function handleDelete(id: number) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    const response = await api.deleteTemplate(id);
    if (response.success) {
      await loadTemplates();
    }
  }

  function handleCancel() {
    setEditingTemplate(null);
    setIsCreating(false);
    setFormData({ title: '', category: '', content: '' });
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">답변 템플릿</h1>
          <p className="text-gray-600 mt-1">자주 사용하는 답변을 템플릿으로 관리하세요</p>
        </div>
        <button
          onClick={handleCreate}
          className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
        >
          + 템플릿 추가
        </button>
      </div>

      {/* 템플릿 생성/수정 폼 */}
      {(isCreating || editingTemplate) && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            {editingTemplate ? '템플릿 수정' : '새 템플릿'}
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                제목 *
              </label>
              <input
                type="text"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                placeholder="VPN 접속 가이드"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                카테고리
              </label>
              <input
                type="text"
                value={formData.category}
                onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                placeholder="네트워크, 권한, 배포 등"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                내용 * <span className="text-gray-500 text-xs">(변수: {`{{이름}}, {{서버명}}`} 등)</span>
              </label>
              <textarea
                value={formData.content}
                onChange={(e) => setFormData({ ...formData, content: e.target.value })}
                rows={8}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none font-mono text-sm"
                placeholder={`안녕하세요 {{이름}}님,\n\nVPN 접속 방법은 다음과 같습니다:\n1. 클라이언트 다운로드\n2. 계정 정보 입력\n3. 연결\n\n문의사항이 있으시면 말씀해주세요.`}
              />
            </div>

            <div className="flex justify-end space-x-3">
              <button
                onClick={handleCancel}
                className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
              >
                취소
              </button>
              <button
                onClick={handleSave}
                className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
              >
                저장
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 템플릿 목록 */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center h-64">
            <div className="text-gray-500">로딩 중...</div>
          </div>
        ) : templates.length === 0 ? (
          <div className="flex items-center justify-center h-64">
            <div className="text-gray-500">템플릿이 없습니다</div>
          </div>
        ) : (
          <div className="divide-y divide-gray-200">
            {templates.map((template) => (
              <div key={template.id} className="p-6">
                <div className="flex items-start justify-between mb-3">
                  <div className="flex-1 min-w-0">
                    <h3 className="text-lg font-medium text-gray-900 mb-1">
                      {template.title}
                    </h3>
                    {template.category && (
                      <span className="inline-block px-2 py-1 bg-blue-100 text-blue-700 text-xs font-medium rounded">
                        {template.category}
                      </span>
                    )}
                  </div>
                  <div className="flex space-x-2 ml-4">
                    <button
                      onClick={() => handleEdit(template)}
                      className="px-3 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300 transition-colors"
                    >
                      수정
                    </button>
                    <button
                      onClick={() => handleDelete(template.id)}
                      className="px-3 py-1 text-sm bg-red-100 text-red-700 rounded hover:bg-red-200 transition-colors"
                    >
                      삭제
                    </button>
                  </div>
                </div>

                <div className="bg-gray-50 rounded p-4 mb-3">
                  <pre className="text-sm text-gray-700 whitespace-pre-wrap font-sans">
                    {template.content}
                  </pre>
                </div>

                <div className="flex items-center justify-between text-xs text-gray-500">
                  <span>사용 횟수: {template.usageCount}회</span>
                  <span>생성: {new Date(template.createdAt).toLocaleDateString('ko-KR')}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
