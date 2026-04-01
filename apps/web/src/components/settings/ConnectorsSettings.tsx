'use client';

import React, { useState, useEffect } from 'react';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import {
  getConnectors,
  createConnector,
  updateConnector,
  deleteConnector,
  syncConnector,
} from '@/services/knowledge';
import type { KnowledgeConnectorResponse, ConnectorType } from '@/types';
import toast from 'react-hot-toast';

export function ConnectorsSettings() {
  const [connectors, setConnectors] = useState<KnowledgeConnectorResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingConnector, setEditingConnector] = useState<KnowledgeConnectorResponse | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    type: 'CONFLUENCE' as ConnectorType,
    config: {} as Record<string, string>,
  });

  useEffect(() => {
    loadConnectors();
  }, []);

  const loadConnectors = async () => {
    try {
      setLoading(true);
      const data = await getConnectors();
      setConnectors(data);
    } catch (error) {
      console.error('Failed to load connectors:', error);
    } finally {
      setLoading(false);
    }
  };

  const openAddModal = () => {
    setFormData({ name: '', type: 'CONFLUENCE', config: {} });
    setEditingConnector(null);
    setShowAddModal(true);
  };

  const openEditModal = (connector: KnowledgeConnectorResponse) => {
    setFormData({
      name: connector.name,
      type: connector.type,
      config: connector.config || {},
    });
    setEditingConnector(connector);
    setShowAddModal(true);
  };

  const handleSave = async () => {
    try {
      if (editingConnector) {
        await updateConnector(editingConnector.id, formData);
        toast.success('커넥터가 수정되었습니다');
      } else {
        await createConnector(formData);
        toast.success('커넥터가 추가되었습니다');
      }
      setShowAddModal(false);
      loadConnectors();
    } catch (error) {
      console.error('Failed to save connector:', error);
      toast.error('커넥터 저장에 실패했습니다');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('정말 이 커넥터를 삭제하시겠습니까?')) return;
    try {
      await deleteConnector(id);
      toast.success('커넥터가 삭제되었습니다');
      loadConnectors();
    } catch (error) {
      console.error('Failed to delete connector:', error);
      toast.error('커넥터 삭제에 실패했습니다');
    }
  };

  const handleSync = async (id: number) => {
    try {
      await syncConnector(id);
      toast.success('동기화를 시작했습니다');
      setTimeout(loadConnectors, 2000);
    } catch (error) {
      console.error('Sync failed:', error);
      toast.error('동기화에 실패했습니다');
    }
  };

  const getStatusBadge = (status: string) => {
    const variants: Record<string, any> = {
      CONNECTED: 'success',
      DISCONNECTED: 'default',
      SYNCING: 'primary',
      ERROR: 'danger',
    };
    return variants[status] || 'default';
  };

  const getConnectorIcon = (type: string) => {
    const icons: Record<string, string> = {
      NOTION: '📝',
      CONFLUENCE: '🌐',
      GOOGLE_DRIVE: '💾',
      WEB_CRAWL: '🕷️',
      LOCAL: '📁',
    };
    return icons[type] || '📚';
  };

  const renderConfigForm = () => {
    switch (formData.type) {
      case 'CONFLUENCE':
        return (
          <div className="space-y-3">
            <input
              type="text"
              placeholder="Confluence URL (https://your-domain.atlassian.net)"
              value={formData.config.url || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, url: e.target.value },
                })
              }
              className="w-full px-4 py-2 border rounded-lg"
            />
            <input
              type="text"
              placeholder="Space Key"
              value={formData.config.spaceKey || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, spaceKey: e.target.value },
                })
              }
              className="w-full px-4 py-2 border rounded-lg"
            />
            <input
              type="text"
              placeholder="Username"
              value={formData.config.username || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, username: e.target.value },
                })
              }
              className="w-full px-4 py-2 border rounded-lg"
            />
            <input
              type="password"
              placeholder="API Token"
              value={formData.config.apiToken || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, apiToken: e.target.value },
                })
              }
              className="w-full px-4 py-2 border rounded-lg"
            />
          </div>
        );
      case 'NOTION':
        return (
          <div className="space-y-3">
            <input
              type="text"
              placeholder="Notion API Token"
              value={formData.config.apiToken || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, apiToken: e.target.value },
                })
              }
              className="w-full px-4 py-2 border rounded-lg"
            />
            <input
              type="text"
              placeholder="Database ID"
              value={formData.config.databaseId || ''}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  config: { ...formData.config, databaseId: e.target.value },
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

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-xl font-semibold">커넥터</h2>
          <p className="text-sm text-gray-600 mt-1">
            외부 지식 소스를 연결하여 AI가 활용합니다
          </p>
        </div>
        <button
          onClick={openAddModal}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          + 커넥터 추가
        </button>
      </div>

      <div className="grid md:grid-cols-2 gap-4">
        {connectors.map((connector) => (
          <div
            key={connector.id}
            className="bg-white rounded-lg border border-gray-200 p-6 hover:shadow-md transition-shadow"
          >
            <div className="flex items-start justify-between mb-4">
              <div className="flex items-center gap-3">
                <span className="text-3xl">{getConnectorIcon(connector.type)}</span>
                <div>
                  <h3 className="font-semibold text-gray-900">{connector.name}</h3>
                  <p className="text-sm text-gray-600">{connector.type}</p>
                </div>
              </div>
              <Badge variant={getStatusBadge(connector.status)}>
                {connector.status}
              </Badge>
            </div>

            <div className="space-y-2 text-sm text-gray-600 mb-4">
              <div className="flex justify-between">
                <span>문서 수</span>
                <span className="font-medium">{connector.documentCount}</span>
              </div>
              {connector.lastSyncAt && (
                <div className="flex justify-between">
                  <span>마지막 동기화</span>
                  <span className="font-medium">
                    {new Date(connector.lastSyncAt).toLocaleString('ko-KR')}
                  </span>
                </div>
              )}
            </div>

            {connector.errorMessage && (
              <div className="bg-red-50 border border-red-200 rounded p-2 text-xs text-red-700 mb-4">
                {connector.errorMessage}
              </div>
            )}

            <div className="flex gap-2">
              <button
                onClick={() => handleSync(connector.id)}
                disabled={connector.status === 'SYNCING'}
                className="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm disabled:bg-gray-400"
              >
                {connector.status === 'SYNCING' ? '동기화 중...' : '동기화'}
              </button>
              <button
                onClick={() => openEditModal(connector)}
                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm"
              >
                수정
              </button>
              <button
                onClick={() => handleDelete(connector.id)}
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
        title={editingConnector ? '커넥터 수정' : '커넥터 추가'}
        size="md"
      >
        <div className="p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">커넥터 이름</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-4 py-2 border rounded-lg"
              placeholder="예: 회사 Confluence"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">커넥터 유형</label>
            <select
              value={formData.type}
              onChange={(e) =>
                setFormData({ ...formData, type: e.target.value as ConnectorType, config: {} })
              }
              className="w-full px-4 py-2 border rounded-lg"
              disabled={!!editingConnector}
            >
              <option value="CONFLUENCE">Confluence</option>
              <option value="NOTION">Notion</option>
              <option value="GOOGLE_DRIVE">Google Drive</option>
              <option value="WEB_CRAWL">Web Crawl</option>
              <option value="LOCAL">Local Files</option>
            </select>
          </div>

          {renderConfigForm()}

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
