# 업무 관리 API

## 개요
칸반 방식의 업무 관리 시스템입니다. CRUD, 칸반 보드, 체크리스트, 댓글, 태그, 구독, 업무 연결, 보류/재개, 담당자 이전 등을 지원합니다.

---

## 1. 기본 CRUD

### POST /api/v1/tasks
**설명**: 새로운 업무를 생성합니다. 생성 시 SLA 마감일이 자동 계산되고, AI 가이드가 자동 생성됩니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "title": "홍길동 고객 결제 오류 해결",
  "description": "카드 결제 시 오류 발생. 원인 파악 필요.",
  "type": "SUPPORT",
  "urgency": "HIGH",
  "status": "WAITING",
  "requesterName": "홍길동",
  "requesterEmail": "hong@example.com",
  "assigneeId": 5,
  "dueDate": "2026-04-05",
  "estimatedHours": 2.5
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 42,
    "title": "홍길동 고객 결제 오류 해결",
    "description": "카드 결제 시 오류 발생...",
    "type": "SUPPORT",
    "urgency": "HIGH",
    "status": "WAITING",
    "requesterName": "홍길동",
    "assigneeId": 5,
    "assigneeName": "김철수",
    "dueDate": "2026-04-05",
    "slaResponseDeadline": "2026-03-31T14:00:00",
    "slaResolveDeadline": "2026-04-01T12:00:00",
    "createdAt": "2026-03-31T12:00:00",
    "tags": [],
    "checklists": []
  }
}
```

**에러 코드**:
- `400 Bad Request`: 필수 필드 누락
- `404 Not Found`: assigneeId가 존재하지 않음

---

### GET /api/v1/tasks
**설명**: 전체 업무 목록을 페이지네이션으로 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Query Parameters**:
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기 (기본값: 20)
- `sort`: 정렬 (예: `createdAt,desc`, `urgency,asc`)

**Response**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 42,
        "title": "홍길동 고객 결제 오류 해결",
        "urgency": "HIGH",
        "status": "IN_PROGRESS",
        "assigneeName": "김철수",
        "createdAt": "2026-03-31T12:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### GET /api/v1/tasks/{id}
**설명**: 특정 업무의 상세 정보를 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 42,
    "title": "홍길동 고객 결제 오류 해결",
    "description": "카드 결제 시 오류 발생...",
    "urgency": "HIGH",
    "status": "IN_PROGRESS",
    "assigneeId": 5,
    "assigneeName": "김철수",
    "requesterName": "홍길동",
    "requesterEmail": "hong@example.com",
    "dueDate": "2026-04-05",
    "estimatedHours": 2.5,
    "actualHours": 1.2,
    "slaResponseDeadline": "2026-03-31T14:00:00",
    "slaResolveDeadline": "2026-04-01T12:00:00",
    "slaRespondedAt": "2026-03-31T13:00:00",
    "slaResponseBreached": false,
    "slaResolveBreached": false,
    "transferCount": 0,
    "createdAt": "2026-03-31T12:00:00",
    "startedAt": "2026-03-31T13:00:00",
    "completedAt": null,
    "tags": ["결제", "긴급"],
    "checklists": [
      {
        "id": 1,
        "itemText": "로그 확인",
        "isDone": true,
        "sortOrder": 0
      }
    ],
    "aiSummary": "고객이 카드 결제 시 오류 발생. PG사 연동 확인 필요.",
    "jiraIssueKey": null,
    "jiraIssueUrl": null
  }
}
```

**에러 코드**:
- `404 Not Found`: 업무를 찾을 수 없음

---

### PUT /api/v1/tasks/{id}
**설명**: 업무 전체 정보를 수정합니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**: TaskRequest와 동일

**Response**: 수정된 TaskResponse 반환

**에러 코드**:
- `400 Bad Request`: 유효하지 않은 필드
- `404 Not Found`: 업무를 찾을 수 없음

---

