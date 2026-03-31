# Jira 연동

## 개요
문의 에스컬레이션 시 Jira 이슈를 자동 생성하고, 상태를 양방향 동기화합니다.

## 목적
- 내부 칸반 시스템 없이 Jira를 업무 관리 도구로 활용
- 문의 에스컬레이션 플로우를 Jira 이슈 생성으로 확장
- BreadDesk와 Jira 간 실시간 상태 동기화로 업무 효율성 향상

## 설정 항목

### 필수 설정
- **Jira Cloud URL**: `https://your-domain.atlassian.net`
- **API Token**: Jira 서비스 계정의 API Token (보안 저장)
- **Service Account Email**: API Token 소유자 이메일
- **프로젝트 키**: Jira 프로젝트 키 (예: `SUPPORT`)

### 선택 설정
- **이슈 유형 매핑**: BreadDesk 문의 타입 → Jira Issue Type
  - 기본값: `Task`
  - 커스텀 가능: `Bug`, `Story`, `Epic` 등
- **우선순위 매핑**: BreadDesk 긴급도 → Jira Priority
  - `URGENT` → `Highest`
  - `HIGH` → `High`
  - `MEDIUM` → `Medium`
  - `LOW` → `Low`
- **상태 매핑**: Jira 상태 → BreadDesk 문의 상태
  - `To Do`, `Open` → `PENDING`
  - `In Progress` → `IN_PROGRESS`
  - `Done`, `Closed` → `CLOSED`
- **자동 에스컬레이션**: SLA 위반 시 자동으로 Jira 이슈 생성 (ON/OFF)

## 플로우

### 1. 문의 에스컬레이션 → Jira 이슈 자동 생성
- 사용자가 문의 상세 페이지에서 "Jira 이슈 생성" 버튼 클릭
- BreadDesk API → Jira API 호출 (POST `/rest/api/3/issue`)
- 생성된 Jira 이슈 키와 URL을 DB에 저장 (`JiraIssueLink`)
- BreadDesk 문의에 Jira 이슈 링크 표시

**생성되는 Jira 이슈 내용:**
- **Summary**: `[BreadDesk #{inquiry_id}] {inquiry_title}`
- **Description**: 문의 내용 + 문의자 정보 + 채널 정보
- **Labels**: `breaddesk`, `support`
- **Priority**: 긴급도 매핑에 따라
- **Assignee**: 미정 (Jira 기본 규칙 적용)

### 2. Jira 이슈 상태 변경 → BreadDesk 문의 상태 동기화
- Jira Webhook 설정: 이슈 상태 변경 이벤트 수신
- Webhook URL: `POST /api/v1/webhooks/jira`
- 검증: Webhook Secret 또는 Jira IP 화이트리스트
- 상태 매핑에 따라 BreadDesk 문의 상태 자동 업데이트
- 내부 메모로 동기화 이력 기록

### 3. Jira 댓글 → BreadDesk 메시지 동기화 (선택적)
- Jira Webhook: 댓글 추가 이벤트 수신
- 댓글 내용을 BreadDesk 내부 메모로 자동 추가
- 댓글 작성자 정보 포함

### 4. BreadDesk에서 Jira 이슈 링크 표시
- 문의 상세 페이지에 Jira 이슈 카드 표시
- 이슈 키, 제목, 상태, 담당자, 우선순위 표시
- "Jira에서 보기" 버튼 (새 탭으로 열기)

## API

### Backend Endpoints

#### 1. Jira 설정 저장
```
POST /api/v1/integrations/jira/config
Authorization: Bearer {token}
Content-Type: application/json

{
  "jiraUrl": "https://your-domain.atlassian.net",
  "email": "service-account@example.com",
  "apiToken": "your-api-token",
  "projectKey": "SUPPORT",
  "issueTypeMapping": {
    "default": "Task",
    "bug": "Bug",
    "feature": "Story"
  },
  "priorityMapping": {
    "URGENT": "Highest",
    "HIGH": "High",
    "MEDIUM": "Medium",
    "LOW": "Low"
  },
  "statusMapping": {
    "To Do": "PENDING",
    "In Progress": "IN_PROGRESS",
    "Done": "CLOSED"
  },
  "autoEscalateOnSlaViolation": false
}

Response: 200 OK
{
  "id": 1,
  "jiraUrl": "https://your-domain.atlassian.net",
  "email": "service-account@example.com",
  "projectKey": "SUPPORT",
  "enabled": true,
  "createdAt": "2026-03-31T20:00:00Z"
}
```

#### 2. Jira 설정 조회
```
GET /api/v1/integrations/jira/config
Authorization: Bearer {token}

Response: 200 OK
{
  "id": 1,
  "jiraUrl": "https://your-domain.atlassian.net",
  "email": "service-account@example.com",
  "projectKey": "SUPPORT",
  "enabled": true,
  "issueTypeMapping": {...},
  "priorityMapping": {...},
  "statusMapping": {...},
  "autoEscalateOnSlaViolation": false
}
```

#### 3. Jira 연결 테스트
```
POST /api/v1/integrations/jira/test
Authorization: Bearer {token}

Response: 200 OK
{
  "success": true,
  "message": "Jira 연결 성공",
  "projectName": "Support Team",
  "availableIssueTypes": ["Task", "Bug", "Story"]
}

Response: 400 Bad Request
{
  "success": false,
  "message": "인증 실패: Invalid API token"
}
```

