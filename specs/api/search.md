# 통합 검색 API

## 개요
문의, 업무, 지식베이스 문서를 통합 검색합니다.

## 엔드포인트

### GET /api/v1/search
**설명**: 키워드로 전체 엔티티를 검색합니다.

**인증**: 필요 (AGENT, ADMIN)

**Query Parameters**:
- `q`: 검색어
- `type`: 검색 대상 (`inquiries`, `tasks`, `knowledge`, `all` - 기본값: `all`)
- `limit`: 최대 결과 수 (기본값: 10)

**Response**:
```json
{
  "success": true,
  "data": {
    "inquiries": [
      {
        "id": 1,
        "senderName": "홍길동",
        "message": "결제가 안 돼요...",
        "status": "AI_ANSWERED",
        "createdAt": "2026-03-31T12:00:00",
        "highlight": "...결제가 안 돼요..."
      }
    ],
    "tasks": [
      {
        "id": 42,
        "title": "홍길동 고객 결제 오류 해결",
        "urgency": "HIGH",
        "status": "IN_PROGRESS",
        "createdAt": "2026-03-31T12:00:00",
        "highlight": "...결제 오류..."
      }
    ],
    "knowledgeDocuments": [
      {
        "id": 1,
        "title": "결제 오류 처리 가이드",
        "content": "고객이 결제 오류를 겪는 경우...",
        "url": "https://notion.so/abc123",
        "similarity": 0.92,
        "highlight": "...결제 오류를 겪는 경우..."
      }
    ]
  }
}
```

---

## 검색 방식

### inquiries, tasks
- 전문 검색(Full-text search): 제목, 내용, 발신자명 등
- PostgreSQL `LIKE` 또는 `tsvector` 사용

### knowledgeDocuments
- 벡터 유사도 검색(pgvector): 임베딩 기반 유사 문서 검색
- 최소 유사도: 0.7

---

## 비즈니스 규칙

1. **권한**: 본인이 담당하거나 구독 중인 업무만 검색 (ADMIN은 전체 검색 가능)
2. **하이라이트**: 검색어가 포함된 부분을 `highlight` 필드로 반환
3. **정렬**: 관련도 순 (벡터 유사도 또는 텍스트 매칭 스코어)

---

## 프론트엔드 구현 참고

- `apps/web/src/services/search.ts`: API 호출 함수 (미구현 시 구현 필요)
- 검색 UI: 상단 헤더에 통합 검색 바 + 결과 드롭다운

---

## 관련 엔티티

- [Inquiry](../data-model/ENTITIES.md#2-inquiry-문의)
- [Task](../data-model/ENTITIES.md#4-task-업무)
- [KnowledgeDocument](../data-model/ENTITIES.md#19-knowledgedocument-지식베이스-문서)
