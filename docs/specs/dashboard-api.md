# Dashboard API 명세 - 대시보드 및 통계

## 개요
대시보드 통계, AI 성과, SLA 현황, 반복 문의 분석 API

**Base URL**: `/api/v1`

---

## 1. 전체 현황 (Overview)

### `GET /stats/overview`

**Authorization**: `Bearer {accessToken}` (헤더, ADMIN만)

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `dateFrom` | Date | N | 시작일 (기본: 30일 전) |
| `dateTo` | Date | N | 종료일 (기본: 오늘) |

#### Response 200 OK
```json
{
  "period": {
    "from": "2026-02-27",
    "to": "2026-03-29"
  },
  "inquiries": {
    "total": 342,
    "aiAnswered": 245,
    "escalated": 67,
    "resolved": 312,
    "avgResponseTime": 8.5,           // 초 (AI)
    "avgResolutionTime": 4.2          // 시간
  },
  "tasks": {
    "total": 89,
    "byStatus": {
      "WAITING": 12,
      "IN_PROGRESS": 18,
      "REVIEW": 5,
      "DONE": 54
    },
    "avgCompletionTime": 5.5,         // 시간
    "slaComplianceRate": 0.89
  },
  "aiPerformance": {
    "autoResolveRate": 0.72,          // 72% AI 자동 해결
    "avgConfidence": 0.78,
    "escalationRate": 0.20            // 20% 에스컬레이션
  }
}
```

---

## 2. AI 성과 분석

### `GET /stats/ai-performance`

