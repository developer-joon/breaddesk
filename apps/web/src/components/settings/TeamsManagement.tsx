'use client';

import React, { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { ErrorMessage } from '@/components/ui/ErrorMessage';
import { getTeams, createTeam, updateTeam, deleteTeam, addTeamMember, removeTeamMember, type Team } from '@/services/teams';
import { getMembers } from '@/services/members';
import type { User } from '@/types';
import toast from 'react-hot-toast';

export function TeamsManagement() {
  const [teams, setTeams] = useState<Team[]>([]);
  const [allUsers, setAllUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Team Modal
  const [showTeamModal, setShowTeamModal] = useState(false);
  const [editingTeam, setEditingTeam] = useState<Team | null>(null);
  const [teamForm, setTeamForm] = useState({
    name: '',
    description: '',
  });

  // Member Modal
  const [showMemberModal, setShowMemberModal] = useState(false);
  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);

  const loadData = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [teamsData, usersData] = await Promise.all([getTeams(), getMembers()]);
      setTeams(teamsData);
      setAllUsers(usersData);
    } catch (err) {
      console.error('Failed to load teams:', err);
      setError('팀 데이터를 불러오지 못했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreateTeam = () => {
    setEditingTeam(null);
    setTeamForm({ name: '', description: '' });
    setShowTeamModal(true);
  };

  const handleEditTeam = (team: Team) => {
    setEditingTeam(team);
    setTeamForm({ name: team.name, description: team.description || '' });
    setShowTeamModal(true);
  };

  const handleSaveTeam = async () => {
    if (!teamForm.name.trim()) {
      toast.error('팀 이름을 입력하세요.');
      return;
    }

    try {
      if (editingTeam) {
        const updated = await updateTeam(editingTeam.id, {
          name: teamForm.name.trim(),
          description: teamForm.description.trim() || undefined,
        });
        setTeams((prev) => prev.map((t) => (t.id === editingTeam.id ? updated : t)));
        toast.success('팀이 수정되었습니다.');
      } else {
        const created = await createTeam({
          name: teamForm.name.trim(),
          description: teamForm.description.trim() || undefined,
        });
        setTeams((prev) => [...prev, created]);
        toast.success('팀이 생성되었습니다.');
      }
      setShowTeamModal(false);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : '팀 저장에 실패했습니다.';
      toast.error(message);
    }
  };

  const handleDeleteTeam = async (teamId: number) => {
    if (!confirm('정말로 이 팀을 삭제하시겠습니까?')) return;

    try {
      await deleteTeam(teamId);
      setTeams((prev) => prev.filter((t) => t.id !== teamId));
      toast.success('팀이 삭제되었습니다.');
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : '팀 삭제에 실패했습니다.';
      toast.error(message);
    }
  };

  const handleAddMember = (team: Team) => {
    setSelectedTeam(team);
    setSelectedUserId(null);
    setShowMemberModal(true);
  };

  const handleSaveMember = async () => {
    if (!selectedTeam || !selectedUserId) {
      toast.error('사용자를 선택하세요.');
      return;
    }

    try {
      await addTeamMember(selectedTeam.id, { userId: selectedUserId });
      await loadData();
      toast.success('팀원이 추가되었습니다.');
      setShowMemberModal(false);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : '팀원 추가에 실패했습니다.';
      toast.error(message);
    }
  };

  const handleRemoveMember = async (teamId: number, userId: number) => {
    if (!confirm('이 팀원을 제거하시겠습니까?')) return;

    try {
      await removeTeamMember(teamId, userId);
      await loadData();
      toast.success('팀원이 제거되었습니다.');
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : '팀원 제거에 실패했습니다.';
      toast.error(message);
    }
  };

  if (isLoading) {
    return <LoadingSpinner text="팀 데이터를 불러오는 중..." />;
  }

  if (error) {
    return <ErrorMessage message={error} />;
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2 className="text-lg font-semibold">🏢 팀 관리</h2>
          <p className="text-sm text-gray-600 mt-1">
            팀을 생성하고 멤버를 관리합니다.
          </p>
        </div>
        <button
          onClick={handleCreateTeam}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
        >
          + 팀 추가
        </button>
      </div>

      {teams.length === 0 ? (
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-6 text-center">
          <p className="text-gray-600">생성된 팀이 없습니다.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {teams.map((team) => (
            <div key={team.id} className="border border-gray-200 rounded-lg p-4">
              <div className="flex items-start justify-between mb-3">
                <div>
                  <h3 className="text-lg font-semibold">{team.name}</h3>
                  {team.description && (
                    <p className="text-sm text-gray-600 mt-1">{team.description}</p>
                  )}
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => handleEditTeam(team)}
                    className="text-blue-600 hover:text-blue-800 text-sm"
                  >
                    수정
                  </button>
                  <button
                    onClick={() => handleDeleteTeam(team.id)}
                    className="text-red-600 hover:text-red-800 text-sm"
                  >
                    삭제
                  </button>
                </div>
              </div>

              <div className="border-t pt-3">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-gray-700">
                    팀원 ({team.members.length}명)
                  </span>
                  <button
                    onClick={() => handleAddMember(team)}
                    className="text-sm text-blue-600 hover:text-blue-800"
                  >
                    + 추가
                  </button>
                </div>
                {team.members.length === 0 ? (
                  <p className="text-sm text-gray-500">팀원이 없습니다.</p>
                ) : (
                  <div className="space-y-2">
                    {team.members.map((member) => (
                      <div
                        key={member.id}
                        className="flex items-center justify-between bg-gray-50 rounded p-2"
                      >
                        <div>
                          <p className="text-sm font-medium">{member.userName}</p>
                          <p className="text-xs text-gray-500">{member.userEmail}</p>
                        </div>
                        <button
                          onClick={() => handleRemoveMember(team.id, member.userId)}
                          className="text-xs text-red-600 hover:text-red-800"
                        >
                          제거
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Team Modal */}
      {showTeamModal && (
        <Modal
          isOpen={showTeamModal}
          onClose={() => setShowTeamModal(false)}
          title={editingTeam ? '팀 수정' : '팀 추가'}
        >
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1">팀 이름 *</label>
              <input
                type="text"
                value={teamForm.name}
                onChange={(e) => setTeamForm({ ...teamForm, name: e.target.value })}
                className="w-full border border-gray-300 rounded px-3 py-2"
                placeholder="팀 이름을 입력하세요"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">설명</label>
              <textarea
                value={teamForm.description}
                onChange={(e) => setTeamForm({ ...teamForm, description: e.target.value })}
                className="w-full border border-gray-300 rounded px-3 py-2"
                rows={3}
                placeholder="팀 설명 (선택사항)"
              />
            </div>
            <div className="flex gap-2 justify-end">
              <button
                onClick={() => setShowTeamModal(false)}
                className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50"
              >
                취소
              </button>
              <button
                onClick={handleSaveTeam}
                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                저장
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* Member Modal */}
      {showMemberModal && selectedTeam && (
        <Modal
          isOpen={showMemberModal}
          onClose={() => setShowMemberModal(false)}
          title={`${selectedTeam.name} - 팀원 추가`}
        >
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1">팀원 선택 *</label>
              <select
                value={selectedUserId || ''}
                onChange={(e) => setSelectedUserId(Number(e.target.value))}
                className="w-full border border-gray-300 rounded px-3 py-2"
              >
                <option value="">선택하세요</option>
                {allUsers
                  .filter(
                    (user) =>
                      !selectedTeam.members.some((m) => Number(m.userId) === Number(user.id)),
                  )
                  .map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.name} ({user.email})
                    </option>
                  ))}
              </select>
            </div>
            <div className="flex gap-2 justify-end">
              <button
                onClick={() => setShowMemberModal(false)}
                className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50"
              >
                취소
              </button>
              <button
                onClick={handleSaveMember}
                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                추가
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
}
