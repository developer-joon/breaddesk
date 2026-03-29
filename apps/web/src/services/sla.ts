import api from '@/lib/api';
import type { ApiResponse, SlaRuleResponse, SlaRuleUpdateRequest, SlaStatsResponse } from '@/types';

// ── Rules ──
export async function getSlaRules(): Promise<SlaRuleResponse[]> {
  const { data } = await api.get<ApiResponse<SlaRuleResponse[]>>('/sla-rules');
  return data.data;
}

export async function updateSlaRule(
  id: number,
  req: SlaRuleUpdateRequest,
): Promise<SlaRuleResponse> {
  const { data } = await api.put<ApiResponse<SlaRuleResponse>>(`/sla-rules/${id}`, req);
  return data.data;
}

// ── Stats ──
export async function getSlaStats(): Promise<SlaStatsResponse> {
  const { data } = await api.get<ApiResponse<SlaStatsResponse>>('/sla-rules/stats');
  return data.data;
}
