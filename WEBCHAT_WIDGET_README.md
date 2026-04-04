# 🍞 BreadDesk 웹챗 위젯

고객 웹사이트에 한 줄의 코드로 임베드할 수 있는 AI 기반 채팅 위젯입니다.

## 📌 주요 기능

- ✅ **간편한 임베드**: `<script>` 태그 한 줄로 설치
- ✅ **AI 자동응답**: 메시지 전송 즉시 AI가 답변 (RAG 기반)
- ✅ **상담원 연동**: AI confidence 부족 시 자동 에스컬레이션 + 30초 폴링
- ✅ **세션 유지**: sessionStorage로 새로고침해도 대화 유지
- ✅ **반응형 디자인**: 모바일/데스크톱 모두 지원
- ✅ **다크모드 지원**: 시스템 설정 자동 감지
- ✅ **외부 도메인 허용**: CORS 설정으로 어떤 웹사이트에서도 사용 가능

## 🚀 사용 방법

### 1. 웹사이트에 임베드

웹사이트의 `</body>` 태그 직전에 아래 코드를 추가하세요:

```html
<script src="https://breaddesk.k6s.app/webchat-widget.js" data-server="https://breaddesk.k6s.app"></script>
```

### 2. 테스트

테스트 페이지: [webchat-test.html](./webchat-test.html)

브라우저에서 열어보면 우하단에 🍞 버튼이 표시됩니다.

## 📡 API 엔드포인트

### 1. 세션 생성
```
POST /api/v1/webchat/sessions
Content-Type: application/json

{
  "senderName": "홍길동",
  "senderEmail": "hong@example.com"  // 선택
}

Response:
{
  "sessionId": "uuid",
  "token": "webchat_uuid"
}
```

### 2. 메시지 전송
```
POST /api/v1/webchat/sessions/{sessionId}/messages
Content-Type: application/json

{
  "message": "배송은 얼마나 걸리나요?"
}

Response:
{
  "messageId": 123,
  "role": "USER",
  "message": "배송은 얼마나 걸리나요?",
  "aiResponse": "일반적으로 2-3일 소요됩니다...",  // AI 답변 (있으면)
  "aiConfidence": 0.85,
  "createdAt": "2026-04-04T10:30:00"
}
```

### 3. 메시지 히스토리
```
GET /api/v1/webchat/sessions/{sessionId}/messages

Response: [
  {
    "messageId": 123,
    "role": "USER",
    "message": "배송은 얼마나 걸리나요?",
    "createdAt": "2026-04-04T10:30:00"
  },
  {
    "messageId": 124,
    "role": "AI",
    "message": "일반적으로 2-3일 소요됩니다...",
    "createdAt": "2026-04-04T10:30:02"
  }
]
```

## 🏗️ 구현 구조

### 백엔드 (Spring Boot)

```
apps/api/src/main/java/com/breadlab/breaddesk/webchat/
├── WebchatController.java       # REST API 엔드포인트
├── WebchatService.java          # 비즈니스 로직
└── dto/
    ├── WebchatSessionRequest.java
    ├── WebchatSessionResponse.java
    ├── WebchatMessageRequest.java
    └── WebchatMessageResponse.java
```

**핵심 로직:**
1. 첫 메시지 → `Inquiry` 엔티티 생성 (`channel: "WEBCHAT"`, `channelMeta: sessionId`)
2. `AIAnswerService.tryAutoAnswer()` 호출 → AI 자동응답 시도
3. AI confidence >= 0.7 → 즉시 답변 반환 (`status: AI_ANSWERED`)
4. AI confidence < 0.7 → 에스컬레이션 (`status: ESCALATED`)
5. 상담원이 답변하면 클라이언트가 30초 폴링으로 확인

### 프론트엔드 (Vanilla JS)

```
apps/web/public/webchat-widget.js  # 자체 포함형 위젯
```

**주요 기능:**
- 플로팅 버튼 (🍞) + 채팅창 UI
- sessionStorage로 세션 유지
- 30초 폴링으로 새 메시지 확인
- Shadow DOM 사용 (호스트 페이지 CSS 충돌 방지)
- 인라인 CSS (외부 의존성 없음)

### SecurityConfig

```java
// 웹챗 API는 인증 불필요
.requestMatchers("/api/v1/webchat/**").permitAll()

// 웹챗용 CORS: 모든 도메인 허용
CorsConfiguration webchatConfig = new CorsConfiguration();
webchatConfig.setAllowedOriginPatterns(List.of("*"));
```