### PATCH /api/v1/tasks/{id}/status
**설명**: 업무 상태만 변경합니다. 상태 변경 시 자동으로 TaskLog 기록, SLA 타이머 업데이트, 알림 발송이 이루어집니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "status": "IN_PROGRESS"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 42,
    "status": "IN_PROGRESS",
    "startedAt": "2026-03-31T13:00:00"
  }
}
```

**상태 전환**:
- `WAITING` → `IN_PROGRESS`: startedAt 설정, SLA 응답 타이머 시작
- `IN_PROGRESS` → `PENDING`: 보류 (외부 대기)
- `PENDING` → `IN_PROGRESS`: 보류 해제
- `IN_PROGRESS` → `REVIEW`: 검토 요청
- `REVIEW` → `DONE`: completedAt 설정, SLA 해결 타이머 종료

**에러 코드**:
- `400 Bad Request`: 유효하지 않은 상태 전환
- `404 Not Found`: 업무를 찾을 수 없음

---

### DELETE /api/v1/tasks/{id}
**설명**: 업무를 삭제합니다. 연관된 체크리스트, 댓글, 로그 등도 함께 삭제됩니다 (CASCADE).

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
- `404 Not Found`: 업무를 찾을 수 없음

---

## 2. 칸반 보드

### GET /api/v1/tasks/kanban
**설명**: 칸반 보드 형태로 업무 목록을 조회합니다. 상태별로 그룹화됩니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": {
    "waiting": [
      {
        "id": 1,
        "title": "신규 문의 처리",
        "urgency": "NORMAL",
        "assigneeName": "김철수"
      }
    ],
    "inProgress": [
      {
        "id": 42,
        "title": "홍길동 고객 결제 오류 해결",
        "urgency": "HIGH",
        "assigneeName": "김철수"
      }
    ],
    "pending": [],
    "review": [
      {
        "id": 15,
        "title": "환불 정책 업데이트",
        "urgency": "LOW",
        "assigneeName": "이영희"
      }
    ],
    "done": [
      {
        "id": 10,
        "title": "회원가입 오류 수정",
        "urgency": "HIGH",
        "assigneeName": "박민수",
        "completedAt": "2026-03-30T18:00:00"
      }
    ]
  }
}
```

---

## 3. 체크리스트

### POST /api/v1/tasks/{taskId}/checklists
**설명**: 체크리스트 항목을 추가합니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "itemText": "PG사 API 로그 확인",
  "isDone": false,
  "sortOrder": 0
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "itemText": "PG사 API 로그 확인",
    "isDone": false,
    "sortOrder": 0
  }
}
```

---

### PUT /api/v1/tasks/{taskId}/checklists/{checklistId}
**설명**: 체크리스트 항목을 수정합니다. 주로 완료 상태(isDone) 토글에 사용됩니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "itemText": "PG사 API 로그 확인",
  "isDone": true,
  "sortOrder": 0
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "itemText": "PG사 API 로그 확인",
    "isDone": true,
    "sortOrder": 0
  }
}
```

---

### DELETE /api/v1/tasks/{taskId}/checklists/{checklistId}
**설명**: 체크리스트 항목을 삭제합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": null
}
```

---

## 4. 태그

### POST /api/v1/tasks/{taskId}/tags
**설명**: 태그를 추가합니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "tag": "결제"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "tag": "결제"
  }
}
```

---

### DELETE /api/v1/tasks/{taskId}/tags/{tag}
**설명**: 특정 태그를 삭제합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": null
}
```

**주의**: `{tag}` 파라미터는 URL 인코딩 필요 (예: `결제` → `%EA%B2%B0%EC%A0%9C`)

---

## 5. 댓글

### POST /api/v1/tasks/{taskId}/comments
**설명**: 댓글을 추가합니다. 작성자는 인증된 사용자입니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "content": "PG사 측 확인 결과 일시적 장애였습니다.",
  "isInternal": false
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "authorId": 5,
    "authorName": "김철수",
    "content": "PG사 측 확인 결과 일시적 장애였습니다.",
    "isInternal": false,
    "createdAt": "2026-03-31T13:30:00"
  }
}
```

