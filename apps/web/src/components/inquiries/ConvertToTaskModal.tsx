'use client';

import React, { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { generateTaskPreview, convertInquiryToTask } from '@/services/inquiries';
import { getTeams } from '@/services/teams';
import type {
  TaskPreviewResponse,
  ConvertToTaskRequest,
  TaskUrgency,
  TeamResponse,
} from '@/types';
import toast from 'react-hot-toast';

interface ConvertToTaskModalProps {
  inquiryId: number;
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: (taskId: number) => void;
}

export function ConvertToTaskModal({
  inquiryId,
  isOpen,
  onClose,
  onSuccess,
}: ConvertToTaskModalProps) {
  const [preview, setPreview] = useState<TaskPreviewResponse | null>(null);
  const [isLoadingPreview, setIsLoadingPreview] = useState(false);
  const [isConverting, setIsConverting] = useState(false);
  const [teams, setTeams] = useState<TeamResponse[]>([]);

  // Editable fields
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [checklist, setChecklist] = useState<string[]>([]);
  const [urgency, setUrgency] = useState<TaskUrgency>('NORMAL');
  const [teamId, setTeamId] = useState<number | undefined>();
  const [type, setType] = useState('GENERAL');

  useEffect(() => {
    if (isOpen) {
      loadPreview();
      loadTeams();
    }
  }, [isOpen, inquiryId]);

  useEffect(() => {
    if (preview) {
      setTitle(preview.title);
      setDescription(preview.description);
      setChecklist(preview.checklist);
      setUrgency((preview.urgency as TaskUrgency) || 'NORMAL');
      setType(preview.category || 'GENERAL');
    }
  }, [preview]);

  const loadPreview = async () => {
    setIsLoadingPreview(true);
    try {
      const data = await generateTaskPreview(inquiryId);
      setPreview(data);
    } catch (error) {
      console.error('Failed to generate task preview:', error);
      toast.error('AI 태스크 미리보기 생성 실패');
    } finally {
      setIsLoadingPreview(false);
    }
  };

  const loadTeams = async () => {
    try {
      const data = await getTeams();
      setTeams(data);
      if (data.length > 0 && !teamId) {
        setTeamId(data[0].id);
      }
    } catch (error) {
      console.error('Failed to load teams:', error);
    }
  };

  const handleConvert = async () => {
    if (!title.trim()) {
      toast.error('제목을 입력해주세요');
      return;
    }

    setIsConverting(true);
    try {
      const request: ConvertToTaskRequest = {
        title,
        description,
        urgency,
        type,
        teamId,
      };

      const task = await convertInquiryToTask(inquiryId, request);
      toast.success('태스크로 전환되었습니다');
      onSuccess?.(task.id);
      onClose();
    } catch (error) {
      console.error('Failed to convert to task:', error);
      toast.error('태스크 전환 실패');
    } finally {
      setIsConverting(false);
    }
  };

  const handleChecklistChange = (index: number, value: string) => {
    const updated = [...checklist];
    updated[index] = value;
    setChecklist(updated);
  };

  const addChecklistItem = () => {
    setChecklist([...checklist, '']);
  };

  const removeChecklistItem = (index: number) => {
    setChecklist(checklist.filter((_, i) => i !== index));
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="문의 → 태스크 전환">
      <div className="space-y-4">
        {isLoadingPreview ? (
          <div className="flex justify-center py-8">
            <LoadingSpinner size="lg" />
          </div>
        ) : (
          <>
            {/* Title */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                제목 *
              </label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="태스크 제목"
              />
            </div>

            {/* Description */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                설명
              </label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={4}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="상세 설명"
              />
            </div>

            {/* Urgency */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                긴급도
              </label>
              <select
                value={urgency}
                onChange={(e) => setUrgency(e.target.value as TaskUrgency)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="LOW">낮음</option>
                <option value="NORMAL">보통</option>
                <option value="HIGH">높음</option>
                <option value="CRITICAL">긴급</option>
              </select>
            </div>

            {/* Type */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                유형
              </label>
              <input
                type="text"
                value={type}
                onChange={(e) => setType(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="예: GENERAL, TECHNICAL, ACCOUNT"
              />
            </div>

            {/* Team */}
            {teams.length > 0 && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  담당 팀
                </label>
                <select
                  value={teamId || ''}
                  onChange={(e) =>
                    setTeamId(e.target.value ? Number(e.target.value) : undefined)
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">선택 안 함</option>
                  {teams.map((team) => (
                    <option key={team.id} value={team.id}>
                      {team.name}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {/* Checklist */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                체크리스트
              </label>
              <div className="space-y-2">
                {checklist.map((item, index) => (
                  <div key={index} className="flex gap-2">
                    <input
                      type="text"
                      value={item}
                      onChange={(e) => handleChecklistChange(index, e.target.value)}
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder={`단계 ${index + 1}`}
                    />
                    <button
                      onClick={() => removeChecklistItem(index)}
                      className="px-3 py-2 text-red-600 hover:bg-red-50 rounded-lg"
                    >
                      ✕
                    </button>
                  </div>
                ))}
                <button
                  onClick={addChecklistItem}
                  className="w-full px-3 py-2 border border-dashed border-gray-300 rounded-lg text-gray-600 hover:bg-gray-50"
                >
                  + 항목 추가
                </button>
              </div>
            </div>

            {/* Actions */}
            <div className="flex justify-end gap-2 pt-4 border-t">
              <button
                onClick={onClose}
                className="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg"
                disabled={isConverting}
              >
                취소
              </button>
              <button
                onClick={handleConvert}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
                disabled={isConverting || !title.trim()}
              >
                {isConverting ? '전환 중...' : '태스크 생성'}
              </button>
            </div>
          </>
        )}
      </div>
    </Modal>
  );
}
