# AI 자동답변 (RAG 기반)

## 개요
고객 문의가 들어오면 지식베이스를 검색하여 자동으로 답변을 생성합니다. pgvector 기반 RAG(Retrieval-Augmented Generation) 파이프라인을 사용합니다.

## 사용자 스토리
- **AS-IS**: 고객 문의가 들어오면 상담원이 수동으로 지식베이스를 검색하고 답변 작성
- **TO-BE**: 문의 생성 시 AI가 자동으로 유사 문의와 관련 문서를 검색하여 답변 생성, 신뢰도가 높으면 자동 답변, 낮으면 상담원에게 에스컬레이션

## 플로우

```
1. 고객 문의 수신 (POST /api/v1/inquiries)
   ↓
2. InquiryService.createInquiry()
   ↓
3. 임베딩 생성 (EmbeddingService.embed(message))
   ↓
4. 유사 문의 검색 (SimilarInquiryService.findSimilar())
   ↓
5. 관련 지식 문서 검색 (VectorSearchService.search())
   ↓
6. LLM 호출 (OpenAI GPT-4 등)
   - Prompt: "다음 문의에 답변하세요. 참고 자료: {similar_inquiries}, {knowledge_docs}"
   ↓
7. AI 응답 저장 (Inquiry.aiResponse, aiConfidence)
   ↓
8. 신뢰도 평가
   - if aiConfidence >= 0.8: status = AI_ANSWERED
   - else: status = OPEN (상담원 확인 필요)
   ↓
9. 채널에 답변 전송 (ChannelService.sendReply())
```

## 관련 API
- `POST /api/v1/inquiries` - 문의 생성 시 자동답변 트리거
- `GET /api/v1/inquiries/{id}/similar` - 유사 문의 조회
- `POST /api/v1/knowledge/search` - 지식 문서 벡터 검색

## 관련 엔티티
- [Inquiry](../data-model/ENTITIES.md#2-inquiry-문의)
- [InquiryMessage](../data-model/ENTITIES.md#3-inquirymessage-문의-대화-이력)
- [KnowledgeDocument](../data-model/ENTITIES.md#19-knowledgedocument-지식베이스-문서)

## 비즈니스 규칙

### 1. 임베딩 생성
- 모델: OpenAI text-embedding-ada-002 (768차원)
- 입력: Inquiry.message 전체 텍스트
- 저장: 임베딩은 저장 안 함 (실시간 생성)

### 2. 유사 문의 검색
- 검색 대상: 과거 해결된 문의 (status IN (RESOLVED, CLOSED))
- 최소 유사도: 0.7 (코사인 유사도)
- 반환 개수: 상위 5개
- 제외: 현재 문의 자신

### 3. 지식 문서 검색
- 검색 대상: knowledge_documents 테이블 전체
- 최소 유사도: 0.7
- 반환 개수: 상위 3개

### 4. LLM 프롬프트 구성
```
시스템: 당신은 BreadDesk 고객 지원 AI입니다. 다음 문의에 친절하고 정확하게 답변하세요.

참고 자료:
[유사 문의]
- Q: {similar1.message}
  A: {similar1.aiResponse}

[지식 문서]
- {doc1.title}: {doc1.content}

고객 문의: {inquiry.message}

답변:
```

### 5. 신뢰도 계산
- GPT-4의 응답에 "확신도: X%" 형식으로 포함하거나
- 유사 문의 최대 유사도를 신뢰도로 사용
- `aiConfidence >= 0.8`: 자동 답변 (AI_ANSWERED)
- `aiConfidence < 0.8`: 상담원 검토 필요 (OPEN)

### 6. 답변 전송
- Slack/Teams: 스레드 답글로 전송
- Email: Reply-To 헤더 사용하여 답장

## 엣지 케이스

### 1. 유사 문의 없음
- knowledge_documents만으로 답변 생성
- 신뢰도를 낮게 설정 (0.5 이하)

### 2. 지식 문서 없음
- "죄송합니다. 관련 정보를 찾을 수 없어 상담원에게 연결하겠습니다." 답변
- status = OPEN, aiConfidence = 0.0

### 3. LLM API 오류
- 에러 로그 기록
- status = OPEN (상담원에게 즉시 할당)
- aiResponse = null

### 4. 다국어 문의
- 언어 자동 감지 (langdetect 라이브러리)
- 같은 언어의 유사 문의/문서만 검색
- LLM 프롬프트에 언어 명시

## 성능 지표

- **자동 답변율**: AI_ANSWERED / 전체 문의
- **평균 신뢰도**: avg(aiConfidence) for AI_ANSWERED
- **에스컬레이션율**: ESCALATED / 전체 문의
- **응답 시간**: 임베딩 + 검색 + LLM 호출 (목표: < 3초)

## 구현 클래스

- `InquiryService.createInquiry()`: 메인 플로우
- `EmbeddingService.embed()`: 임베딩 생성
- `SimilarInquiryService.findSimilar()`: 유사 문의 검색
- `VectorSearchService.search()`: 벡터 검색
- `LLMService.generateResponse()`: GPT-4 호출 (미구현 시 구현 필요)
- `ChannelService.sendReply()`: 채널별 답변 전송 (미구현 시 구현 필요)

## 향후 개선 방향

1. **Fine-tuning**: 회사 데이터로 GPT-4 fine-tuning
2. **피드백 루프**: 상담원이 AI 답변 수정 시 학습 데이터로 활용
3. **다단계 대화**: 추가 질문 시 대화 이력 포함하여 답변
4. **감정 분석**: 부정적 감정 감지 시 우선순위 상향