---

### GET /api/v1/tasks/{taskId}/comments
**설명**: 전체 댓글 목록을 조회합니다. 내부 댓글(isInternal=true)은 ADMIN만 볼 수 있습니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "authorName": "김철수",
      "content": "PG사 측 확인 결과...",
      "isInternal": false,
      "createdAt": "2026-03-31T13:30:00"
    }
  ]
}
```

---

### GET /api/v1/tasks/{taskId}/comments/internal
**설명**: 내부 댓글만 조회합니다. ADMIN 전용입니다.

**인증**: 필요 (ADMIN)

**Response**: 위와 동일, `isInternal=true`인 댓글만 반환

---

## 6. 업무 로그

### GET /api/v1/tasks/{taskId}/logs
**설명**: 업무 이력 로그를 조회합니다. 상태 변경, 담당자 변경, 댓글 추가 등이 자동 기록됩니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "action": "STATUS_CHANGED",
      "actorName": "김철수",
      "details": {
        "from": "WAITING",
        "to": "IN_PROGRESS"
      },
      "createdAt": "2026-03-31T13:00:00"
    },
    {
      "id": 2,
      "action": "COMMENT_ADDED",
      "actorName": "김철수",
      "details": {
        "content": "PG사 측 확인 결과..."
      },
      "createdAt": "2026-03-31T13:30:00"
    }
  ]
}
```

**자동 기록되는 액션**:
- `CREATED`: 업무 생성
- `STATUS_CHANGED`: 상태 변경
- `ASSIGNED`: 담당자 할당
- `TRANSFERRED`: 담당자 이전
- `COMMENT_ADDED`: 댓글 추가
- `TAG_ADDED`: 태그 추가
- `HELD`: 보류
- `RESUMED`: 재개

---

## 7. 보류 / 재개

### POST /api/v1/tasks/{taskId}/hold
**설명**: 업무를 보류합니다. SLA 타이머를 일시정지할 수 있습니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "reason": "고객 추가 정보 대기 중"
}
```

**Response**:
```json
{
  "success": true,
  "data": null
}
```

**비즈니스 규칙**:
- 상태가 자동으로 `PENDING`으로 변경됨
- SLA 타이머 일시정지

---

### POST /api/v1/tasks/{taskId}/resume
**설명**: 보류된 업무를 재개합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": null
}
```

**비즈니스 규칙**:
- 상태가 자동으로 `IN_PROGRESS`로 변경됨
- SLA 타이머 재개 (보류 시간 제외)

---

## 8. 담당자 이전

### POST /api/v1/tasks/{taskId}/transfer
**설명**: 업무를 다른 담당자에게 이전합니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "toMemberId": 7,
  "reason": "전문가 배정 필요"
}
```

**Response**:
```json
{
  "success": true,
  "data": null
}
```

**비즈니스 규칙**:
- `transferCount` 증가
- TaskTransfer 이력 기록
- 새 담당자에게 알림 발송

**에러 코드**:
- `404 Not Found`: toMemberId가 존재하지 않음

---

## 9. 구독 (Watch)

### POST /api/v1/tasks/{taskId}/watch
**설명**: 업무를 구독합니다. 구독자는 업무 변경 시 알림을 받습니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": null
}
```

---

### DELETE /api/v1/tasks/{taskId}/watch
**설명**: 구독을 해제합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": null
}
```

---

### GET /api/v1/tasks/{taskId}/watching
**설명**: 현재 사용자가 구독 중인지 확인합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": true
}
```

---

## 10. 업무 연결

