# SLA 규칙/통계 API

## 개요
SLA(Service Level Agreement) 규칙을 관리하고, 준수율 통계를 조회합니다.

## 엔드포인트

### GET /api/v1/sla/rules
**설명**: 전체 SLA 규칙 목록을 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "urgency": "CRITICAL",
      "responseMinutes": 30,
      "resolveMinutes": 240,
      "isActive": true
    },
    {
      "id": 2,
      "urgency": "HIGH",
      "responseMinutes": 120,
      "resolveMinutes": 1440,
      "isActive": true
    },
    {
      "id": 3,
      "urgency": "NORMAL",
      "responseMinutes": 240,
      "resolveMinutes": 4320,
      "isActive": true
    },
    {
      "id": 4,
      "urgency": "LOW",
      "responseMinutes": 1440,
      "resolveMinutes": 7200,
      "isActive": true
    }
  ]
}
```

---

### PUT /api/v1/sla/rules/{id}
**설명**: SLA 규칙을 수정합니다.

**인증**: 필요 (ADMIN)

**Request Body**:
```json
{
  "responseMinutes": 60,
  "resolveMinutes": 480,
  "isActive": true
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "urgency": "CRITICAL",
    "responseMinutes": 60,
    "resolveMinutes": 480,
    "isActive": true
  }
}
```

**에러 코드**:
- `403 Forbidden`: ADMIN 권한 필요
- `404 Not Found`: 규칙을 찾을 수 없음

---

### GET /api/v1/sla/stats
**설명**: SLA 준수율 통계를 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": {
    "totalTasks": 100,
    "responseBreachedCount": 5,
    "resolveBreachedCount": 3,
    "responseComplianceRate": 95.0,
    "resolveComplianceRate": 97.0,
    "byUrgency": {
      "CRITICAL": {
        "totalTasks": 10,
        "responseBreachedCount": 0,
        "resolveBreachedCount": 0,
        "responseComplianceRate": 100.0,
        "resolveComplianceRate": 100.0
      },
      "HIGH": {
        "totalTasks": 30,
        "responseBreachedCount": 2,
        "resolveBreachedCount": 1,
        "responseComplianceRate": 93.33,
        "resolveComplianceRate": 96.67
      },
      "NORMAL": {
        "totalTasks": 50,
        "responseBreachedCount": 3,
        "resolveBreachedCount": 2,
        "responseComplianceRate": 94.0,
        "resolveComplianceRate": 96.0
      },
      "LOW": {
        "totalTasks": 10,
        "responseBreachedCount": 0,
        "resolveBreachedCount": 0,
        "responseComplianceRate": 100.0,
        "resolveComplianceRate": 100.0
      }
    }
  }
}
```

---

## 비즈니스 규칙

1. **SLA 계산**: Task 생성 시 urgency에 따라 자동 계산
2. **응답 SLA**: 첫 번째 상담원 응답(댓글) 또는 상태 변경 시각
3. **해결 SLA**: 상태가 DONE으로 변경된 시각
4. **위반 감지**: SlaCheckScheduler가 주기적으로 확인 (매 5분)
5. **보류 중 일시정지**: TaskHold 기간은 SLA 시간에서 제외

---

## 관련 엔티티

- [SlaRule](../data-model/ENTITIES.md#14-slarule-sla-규칙)
- [Task](../data-model/ENTITIES.md#4-task-업무)
