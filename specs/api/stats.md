# 통계 API

## 개요
시스템 전체 통계 및 AI 성능, 팀원별 현황을 조회합니다.

## 엔드포인트

### GET /api/v1/stats/overview
**설명**: 전체 시스템 현황을 조회합니다.

**인증**: 필요 (ADMIN)

**Query Parameters**:
- `from` (optional): 조회 시작일 (ISO 8601 date, 기본값: 30일 전)
- `to` (optional): 조회 종료일 (ISO 8601 date, 기본값: 오늘)

**Response**:
```json
{
  "success": true,
  "data": {
    "period": {
      "from": "2026-03-01",
      "to": "2026-03-31"
    },
    "inquiries": {
      "total": 500,
      "byStatus": {
        "OPEN": 10,
        "AI_ANSWERED": 350,
        "ESCALATED": 100,
        "RESOLVED": 40
      }
    },
    "tasks": {
      "total": 150,
      "byStatus": {
        "WAITING": 10,
        "IN_PROGRESS": 50,
        "REVIEW": 20,
        "DONE": 70
      },
      "byUrgency": {
        "LOW": 30,
        "NORMAL": 80,
        "HIGH": 30,
        "CRITICAL": 10
      }
    },
    "sla": {
      "responseComplianceRate": 95.5,
      "resolveComplianceRate": 92.0,
      "responseBreached": 5,
      "resolveBreached": 12
    },
    "avgResolutionTime": {
      "hours": 4.5,
      "byUrgency": {
        "CRITICAL": 2.0,
        "HIGH": 3.5,
        "NORMAL": 5.0,
        "LOW": 8.0
      }
    }
  }
}
```

**비즈니스 규칙**:
- 기간 미지정 시 최근 30일 데이터 조회
- 관리자만 접근 가능

---

### GET /api/v1/stats/ai
**설명**: AI 자동답변 성능 통계를 조회합니다.

**인증**: 필요 (ADMIN)

**Query Parameters**:
- `from` (optional): 조회 시작일
- `to` (optional): 조회 종료일

**Response**:
```json
{
  "success": true,
  "data": {
    "period": {
      "from": "2026-03-01",
      "to": "2026-03-31"
    },
    "autoResponseRate": 70.0,
    "avgConfidence": 0.82,
    "confidenceDistribution": {
      "high": 250,
      "medium": 150,
      "low": 100
    },
    "resolutionRate": {
      "aiOnly": 60.0,
      "humanIntervention": 40.0
    },
    "escalationRate": 20.0,
    "topCategories": [
      {
        "category": "VPN",
        "count": 80,
        "autoResolveRate": 85.0
      },
      {
        "category": "권한요청",
        "count": 60,
        "autoResolveRate": 50.0
      }
    ]
  }
}
```

**비즈니스 규칙**:
- `autoResponseRate`: AI가 답변한 비율
- `confidenceDistribution`: 신뢰도별 분포 (high ≥0.8, medium 0.5~0.8, low <0.5)
- `resolutionRate`: AI만으로 해결된 비율
- `escalationRate`: 에스컬레이션된 비율

---

### GET /api/v1/stats/team
**설명**: 팀원별 업무 현황 통계를 조회합니다.

**인증**: 필요 (ADMIN)

**Query Parameters**:
- `from` (optional): 조회 시작일
- `to` (optional): 조회 종료일

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "memberId": 1,
      "memberName": "김담당",
      "assignedTasks": 25,
      "completedTasks": 18,
      "inProgressTasks": 5,
      "avgResolutionTimeHours": 3.5,
      "slaComplianceRate": 96.0,
      "workload": "NORMAL"
    },
    {
      "memberId": 2,
      "memberName": "이담당",
      "assignedTasks": 30,
      "completedTasks": 20,
      "inProgressTasks": 8,
      "avgResolutionTimeHours": 4.2,
      "slaComplianceRate": 92.0,
      "workload": "HIGH"
    }
  ]
}
```

**비즈니스 규칙**:
- 기간 내 각 담당자에게 할당된 업무 현황
- `workload`: LOW(< 10건), NORMAL(10~20건), HIGH(> 20건)

---

### GET /api/v1/stats/weekly-report
**설명**: 주간 리포트를 조회합니다.

**인증**: 필요 (ADMIN)

**Response**:
```json
{
  "success": true,
  "data": {
    "weekStart": "2026-03-24",
    "weekEnd": "2026-03-30",
    "summary": {
      "totalInquiries": 120,
      "totalTasks": 40,
      "completedTasks": 35,
      "aiAutoResolveRate": 72.0,
      "slaComplianceRate": 94.0
    },
    "highlights": [
      "AI 자동해결률 전주 대비 5% 상승",
      "VPN 문의 급증 (30건 → 50건)",
      "SLA 준수율 목표(95%) 달성"
    ],
    "topIssues": [
      {
        "category": "VPN",
        "count": 50,
        "trend": "UP"
      }
    ],
    "topPerformers": [
      {
        "memberName": "김담당",
        "completedTasks": 20,
        "slaComplianceRate": 98.0
      }
    ]
  }
}
```

**비즈니스 규칙**:
- 매주 월요일 기준으로 지난 주 데이터 집계
- 주요 지표, 하이라이트, 주요 이슈, 우수 담당자 포함

---

## 관련 엔티티

- [Inquiry](../data-model/ENTITIES.md#2-inquiry-문의)
- [Task](../data-model/ENTITIES.md#4-task-업무)
- [Member](../data-model/ENTITIES.md#8-member-팀원)
- [SlaRule](../data-model/ENTITIES.md#14-slarule-sla-규칙)

---

## 프론트엔드 구현 참고

- `apps/web/src/services/stats.ts`: API 호출 함수
- `apps/web/src/app/stats/page.tsx`: 통계 페이지
- 차트 라이브러리: recharts, chart.js 등