#### 4. 수동 Jira 이슈 생성
```
POST /api/v1/inquiries/{inquiryId}/create-jira-issue
Authorization: Bearer {token}
Content-Type: application/json

{
  "issueType": "Task",
  "priority": "High",
  "assigneeAccountId": "5d12345678901234567890ab" // optional
}

Response: 201 Created
{
  "issueKey": "SUPPORT-123",
  "issueUrl": "https://your-domain.atlassian.net/browse/SUPPORT-123",
  "summary": "[BreadDesk #42] 결제 오류 문의",
  "status": "To Do",
  "createdAt": "2026-03-31T20:05:00Z"
}
```

#### 5. Jira Webhook 수신 (내부)
```
POST /api/v1/webhooks/jira
Content-Type: application/json
X-Webhook-Token: {configured-secret}

{
  "webhookEvent": "jira:issue_updated",
  "issue": {
    "key": "SUPPORT-123",
    "fields": {
      "status": {
        "name": "In Progress"
      }
    }
  }
}

Response: 200 OK
```

## 엔티티

### JiraConfig
```java
@Entity
@Table(name = "jira_configs")
public class JiraConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jiraUrl;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String apiToken; // 암호화 저장

    @Column(nullable = false)
    private String projectKey;

    @Column(columnDefinition = "jsonb")
    private String issueTypeMapping; // JSON

    @Column(columnDefinition = "jsonb")
    private String priorityMapping; // JSON

    @Column(columnDefinition = "jsonb")
    private String statusMapping; // JSON

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean autoEscalateOnSlaViolation = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### JiraIssueLink
```java
@Entity
@Table(name = "jira_issue_links")
public class JiraIssueLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inquiryId;

    @Column(nullable = false, unique = true)
    private String jiraIssueKey; // e.g., "SUPPORT-123"

    @Column(nullable = false)
    private String jiraIssueUrl;

    @Column
    private String summary;

    @Column
    private String status;

    @Column
    private String priority;

    @Column
    private String assigneeDisplayName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime syncedAt; // 마지막 동기화 시간
}
```

## UI

### 설정 > 연동 탭
- **Jira 연동 카드**
  - 연동 상태 표시 (활성화/비활성화)
  - 설정 폼:
    - Jira Cloud URL
    - Service Account Email
    - API Token (마스킹 표시)
    - 프로젝트 키
    - 이슈 유형 매핑 (드롭다운)
    - 우선순위 매핑 (테이블)
    - 상태 매핑 (테이블)
  - "연결 테스트" 버튼 (설정 저장 전 검증)
  - "저장" 버튼

### 문의 상세 페이지
- **Jira 이슈 생성 버튼** (`jira-integration` Feature Flag ON일 때)
  - 위치: 문의 상세 헤더 또는 액션 버튼 영역
  - 조건: Jira 설정이 활성화되어 있고, 아직 Jira 이슈가 생성되지 않은 경우
  - 클릭 시: 이슈 생성 다이얼로그 표시 (이슈 유형, 우선순위 선택)

- **Jira 이슈 카드** (이슈가 생성된 경우)
  - 이슈 키 (링크)
  - 제목
  - 상태 (뱃지)
  - 우선순위 (뱃지)
  - 담당자
  - 마지막 동기화 시간
  - "Jira에서 보기" 버튼 (새 탭으로 열기)

## 구현 계획

### Phase 1: 기본 연동 (MVP)
- [x] Feature Flag 시스템 구축
- [ ] Jira 설정 API 구현
- [ ] 수동 Jira 이슈 생성 기능
- [ ] BreadDesk → Jira 단방향 동기화
- [ ] 설정 페이지 UI 구현
- [ ] 문의 상세 페이지 Jira 이슈 카드 UI

### Phase 2: 양방향 동기화
- [ ] Jira Webhook 수신 엔드포인트
- [ ] Jira 상태 변경 → BreadDesk 문의 상태 동기화
- [ ] Jira 댓글 → BreadDesk 내부 메모 동기화
- [ ] 동기화 이력 기록 및 표시

### Phase 3: 자동화 및 고급 기능
- [ ] SLA 위반 시 자동 Jira 이슈 생성
- [ ] Jira 이슈 정보 주기적 폴링 및 캐싱
- [ ] Jira 담당자 자동 할당 (BreadDesk 담당자 기반)
- [ ] Jira 필드 커스텀 매핑 (Custom Fields)

## 보안 고려사항
- API Token은 암호화하여 DB 저장 (AES-256)
- Jira Webhook은 Secret Token 또는 IP 화이트리스트로 검증
- HTTPS 통신 필수
- 민감한 정보는 로그에 남기지 않음

## 제약사항
- Jira Cloud API만 지원 (Server/Data Center는 향후 고려)
- 양방향 동기화는 충돌 발생 시 Jira 우선 (Last Write Wins)
- Jira API Rate Limit: 초당 10 요청 (Rate Limiter 적용 필요)

## 참고 자료
- [Jira Cloud REST API v3](https://developer.atlassian.com/cloud/jira/platform/rest/v3/intro/)
- [Jira Webhooks](https://developer.atlassian.com/cloud/jira/platform/webhooks/)