### POST /api/v1/tasks/{taskId}/relations
**설명**: 다른 업무와 연결합니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "targetTaskId": 50,
  "relationType": "BLOCKS"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "sourceTaskId": 42,
    "targetTaskId": 50,
    "targetTaskTitle": "PG사 연동 API 개선",
    "relationType": "BLOCKS"
  }
}
```

**관계 타입**:
- `BLOCKS`: 현재 업무가 대상 업무를 블로킹 (현재 업무가 완료되어야 대상 진행 가능)
- `RELATED`: 관련 업무
- `DUPLICATE`: 중복 업무

---

### GET /api/v1/tasks/{taskId}/relations
**설명**: 연결된 업무 목록을 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "targetTaskId": 50,
      "targetTaskTitle": "PG사 연동 API 개선",
      "relationType": "BLOCKS"
    }
  ]
}
```

---

### DELETE /api/v1/tasks/{taskId}/relations/{relationId}
**설명**: 업무 연결을 삭제합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": null
}
```

---

## 11. AI 담당자 추천

### GET /api/v1/tasks/{taskId}/recommend-assignee
**설명**: AI가 해당 업무에 적합한 담당자를 추천합니다. 팀원의 스킬과 과거 유사 업무 처리 이력을 바탕으로 추천합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "memberId": 5,
      "memberName": "김철수",
      "score": 0.92,
      "reason": "결제 관련 업무 10건 처리 경험, 스킬: [결제, PG 연동]"
    },
    {
      "memberId": 7,
      "memberName": "이영희",
      "score": 0.85,
      "reason": "유사 업무 5건 처리 경험"
    }
  ]
}
```

**비즈니스 규칙**:
- 팀원의 `skills` JSONB 필드와 업무 내용 매칭
- 과거 유사 업무(임베딩 유사도) 처리 이력 반영
- 현재 업무 부하(진행 중인 업무 수) 고려

---

## 비즈니스 규칙 요약

1. **SLA 자동 계산**: Task 생성 시 urgency에 따라 sla_response_deadline, sla_resolve_deadline 자동 설정
2. **SLA 위반 감지**: 스케줄러(SlaCheckScheduler)가 주기적으로 확인하여 sla_response_breached, sla_resolve_breached 업데이트
3. **보류 중 SLA 일시정지**: TaskHold.ended_at = NULL인 동안 SLA 타이머 정지
4. **담당자 이전 제한**: transfer_count > 3일 경우 경고 (과도한 핑퐁 방지)
5. **BLOCKS 관계**: source_task가 DONE 상태가 되어야 target_task를 IN_PROGRESS로 변경 가능
6. **AI 가이드 자동 생성**: Task 생성/할당 시 TaskGuide 자동 생성 (체크리스트, 관련 문서, 유사 업무, 예상 시간)

---

## 프론트엔드 구현 참고

- `apps/web/src/services/tasks.ts`: API 호출 함수
- `apps/web/src/app/tasks/page.tsx`: 칸반 보드 페이지
- Drag & Drop: react-beautiful-dnd 등 사용
- 실시간 업데이트: WebSocket 또는 Polling

---

## 관련 엔티티

- [Task](../data-model/ENTITIES.md#4-task-업무)
- [TaskChecklist](../data-model/ENTITIES.md#5-taskchecklist-체크리스트)
- [TaskTag](../data-model/ENTITIES.md#6-tasktag-태그)
- [TaskComment](../data-model/ENTITIES.md#7-taskcomment-댓글)
- [TaskLog](../data-model/ENTITIES.md#8-tasklog-업무-이력)
- [TaskHold](../data-model/ENTITIES.md#9-taskhold-보류-이력)
- [TaskTransfer](../data-model/ENTITIES.md#10-tasktransfer-담당자-이전-이력)
- [TaskWatcher](../data-model/ENTITIES.md#11-taskwatcher-업무-구독)
- [TaskRelation](../data-model/ENTITIES.md#12-taskrelation-업무-연결)
- [TaskGuide](../data-model/ENTITIES.md#13-taskguide-ai-가이드)
