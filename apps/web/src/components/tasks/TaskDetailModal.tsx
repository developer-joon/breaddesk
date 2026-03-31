'use client';

import { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Badge } from '@/components/ui/Badge';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import {
  getAssigneeRecommendations,
  isWatching,
  watchTask,
  unwatchTask,
  getTaskRelations,
  addTaskRelation,
  deleteTaskRelation,
  getInternalComments,
  addComment,
  updateTask,
} from '@/services/tasks';
import { getMembers } from '@/services/members';
import type {
  TaskResponse,
  AssigneeRecommendation,
  TaskRelationResponse,
  TaskCommentResponse,
  User,
} from '@/types';
import toast from 'react-hot-toast';

interface TaskDetailModalProps {
  task: TaskResponse;
  onClose: () => void;
  onUpdate: () => void;
}

export function TaskDetailModal({ task, onClose, onUpdate }: TaskDetailModalProps) {
  const [activeTab, setActiveTab] = useState<'details' | 'comments' | 'internal' | 'relations'>(
    'details',
  );
  const [watching, setWatching] = useState(false);
  const [recommendations, setRecommendations] = useState<AssigneeRecommendation[]>([]);
  const [showRecommendations, setShowRecommendations] = useState(false);
  const [relations, setRelations] = useState<TaskRelationResponse[]>([]);
  const [internalComments, setInternalComments] = useState<TaskCommentResponse[]>([]);
  const [newComment, setNewComment] = useState('');
  const [newInternalComment, setNewInternalComment] = useState('');
  const [newRelationId, setNewRelationId] = useState('');
  const [allMembers, setAllMembers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    loadWatchingStatus();
    loadRelations();
    loadInternalComments();
    loadMembers();
  }, [task.id]);

  const loadWatchingStatus = async () => {
    try {
      const status = await isWatching(task.id);
      setWatching(status);
    } catch (err) {
      console.error('Failed to load watching status:', err);
    }
  };

  const loadRelations = async () => {
    try {
      const data = await getTaskRelations(task.id);
      setRelations(data);
    } catch (err) {
      console.error('Failed to load relations:', err);
    }
  };

  const loadInternalComments = async () => {
    try {
      const data = await getInternalComments(task.id);
      setInternalComments(data);
    } catch (err) {
      console.error('Failed to load internal comments:', err);
    }
  };

  const loadMembers = async () => {
    try {
      const data = await getMembers();
      setAllMembers(data.content);
    } catch (err) {
      console.error('Failed to load members:', err);
    }
  };

  const handleToggleWatch = async () => {
    try {
      if (watching) {
        await unwatchTask(task.id);
        setWatching(false);
        toast.success('구독 취소되었습니다');
      } else {
        await watchTask(task.id);
        setWatching(true);
        toast.success('구독되었습니다');
      }
    } catch (err) {
      toast.error('구독 설정에 실패했습니다');
    }
  };

  const handleLoadRecommendations = async () => {
    if (recommendations.length > 0) {
      setShowRecommendations(!showRecommendations);
      return;
    }

    setIsLoading(true);
    try {
      const data = await getAssigneeRecommendations(task.id);
      setRecommendations(data);
      setShowRecommendations(true);
    } catch (err) {
      toast.error('추천 로드에 실패했습니다');
    } finally {
      setIsLoading(false);
    }
  };

  const handleAssignRecommended = async (memberId: number) => {
    try {
      await updateTask(task.id, { ...task, assigneeId: memberId });
      toast.success('담당자가 할당되었습니다');
      onUpdate();
      setShowRecommendations(false);
    } catch (err) {
      toast.error('할당에 실패했습니다');
    }
  };

  const handleAddRelation = async () => {
    if (!newRelationId.trim()) return;
    const relatedTaskId = parseInt(newRelationId);
    if (isNaN(relatedTaskId)) {
      toast.error('올바른 업무 ID를 입력하세요');
      return;
    }

    try {
      await addTaskRelation(task.id, { relatedTaskId });
      toast.success('연결되었습니다');
      setNewRelationId('');
      await loadRelations();
    } catch (err) {
      toast.error('연결에 실패했습니다');
    }
  };

  const handleDeleteRelation = async (relationId: number) => {
    try {
      await deleteTaskRelation(task.id, relationId);
      toast.success('연결이 해제되었습니다');
      await loadRelations();
    } catch (err) {
      toast.error('연결 해제에 실패했습니다');
    }
  };

  const handleAddComment = async () => {
    if (!newComment.trim()) return;
    try {
      await addComment(task.id, { content: newComment, internal: false });
      setNewComment('');
      toast.success('댓글이 추가되었습니다');
      onUpdate();
    } catch (err) {
      toast.error('댓글 추가에 실패했습니다');
    }
  };

  const handleAddInternalComment = async () => {
    if (!newInternalComment.trim()) return;
    try {
      await addComment(task.id, { content: newInternalComment, internal: true });
      setNewInternalComment('');
      toast.success('내부 메모가 추가되었습니다');
      await loadInternalComments();
    } catch (err) {
      toast.error('내부 메모 추가에 실패했습니다');
    }
  };

  return (
    <Modal isOpen onClose={onClose} title={`업무 상세 #${task.id}`} size="xl">
      <div className="p-6">
        {/* Header Actions */}
        <div className="mb-4 flex items-center gap-2 flex-wrap">
          <button
            onClick={handleToggleWatch}
            className={`px-3 py-1 rounded-lg text-sm font-medium transition-colors ${
              watching
                ? 'bg-blue-600 text-white hover:bg-blue-700'
                : 'bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600'
            }`}
          >
            {watching ? '👁 구독중' : '👁‍🗨 구독'}
          </button>

          <button
            onClick={handleLoadRecommendations}
            className="px-3 py-1 bg-purple-600 text-white rounded-lg text-sm font-medium hover:bg-purple-700 transition-colors"
          >
            🤖 AI 추천
          </button>
        </div>

        {/* AI Recommendations */}
        {showRecommendations && (
          <div className="mb-4 p-4 bg-purple-50 dark:bg-purple-900/20 rounded-lg border border-purple-200 dark:border-purple-700">
            <h3 className="font-semibold mb-3 text-purple-900 dark:text-purple-100">
              추천 담당자
            </h3>
            {isLoading ? (
              <LoadingSpinner size="sm" />
            ) : recommendations.length === 0 ? (
              <p className="text-sm text-gray-600 dark:text-gray-400">추천 담당자가 없습니다</p>
            ) : (
              <div className="space-y-2">
                {recommendations.map((rec) => (
                  <div
                    key={rec.memberId}
                    className="flex items-center justify-between p-2 bg-white dark:bg-gray-800 rounded"
                  >
                    <div>
                      <div className="font-medium text-sm">{rec.memberName}</div>
                      <div className="text-xs text-gray-600 dark:text-gray-400">{rec.reason}</div>
                      <div className="text-xs text-purple-600 dark:text-purple-400">
                        신뢰도: {(rec.score * 100).toFixed(0)}%
                      </div>
                    </div>
                    <button
                      onClick={() => handleAssignRecommended(rec.memberId)}
                      className="px-3 py-1 bg-blue-600 text-white rounded text-sm hover:bg-blue-700"
                    >
                      할당
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Task Info */}
        <div className="mb-6">
          <h2 className="text-xl font-bold mb-2 text-gray-900 dark:text-white">{task.title}</h2>
          <div className="flex gap-2 flex-wrap">
            <Badge variant={task.urgency === 'CRITICAL' ? 'danger' : 'info'}>
              {task.urgency}
            </Badge>
            <Badge>{task.status}</Badge>
            {task.assigneeId && <Badge variant="success">담당자 할당됨</Badge>}
          </div>
          {task.description && (
            <p className="mt-4 text-gray-700 dark:text-gray-300 whitespace-pre-wrap">
              {task.description}
            </p>
          )}
        </div>

        {/* Tabs */}
        <div className="border-b border-gray-200 dark:border-gray-700 mb-4">
          <div className="flex gap-4">
            <button
              onClick={() => setActiveTab('details')}
              className={`pb-2 px-1 font-medium text-sm transition-colors ${
                activeTab === 'details'
                  ? 'border-b-2 border-blue-600 text-blue-600'
                  : 'text-gray-600 dark:text-gray-400'
              }`}
            >
              상세 정보
            </button>
            <button
              onClick={() => setActiveTab('comments')}
              className={`pb-2 px-1 font-medium text-sm transition-colors ${
                activeTab === 'comments'
                  ? 'border-b-2 border-blue-600 text-blue-600'
                  : 'text-gray-600 dark:text-gray-400'
              }`}
            >
              댓글 ({task.comments?.length || 0})
            </button>
            <button
              onClick={() => setActiveTab('internal')}
              className={`pb-2 px-1 font-medium text-sm transition-colors ${
                activeTab === 'internal'
                  ? 'border-b-2 border-yellow-600 text-yellow-600'
                  : 'text-gray-600 dark:text-gray-400'
              }`}
            >
              내부 메모 ({internalComments.length})
            </button>
            <button
              onClick={() => setActiveTab('relations')}
              className={`pb-2 px-1 font-medium text-sm transition-colors ${
                activeTab === 'relations'
                  ? 'border-b-2 border-blue-600 text-blue-600'
                  : 'text-gray-600 dark:text-gray-400'
              }`}
            >
              연결된 업무 ({relations.length})
            </button>
          </div>
        </div>

        {/* Tab Content */}
        <div className="min-h-[300px]">
          {activeTab === 'details' && (
            <div className="space-y-4">
              {task.checklists && task.checklists.length > 0 && (
                <div>
                  <h3 className="font-semibold mb-2">체크리스트</h3>
                  <div className="space-y-2">
                    {task.checklists.map((item) => (
                      <div key={item.id} className="flex items-center gap-2">
                        <input
                          type="checkbox"
                          checked={item.done}
                          readOnly
                          className="w-4 h-4"
                        />
                        <span
                          className={`text-sm ${item.done ? 'line-through text-gray-500' : ''}`}
                        >
                          {item.itemText}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {task.tags && task.tags.length > 0 && (
                <div>
                  <h3 className="font-semibold mb-2">태그</h3>
                  <div className="flex gap-2 flex-wrap">
                    {task.tags.map((tag) => (
                      <Badge key={tag.id}>{tag.tag}</Badge>
                    ))}
                  </div>
                </div>
              )}

              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="text-gray-600 dark:text-gray-400">생성일:</span>
                  <p className="font-medium">{new Date(task.createdAt).toLocaleString('ko-KR')}</p>
                </div>
                {task.dueDate && (
                  <div>
                    <span className="text-gray-600 dark:text-gray-400">마감일:</span>
                    <p className="font-medium">{new Date(task.dueDate).toLocaleString('ko-KR')}</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {activeTab === 'comments' && (
            <div>
              <div className="space-y-3 mb-4 max-h-[400px] overflow-y-auto">
                {task.comments && task.comments.length > 0 ? (
                  task.comments
                    .filter((c) => !c.internal)
                    .map((comment) => (
                      <div
                        key={comment.id}
                        className="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
                      >
                        <div className="text-sm font-medium mb-1">
                          담당자 #{comment.authorId}
                        </div>
                        <p className="text-sm text-gray-700 dark:text-gray-300">
                          {comment.content}
                        </p>
                        <p className="text-xs text-gray-500 mt-1">
                          {new Date(comment.createdAt).toLocaleString('ko-KR')}
                        </p>
                      </div>
                    ))
                ) : (
                  <p className="text-sm text-gray-500 text-center py-8">댓글이 없습니다</p>
                )}
              </div>

              <div className="flex gap-2">
                <input
                  type="text"
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleAddComment()}
                  placeholder="댓글 추가..."
                  className="flex-1 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                />
                <button
                  onClick={handleAddComment}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  추가
                </button>
              </div>
            </div>
          )}

          {activeTab === 'internal' && (
            <div>
              <div className="space-y-3 mb-4 max-h-[400px] overflow-y-auto">
                {internalComments.length > 0 ? (
                  internalComments.map((comment) => (
                    <div
                      key={comment.id}
                      className="p-3 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg border border-yellow-200 dark:border-yellow-700"
                    >
                      <div className="text-sm font-medium mb-1 text-yellow-900 dark:text-yellow-100">
                        🔒 내부 메모 - 담당자 #{comment.authorId}
                      </div>
                      <p className="text-sm text-gray-700 dark:text-gray-300">{comment.content}</p>
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                        {new Date(comment.createdAt).toLocaleString('ko-KR')}
                      </p>
                    </div>
                  ))
                ) : (
                  <p className="text-sm text-gray-500 text-center py-8">내부 메모가 없습니다</p>
                )}
              </div>

              <div className="flex gap-2">
                <input
                  type="text"
                  value={newInternalComment}
                  onChange={(e) => setNewInternalComment(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleAddInternalComment()}
                  placeholder="내부 메모 추가... (팀 내부에만 표시)"
                  className="flex-1 px-3 py-2 border border-yellow-300 dark:border-yellow-600 rounded-lg bg-yellow-50 dark:bg-yellow-900/20"
                />
                <button
                  onClick={handleAddInternalComment}
                  className="px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700"
                >
                  추가
                </button>
              </div>
            </div>
          )}

          {activeTab === 'relations' && (
            <div>
              <div className="space-y-2 mb-4">
                {relations.length > 0 ? (
                  relations.map((rel) => (
                    <div
                      key={rel.id}
                      className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
                    >
                      <div>
                        <div className="font-medium">#{rel.relatedTaskId}</div>
                        <div className="text-sm text-gray-600 dark:text-gray-400">
                          {rel.relatedTaskTitle}
                        </div>
                      </div>
                      <button
                        onClick={() => handleDeleteRelation(rel.id)}
                        className="text-red-600 hover:text-red-700 text-sm"
                      >
                        🗑️ 해제
                      </button>
                    </div>
                  ))
                ) : (
                  <p className="text-sm text-gray-500 text-center py-8">
                    연결된 업무가 없습니다
                  </p>
                )}
              </div>

              <div className="flex gap-2">
                <input
                  type="number"
                  value={newRelationId}
                  onChange={(e) => setNewRelationId(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleAddRelation()}
                  placeholder="연결할 업무 ID 입력..."
                  className="flex-1 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700"
                />
                <button
                  onClick={handleAddRelation}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  연결
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </Modal>
  );
}
