import api from '@/lib/api';
import type {
  ApiResponse,
  Page,
  KnowledgeConnectorResponse,
  KnowledgeConnectorRequest,
  KnowledgeDocumentResponse,
  VectorSearchResult,
} from '@/types';

// ── Connectors ──
export async function getConnectors(): Promise<KnowledgeConnectorResponse[]> {
  const { data } = await api.get<ApiResponse<KnowledgeConnectorResponse[]>>(
    '/knowledge/connectors',
  );
  return data.data;
}

export async function getConnectorById(id: number): Promise<KnowledgeConnectorResponse> {
  const { data } = await api.get<ApiResponse<KnowledgeConnectorResponse>>(
    `/knowledge/connectors/${id}`,
  );
  return data.data;
}

export async function createConnector(
  req: KnowledgeConnectorRequest,
): Promise<KnowledgeConnectorResponse> {
  const { data } = await api.post<ApiResponse<KnowledgeConnectorResponse>>(
    '/knowledge/connectors',
    req,
  );
  return data.data;
}

export async function updateConnector(
  id: number,
  req: KnowledgeConnectorRequest,
): Promise<KnowledgeConnectorResponse> {
  const { data } = await api.put<ApiResponse<KnowledgeConnectorResponse>>(
    `/knowledge/connectors/${id}`,
    req,
  );
  return data.data;
}

export async function deleteConnector(id: number): Promise<void> {
  await api.delete(`/knowledge/connectors/${id}`);
}

export async function syncConnector(id: number): Promise<void> {
  await api.post(`/knowledge/connectors/${id}/sync`);
}

// ── Documents ──
export async function getDocuments(
  page = 0,
  size = 20,
  params?: { search?: string; connectorId?: number },
): Promise<Page<KnowledgeDocumentResponse>> {
  const { data } = await api.get<ApiResponse<Page<KnowledgeDocumentResponse>>>(
    '/knowledge/documents',
    {
      params: { page, size, ...params },
    },
  );
  return data.data;
}

export async function getDocumentById(id: number): Promise<KnowledgeDocumentResponse> {
  const { data } = await api.get<ApiResponse<KnowledgeDocumentResponse>>(
    `/knowledge/documents/${id}`,
  );
  return data.data;
}

// ── Vector Search ──
export async function vectorSearch(
  query: string,
  limit = 10,
  connectorId?: number,
): Promise<VectorSearchResult[]> {
  const { data } = await api.post<ApiResponse<VectorSearchResult[]>>('/knowledge/search', {
    query,
    limit,
    connectorId,
  });
  return data.data;
}
