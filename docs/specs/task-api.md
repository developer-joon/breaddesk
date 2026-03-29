# Task API 명세 - 업무 관리 및 칸반

## 개요
업무 CRUD, 칸반 보드 조회, 할당, 태그, 체크리스트 관리 API

**Base URL**: `/api/v1`

---

## 1. 업무 생성

### `POST /tasks`

#### Request
```json
{
  "title": "VPN 접속 불가 - 김철수",
  "description": "사용자가 VPN 클라이언트 재설치 후에도 접속 불가 문제 지속",
  "type": "INFRA",
  "urgency": "HIGH",
  "requesterName": "김철수",
  "requesterEmail": "cskim@company.com",
  "inquiryId": 42,              // optional - 문의에서 에스컬레이션된 경우
  "dueDate": "2026-04-05",      // optional - ISO 8601 date
  "estimatedHours": 2.0,        // optional
  "tags": ["vpn", "network"],   // optional
  "checklist": [                // optional - AI 생성 or 수동
    "VPN 클라이언트 로그 확인",
    "방화벽 규칙 점검",
    "사용자 계정 권한 확인"
  ]
}
```

**필드 설명**
- `type`: `DEVELOPMENT` | `ACCESS` | `INFRA` | `FIREWALL` | `DEPLOY` | `INCIDENT` | `GENERAL`
- `urgency`: `LOW` | `NORMAL` | `HIGH` | `CRITICAL`

#### Response 201 Created
```json
{
  "id": 123,
  "title": "VPN 접속 불가 - 김철수",
  "description": "...",
  "type": "INFRA",
  "urgency": "HIGH",
  "status": "WAITING",
  "requesterName": "김철수",
  "requesterEmail": "cskim@company.com",
  "assigneeId": null,
  "assigneeName": null,
  "inquiryId": 42,
  "aiSummary": null,
  "dueDate": "2026-04-05",
  "estimatedHours": 2.0,
  "actualHours": null,
  "tags": ["vpn", "network"],
  "checklist": [
    {
      "id": 1,
      "text": "VPN 클라이언트 로그 확인",
      "isDone": false,
      "sortOrder": 0
    },
    {
      "id": 2,
      "text": "방화벽 규칙 점검",
      "isDone": false,
      "sortOrder": 1
    },
    {
      "id": 3,
      "text": "사용자 계정 권한 확인",
      "isDone": false,
      "sortOrder": 2
    }
  ],
  "sla": {
    "responseDeadline": "2026-03-29T12:58:00Z",
    "resolveDeadline": "2026-03-30T10:58:00Z",
    "respondedAt": null,
    "responseBreached": false,
    "resolveBreached": false
  },
  "createdAt": "2026-03-29T10:58:00Z",
  "startedAt": null,
  "completedAt": null,
  "updatedAt": "2026-03-29T10:58:00Z"
}
```

