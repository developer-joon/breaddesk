'use client';

import React, { useState, useEffect } from 'react';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { channelService } from '@/services/channels';
import type { ChannelResponse } from '@/types';
import toast from 'react-hot-toast';

export function ChannelsSettings() {
  const [channels, setChannels] = useState<ChannelResponse[]>([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingChannel, setEditingChannel] = useState<ChannelResponse | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    type: 'EMAIL' as string,
    enabled: true,
    config: {} as Record<string, string>,
  });

  useEffect(() => {
    loadChannels();
  }, []);

  const loadChannels = async () => {
    try {
      const data = await channelService.list();
      setChannels(data);
    } catch (error) {
      console.error('Failed to load channels:', error);
    }
  };

  const openAddModal = () => {
    setFormData({ name: '', type: 'EMAIL', enabled: true, config: {} });
    setEditingChannel(null);
    setShowAddModal(true);
  };

  const openEditModal = (channel: ChannelResponse) => {
    setFormData({
      name: channel.name,
      type: channel.type,
      enabled: channel.enabled,
      config: channel.config || {},
    });
    setEditingChannel(channel);
    setShowAddModal(true);
  };

  const handleSave = async () => {
    try {
      if (editingChannel) {
        await channelService.update(editingChannel.id, formData);
        toast.success('채널이 수정되었습니다');
      } else {
        await channelService.create(formData);
        toast.success('채널이 추가되었습니다');
      }
      setShowAddModal(false);
      loadChannels();
    } catch (error) {
      console.error('Failed to save channel:', error);
      toast.error('채널 저장에 실패했습니다');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('정말 이 채널을 삭제하시겠습니까?')) return;
    try {
      await channelService.delete(id);
      toast.success('채널이 삭제되었습니다');
      loadChannels();
    } catch (error) {
      console.error('Failed to delete channel:', error);
      toast.error('채널 삭제에 실패했습니다');
    }
  };

  const handleTest = async (id: number) => {
    try {
      const result = await channelService.test(id);
      if (result.success) {
        toast.success(result.message || '연결 테스트 성공');
      } else {
        toast.error(result.message || '연결 테스트 실패');
      }
    } catch (error) {
      console.error('Test failed:', error);
      toast.error('테스트에 실패했습니다');
    }
  };

  const getChannelIcon = (type: string) => {
    const icons: Record<string, string> = {
      EMAIL: '📧',
      WEBCHAT: '💬',
      KAKAO: '💛',
      TELEGRAM: '✈️',
      WEBHOOK: '🔗',
    };
    return icons[type] || '📱';
  };

  const renderConfigForm = () => {
    switch (formData.type) {
      case 'EMAIL':
        return (
          <div className="space-y-3">
            <input
              type="text"
              placeholder="IMAP Host"
              value={formData.config.imapHost || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, imapHost: e.target.value },
                })
              }
              className="w-full px-4 py-2 border rounded-lg"
            />
            <input
              type="number"
              placeholder="IMAP Port (993)"
              value={formData.config.imapPort || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, imapPort: e.target.value },
                })
              }
              className="w-full px-4 py-2 border rounded-lg"
            />
            <input
              type="text"
              placeholder="Username"
              value={formData.config.imapUsername || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, imapUsername: e.target.value },
                })
              }
              className="w-full px-4 py-2 border rounded-lg"
            />
            <input
              type="password"
              placeholder="Password"
              value={formData.config.imapPassword || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, imapPassword: e.target.value },
                })
              }
              className="w-full px-4 py-2 border rounded-lg"
            />
          </div>
        );
      case 'KAKAO':
        return (
          <div className="space-y-3">
            <input
              type="text"
              placeholder="Kakao API Key"
              value={formData.config.kakaoApiKey || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, kakaoApiKey: e.target.value },
                })
              }
              className="w-full px-4 py-2 border rounded-lg"
            />
            <input
              type="text"
              placeholder="Channel ID"
              value={formData.config.kakaoChannelId || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, kakaoChannelId: e.target.value },
                })
              }
              className="w-full px-4 py-2 border rounded-lg"
            />
          </div>
        );
      case 'TELEGRAM':
        return (
          <input
            type="text"
            placeholder="Bot Token"
            value={formData.config.telegramBotToken || ''}
            onChange={(e) =>
              setFormData({
                ...formData,
                config: { ...formData.config, telegramBotToken: e.target.value },
              })
            }
            className="w-full px-4 py-2 border rounded-lg"
          />
        );
      case 'WEBHOOK':
        return (
          <div className="space-y-3">
            <input
              type="text"
              placeholder="Webhook URL"
              value={formData.config.webhookUrl || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, webhookUrl: e.target.value },
                })
              }
              className="w-full px-4 py-2 border rounded-lg"
            />
            <input
              type="text"
              placeholder="Secret (optional)"
              value={formData.config.webhookSecret || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, webhookSecret: e.target.value },
                })
              }
              className="w-full px-4 py-2 border rounded-lg"
            />
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-xl font-semibold">채널 설정</h2>
          <p className="text-sm text-gray-600 mt-1">
            고객 문의를 받을 채널을 관리합니다
          </p>
        </div>
        <button
          onClick={openAddModal}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          + 채널 추가
        </button>
      </div>

      <div className="grid md:grid-cols-2 gap-4">
        {channels.map((channel) => (
          <div
            key={channel.id}
            className="bg-white rounded-lg border border-gray-200 p-6 hover:shadow-md transition-shadow"
          >
            <div className="flex items-start justify-between mb-4">
              <div className="flex items-center gap-3">
                <span className="text-3xl">{getChannelIcon(channel.type)}</span>
                <div>
                  <h3 className="font-semibold text-gray-900">{channel.name}</h3>
                  <p className="text-sm text-gray-600">{channel.type}</p>
                </div>
              </div>
              <Badge variant={channel.enabled ? 'success' : 'default'}>
                {channel.enabled ? 'Active' : 'Disabled'}
              </Badge>
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => handleTest(channel.id)}
                className="flex-1 px-3 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 text-sm"
              >
                테스트
              </button>
              <button
                onClick={() => openEditModal(channel)}
                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm"
              >
                수정
              </button>
              <button
                onClick={() => handleDelete(channel.id)}
                className="px-3 py-2 border border-red-300 text-red-600 rounded-lg hover:bg-red-50 text-sm"
              >
                삭제
              </button>
            </div>
          </div>
        ))}
      </div>

      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title={editingChannel ? '채널 수정' : '채널 추가'}
        size="md"
      >
        <div className="p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">채널 이름</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-4 py-2 border rounded-lg"
              placeholder="예: 고객 지원 이메일"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">채널 유형</label>
            <select
              value={formData.type}
              onChange={(e) =>
                setFormData({ ...formData, type: e.target.value, config: {} })
              }
              className="w-full px-4 py-2 border rounded-lg"
              disabled={!!editingChannel}
            >
              <option value="EMAIL">Email</option>
              <option value="WEBCHAT">Web Chat</option>
              <option value="KAKAO">Kakao Talk</option>
              <option value="TELEGRAM">Telegram</option>
              <option value="WEBHOOK">Webhook</option>
            </select>
          </div>

          {renderConfigForm()}

          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="enabled"
              checked={formData.enabled}
              onChange={(e) => setFormData({ ...formData, enabled: e.target.checked })}
              className="rounded"
            />
            <label htmlFor="enabled" className="text-sm text-gray-700">
              활성화
            </label>
          </div>

          <div className="flex gap-2">
            <button
              onClick={handleSave}
              className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              저장
            </button>
            <button
              onClick={() => setShowAddModal(false)}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
            >
              취소
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
