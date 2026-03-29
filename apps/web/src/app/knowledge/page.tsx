'use client';

import React, { useState, useEffect } from 'react';
import { AppLayout } from '@/components/layout/AppLayout';
import { Badge } from '@/components/ui/Badge';
import { KnowledgeConnector, KnowledgeDocument } from '@/types';

export default function KnowledgePage() {
  const [connectors, setConnectors] = useState<KnowledgeConnector[]>([]);
  const [documents, setDocuments] = useState<KnowledgeDocument[]>([]);

  useEffect(() => {
    fetchKnowledge();
  }, []);

  const fetchKnowledge = async () => {
    // Mock data
    setConnectors([
      {
        id: '1',
        name: 'Notion Workspace',
        type: 'NOTION',
        status: 'CONNECTED',
        lastSyncAt: new Date().toISOString(),
        documentCount: 42,
      },
      {
        id: '2',
        name: 'Google Drive',
        type: 'GOOGLE_DRIVE',
        status: 'DISCONNECTED',
        documentCount: 0,
      },
    ]);

    setDocuments([
      {
        id: '1',
        title: '제품 사용 가이드',
        connectorId: '1',
        connectorName: 'Notion Workspace',
        url: 'https://notion.so/...',
        lastUpdated: new Date().toISOString(),
      },
      {
        id: '2',
        title: 'FAQ 문서',
        connectorId: '1',
        connectorName: 'Notion Workspace',
        url: 'https://notion.so/...',
        lastUpdated: new Date().toISOString(),
      },
    ]);
  };

  const getStatusBadgeVariant = (status: string) => {
    switch (status) {
      case 'CONNECTED':
        return 'success';
      case 'DISCONNECTED':
        return 'default';
      case 'ERROR':
        return 'danger';
      default:
        return 'default';
    }
  };

  const getConnectorIcon = (type: string) => {
    switch (type) {
      case 'NOTION':
        return '📝';
      case 'CONFLUENCE':
        return '🌐';
      case 'GOOGLE_DRIVE':
        return '💾';
      case 'LOCAL':
        return '📁';
      default:
        return '📚';
    }
  };

  return (
    <AppLayout>
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-2xl font-bold text-gray-900">지식베이스</h1>
          <p className="text-gray-600 mt-1">외부 문서를 연결하고 AI가 답변에 활용합니다</p>
        </div>

        {/* Connectors */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold">커넥터</h2>
            <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
              + 커넥터 추가
            </button>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
            {connectors.map((connector) => (
              <div
                key={connector.id}
                className="bg-white rounded-lg p-6 shadow-sm border border-gray-200 hover-lift"
              >
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <span className="text-3xl">{getConnectorIcon(connector.type)}</span>
                    <div>
                      <h3 className="font-semibold text-gray-900">{connector.name}</h3>
                      <p className="text-sm text-gray-600">{connector.type}</p>
                    </div>
                  </div>
                  <Badge variant={getStatusBadgeVariant(connector.status)}>
                    {connector.status}
                  </Badge>
                </div>

                <div className="space-y-2 text-sm text-gray-600">
                  <div className="flex items-center justify-between">
                    <span>문서 수</span>
                    <span className="font-medium">{connector.documentCount}</span>
                  </div>
                  {connector.lastSyncAt && (
                    <div className="flex items-center justify-between">
                      <span>마지막 동기화</span>
                      <span className="font-medium">
                        {new Date(connector.lastSyncAt).toLocaleString('ko-KR')}
                      </span>
                    </div>
                  )}
                </div>

                <div className="flex gap-2 mt-4">
                  <button className="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm">
                    동기화
                  </button>
                  <button className="px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm">
                    설정
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Documents */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold">문서 ({documents.length})</h2>
            <input
              type="text"
              placeholder="문서 검색..."
              className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    제목
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    커넥터
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    마지막 업데이트
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                    작업
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {documents.map((doc) => (
                  <tr key={doc.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="font-medium text-gray-900">{doc.title}</div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600">{doc.connectorName}</td>
                    <td className="px-6 py-4 text-sm text-gray-600">
                      {new Date(doc.lastUpdated).toLocaleDateString('ko-KR')}
                    </td>
                    <td className="px-6 py-4 text-right">
                      {doc.url && (
                        <a
                          href={doc.url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-blue-600 hover:text-blue-700 text-sm"
                        >
                          열기 →
                        </a>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Info Banner */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-start gap-3">
            <span className="text-2xl">💡</span>
            <div>
              <h3 className="font-semibold text-blue-900 mb-1">Phase 1 안내</h3>
              <p className="text-sm text-blue-800">
                현재 버전에서는 커넥터 관리 UI만 제공됩니다. 실제 연동 기능은 Phase 2에서
                구현됩니다.
              </p>
            </div>
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