#### Error 400 Bad Request
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid task type",
  "details": {
    "field": "type",
    "value": "INVALID_TYPE",
    "allowedValues": ["DEVELOPMENT", "ACCESS", "INFRA", "FIREWALL", "DEPLOY", "INCIDENT", "GENERAL"]
  }
}
```

---

## 2. 업무 목록 조회 (필터/정렬/페이지네이션)

### `GET /tasks`

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `status` | String | N | 상태 필터 (`,` 구분 다중) | `WAITING,IN_PROGRESS` |
| `type` | String | N | 유형 필터 (`,` 구분 다중) | `INFRA,FIREWALL` |
| `urgency` | String | N | 긴급도 필터 (`,` 구분 다중) | `HIGH,CRITICAL` |
| `assigneeId` | Long | N | 할당자 ID | `5` |
| `requesterId` | Long | N | 요청자 ID | `12` |
| `tags` | String | N | 태그 필터 (`,` 구분, AND 조건) | `vpn,network` |
| `search` | String | N | 제목/설명 검색 | `VPN` |
| `dueDateFrom` | Date | N | 마감일 시작 | `2026-03-29` |
| `dueDateTo` | Date | N | 마감일 끝 | `2026-04-05` |
| `sort` | String | N | 정렬 (기본: `createdAt,desc`) | `urgency,asc` |
| `page` | Int | N | 페이지 번호 (0부터) | `0` |
| `size` | Int | N | 페이지 크기 (기본: 20, 최대: 100) | `20` |

**정렬 가능 필드**
- `createdAt` (기본)
- `updatedAt`
- `dueDate`
- `urgency` (CRITICAL → HIGH → NORMAL → LOW)
- `status` (WAITING → IN_PROGRESS → REVIEW → DONE)

#### Response 200 OK
```json
{
  "content": [
    {
      "id": 123,
      "title": "VPN 접속 불가 - 김철수",
      "type": "INFRA",
      "urgency": "HIGH",
      "status": "WAITING",
      "assigneeId": null,
      "assigneeName": null,
      "requesterName": "김철수",
      "dueDate": "2026-04-05",
      "tags": ["vpn", "network"],
      "checklistProgress": {
        "total": 3,
        "completed": 0
      },
      "sla": {
        "responseDeadline": "2026-03-29T12:58:00Z",
        "resolveDeadline": "2026-03-30T10:58:00Z",
        "responseBreached": false,
        "resolveBreached": false
      },
      "createdAt": "2026-03-29T10:58:00Z",
      "updatedAt": "2026-03-29T10:58:00Z"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 42,
    "totalPages": 3
  }
}
```

---

## 3. 업무 상세 조회

### `GET /tasks/{id}`

#### Response 200 OK
```json
{
  "id": 123,
  "title": "VPN 접속 불가 - 김철수",
  "description": "사용자가 VPN 클라이언트 재설치 후에도 접속 불가 문제 지속",
  "type": "INFRA",
  "urgency": "HIGH",
  "status": "WAITING",
  "requesterName": "김철수",
  "requesterEmail": "cskim@company.com",
  "assigneeId": null,
  "assigneeName": null,
  "inquiryId": 42,
  "aiSummary": "VPN 클라이언트 재설치 가이드 제공했으나 미해결",
  "dueDate": "2026-04-05",
  "estimatedHours": 2.0,
  "actualHours": null,
  "tags": ["vpn", "network"],
  "checklist": [
    {
      "id": 1,
      "text": "VPN 클라이언트 로그 확인",
      "isDone": false,
      "sortOrder": 0
    },
    {
      "id": 2,
      "text": "방화벽 규칙 점검",
      "isDone": false,
      "sortOrder": 1
    },
    {
      "id": 3,
      "text": "사용자 계정 권한 확인",
      "isDone": false,
      "sortOrder": 2
    }
  ],
  "attachments": [
    {
      "id": 7,
      "filename": "screenshot.png",
      "mimeType": "image/png",
      "fileSize": 524288,
      "uploadedBy": 3,
      "uploadedAt": "2026-03-29T11:00:00Z"
    }
  ],
  "sla": {
    "responseDeadline": "2026-03-29T12:58:00Z",
    "resolveDeadline": "2026-03-30T10:58:00Z",
    "respondedAt": null,
    "responseBreached": false,
    "resolveBreached": false
  },
  "createdAt": "2026-03-29T10:58:00Z",
  "startedAt": null,
  "completedAt": null,
  "updatedAt": "2026-03-29T10:58:00Z"
}
```

#### Error 404 Not Found
```json
{
  "error": "TASK_NOT_FOUND",
  "message": "Task with id 999 not found"
}
```

---

## 4. 업무 수정

### `PATCH /tasks/{id}`

#### Request
```json
{
  "title": "VPN 접속 불가 - 김철수 (긴급)",  // optional
  "description": "...",                      // optional
  "status": "IN_PROGRESS",                   // optional
  "urgency": "CRITICAL",                     // optional
  "dueDate": "2026-04-03",                   // optional
  "estimatedHours": 3.0,                     // optional
  "actualHours": 1.5                         // optional
}
```

**상태 전이 규칙**
- `WAITING` → `IN_PROGRESS`, `DONE`
- `IN_PROGRESS` → `REVIEW`, `WAITING`, `DONE`
- `REVIEW` → `IN_PROGRESS`, `DONE`
- `DONE` → `IN_PROGRESS` (재오픈)

#### Response 200 OK
```json
{
  "id": 123,
  "status": "IN_PROGRESS",
  "urgency": "CRITICAL",
  // ... (전체 업무 데이터)
  "updatedAt": "2026-03-29T11:30:00Z"
}
```

#### Error 400 Bad Request (잘못된 상태 전이)
```json
{
  "error": "INVALID_STATUS_TRANSITION",
  "message": "Cannot transition from DONE to WAITING",
  "details": {
    "currentStatus": "DONE",
    "requestedStatus": "WAITING",
    "allowedTransitions": ["IN_PROGRESS"]
  }
}
```

---

## 5. 업무 삭제

### `DELETE /tasks/{id}`

#### Response 204 No Content

#### Error 403 Forbidden
```json
{
  "error": "TASK_DELETE_FORBIDDEN",
  "message": "Cannot delete task with status DONE"
}
```

---

## 6. 업무 할당

### `POST /tasks/{id}/assign`

#### Request
```json
{
  "assigneeId": 5  // null = 할당 해제
}
```

#### Response 200 OK
```json
{
  "id": 123,
  "assigneeId": 5,
  "assigneeName": "이영희",
  // ... (전체 업무 데이터)
  "updatedAt": "2026-03-29T11:45:00Z"
}
```

#### Error 404 Not Found
```json
{
  "error": "MEMBER_NOT_FOUND",
  "message": "Member with id 999 not found"
}
```

---

## 7. 칸반 보드 조회

### `GET /tasks/kanban`

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `assigneeId` | Long | N | 특정 담당자 필터 |
| `type` | String | N | 업무 유형 필터 |
| `urgency` | String | N | 긴급도 필터 |

#### Response 200 OK
```json
{
  "columns": {
    "WAITING": [
      {
        "id": 123,
        "title": "VPN 접속 불가 - 김철수",
        "type": "INFRA",
        "urgency": "HIGH",
        "assigneeId": null,
        "assigneeName": null,
        "tags": ["vpn", "network"],
        "checklistProgress": { "total": 3, "completed": 0 },
        "dueDate": "2026-04-05",
        "createdAt": "2026-03-29T10:58:00Z"
      }
    ],
    "IN_PROGRESS": [
      {
        "id": 124,
        "title": "DB 권한 요청 - 박민수",
        "type": "ACCESS",
        "urgency": "NORMAL",
        "assigneeId": 5,
        "assigneeName": "이영희",
        "tags": ["database", "access"],
        "checklistProgress": { "total": 2, "completed": 1 },
        "dueDate": "2026-04-02",
        "createdAt": "2026-03-28T14:20:00Z"
      }
    ],
    "REVIEW": [],
    "DONE": [
      {
        "id": 122,
        "title": "방화벽 규칙 추가 - 정수진",
        "type": "FIREWALL",
        "urgency": "NORMAL",
        "assigneeId": 3,
        "assigneeName": "김태호",
        "tags": ["firewall"],
        "checklistProgress": { "total": 4, "completed": 4 },
        "dueDate": "2026-03-28",
        "completedAt": "2026-03-28T16:30:00Z"
      }
    ]
  },
  "summary": {
    "total": 3,
    "byStatus": {
      "WAITING": 1,
      "IN_PROGRESS": 1,
      "REVIEW": 0,
      "DONE": 1
    },
    "byUrgency": {
      "CRITICAL": 0,
      "HIGH": 1,
      "NORMAL": 2,
      "LOW": 0
    }
  }
}
```

---

## 8. 태그 CRUD

### 8.1 업무에 태그 추가

### `POST /tasks/{id}/tags`

#### Request
```json
{
  "tags": ["urgent-deploy", "project-alpha"]
}
```

#### Response 200 OK
```json
{
  "id": 123,
  "tags": ["vpn", "network", "urgent-deploy", "project-alpha"],
  // ... (전체 업무 데이터)
}
```

### 8.2 업무에서 태그 제거

### `DELETE /tasks/{id}/tags`

#### Request
```json
{
  "tags": ["vpn"]
}
```

#### Response 200 OK
```json
{
  "id": 123,
  "tags": ["network", "urgent-deploy", "project-alpha"],
  // ...
}
```

---

## 9. 체크리스트 CRUD

### 9.1 체크리스트 항목 추가

### `POST /tasks/{id}/checklist`

#### Request
```json
{
  "items": [
    "사용자에게 결과 전달",
    "문서화 업데이트"
  ]
}
```

#### Response 200 OK
```json
{
  "id": 123,
  "checklist": [
    {
      "id": 1,
      "text": "VPN 클라이언트 로그 확인",
      "isDone": false,
      "sortOrder": 0
    },
    {
      "id": 2,
      "text": "방화벽 규칙 점검",
      "isDone": false,
      "sortOrder": 1
    },
    {
      "id": 3,
      "text": "사용자 계정 권한 확인",
      "isDone": false,
      "sortOrder": 2
    },
    {
      "id": 4,
      "text": "사용자에게 결과 전달",
      "isDone": false,
      "sortOrder": 3
    },
    {
      "id": 5,
      "text": "문서화 업데이트",
      "isDone": false,
      "sortOrder": 4
    }
  ]
}
```

### 9.2 체크리스트 항목 상태 변경

### `PATCH /tasks/{id}/checklist/{checklistItemId}`

#### Request
```json
{
  "isDone": true
}
```

#### Response 200 OK
```json
{
  "id": 1,
  "text": "VPN 클라이언트 로그 확인",
  "isDone": true,
  "sortOrder": 0
}
```

### 9.3 체크리스트 항목 삭제

### `DELETE /tasks/{id}/checklist/{checklistItemId}`

#### Response 204 No Content

---

## 에러 코드 정리

| HTTP 상태 | 에러 코드 | 설명 |
|-----------|----------|------|
| 400 | `VALIDATION_ERROR` | 요청 데이터 검증 실패 |
| 400 | `INVALID_STATUS_TRANSITION` | 잘못된 상태 전이 |
| 403 | `TASK_DELETE_FORBIDDEN` | 완료된 업무 삭제 불가 |
| 404 | `TASK_NOT_FOUND` | 업무 없음 |
| 404 | `MEMBER_NOT_FOUND` | 팀원 없음 |
| 500 | `INTERNAL_ERROR` | 서버 에러 |

---

**작성일**: 2026-03-29  
**버전**: v1.0
