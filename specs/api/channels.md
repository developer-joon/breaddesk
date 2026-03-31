# 채널 설정/웹훅 API

## 개요
멀티채널(Slack, Teams, Email 등) 통합 설정을 관리합니다. 웹훅 URL, 인증 토큰 등을 저장하고 테스트합니다.

## 엔드포인트

### GET /api/v1/channels
**설명**: 전체 채널 목록을 조회합니다.

**인증**: 필요 (ADMIN)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "channelType": "slack",
      "webhookUrl": "https://hooks.slack.com/services/...",
      "authToken": "xoxb-***",
      "isActive": true,
      "config": {
        "displayName": "Slack",
        "icon": "slack"
      },
      "createdAt": "2026-03-01T12:00:00",
      "updatedAt": "2026-03-31T10:00:00"
    },
    {
      "id": 2,
      "channelType": "teams",
      "webhookUrl": null,
      "isActive": false,
      "config": {
        "displayName": "Microsoft Teams",
        "icon": "teams"
      },
      "createdAt": "2026-03-01T12:00:00"
    }
  ]
}
```

---

### GET /api/v1/channels/active
**설명**: 활성화된 채널만 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "channelType": "slack",
      "isActive": true,
      "config": {
        "displayName": "Slack",
        "icon": "slack"
      }
    }
  ]
}
```

---

### GET /api/v1/channels/{id}
**설명**: 특정 채널의 상세 정보를 조회합니다.

**인증**: 필요 (ADMIN)

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "channelType": "slack",
    "webhookUrl": "https://hooks.slack.com/services/...",
    "authToken": "xoxb-***",
    "isActive": true,
    "config": {
      "displayName": "Slack",
      "icon": "slack",
      "defaultChannel": "#support"
    },
    "createdAt": "2026-03-01T12:00:00",
    "updatedAt": "2026-03-31T10:00:00"
  }
}
```

**에러 코드**:
- `404 Not Found`: 채널을 찾을 수 없음

---

### POST /api/v1/channels
**설명**: 새로운 채널을 생성합니다.

**인증**: 필요 (ADMIN)

**Request Body**:
```json
{
  "channelType": "slack",
  "webhookUrl": "https://hooks.slack.com/services/...",
  "authToken": "xoxb-123456...",
  "isActive": true,
  "config": {
    "displayName": "Slack",
    "icon": "slack",
    "defaultChannel": "#support"
  }
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "channelType": "slack",
    "webhookUrl": "https://hooks.slack.com/services/...",
    "isActive": true,
    "createdAt": "2026-03-31T12:00:00"
  }
}
```

**에러 코드**:
- `400 Bad Request`: channelType이 중복되거나 유효하지 않음

---

### PUT /api/v1/channels/{id}
**설명**: 채널 설정을 수정합니다.

**인증**: 필요 (ADMIN)

**Request Body**: POST와 동일

**Response**: 수정된 채널 반환

**에러 코드**:
- `404 Not Found`: 채널을 찾을 수 없음

---

### DELETE /api/v1/channels/{id}
**설명**: 채널을 삭제합니다.

**인증**: 필요 (ADMIN)

**Response** (204 No Content)

**에러 코드**:
- `404 Not Found`: 채널을 찾을 수 없음

---

### POST /api/v1/channels/{id}/test
**설명**: 웹훅 연결을 테스트합니다. 설정된 웹훅 URL로 테스트 메시지를 전송합니다.

**인증**: 필요 (ADMIN)

**Response**:
```json
{
  "success": true,
  "data": "Webhook URL configured: https://hooks.slack.com/services/..."
}
```

**에러 응답 (웹훅 URL 미설정)**:
```json
{
  "success": false,
  "data": "Webhook URL is not configured"
}
```

---

## 지원되는 채널 타입

### slack
**Config 예시**:
```json
{
  "displayName": "Slack",
  "icon": "slack",
  "defaultChannel": "#support",
  "threadReplies": true
}
```

**필수 설정**:
- `webhookUrl`: Slack Incoming Webhook URL
- `authToken`: Bot Token (xoxb-...)

---

### teams
**Config 예시**:
```json
{
  "displayName": "Microsoft Teams",
  "icon": "teams",
  "teamId": "...",
  "channelId": "..."
}
```

**필수 설정**:
- `webhookUrl`: Teams Incoming Webhook URL

---

### email
**Config 예시**:
```json
{
  "displayName": "Email",
  "icon": "email",
  "smtpHost": "smtp.gmail.com",
  "smtpPort": 587,
  "fromEmail": "support@company.com"
}
```

**필수 설정**:
- `authToken`: SMTP 비밀번호 또는 App Password
- `config.smtpHost`, `config.smtpPort`, `config.fromEmail`

---

## 웹훅 수신 (Webhook Receiver)

**엔드포인트**: `POST /api/v1/webhooks/{channelType}`
- 예: `/api/v1/webhooks/slack`, `/api/v1/webhooks/teams`
- 외부 채널에서 메시지를 수신하여 Inquiry로 변환
- 자세한 내용은 [WebhookController](./webhooks.md) 참고

---

## 비즈니스 규칙

1. **중복 방지**: channelType은 고유해야 함 (UNIQUE 제약)
2. **보안**: authToken은 암호화하여 저장 권장
3. **활성화 조건**: webhookUrl 또는 필수 config가 없으면 isActive=false
4. **테스트 메시지**: "BreadDesk 테스트 메시지입니다." 전송

---

## 프론트엔드 구현 참고

- `apps/web/src/services/channels.ts`: API 호출 함수
- `apps/web/src/app/settings/page.tsx`: 채널 설정 UI (ADMIN 전용)
- Webhook URL 입력, 테스트 버튼, 활성화 토글

---

## 관련 엔티티

- [ChannelConfig](../data-model/ENTITIES.md#21-channelconfig-채널-설정)
- [Inquiry](../data-model/ENTITIES.md#2-inquiry-문의)
