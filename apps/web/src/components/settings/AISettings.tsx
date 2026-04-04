'use client';

import React, { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import api from '@/lib/api';

interface PromptConfig {
  id: number;
  key: string;
  name: string;
  promptTemplate: string;
  description?: string;
  active: boolean;
}

const TONE_OPTIONS = [
  { value: 'friendly', label: '친절한 톤', emoji: '😊' },
  { value: 'professional', label: '공식적인 톤', emoji: '💼' },
  { value: 'concise', label: '간결한 톤', emoji: '📝' },
];

export function AISettings() {
  const [prompts, setPrompts] = useState<PromptConfig[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedPrompt, setSelectedPrompt] = useState<PromptConfig | null>(null);
  const [editingTemplate, setEditingTemplate] = useState('');
  const [isSaving, setIsSaving] = useState(false);

  // Custom settings (stored as separate prompt configs)
  const [confidenceThreshold, setConfidenceThreshold] = useState(0.7);
  const [selectedTone, setSelectedTone] = useState('friendly');

  useEffect(() => {
    loadPrompts();
  }, []);

  const loadPrompts = async () => {
    setIsLoading(true);
    try {
      const response = await api.get<{ success: boolean; data: PromptConfig[] }>('/ai/prompts');
      if (response.data.success) {
        setPrompts(response.data.data);
        // Load main answer prompt if exists
        const mainPrompt = response.data.data.find((p) => p.key === 'ai_answer');
        if (mainPrompt) {
          setSelectedPrompt(mainPrompt);
          setEditingTemplate(mainPrompt.promptTemplate);
        }
      }
    } catch (error) {
      console.error('Failed to load prompts:', error);
      toast.error('AI 프롬프트 설정을 불러오지 못했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelectPrompt = (prompt: PromptConfig) => {
    setSelectedPrompt(prompt);
    setEditingTemplate(prompt.promptTemplate);
  };

  const handleSavePrompt = async () => {
    if (!selectedPrompt) return;

    setIsSaving(true);
    try {
      await api.put(`/ai/prompts/${selectedPrompt.id}`, {
        key: selectedPrompt.key,
        name: selectedPrompt.name,
        promptTemplate: editingTemplate,
        description: selectedPrompt.description,
        active: selectedPrompt.active,
      });
      toast.success('프롬프트가 저장되었습니다');
      loadPrompts();
    } catch (error) {
      console.error('Failed to save prompt:', error);
      toast.error('프롬프트 저장에 실패했습니다');
    } finally {
      setIsSaving(false);
    }
  };

  const handleResetPrompt = () => {
    if (selectedPrompt) {
      setEditingTemplate(selectedPrompt.promptTemplate);
      toast('프롬프트가 초기화되었습니다', { icon: 'ℹ️' });
    }
  };

  if (isLoading) {
    return <LoadingSpinner text="AI 설정을 불러오는 중..." />;
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-lg font-semibold">🤖 AI 설정</h2>
        <p className="text-sm text-gray-600 mt-1">
          AI 자동 응답 시스템의 프롬프트와 동작을 커스터마이징합니다.
        </p>
      </div>

      {/* Prompt Selection */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          프롬프트 선택
        </label>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {prompts.map((prompt) => (
            <button
              key={prompt.id}
              onClick={() => handleSelectPrompt(prompt)}
              className={`text-left p-3 border rounded-lg transition-colors ${
                selectedPrompt?.id === prompt.id
                  ? 'border-blue-500 bg-blue-50'
                  : 'border-gray-200 hover:border-gray-300'
              }`}
            >
              <div className="font-medium text-gray-900">{prompt.name}</div>
              <div className="text-xs text-gray-500 mt-1">
                {prompt.description || prompt.key}
              </div>
            </button>
          ))}
        </div>

        {prompts.length === 0 && (
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <p className="text-sm text-yellow-800">
              등록된 프롬프트가 없습니다. 백엔드에서 기본 프롬프트를 생성해주세요.
            </p>
          </div>
        )}
      </div>

      {/* Prompt Editor */}
      {selectedPrompt && (
        <div className="border border-gray-200 rounded-lg p-4 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-medium text-gray-900">
              {selectedPrompt.name} 편집
            </h3>
            <div className="flex items-center gap-2">
              <button
                onClick={handleResetPrompt}
                className="px-3 py-1.5 text-sm border border-gray-300 rounded-md hover:bg-gray-50"
              >
                🔄 초기화
              </button>
              <button
                onClick={handleSavePrompt}
                disabled={isSaving}
                className="px-3 py-1.5 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                {isSaving ? '저장 중...' : '💾 저장'}
              </button>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              시스템 프롬프트
            </label>
            <textarea
              value={editingTemplate}
              onChange={(e) => setEditingTemplate(e.target.value)}
              rows={12}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono text-sm"
              placeholder="프롬프트를 입력하세요..."
            />
            <p className="text-xs text-gray-500 mt-1">
              💡 변수: <code>{'{{inquiry}}'}</code>, <code>{'{{knowledge}}'}</code>, <code>{'{{history}}'}</code> 등을 사용할 수 있습니다.
            </p>
          </div>
        </div>
      )}

      {/* Response Tone */}
      <div className="border border-gray-200 rounded-lg p-4 space-y-4">
        <h3 className="font-medium text-gray-900">응답 톤 설정</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {TONE_OPTIONS.map((tone) => (
            <button
              key={tone.value}
              onClick={() => setSelectedTone(tone.value)}
              className={`p-3 border rounded-lg transition-colors ${
                selectedTone === tone.value
                  ? 'border-blue-500 bg-blue-50'
                  : 'border-gray-200 hover:border-gray-300'
              }`}
            >
              <div className="text-2xl mb-1">{tone.emoji}</div>
              <div className="text-sm font-medium">{tone.label}</div>
            </button>
          ))}
        </div>
        <p className="text-xs text-gray-500">
          응답 톤은 프롬프트 생성 시 자동으로 반영됩니다.
        </p>
      </div>

      {/* Confidence Threshold */}
      <div className="border border-gray-200 rounded-lg p-4 space-y-4">
        <h3 className="font-medium text-gray-900">AI 신뢰도 임계값</h3>
        <div>
          <div className="flex items-center justify-between mb-2">
            <label className="text-sm text-gray-700">
              자동 응답 임계값
            </label>
            <span className="text-sm font-medium text-blue-600">
              {(confidenceThreshold * 100).toFixed(0)}%
            </span>
          </div>
          <input
            type="range"
            min="0.5"
            max="0.9"
            step="0.05"
            value={confidenceThreshold}
            onChange={(e) => setConfidenceThreshold(parseFloat(e.target.value))}
            className="w-full"
          />
          <div className="flex justify-between text-xs text-gray-500 mt-1">
            <span>50% (낮음)</span>
            <span>70% (권장)</span>
            <span>90% (높음)</span>
          </div>
          <p className="text-xs text-gray-500 mt-2">
            AI 신뢰도가 이 값보다 낮으면 자동 응답하지 않고 담당자에게 에스컬레이션합니다.
          </p>
        </div>

        <button
          onClick={() => toast('신뢰도 설정 저장 기능은 구현 중입니다', { icon: 'ℹ️' })}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          💾 신뢰도 설정 저장
        </button>
      </div>

      {/* Info Box */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-start gap-3">
          <span className="text-xl">ℹ️</span>
          <div className="text-sm text-blue-800">
            <p className="font-semibold mb-1">AI 프롬프트 커스터마이징이란?</p>
            <p>
              프롬프트는 AI가 문의에 답변할 때 참고하는 "지시사항"입니다.
              회사의 톤앤매너, 정책, 응대 가이드를 반영하여 AI 답변의 품질을 높일 수 있습니다.
            </p>
            <ul className="list-disc list-inside mt-2 space-y-1">
              <li>프롬프트 변경은 즉시 반영됩니다</li>
              <li>신뢰도 임계값을 조정하여 정확도와 자동화 비율의 균형을 맞추세요</li>
              <li>톤 설정은 프롬프트 생성 시 자동으로 적용됩니다</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
