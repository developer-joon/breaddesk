# Inquiry API 명세 - 문의 접수 및 AI 답변

## 개요
문의 접수, AI 자동 답변, 대화 이력, 담당자 답변, 사용자 피드백, 에스컬레이션 API

**Base URL**: `/api/v1`

---

## 1. 문의 접수 (n8n → BreadDesk)

### `POST /inquiries`

#### Request
```json
{
  "channel": "slack",
  "channelMeta": {
    "teamId": "T01234567",
    "channelId": "C01234567",
    "threadTs": "1711695480.123456",
    "userId": "U01234567"
  },
  "sender": "김철수",
  "senderEmail": "cskim@company.com",
  "message": "VPN 접속이 안 되는데 어떻게 해야 하나요?",
  "attachments": [
    {
      "filename": "error-screenshot.png",
      "url": "https://files.slack.com/...",
      "mimeType": "image/png",
      "size": 245678
    }
  ]
}
```

**필드 설명**
- `channel`: 출처 채널 (`slack`, `teams`, `jira`, `web`, `email`)
- `channelMeta`: 채널별 메타데이터 (응답 역전달용)
  - Slack: `teamId`, `channelId`, `threadTs`, `userId`
  - Teams: `conversationId`, `activityId`, `serviceUrl`
  - 웹: `sessionId`
- `sender`: 요청자 이름
- `senderEmail`: 요청자 이메일
- `message`: 문의 내용
- `attachments`: 첨부 파일 (optional)

#### Response 201 Created
```json
{
  "id": 42,
  "channel": "slack",
  "channelMeta": { "teamId": "T01234567", "channelId": "C01234567", "threadTs": "1711695480.123456" },
  "sender": "김철수",
  "senderEmail": "cskim@company.com",
  "message": "VPN 접속이 안 되는데 어떻게 해야 하나요?",
  "status": "AI_PROCESSING",
  "aiResponse": null,
  "aiConfidence": null,
  "resolvedBy": null,
  "taskId": null,
  "createdAt": "2026-03-29T11:00:00Z",
  "resolvedAt": null
}
```

**상태 값**
- `AI_PROCESSING`: AI 답변 생성 중 (비동기)
- `AI_ANSWERED`: AI 자동 답변 완료
- `ESCALATED`: 담당자에게 에스컬레이션
- `RESOLVED`: 해결됨 (사용자 피드백 "해결")
- `CLOSED`: 종료 (사용자 피드백 "미해결" → 업무 생성 완료)

#### AI 답변 흐름 (백그라운드)
1. 벡터 검색으로 관련 지식 문서 3~5개 조회
2. LLM에 문의 + 관련 문서 전달 → 답변 생성
3. 신뢰도 계산
   - **신뢰도 ≥ 0.8 (높음)**: 자동 답변 → 사용자에게 전달 + "해결됐나요?" 버튼
   - **신뢰도 0.5~0.8 (중간)**: 자동 답변 → 사용자 + 동시에 담당자 알림
   - **신뢰도 < 0.5 (낮음)**: "담당자에게 전달합니다" → 업무 자동 생성
4. 상태 업데이트: `AI_ANSWERED` or `ESCALATED`

