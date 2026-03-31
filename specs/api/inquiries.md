# 문의 관리 API

## 개요
고객 문의를 수집하고, AI 자동답변을 생성하며, 필요 시 업무로 에스컬레이션하는 API입니다.

## 엔드포인트

### POST /api/v1/inquiries
**설명**: 새로운 문의를 생성합니다. 생성 시 자동으로 AI 자동답변을 시도합니다.

**인증**: 불필요 (공개 API, 웹훅/채널에서 호출)

**Request Body**:
```json
{
  "channel": "slack",
  "channelMeta": "{\"thread_ts\": \"1234567890.123456\"}",
  "senderName": "홍길동",
  "senderEmail": "hong@example.com",
  "message": "결제가 안 돼요. 어떻게 해야 하나요?"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "channel": "slack",
    "channelMeta": "{\"thread_ts\": \"1234567890.123456\"}",
    "senderName": "홍길동",
    "senderEmail": "hong@example.com",
    "message": "결제가 안 돼요. 어떻게 해야 하나요?",
    "aiResponse": "결제 오류는 다음과 같이 해결할 수 있습니다...",
    "aiConfidence": 0.85,
    "status": "AI_ANSWERED",
    "taskId": null,
    "resolvedBy": null,
    "createdAt": "2026-03-31T12:00:00",
    "resolvedAt": null,
    "messages": [
      {
        "id": 1,
        "role": "USER",
        "message": "결제가 안 돼요. 어떻게 해야 하나요?",
        "createdAt": "2026-03-31T12:00:00"
      },
      {
        "id": 2,
        "role": "AI",
        "message": "결제 오류는 다음과 같이 해결할 수 있습니다...",
        "createdAt": "2026-03-31T12:00:01"
      }
    ]
  }
}
```

**에러 코드**:
- `400 Bad Request`: 필수 필드 누락 또는 유효하지 않은 이메일 형식

---

### GET /api/v1/inquiries
**설명**: 전체 문의 목록을 페이지네이션으로 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Query Parameters**:
- `page`: 페이지 번호 (0부터 시작, 기본값: 0)
- `size`: 페이지 크기 (기본값: 20)
- `sort`: 정렬 (예: `createdAt,desc`)

**Response**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "channel": "slack",
        "senderName": "홍길동",
        "message": "결제가 안 돼요...",
        "status": "AI_ANSWERED",
        "createdAt": "2026-03-31T12:00:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### GET /api/v1/inquiries/{id}
**설명**: 특정 문의의 상세 정보를 조회합니다. 대화 이력도 함께 반환됩니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "channel": "slack",
    "senderName": "홍길동",
    "senderEmail": "hong@example.com",
    "message": "결제가 안 돼요. 어떻게 해야 하나요?",
    "aiResponse": "결제 오류는...",
    "aiConfidence": 0.85,
    "status": "AI_ANSWERED",
    "taskId": null,
    "resolvedBy": null,
    "createdAt": "2026-03-31T12:00:00",
    "resolvedAt": null,
    "messages": [
      {
        "id": 1,
        "role": "USER",
        "message": "결제가 안 돼요...",
        "createdAt": "2026-03-31T12:00:00"
      },
      {
        "id": 2,
        "role": "AI",
        "message": "결제 오류는...",
        "createdAt": "2026-03-31T12:00:01"
      }
    ]
  }
}
```

**에러 코드**:
- `404 Not Found`: 문의를 찾을 수 없음

---

### PATCH /api/v1/inquiries/{id}/status
**설명**: 문의 상태를 변경합니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "status": "RESOLVED",
  "resolvedBy": "HUMAN"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "RESOLVED",
    "resolvedBy": "HUMAN",
    "resolvedAt": "2026-03-31T12:30:00"
  }
}
```

**가능한 상태**:
- `OPEN`: 신규 문의
- `AI_ANSWERED`: AI 자동답변 완료
- `ESCALATED`: 업무로 에스컬레이션
- `RESOLVED`: 해결됨
- `CLOSED`: 종료

**에러 코드**:
- `400 Bad Request`: 유효하지 않은 상태값
- `404 Not Found`: 문의를 찾을 수 없음

---

