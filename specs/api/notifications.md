# 알림 API

## 개요
팀원별 알림을 조회하고 읽음 처리합니다.

## 엔드포인트

### GET /api/v1/notifications
**설명**: 현재 사용자의 알림 목록을 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Query Parameters**:
- `unreadOnly`: true일 경우 읽지 않은 알림만 반환 (기본값: false)
- `page`, `size`: 페이지네이션

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "type": "TASK_ASSIGNED",
      "title": "새 업무가 할당되었습니다",
      "message": "홍길동 고객 결제 오류 해결",
      "link": "/tasks/42",
      "isRead": false,
      "createdAt": "2026-03-31T12:00:00"
    },
    {
      "id": 2,
      "type": "SLA_WARNING",
      "title": "SLA 기한이 임박했습니다",
      "message": "업무 #42의 SLA 기한이 30분 남았습니다",
      "link": "/tasks/42",
      "isRead": false,
      "createdAt": "2026-03-31T11:30:00"
    }
  ]
}
```

**알림 타입**:
- `TASK_ASSIGNED`: 업무 할당
- `TASK_TRANSFERRED`: 업무 이전
- `TASK_COMMENTED`: 댓글 추가 (구독 중인 업무)
- `TASK_STATUS_CHANGED`: 상태 변경 (구독 중인 업무)
- `SLA_WARNING`: SLA 기한 임박
- `SLA_BREACHED`: SLA 위반
- `INQUIRY_ESCALATED`: 문의 에스컬레이션

---

### PATCH /api/v1/notifications/{id}/read
**설명**: 알림을 읽음 처리합니다.

**인증**: 필요 (AGENT, ADMIN, 본인 알림만)

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "isRead": true
  }
}
```

---

### POST /api/v1/notifications/read-all
**설명**: 모든 알림을 읽음 처리합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": null
}
```

---

## 비즈니스 규칙

1. **자동 생성**: 업무 할당, 댓글 추가, SLA 위반 등 이벤트 발생 시 자동 생성
2. **구독**: TaskWatcher에 등록된 사용자에게 해당 업무의 변경사항 알림
3. **SLA 경고**: SLA 기한 30분 전에 알림 발송
4. **읽음 여부**: isRead로 관리, 프론트엔드에서 배지/카운트 표시

---

## 프론트엔드 구현 참고

- `apps/web/src/services/notifications.ts`: API 호출 함수
- 헤더에 알림 아이콘 + 미읽음 카운트 표시
- 실시간 알림: WebSocket 또는 Polling (30초 간격)

---

## 관련 엔티티

- [Notification](../data-model/ENTITIES.md#15-notification-알림)