#### Error 400 Bad Request
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid channel type",
  "details": {
    "field": "channel",
    "value": "kakao",
    "allowedValues": ["slack", "teams", "jira", "web", "email"]
  }
}
```

---

## 2. 문의 목록 조회

### `GET /inquiries`

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `status` | String | N | 상태 필터 (`,` 구분) |
| `channel` | String | N | 채널 필터 |
| `sender` | String | N | 요청자 이름 검색 |
| `search` | String | N | 문의 내용 검색 |
| `resolvedBy` | String | N | `AI`, `HUMAN` |
| `dateFrom` | DateTime | N | 접수일 시작 |
| `dateTo` | DateTime | N | 접수일 끝 |
| `sort` | String | N | 정렬 (기본: `createdAt,desc`) |
| `page` | Int | N | 페이지 번호 (0부터) |
| `size` | Int | N | 페이지 크기 (기본: 20) |

#### Response 200 OK
```json
{
  "content": [
    {
      "id": 42,
      "channel": "slack",
      "sender": "김철수",
      "senderEmail": "cskim@company.com",
      "message": "VPN 접속이 안 되는데...",
      "status": "AI_ANSWERED",
      "aiConfidence": 0.85,
      "resolvedBy": "AI",
      "taskId": null,
      "createdAt": "2026-03-29T11:00:00Z",
      "resolvedAt": "2026-03-29T11:01:30Z"
    },
    {
      "id": 41,
      "channel": "teams",
      "sender": "박민수",
      "senderEmail": "mspark@company.com",
      "message": "프로덕션 DB 권한 필요합니다",
      "status": "ESCALATED",
      "aiConfidence": 0.35,
      "resolvedBy": null,
      "taskId": 124,
      "createdAt": "2026-03-29T09:30:00Z",
      "resolvedAt": null
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 127,
    "totalPages": 7
  }
}
```

---

## 3. 문의 상세 조회 (대화 이력 포함)

### `GET /inquiries/{id}`

#### Response 200 OK
```json
{
  "id": 42,
  "channel": "slack",
  "channelMeta": {
    "teamId": "T01234567",
    "channelId": "C01234567",
    "threadTs": "1711695480.123456"
  },
  "sender": "김철수",
  "senderEmail": "cskim@company.com",
  "message": "VPN 접속이 안 되는데 어떻게 해야 하나요?",
  "status": "AI_ANSWERED",
  "aiResponse": "VPN 접속 문제는 다음 단계로 해결할 수 있습니다:\n\n1. VPN 클라이언트 재시작...",
  "aiConfidence": 0.85,
  "aiReferenceDocs": [
    {
      "id": "confluence:12345",
      "title": "VPN 접속 가이드",
      "url": "https://wiki.company.com/vpn-guide",
      "relevanceScore": 0.92
    },
    {
      "id": "confluence:67890",
      "title": "네트워크 트러블슈팅",
      "url": "https://wiki.company.com/network-troubleshooting",
      "relevanceScore": 0.78
    }
  ],
  "resolvedBy": "AI",
  "taskId": null,
  "createdAt": "2026-03-29T11:00:00Z",
  "resolvedAt": "2026-03-29T11:01:30Z",
  "conversationHistory": [
    {
      "id": 1,
      "role": "USER",
      "message": "VPN 접속이 안 되는데 어떻게 해야 하나요?",
      "createdAt": "2026-03-29T11:00:00Z"
    },
    {
      "id": 2,
      "role": "AI",
      "message": "VPN 접속 문제는 다음 단계로 해결할 수 있습니다:\n\n1. VPN 클라이언트 재시작...",
      "createdAt": "2026-03-29T11:00:30Z"
    }
  ],
  "attachments": [
    {
      "id": 5,
      "filename": "error-screenshot.png",
      "mimeType": "image/png",
      "fileSize": 245678,
      "uploadedAt": "2026-03-29T11:00:00Z"
    }
  ]
}
```

**대화 이력 역할 (role)**
- `USER`: 사용자 메시지
- `AI`: AI 자동 답변
- `AGENT`: 담당자 직접 답변

#### Error 404 Not Found
```json
{
  "error": "INQUIRY_NOT_FOUND",
  "message": "Inquiry with id 999 not found"
}
```

---

## 4. 담당자 직접 답변

### `POST /inquiries/{id}/reply`

#### Request
```json
{
  "message": "VPN 클라이언트 최신 버전으로 업데이트 후 다시 시도해보세요. 문제 지속 시 IT팀으로 연락 주세요.",
  "closeInquiry": true  // optional - true면 상태를 RESOLVED로 변경
}
```

#### Response 200 OK
```json
{
  "id": 42,
  "status": "RESOLVED",
  "resolvedBy": "HUMAN",
  "resolvedAt": "2026-03-29T12:30:00Z",
  "conversationHistory": [
    {
      "id": 1,
      "role": "USER",
      "message": "VPN 접속이 안 되는데...",
      "createdAt": "2026-03-29T11:00:00Z"
    },
    {
      "id": 2,
      "role": "AI",
      "message": "VPN 접속 문제는...",
      "createdAt": "2026-03-29T11:00:30Z"
    },
    {
      "id": 3,
      "role": "AGENT",
      "message": "VPN 클라이언트 최신 버전으로 업데이트 후...",
      "createdAt": "2026-03-29T12:30:00Z"
    }
  ]
}
```

**동작**
1. 담당자 메시지를 대화 이력에 추가
2. n8n 웹훅 트리거 → 사용자 채널로 메시지 전달
3. `closeInquiry: true` 시 상태를 `RESOLVED`로 변경
4. 담당자가 답변한 내용은 자동으로 지식베이스에 추가 고려 (Phase 2+)

---

## 5. 사용자 피드백 (해결/미해결)

### `POST /inquiries/{id}/feedback`

#### Request
```json
{
  "resolved": true,      // true: 해결됨, false: 미해결
  "comment": "문제 해결됐습니다. 감사합니다!"  // optional
}
```

#### Response 200 OK (해결됨)
```json
{
  "id": 42,
  "status": "RESOLVED",
  "resolvedBy": "AI",
  "resolvedAt": "2026-03-29T11:05:00Z",
  "userFeedback": {
    "resolved": true,
    "comment": "문제 해결됐습니다. 감사합니다!",
    "providedAt": "2026-03-29T11:05:00Z"
  }
}
```

#### Response 200 OK (미해결 → 에스컬레이션)
```json
{
  "id": 42,
  "status": "ESCALATED",
  "resolvedBy": null,
  "taskId": 125,         // 자동 생성된 업무 ID
  "task": {
    "id": 125,
    "title": "VPN 접속 불가 - 김철수",
    "status": "WAITING",
    "assigneeId": null,
    "aiSummary": "AI가 VPN 재시작 가이드 제공했으나 사용자가 미해결 표시",
    "createdAt": "2026-03-29T11:05:30Z"
  },
  "userFeedback": {
    "resolved": false,
    "comment": "여전히 안 됩니다",
    "providedAt": "2026-03-29T11:05:00Z"
  }
}
```

**미해결 처리 흐름**
1. 사용자 피드백 "미해결" 접수
2. 업무 자동 생성
   - 제목: 원본 문의 제목 또는 `"{주제} - {요청자}"`
   - 상태: `WAITING`
   - 원본 문의 연결 (`inquiryId`)
   - AI 요약: AI가 시도한 답변 내용 요약
   - 체크리스트: AI가 추천한 해결 단계 (선택적)
3. 문의 상태 → `ESCALATED`
4. 담당자에게 알림 전송 (n8n 웹훅)

---

## 6. 유사 문의 검색

### `GET /inquiries/{id}/similar`

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `threshold` | Float | N | 유사도 임계값 (기본: 0.85) |
| `limit` | Int | N | 최대 결과 수 (기본: 5) |

#### Response 200 OK
```json
{
  "similarInquiries": [
    {
      "id": 35,
      "sender": "최민지",
      "message": "VPN 연결이 계속 끊어져요",
      "status": "RESOLVED",
      "resolvedBy": "HUMAN",
      "similarityScore": 0.92,
      "resolution": {
        "finalMessage": "VPN 클라이언트 버전 업데이트로 해결",
        "resolvedAt": "2026-03-25T14:20:00Z"
      }
    },
    {
      "id": 28,
      "sender": "정수진",
      "message": "VPN 로그인 안 됨",
      "status": "RESOLVED",
      "resolvedBy": "AI",
      "similarityScore": 0.87,
      "resolution": {
        "aiResponse": "VPN 클라이언트 재시작 후...",
        "resolvedAt": "2026-03-22T10:15:00Z"
      }
    }
  ]
}
```

**용도**
- 문의 접수 직후 자동으로 유사 문의 검색
- 담당자 화면에 "이전 유사 문의" 표시
- 반복 문의 패턴 감지 (같은 주제 3회 이상 → 관리자 알림)

---

## 7. 문의 통계

### `GET /inquiries/stats`

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `dateFrom` | Date | N | 시작일 (기본: 30일 전) |
| `dateTo` | Date | N | 종료일 (기본: 오늘) |
| `channel` | String | N | 특정 채널 필터 |

#### Response 200 OK
```json
{
  "period": {
    "from": "2026-02-27",
    "to": "2026-03-29"
  },
  "total": 342,
  "byStatus": {
    "AI_ANSWERED": 245,
    "ESCALATED": 67,
    "RESOLVED": 312,
    "AI_PROCESSING": 3,
    "CLOSED": 27
  },
  "byChannel": {
    "slack": 198,
    "teams": 102,
    "web": 35,
    "email": 7
  },
  "aiPerformance": {
    "autoResolveRate": 0.72,        // 72% AI 자동 해결
    "avgConfidence": 0.78,
    "avgResponseTime": 8.5,         // 초
    "escalationRate": 0.20          // 20% 에스컬레이션
  },
  "topResolvedTopics": [
    { "topic": "VPN 접속", "count": 45, "autoResolveRate": 0.80 },
    { "topic": "권한 요청", "count": 38, "autoResolveRate": 0.35 },
    { "topic": "비밀번호 초기화", "count": 32, "autoResolveRate": 0.95 }
  ]
}
```

---

## 8. 웹훅 콜백 (BreadDesk → n8n)

### AI 답변 완료 시

**Webhook URL**: n8n 워크플로우가 등록한 URL (설정에서 관리)

#### POST (BreadDesk → n8n)
```json
{
  "event": "inquiry.ai_answered",
  "inquiryId": 42,
  "channel": "slack",
  "channelMeta": {
    "teamId": "T01234567",
    "channelId": "C01234567",
    "threadTs": "1711695480.123456"
  },
  "sender": "김철수",
  "aiResponse": "VPN 접속 문제는 다음 단계로...",
  "aiConfidence": 0.85,
  "needsFeedback": true,      // 사용자 피드백 요청 여부
  "timestamp": "2026-03-29T11:00:30Z"
}
```

**n8n 동작**: Slack/Teams로 AI 답변 전송 + "해결됐나요?" 버튼 추가

### 담당자 답변 시

#### POST (BreadDesk → n8n)
```json
{
  "event": "inquiry.agent_replied",
  "inquiryId": 42,
  "channel": "slack",
  "channelMeta": { "teamId": "T01234567", "channelId": "C01234567", "threadTs": "1711695480.123456" },
  "agentName": "이영희",
  "agentMessage": "VPN 클라이언트 최신 버전으로...",
  "timestamp": "2026-03-29T12:30:00Z"
}
```

### 에스컬레이션 시

#### POST (BreadDesk → n8n)
```json
{
  "event": "inquiry.escalated",
  "inquiryId": 42,
  "taskId": 125,
  "channel": "slack",
  "sender": "김철수",
  "reason": "AI 신뢰도 낮음 (0.35)",  // 또는 "사용자 미해결 피드백"
  "timestamp": "2026-03-29T11:05:30Z"
}
```

**n8n 동작**: 담당자 Slack 채널에 알림 전송

---

## 에러 코드 정리

| HTTP 상태 | 에러 코드 | 설명 |
|-----------|----------|------|
| 400 | `VALIDATION_ERROR` | 요청 데이터 검증 실패 |
| 400 | `INVALID_CHANNEL` | 지원하지 않는 채널 |
| 404 | `INQUIRY_NOT_FOUND` | 문의 없음 |
| 409 | `ALREADY_RESOLVED` | 이미 해결된 문의에 답변 시도 |
| 500 | `AI_PROCESSING_ERROR` | AI 답변 생성 실패 |
| 500 | `INTERNAL_ERROR` | 서버 에러 |

---

## 추가 고려사항 (Phase 2+)

### AI 답변 품질 개선
- 사용자 피드백 (해결/미해결) → AI 학습 데이터로 활용
- 담당자 직접 답변 → 자동으로 지식베이스에 추가
- 반복 문의 감지 → 근본 원인 해결 유도

### 대화 연속성
- 사용자 후속 질문 → 기존 문의 스레드에 연결
- 컨텍스트 유지 (이전 대화 기반 답변)

---

**작성일**: 2026-03-29  
**버전**: v1.0
