# 멀티채널 통합 (웹훅, Slack, Teams, Email)

## 개요
Slack, Microsoft Teams, Email 등 다양한 채널에서 고객 문의를 수신하고, BreadDesk에서 처리한 답변을 다시 채널로 전송합니다.

## 지원 채널

### 1. Slack
- **수신**: Slack Incoming Webhook으로 메시지 수신
- **발신**: Slack Bot Token으로 스레드 답글 전송
- **설정**: `POST /api/v1/channels` - webhookUrl, authToken (xoxb-...)

### 2. Microsoft Teams
- **수신**: Teams Incoming Webhook으로 메시지 수신
- **발신**: Teams Bot API로 답글 전송
- **설정**: `POST /api/v1/channels` - webhookUrl

### 3. Email
- **수신**: SMTP/IMAP으로 이메일 수신 (향후 구현)
- **발신**: SMTP로 답장 전송
- **설정**: `POST /api/v1/channels` - config에 smtpHost, smtpPort, fromEmail

## 플로우

### 수신 (Webhook → Inquiry)

```
1. 외부 채널에서 웹훅 호출
   POST /api/v1/webhooks/slack
   ↓
2. WebhookController.handleSlackWebhook()
   ↓
3. Payload 파싱
   - senderName, message, thread_ts 등 추출
   ↓
4. Inquiry 생성
   POST /api/v1/inquiries
   {
     "channel": "slack",
     "channelMeta": "{\"thread_ts\": \"1234567890.123456\"}",
     "senderName": "홍길동",
     "message": "결제가 안 돼요"
   }
   ↓
5. AI 자동답변 생성 (ai-auto-response 참고)
   ↓
6. aiConfidence >= 0.8이면 채널로 답변 전송
   ChannelService.sendReply(inquiry, aiResponse)
```

### 발신 (Inquiry/Task → Channel)

```
1. AI 답변 또는 상담원 댓글 생성
   ↓
2. ChannelService.sendReply(inquiry, message)
   ↓
3. inquiry.channel에 따라 분기
   - slack: SlackService.sendMessage(channelMeta.thread_ts, message)
   - teams: TeamsService.sendMessage(channelMeta.conversationId, message)
   - email: EmailService.sendReply(senderEmail, message)
   ↓
4. 채널 API 호출
   - Slack: POST https://slack.com/api/chat.postMessage
   - Teams: POST https://graph.microsoft.com/v1.0/teams/.../messages
   - Email: SMTP send
   ↓
5. 실패 시 재시도 (최대 3회)
   - 실패하면 에러 로그 기록 + ADMIN 알림
```

## 관련 API
- `POST /api/v1/webhooks/{channelType}` - 웹훅 수신
- `GET /api/v1/channels` - 채널 설정 조회
- `POST /api/v1/channels` - 채널 설정 생성
- `POST /api/v1/channels/{id}/test` - 웹훅 테스트

## 관련 엔티티
- [ChannelConfig](../data-model/ENTITIES.md#21-channelconfig-채널-설정)
- [Inquiry](../data-model/ENTITIES.md#2-inquiry-문의)

## 비즈니스 규칙

### Slack
- **스레드 답글**: `channelMeta.thread_ts`를 사용하여 같은 스레드에 답글
- **멘션**: `@사용자명` 형식으로 멘션 가능
- **인증**: Bot Token (xoxb-...) 필요
- **권한**: `chat:write`, `chat:write.public` 스코프 필요

### Teams
- **대화 ID**: `channelMeta.conversationId`로 같은 대화에 답글
- **인증**: Webhook URL만으로 발신 가능, 수신은 Bot 등록 필요
- **권한**: Teams Bot 앱 등록 필요

### Email
- **Reply-To**: `In-Reply-To`, `References` 헤더로 스레드 유지
- **인증**: SMTP 비밀번호 또는 App Password
- **보안**: TLS/SSL 사용

### 공통
- **중복 방지**: 같은 메시지를 여러 번 수신하지 않도록 `channelMeta.message_id` 체크
- **재시도**: 채널 API 호출 실패 시 exponential backoff로 재시도
- **비활성 채널**: `isActive=false`인 채널은 수신/발신 안 함

## 엣지 케이스

1. **Slack 스레드 만료**: thread_ts가 너무 오래되면 새 메시지로 전송
2. **Teams 대화 종료**: conversationId가 유효하지 않으면 새 대화 시작
3. **Email 반송**: 이메일 주소가 유효하지 않으면 Inquiry에 에러 기록
4. **웹훅 변경**: webhookUrl 변경 시 기존 Inquiry는 영향 없음 (channelMeta에 저장된 정보 사용)

## 구현 클래스

- `WebhookController` - 웹훅 수신
- `ChannelService` - 채널별 메시지 발송 라우팅
- `SlackService` - Slack API 호출
- `TeamsService` - Teams API 호출
- `EmailService` - SMTP 발송

## 보안

1. **Webhook Signature 검증**: Slack/Teams의 서명 검증으로 위조 방지
2. **Token 암호화**: authToken은 암호화하여 DB 저장
3. **HTTPS 강제**: 웹훅 URL은 HTTPS만 허용
4. **Rate Limiting**: 채널별 API 호출 제한 준수

## 향후 개선 방향

1. **더 많은 채널**: WhatsApp, Telegram, Discord 등
2. **양방향 동기화**: 채널에서 직접 상태 변경 (예: 이모지 반응으로 해결 처리)
3. **멀티채널 통합 뷰**: 여러 채널의 문의를 하나의 대시보드에서 관리
4. **자동 라우팅**: 채널별로 담당 팀 자동 할당