**Authorization**: `Bearer {accessToken}` (헤더, ADMIN만)

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
  "overall": {
    "totalInquiries": 342,
    "aiAnswered": 245,
    "autoResolved": 246,              // AI 답변 후 사용자 "해결" 피드백
    "autoResolveRate": 0.72,
    "avgConfidence": 0.78,
    "avgResponseTime": 8.5,           // 초
    "escalationRate": 0.20
  },
  "byConfidenceRange": [
    {
      "range": "0.8 - 1.0",
      "count": 198,
      "autoResolveRate": 0.85
    },
    {
      "range": "0.5 - 0.8",
      "count": 89,
      "autoResolveRate": 0.62
    },
    {
      "range": "0.0 - 0.5",
      "count": 55,
      "autoResolveRate": 0.15
    }
  ],
  "byTopic": [
    {
      "topic": "VPN 접속",
      "count": 45,
      "autoResolveRate": 0.80,
      "avgConfidence": 0.82
    },
    {
      "topic": "권한 요청",
      "count": 38,
      "autoResolveRate": 0.35,
      "avgConfidence": 0.58
    },
    {
      "topic": "비밀번호 초기화",
      "count": 32,
      "autoResolveRate": 0.95,
      "avgConfidence": 0.92
    }
  ],
  "trend": {
    "daily": [
      {
        "date": "2026-03-27",
        "totalInquiries": 15,
        "autoResolved": 11,
        "autoResolveRate": 0.73
      },
      {
        "date": "2026-03-28",
        "totalInquiries": 18,
        "autoResolved": 13,
        "autoResolveRate": 0.72
      },
      {
        "date": "2026-03-29",
        "totalInquiries": 12,
        "autoResolved": 9,
        "autoResolveRate": 0.75
      }
    ]
  }
}
```

**용도**
- AI 답변 품질 모니터링
- 신뢰도 범위별 성과 비교
- 주제별 AI 강점/약점 파악
- 시간 경과에 따른 개선 추이

---

## 3. SLA 현황

### `GET /stats/sla`

**Authorization**: `Bearer {accessToken}` (헤더, ADMIN만)

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `dateFrom` | Date | N | 시작일 (기본: 30일 전) |
| `dateTo` | Date | N | 종료일 (기본: 오늘) |
| `urgency` | String | N | 긴급도 필터 |

#### Response 200 OK
```json
{
  "period": {
    "from": "2026-02-27",
    "to": "2026-03-29"
  },
  "overall": {
    "totalTasks": 89,
    "responseCompliant": 79,
    "responseComplianceRate": 0.89,
    "resolveCompliant": 76,
    "resolveComplianceRate": 0.85
  },
  "byUrgency": [
    {
      "urgency": "CRITICAL",
      "totalTasks": 8,
      "responseCompliant": 7,
      "responseComplianceRate": 0.88,
      "resolveCompliant": 6,
      "resolveComplianceRate": 0.75,
      "avgResponseTime": 25,        // 분
      "avgResolveTime": 180         // 분
    },
    {
      "urgency": "HIGH",
      "totalTasks": 23,
      "responseCompliant": 21,
      "responseComplianceRate": 0.91,
      "resolveCompliant": 20,
      "resolveComplianceRate": 0.87,
      "avgResponseTime": 95,
      "avgResolveTime": 520
    },
    {
      "urgency": "NORMAL",
      "totalTasks": 45,
      "responseCompliant": 42,
      "responseComplianceRate": 0.93,
      "resolveCompliant": 41,
      "resolveComplianceRate": 0.91,
      "avgResponseTime": 180,
      "avgResolveTime": 1200
    },
    {
      "urgency": "LOW",
      "totalTasks": 13,
      "responseCompliant": 13,
      "responseComplianceRate": 1.0,
      "resolveCompliant": 13,
      "resolveComplianceRate": 1.0,
      "avgResponseTime": 300,
      "avgResolveTime": 2400
    }
  ],
  "breaches": {
    "responseBreaches": 10,
    "resolveBreaches": 13,
    "recentBreaches": [
      {
        "taskId": 118,
        "title": "장애: 프로덕션 DB 다운",
        "urgency": "CRITICAL",
        "breachType": "RESOLVE",
        "deadline": "2026-03-29T14:00:00Z",
        "completedAt": "2026-03-29T15:30:00Z",
        "delayMinutes": 90
      }
    ]
  },
  "atRisk": [
    {
      "taskId": 125,
      "title": "VPN 접속 불가 - 김철수",
      "urgency": "HIGH",
      "type": "INFRA",
      "assigneeId": 3,
      "assigneeName": "이영희",
      "responseDeadline": "2026-03-29T18:00:00Z",
      "resolveDeadline": "2026-03-30T16:00:00Z",
      "minutesUntilResponseDeadline": 45,
      "minutesUntilResolveDeadline": 1485,
      "riskLevel": "HIGH"
    }
  ]
}
```

**용도**
- SLA 준수율 모니터링
- 긴급도별 성과 분석
- 초과 건 조기 감지
- 관리자 개입 필요 업무 식별

---

## 4. 반복 문의 분석

### `GET /stats/repeat-inquiries`

**Authorization**: `Bearer {accessToken}` (헤더, ADMIN만)

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `dateFrom` | Date | N | 시작일 (기본: 30일 전) |
| `dateTo` | Date | N | 종료일 (기본: 오늘) |
| `threshold` | Int | N | 최소 반복 횟수 (기본: 3) |
| `limit` | Int | N | 최대 결과 수 (기본: 10) |

#### Response 200 OK
```json
{
  "period": {
    "from": "2026-02-27",
    "to": "2026-03-29"
  },
  "topRepeatTopics": [
    {
      "topic": "VPN 접속 불가",
      "count": 45,
      "uniqueUsers": 12,
      "avgResolutionTime": 2.5,       // 시간
      "autoResolveRate": 0.80,
      "trend": "INCREASING",          // INCREASING | STABLE | DECREASING
      "rootCauseIdentified": false,
      "recentInquiries": [
        {
          "id": 42,
          "sender": "김철수",
          "createdAt": "2026-03-29T11:00:00Z",
          "status": "AI_ANSWERED"
        },
        {
          "id": 38,
          "sender": "최민지",
          "createdAt": "2026-03-28T14:20:00Z",
          "status": "RESOLVED"
        }
      ]
    },
    {
      "topic": "권한 요청",
      "count": 38,
      "uniqueUsers": 32,
      "avgResolutionTime": 6.2,
      "autoResolveRate": 0.35,
      "trend": "STABLE",
      "rootCauseIdentified": true,
      "rootCauseNote": "프로젝트마다 권한 설정이 달라서 자동화 어려움",
      "recentInquiries": [
        {
          "id": 41,
          "sender": "박민수",
          "createdAt": "2026-03-29T09:30:00Z",
          "status": "ESCALATED"
        }
      ]
    }
  ],
  "insights": [
    {
      "type": "HIGH_VOLUME",
      "message": "VPN 접속 불가 문의가 전주 대비 30% 증가했습니다.",
      "recommendation": "VPN 클라이언트 업데이트 공지 필요"
    },
    {
      "type": "LOW_AUTO_RESOLVE",
      "message": "권한 요청의 AI 자동 해결률이 35%로 낮습니다.",
      "recommendation": "권한 요청 프로세스 표준화 고려"
    }
  ]
}
```

**용도**
- 반복 문의 패턴 감지
- 근본 원인 해결 유도
- 지식베이스 개선 우선순위 파악
- 프로세스 자동화 기회 식별

---

## 5. 팀 현황

### `GET /stats/team`

**Authorization**: `Bearer {accessToken}` (헤더)

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `dateFrom` | Date | N | 시작일 (기본: 30일 전) |
| `dateTo` | Date | N | 종료일 (기본: 오늘) |

#### Response 200 OK
```json
{
  "period": {
    "from": "2026-02-27",
    "to": "2026-03-29"
  },
  "members": [
    {
      "id": 3,
      "name": "이영희",
      "role": "AGENT",
      "currentTasks": 3,
      "tasksCompleted": 17,
      "avgCompletionTime": 5.2,       // 시간
      "slaComplianceRate": 0.92,
      "workload": "NORMAL"            // LOW | NORMAL | HIGH | OVERLOADED
    },
    {
      "id": 5,
      "name": "김태호",
      "role": "AGENT",
      "currentTasks": 2,
      "tasksCompleted": 14,
      "avgCompletionTime": 4.8,
      "slaComplianceRate": 0.88,
      "workload": "LOW"
    },
    {
      "id": 7,
      "name": "정수진",
      "role": "AGENT",
      "currentTasks": 8,
      "tasksCompleted": 12,
      "avgCompletionTime": 6.8,
      "slaComplianceRate": 0.75,
      "workload": "HIGH"
    }
  ],
  "workloadDistribution": {
    "LOW": 3,
    "NORMAL": 5,
    "HIGH": 2,
    "OVERLOADED": 0
  },
  "recommendations": [
    {
      "type": "WORKLOAD_IMBALANCE",
      "message": "정수진님의 업무량이 평균보다 60% 높습니다.",
      "suggestion": "일부 업무를 김태호님에게 재할당 고려"
    }
  ]
}
```

**업무량 기준 (workload)**
- `LOW`: 현재 업무 ≤ 2개
- `NORMAL`: 현재 업무 3~5개
- `HIGH`: 현재 업무 6~8개
- `OVERLOADED`: 현재 업무 ≥ 9개

---

## 6. 주간 리포트

### `GET /stats/weekly-report`

**Authorization**: `Bearer {accessToken}` (헤더, ADMIN만)

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `weekStart` | Date | N | 주 시작일 (기본: 이번 주 월요일) |

#### Response 200 OK
```json
{
  "week": {
    "start": "2026-03-24",
    "end": "2026-03-30"
  },
  "summary": {
    "totalInquiries": 87,
    "aiAutoResolved": 63,
    "escalated": 18,
    "totalTasks": 32,
    "tasksCompleted": 24,
    "slaComplianceRate": 0.89
  },
  "highlights": [
    {
      "type": "IMPROVEMENT",
      "message": "AI 자동 해결률이 전주 대비 5% 증가했습니다 (67% → 72%)"
    },
    {
      "type": "ISSUE",
      "message": "SLA 초과 건이 3건 발생했습니다 (전주: 1건)"
    },
    {
      "type": "ACHIEVEMENT",
      "message": "김태호님이 평균 처리 시간 4.8시간으로 팀 최고 기록 달성"
    }
  ],
  "topPerformers": [
    {
      "memberId": 5,
      "memberName": "김태호",
      "tasksCompleted": 8,
      "avgCompletionTime": 4.8,
      "slaComplianceRate": 0.88
    }
  ],
  "topIssues": [
    {
      "topic": "VPN 접속 불가",
      "count": 12,
      "trend": "INCREASING"
    }
  ],
  "recommendations": [
    "VPN 클라이언트 업데이트 공지 필요",
    "정수진님 업무 재분배 검토",
    "권한 요청 프로세스 문서화 개선"
  ]
}
```

---

## 7. 데이터 내보내기

### `GET /stats/export`

**Authorization**: `Bearer {accessToken}` (헤더, ADMIN만)

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `type` | String | Y | `INQUIRIES` | `TASKS` | `MEMBERS` |
| `format` | String | N | `CSV` | `JSON` (기본: `CSV`) |
| `dateFrom` | Date | N | 시작일 |
| `dateTo` | Date | N | 종료일 |

#### Response 200 OK
**Content-Type**: `text/csv` 또는 `application/json`  
**Content-Disposition**: `attachment; filename="inquiries_2026-02-27_2026-03-29.csv"`

**CSV 예시 (문의 내보내기)**
```csv
ID,Channel,Sender,Email,Message,Status,AI Confidence,Resolved By,Created At,Resolved At
42,slack,김철수,cskim@company.com,"VPN 접속이 안 되는데...",AI_ANSWERED,0.85,AI,2026-03-29T11:00:00Z,2026-03-29T11:01:30Z
41,teams,박민수,mspark@company.com,"프로덕션 DB 권한 필요",ESCALATED,0.35,,2026-03-29T09:30:00Z,
```

**JSON 예시**
```json
[
  {
    "id": 42,
    "channel": "slack",
    "sender": "김철수",
    "senderEmail": "cskim@company.com",
    "message": "VPN 접속이 안 되는데...",
    "status": "AI_ANSWERED",
    "aiConfidence": 0.85,
    "resolvedBy": "AI",
    "createdAt": "2026-03-29T11:00:00Z",
    "resolvedAt": "2026-03-29T11:01:30Z"
  }
]
```

---

## 8. 실시간 대시보드 (간단 버전)

### `GET /stats/realtime`

**Authorization**: `Bearer {accessToken}` (헤더)

#### Response 200 OK
```json
{
  "timestamp": "2026-03-29T16:30:00Z",
  "currentInquiries": {
    "aiProcessing": 3,
    "waitingForAgent": 7
  },
  "currentTasks": {
    "WAITING": 12,
    "IN_PROGRESS": 18,
    "REVIEW": 5
  },
  "slaAlerts": {
    "responseDeadlineApproaching": 2,
    "resolveDeadlineApproaching": 5,
    "breached": 1
  },
  "activeMembers": 8,
  "lastUpdated": "2026-03-29T16:30:00Z"
}
```

**용도**
- 대시보드 상단 실시간 요약
- 5초마다 폴링 또는 WebSocket 구독 (Phase 2+)

---

## 에러 코드 정리

| HTTP 상태 | 에러 코드 | 설명 |
|-----------|----------|------|
| 400 | `VALIDATION_ERROR` | 요청 데이터 검증 실패 |
| 403 | `ADMIN_REQUIRED` | 관리자 권한 필요 |
| 500 | `INTERNAL_ERROR` | 서버 에러 |

---

## 향후 확장 (Phase 2+)

### 실시간 업데이트
- WebSocket 또는 SSE (Server-Sent Events)
- 업무 상태 변경 시 대시보드 자동 갱신

### 커스텀 대시보드
- 위젯 배치 커스터마이징
- 개인별 즐겨찾기 차트

### 예측 분석
- AI 기반 문의량 예측 (다음 주 트렌드)
- 업무 완료 시간 예측
- SLA 초과 위험 예측

---

**작성일**: 2026-03-29  
**버전**: v1.0
