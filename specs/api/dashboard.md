# 대시보드/통계 API

## 개요
대시보드 화면에 표시할 주요 지표를 조회합니다.

## 엔드포인트

### GET /api/v1/dashboard
**설명**: 대시보드 요약 통계를 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": {
    "inquiries": {
      "total": 150,
      "open": 10,
      "aiAnswered": 100,
      "escalated": 30,
      "resolved": 10
    },
    "tasks": {
      "total": 50,
      "waiting": 5,
      "inProgress": 20,
      "pending": 3,
      "review": 7,
      "done": 15
    },
    "sla": {
      "responseComplianceRate": 95.0,
      "resolveComplianceRate": 97.0,
      "breachedCount": 2
    },
    "aiPerformance": {
      "autoResponseRate": 66.67,
      "avgConfidence": 0.82
    },
    "recentInquiries": [
      {
        "id": 1,
        "senderName": "홍길동",
        "message": "결제가 안 돼요...",
        "status": "AI_ANSWERED",
        "createdAt": "2026-03-31T12:00:00"
      }
    ],
    "myTasks": [
      {
        "id": 42,
        "title": "홍길동 고객 결제 오류 해결",
        "urgency": "HIGH",
        "status": "IN_PROGRESS",
        "slaResolveDeadline": "2026-04-01T12:00:00"
      }
    ]
  }
}
```

**비즈니스 규칙**:
- `inquiries`: 문의 상태별 건수
- `tasks`: 업무 상태별 건수
- `sla`: SLA 준수율 및 위반 건수
- `aiPerformance`: AI 자동답변율 및 평균 신뢰도
- `recentInquiries`: 최근 5건
- `myTasks`: 현재 사용자에게 할당된 미완료 업무

---

## 프론트엔드 구현 참고

- `apps/web/src/services/dashboard.ts`: API 호출 함수
- `apps/web/src/app/dashboard/page.tsx`: 대시보드 페이지
- 차트 라이브러리: recharts, chart.js 등

---

## 관련 엔티티

- [Inquiry](../data-model/ENTITIES.md#2-inquiry-문의)
- [Task](../data-model/ENTITIES.md#4-task-업무)
- [SlaRule](../data-model/ENTITIES.md#14-slarule-sla-규칙)