### POST /api/v1/inquiries/{id}/messages
**설명**: 문의에 새로운 메시지(댓글)를 추가합니다.

**인증**: 필요 (AGENT, ADMIN) 또는 불필요 (고객이 추가 답변 시)

**Request Body**:
```json
{
  "role": "AGENT",
  "message": "네, 이 문제는 다음과 같이 해결할 수 있습니다..."
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 3,
    "role": "AGENT",
    "message": "네, 이 문제는...",
    "createdAt": "2026-03-31T12:05:00"
  }
}
```

**role 옵션**:
- `USER`: 고객 메시지
- `AI`: AI 응답
- `AGENT`: 상담원 메시지

**에러 코드**:
- `400 Bad Request`: 필수 필드 누락
- `404 Not Found`: 문의를 찾을 수 없음

---

### POST /api/v1/inquiries/{id}/convert-to-task
**설명**: 문의를 업무(Task)로 에스컬레이션합니다. 자동으로 Inquiry.status를 ESCALATED로 변경하고, 새로운 Task를 생성합니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "title": "[결제 오류] 홍길동 고객 문의",
  "urgency": "HIGH",
  "assigneeId": 5
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "ESCALATED",
    "taskId": 42,
    "resolvedBy": null
  }
}
```

**에러 코드**:
- `400 Bad Request`: 필수 필드 누락 또는 이미 에스컬레이션된 문의
- `404 Not Found`: 문의를 찾을 수 없음

---

### GET /api/v1/inquiries/{id}/similar
**설명**: 유사한 문의를 검색합니다. pgvector 기반 임베딩 검색을 사용합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "message": "결제가 진행되지 않아요",
      "similarity": 0.92,
      "aiResponse": "결제 오류는...",
      "createdAt": "2026-03-25T10:00:00"
    },
    {
      "id": 12,
      "message": "카드 결제 실패",
      "similarity": 0.88,
      "aiResponse": "카드 결제 실패 시...",
      "createdAt": "2026-03-20T14:30:00"
    }
  ]
}
```

**비즈니스 규칙**:
- 현재 문의(id)는 제외
- 코사인 유사도 기준 상위 5개 반환
- 최소 유사도 threshold: 0.7

**에러 코드**:
- `404 Not Found`: 문의를 찾을 수 없음

---

### DELETE /api/v1/inquiries/{id}
**설명**: 문의를 삭제합니다. 연관된 메시지도 함께 삭제됩니다 (CASCADE).

**인증**: 필요 (ADMIN)

**Response**:
```json
{
  "success": true,
  "data": null
}
```

**에러 코드**:
- `403 Forbidden`: ADMIN 권한 필요
- `404 Not Found`: 문의를 찾을 수 없음

---

## 비즈니스 규칙

1. **AI 자동답변 생성**: 
   - 문의 생성 시 자동으로 유사 문의 검색 → RAG 기반 답변 생성
   - `aiConfidence >= 0.8`: AI_ANSWERED 상태로 전환
   - `aiConfidence < 0.8`: OPEN 상태 유지, 상담원 확인 필요

2. **에스컬레이션 조건**:
   - AI 신뢰도가 낮거나
   - 상담원이 수동으로 에스컬레이션 요청 시

3. **해결(RESOLVED) 조건**:
   - `resolvedBy = AI`: AI가 자동으로 해결
   - `resolvedBy = HUMAN`: 상담원이 수동으로 해결

4. **채널 메타데이터**:
   - Slack: `{"thread_ts": "..."}`
   - Teams: `{"conversationId": "..."}`
   - Email: `{"messageId": "...", "inReplyTo": "..."}`

---

## 프론트엔드 구현 참고

- `apps/web/src/services/inquiries.ts`: API 호출 함수
- `apps/web/src/app/inquiries/page.tsx`: 문의 관리 페이지
- 실시간 알림: WebSocket 또는 Polling으로 새 문의 감지

---

## 관련 엔티티

- [Inquiry](../data-model/ENTITIES.md#2-inquiry-문의)
- [InquiryMessage](../data-model/ENTITIES.md#3-inquirymessage-문의-대화-이력)
- [Task](../data-model/ENTITIES.md#4-task-업무)