## 🔄 동작 흐름

```
1. 고객 웹사이트에서 위젯 로드
   └─> 플로팅 버튼 표시

2. 고객이 채팅 시작 버튼 클릭
   └─> POST /webchat/sessions → sessionId 생성
   └─> sessionStorage에 저장

3. 고객이 메시지 전송
   └─> POST /webchat/sessions/{id}/messages
   └─> 내부적으로 Inquiry 생성 (channel: "WEBCHAT")
   └─> AIAnswerService.tryAutoAnswer() 호출
       ├─ VectorSearchService로 관련 문서 검색 (RAG)
       ├─ LLM에 질문 + 문서 전달
       └─ AI 답변 + confidence 반환
   
   ├─ [AI confidence >= 0.7]
   │  └─> 즉시 AI 답변 반환
   │  └─> status: AI_ANSWERED
   
   └─ [AI confidence < 0.7]
      └─> 에스컬레이션 (상담원 배정)
      └─> status: ESCALATED

4. 30초마다 폴링
   └─> GET /webchat/sessions/{id}/messages
   └─> 상담원 답변 있으면 채팅창에 표시
```

## 📊 데이터베이스 스키마

기존 `inquiries` 테이블 사용:
- `channel`: "WEBCHAT"
- `channel_meta`: sessionId (JSON)
- `sender_name`: 사용자 입력 이름 (기본값: "웹챗 사용자")
- `sender_email`: 사용자 입력 이메일 (선택)
- `message`: 첫 메시지 내용
- `ai_response`: AI 답변
- `ai_confidence`: AI 신뢰도
- `status`: OPEN → AI_ANSWERED / ESCALATED

## 🎨 커스터마이징

### 테마 색상 변경

`webchat-widget.js` 파일에서 CSS 수정:

```css
/* 기본 테마: 파란색 (#3B82F6) */
#breaddesk-float-btn {
  background: #3B82F6;  /* 여기 수정 */
}

.breaddesk-header {
  background: #3B82F6;  /* 여기 수정 */
}
```

### 폴링 주기 변경

```javascript
// 30초 → 10초로 변경
}, 10000);  // 기본값: 30000
```

## 🧪 테스트

### 1. 로컬 테스트

```bash
# webchat-test.html을 브라우저로 열기
open webchat-test.html
```

### 2. API 직접 호출

```bash
# 세션 생성
curl -X POST https://breaddesk.k6s.app/api/v1/webchat/sessions \
  -H "Content-Type: application/json" \
  -d '{"senderName":"테스트","senderEmail":"test@example.com"}'

# 메시지 전송
curl -X POST https://breaddesk.k6s.app/api/v1/webchat/sessions/{sessionId}/messages \
  -H "Content-Type: application/json" \
  -d '{"message":"배송 문의입니다"}'

# 히스토리 조회
curl https://breaddesk.k6s.app/api/v1/webchat/sessions/{sessionId}/messages
```

## 🚢 배포

```bash
# 백엔드 빌드
cd apps/api
./gradlew build -x test --no-daemon

# Docker 이미지 빌드 + 푸시
podman build -t ghcr.io/developer-joon/breaddesk-api:latest -f apps/api/Dockerfile apps/api/
podman push ghcr.io/developer-joon/breaddesk-api:latest

# 프론트엔드 (위젯 포함)
podman build --no-cache -t ghcr.io/developer-joon/breaddesk-web:latest -f apps/web/Dockerfile .
podman push ghcr.io/developer-joon/breaddesk-web:latest

# K8s 재시작
kubectl rollout restart deployment/breaddesk-api -n openclaw
kubectl rollout restart deployment/breaddesk-web -n openclaw
```

## 📈 향후 개선사항

- [ ] 실시간 웹소켓 연결 (폴링 대신)
- [ ] 타이핑 인디케이터
- [ ] 파일 첨부 지원
- [ ] 대화 만족도 평가
- [ ] 위젯 커스터마이징 옵션 (색상, 위치, 언어)
- [ ] JWT 기반 인증 강화
- [ ] 메시지 읽음 표시
- [ ] 상담원 온라인 상태 표시

## 📝 라이센스

MIT License

---

**개발 완료일**: 2026-04-04  
**개발자**: Brad (OpenClaw Agent)  
**커밋**: `180ad1d` - feat: 웹챗 위젯 구현
