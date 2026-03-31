import api from '@/lib/api';

export interface AIStatus {
  available: boolean;
  model: string;
  message: string;
}

export interface SuggestReplyResponse {
  suggestion: string;
  confidence: number;
  metadata: Record<string, unknown>;
}

export interface RewriteReplyRequest {
  originalReply: string;
  tone: 'friendly' | 'formal' | 'concise';
}

export interface RewriteReplyResponse {
  original: string;
  rewritten: string;
  tone: string;
}

export interface ClassificationResult {
  category: string;
  urgency: 'LOW' | 'NORMAL' | 'HIGH' | 'CRITICAL';
  reason: string;
}

export interface AssigneeRecommendation {
  memberId: number;
  memberName: string;
  score: number;
  reason: string;
}

export const aiService = {
  /**
   * AI 서비스 상태 확인
   */
  async getStatus(): Promise<AIStatus> {
    const { data } = await api.get<AIStatus>('/ai/status');
    return data;
  },

  /**
   * AI 응답 추천 (Copilot) — 상담원용
   */
  async suggestReply(inquiryId: number): Promise<SuggestReplyResponse> {
    const { data } = await api.post<SuggestReplyResponse>(
      `/ai/inquiries/${inquiryId}/suggest-reply`,
    );
    return data;
  },

  /**
   * 답변 리라이트 (톤 조절)
   */
  async rewriteReply(request: RewriteReplyRequest): Promise<RewriteReplyResponse> {
    const { data } = await api.post<RewriteReplyResponse>('/ai/rewrite', request);
    return data;
  },

  /**
   * 문의 자동 분류
   */
  async classifyInquiry(inquiryId: number): Promise<ClassificationResult> {
    const { data } = await api.post<ClassificationResult>(
      `/ai/classify/inquiry/${inquiryId}`,
    );
    return data;
  },

  /**
   * 업무 자동 분류
   */
  async classifyTask(taskId: number): Promise<ClassificationResult> {
    const { data } = await api.post<ClassificationResult>(`/ai/classify/task/${taskId}`);
    return data;
  },

  /**
   * 텍스트 분류 (임의 텍스트)
   */
  async classifyText(text: string): Promise<ClassificationResult> {
    const { data } = await api.post<ClassificationResult>('/ai/classify/text', { text });
    return data;
  },

  /**
   * AI 담당자 추천
   */
  async recommendAssignees(taskId: number): Promise<AssigneeRecommendation[]> {
    const { data } = await api.get<AssigneeRecommendation[]>(
      `/ai/tasks/${taskId}/recommend-assignees`,
    );
    return data;
  },
};
