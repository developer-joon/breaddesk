# BreadDesk - AI 서비스데스크 + 업무관리 시스템

> **한 줄 요약**: 전사 문의를 AI가 1차 대응하고, 미해결 건은 담당자가 직접 처리하며, 모든 업무를 칸반으로 추적하는 시스템
>
> **설계 철학**: 의존 최소화, 확장 최대화

---

## 1. 개요

### 1.1 왜 만드는가

- 전사 직원의 IT/업무 문의가 Slack, Teams, Jira 등 여러 채널로 흩어짐
- 반복 문의에 매번 사람이 대응 → 비효율
- AI가 1차 대응하고, 사람은 진짜 필요한 것만 처리

### 1.2 핵심 플로우

```
[사용자] ──→ [멀티채널] ──→ [n8n] ──→ [BreadDesk API]
                                            │
                                    ┌───────┴───────┐
                                    ▼               ▼
                              AI 자동답변       업무 생성
                              (RAG 기반)       (에스컬레이션)
                                    │               │
                                해결 → 종료    담당자 대응
                                               │
                                           칸반 추적
                                               │
                                            완료 → 지식 축적
```

### 1.3 사용자 구분

| 역할 | 누가 | 하는 일 |
|------|------|--------|
| **요청자** | 전사 직원 | 문의/요청 |
| **담당자** | IT팀/운영팀 | 업무 처리, 수동 할당 |
| **관리자** | 팀리드 | 대시보드, 통계, 설정 |

---

## 2. 설계 원칙

### 2.1 의존 최소화

| 영역 | 전략 | 교체 가능 |
|------|------|----------|
| **LLM** | Provider 인터페이스 추상화 | Ollama, OpenAI, Claude, vLLM, 사내 LLM |
| **임베딩** | 로컬 모델 기본 | sentence-transformers, OpenAI, Cohere |
| **메신저** | n8n 웹훅 통합 | Slack, Teams, Jira, 메일, 웹 채팅 |
| **지식 소스** | 커넥터 플러그인 | Confluence, Datadog, ArgoCD, AWS, 기타 |
| **벡터 DB** | 추상화 레이어 | pgvector, Milvus, Chroma |
| **DB** | JPA/Hibernate | PostgreSQL, PostgreSQL |
| **인프라** | Docker 컨테이너 | K8s, Docker Compose, 단독 실행 |

### 2.2 확장 최대화

- 새 LLM → Provider 구현체 1개 추가
- 새 채널 → n8n 워크플로우 1개 추가 (코드 변경 없음)
- 새 지식 소스 → Connector 구현체 1개 추가
- 새 업무 유형 → 설정 파일 수정

---

## 3. 기능 상세

### 3.1 문의 접수 & AI 자동답변

#### 접수
```
사용자 문의 → n8n이 채널별 수집 → BreadDesk API (POST /api/v1/inquiries)

요청 데이터:
{
  "channel": "slack",           // 출처 채널
  "channelMeta": {...},         // 채널별 메타 (채널ID, 스레드ID 등)
  "sender": "김철수",
  "senderEmail": "cskim@company.com",
  "message": "VPN 접속이 안 돼요",
  "attachments": [...]
}
```

#### AI 처리 흐름
```
1. 문의 접수
2. 벡터 DB 검색 → 관련 문서 3~5개 조회
3. LLM에 문의 + 관련 문서 전달 → 답변 생성
4. 신뢰도 판단
   ├─ 높음 (≥ 0.8) → 자동 답변 + 사용자에게 "해결됐나요?" 확인
   ├─ 중간 (0.5~0.8) → 자동 답변 + 동시에 담당자 알림
   └─ 낮음 (< 0.5) → "담당자에게 전달합니다" + 업무 자동 생성
5. 사용자 피드백 (해결/미해결)
   └─ 미해결 → 업무 자동 생성 + 에스컬레이션
```

#### 답변 품질 개선 루프
```
담당자가 직접 답변한 내용 → 자동으로 지식베이스에 추가
→ 다음 유사 문의 시 AI가 활용
```

### 3.2 업무 관리

#### 업무 생성 (2가지 경로)
1. **자동**: AI 미해결 문의 → 업무 자동 전환
2. **수동**: 담당자가 직접 생성

#### 업무 데이터
```
{
  "title": "VPN 접속 불가 - 김철수",
  "type": "INFRA",               // 유형
  "urgency": "HIGH",             // 긴급도
  "status": "WAITING",           // 상태
  "assignee": null,              // 할당자 (수동 할당)
  "requester": "김철수",
  "description": "...",
  "aiSummary": "VPN 클라이언트 재설치 가이드 제공했으나 미해결",
  "conversationId": "INQ-42",    // 원본 문의 연결
  "dueDate": null,
  "estimatedHours": 2.0,         // AI 예측
  "checklist": [...]             // AI 생성 체크리스트
}
```

#### 업무 상태 (4단계)
```
WAITING → IN_PROGRESS → REVIEW → DONE
  대기       진행중        리뷰     완료
```

#### 업무 유형
```
DEVELOPMENT   - 개발 (기능, 버그, 개선)
ACCESS        - 권한 요청 (서버, DB, VPN)
INFRA         - 인프라 (VM, 서버, DNS)
FIREWALL      - 방화벽/통신
DEPLOY        - 배포
INCIDENT      - 장애
GENERAL       - 기타 문의
```

#### 할당
- **기본: 수동 할당** — 담당자가 직접 가져가거나 관리자가 배정
- **보조: AI 추천** — 업무 많을 때 "이 사람이 적합합니다" 추천 (강제 아님)

#### 업무 재할당 (전달)
- 담당자가 업무를 다른 사람에게 전달 가능
- 전달 시 **사유** 입력 (필수): "네트워크 쪽이라 인프라팀이 적합"
- 전달 이력 자동 기록 (누가 → 누구에게, 사유, 시간)
- 전달 받은 사람에게 알림 + 이전 대화/작업 이력 자동 포함
- 전달 횟수 통계 (핑퐁 방지 — 3회 이상 전달 시 관리자 알림)

#### AI 작업 가이드 (업무 할당 시 자동 제공)
```
업무가 할당되면 AI가 자동으로 제공:

1. 📋 작업 체크리스트 (유형별 자동 생성)
2. 📚 관련 문서 (RAG 검색 — 지식베이스에서)
3. 🕐 과거 유사 업무 이력
   - 누가, 어떻게, 얼마나 걸렸는지
   - 이전 해결 방법 요약
4. 📖 작업 가이드라인
   - 유형별 표준 절차 (설정 가능)
   - 주의사항, 참고 링크
5. ⏱️ 예상 소요시간 (과거 데이터 기반)
```

#### Jira 이슈 매핑
- 업무 1개 ↔ Jira 이슈 1개 매핑
- **자동 생성**: 업무 생성 시 Jira 이슈 자동 생성 (n8n 경유)
- **양방향 동기화**: 상태 변경 시 양쪽 반영
  - BreadDesk 상태 변경 → Jira 상태 업데이트
  - Jira 상태 변경 → BreadDesk 상태 업데이트
- **연결 표시**: 업무 상세에 Jira 이슈 링크 표시
- 매핑 필드: `jira_issue_key` (예: DESK-42)

### 3.3 지식베이스 (RAG)

#### 커넥터 플러그인 구조
```java
public interface KnowledgeConnector {
    String getSourceType();                    // "confluence", "datadog" 등
    List<Document> fetchDocuments();            // 문서 가져오기
    List<Document> fetchUpdatedSince(Instant lastSync);  // 증분 동기화
    Document transformToStandard(Object raw);  // 통일 포맷 변환
}
```

#### 통일 문서 포맷
```json
{
  "id": "confluence:12345",
  "source": "confluence",
  "title": "VPN 접속 가이드",
  "content": "...",
  "url": "https://wiki.company.com/...",
  "tags": ["vpn", "network"],
  "updatedAt": "2026-03-29T...",
  "embeddings": [...]
}
```

#### 동기화 전략
- **전체 동기화**: 최초 1회 (커넥터 등록 시)
- **증분 동기화**: 주기적 (설정 가능, 기본 1시간)
- **수동 동기화**: 관리자 UI에서 트리거

#### MVP 커넥터
- **Phase 1**: Confluence (가장 지식이 많은 곳)
- **Phase 2**: ArgoCD, AWS 문서
- **Phase 3**: Datadog (런북, 모니터 설명)

### 3.4 통합 검색

- 헤더 검색바에서 문의/업무/지식 전부 한 번에 검색
- 결과를 카테고리별 탭으로 표시 (전체/문의/업무/지식)
- 키워드 하이라이트
- 최근 검색어 저장 (5개)

### 3.5 알림 센터

- 헤더 벨 아이콘 → 알림 목록 드롭다운
- 안 읽은 개수 뱃지 표시
- 알림 유형:
  - 업무 할당됨 / 전달됨
  - SLA 임박 (80%) / 초과
  - 코멘트 달림
  - 문의 에스컬레이션
  - 반복 문의 감지
- 읽음/안읽음 상태 관리
- 외부 알림 연동 (n8n → Slack/텔레그램/메일)

### 3.6 업무 보류 + SLA 일시정지

- 업무 상태에 **PENDING (보류)** 추가
```
WAITING → IN_PROGRESS → PENDING → IN_PROGRESS → REVIEW → DONE
                         보류 (SLA 정지)
```
- 보류 사유 입력 필수: "외부 업체 회신 대기", "승인 대기" 등
- 보류 중 SLA 타이머 **일시정지** → 재개 시 남은 시간 이어서
- 보류 이력 기록 (시작/종료/사유/총 보류 시간)

### 3.7 우선순위 자동 조정

- 같은 유형 문의 3건 이상 → 긴급도 자동 승격 + 관리자 알림
- SLA 80% 도달 → 긴급도 한 단계 올림
- 자동 조정 이력 기록 (수동 오버라이드 가능)

### 3.8 개인 대시보드 (My Page)

- 내 업무 요약 (대기/진행/보류/리뷰 건수)
- 오늘 할 일 (마감 임박 순)
- 최근 완료 업무
- 개인 성과 (처리 건수, 평균 처리시간, AI 도움률)
- 개인 메모 (나만 보는 퀵노트)

### 3.9 SLA 관리

#### SLA 규칙 (긴급도별)
```
CRITICAL: 응답 30분 / 해결 4시간
HIGH:     응답 2시간 / 해결 1일
NORMAL:   응답 4시간 / 해결 3일
LOW:      응답 1일   / 해결 5일
```

- 관리자가 규칙 커스텀 가능
- 타이머 자동 시작 (문의 접수 시점)
- 초과 임박 (80%) → 담당자 경고 알림
- 초과 시 → 관리자에게 에스컬레이션 알림
- 대시보드에 SLA 준수율 표시

### 3.5 유사 문의 감지

- 새 문의 접수 시 벡터 유사도로 과거 문의 검색
- 유사도 ≥ 0.85 → "비슷한 문의가 있었습니다" + 이전 해결 방법 표시
- 반복 문의 패턴 감지 → 통계에 "반복 문의 TOP 10" 제공
- 근본 원인 해결 유도 (같은 문의 3회 이상 → 관리자 알림)

### 3.6 답변 템플릿

- 담당자가 자주 쓰는 답변 저장
- 카테고리별 분류 (권한, VPN, 배포 등)
- 변수 지원: `{{이름}}`, `{{서버명}}` 등 자동 치환
- 템플릿 사용 빈도 통계 → 인기 템플릿 상단 노출

### 3.7 파일 첨부

- 문의/업무에 파일 첨부 (스크린샷, 로그 등)
- 최대 10MB/파일, 5개/건
- 이미지: 인라인 미리보기
- 저장: 로컬 파일시스템 (S3 교체 가능하게 추상화)

### 3.8 태그/라벨

- 업무에 자유 태그 부여 (#프로젝트A, #긴급배포, #DB이관)
- 태그 기반 필터링/검색
- 자주 쓰는 태그 자동 추천
- 태그별 통계 (어떤 주제 업무가 많은지)

### 3.9 대시보드

#### 담당자 화면
- **칸반 보드** — 대기/진행/리뷰/완료
- **내 업무** — 나에게 할당된 업무 목록
- **문의 내역** — AI 대화 로그 + 에스컬레이션 건
- **답변 작성** — 에스컬레이션 건 직접 답변 (→ 사용자에게 전달)

#### 관리자 화면
- **팀 현황** — 전체 업무 분배 현황
- **통계** — 문의량, 해결률, AI 자동 답변률, 평균 처리시간
- **SLA 현황** — 준수율, 초과 건, 임박 건
- **반복 문의** — 패턴 분석, 근본 원인 추적
- **지식베이스 관리** — 커넥터 등록/동기화, 문서 수동 추가
- **LLM 설정** — Provider 선택, 모델 변경, 프롬프트 커스텀
- **업무 유형/SLA 관리** — 유형 추가/수정, SLA 규칙 설정
- **답변 템플릿 관리** — 등록/수정/삭제
- **데이터 내보내기** — CSV 다운로드

---

## 4. 기술 스택

### 4.1 코어

| 구분 | 기술 | 비고 |
|------|------|------|
| **Monorepo** | Turborepo + Bun | 빌드 오케스트레이션 |
| **프론트엔드** | Next.js + TypeScript + Bun | 대시보드 |
| **백엔드** | Java 25 LTS + Spring Boot 3.x | REST API + AI 처리 |
| **DB** | PostgreSQL + pgvector (또는 별도 벡터 DB) | 데이터 + 벡터 검색 |
| **자동화** | n8n (별도 운영) | 멀티채널 수집 |

### 4.2 AI

| 구분 | 기본값 | 교체 가능 |
|------|--------|----------|
| **LLM** | Ollama (로컬) | OpenAI, Claude, vLLM, 사내 LLM |
| **임베딩** | sentence-transformers (로컬) | OpenAI Embedding, Cohere |
| **벡터 검색** | pgvector | Milvus, Chroma, Qdrant |

### 4.3 인프라

| 구분 | 개발 | 프로덕션 |
|------|------|---------|
| **실행** | Docker Compose | K8s |
| **이미지** | ghcr.io | ghcr.io |
| **CI/CD** | GitHub Actions | GitHub Actions + ArgoCD |

---

## 5. API 설계 (주요)

### 5.1 문의

```
POST   /api/v1/inquiries              # 문의 접수 (n8n → BreadDesk)
GET    /api/v1/inquiries               # 문의 목록
GET    /api/v1/inquiries/{id}          # 문의 상세 (대화 포함)
POST   /api/v1/inquiries/{id}/reply    # 담당자 직접 답변
POST   /api/v1/inquiries/{id}/feedback # 사용자 피드백 (해결/미해결)
```

### 5.2 업무

```
POST   /api/v1/tasks                   # 업무 생성 (수동)
GET    /api/v1/tasks                    # 업무 목록 (필터: 상태, 할당자, 유형)
GET    /api/v1/tasks/{id}              # 업무 상세
PATCH  /api/v1/tasks/{id}             # 업무 수정 (상태, 할당, 내용)
DELETE /api/v1/tasks/{id}             # 업무 삭제
POST   /api/v1/tasks/{id}/assign      # 업무 할당 (수동)
GET    /api/v1/tasks/kanban            # 칸반 뷰
```

### 5.3 지식베이스

```
GET    /api/v1/knowledge/connectors        # 등록된 커넥터 목록
POST   /api/v1/knowledge/connectors        # 커넥터 등록
POST   /api/v1/knowledge/connectors/{id}/sync  # 수동 동기화
POST   /api/v1/knowledge/documents         # 문서 수동 추가
GET    /api/v1/knowledge/search            # 벡터 검색 (디버그/테스트용)
```

### 5.4 태그

```
GET    /api/v1/tags                        # 태그 목록 (사용 빈도순)
```

### 5.5 첨부파일

```
POST   /api/v1/attachments                 # 파일 업로드
GET    /api/v1/attachments/{id}            # 파일 다운로드
DELETE /api/v1/attachments/{id}            # 파일 삭제
```

### 5.6 답변 템플릿

```
GET    /api/v1/templates                   # 템플릿 목록
POST   /api/v1/templates                   # 템플릿 생성
PUT    /api/v1/templates/{id}              # 템플릿 수정
DELETE /api/v1/templates/{id}              # 템플릿 삭제
```

### 5.7 통계

```
GET    /api/v1/stats/overview              # 전체 현황
GET    /api/v1/stats/ai-performance        # AI 답변 성과 (자동해결률 등)
GET    /api/v1/stats/team                  # 팀원별 업무 현황
GET    /api/v1/stats/sla                   # SLA 준수율
GET    /api/v1/stats/repeat-inquiries      # 반복 문의 TOP N
GET    /api/v1/stats/weekly-report         # 주간 리포트
GET    /api/v1/stats/export                # CSV 내보내기
```

### 5.5 설정

```
GET    /api/v1/settings/llm                # LLM 설정 조회
PUT    /api/v1/settings/llm                # LLM 설정 변경
GET    /api/v1/settings/task-types         # 업무 유형 목록
POST   /api/v1/settings/task-types         # 업무 유형 추가
```

---

## 6. 데이터 모델

### 6.1 주요 테이블

```sql
-- 문의
inquiries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel VARCHAR(50),              -- slack, teams, jira, web
    channel_meta JSON,                -- 채널별 메타 (스레드ID 등)
    sender_name VARCHAR(100),
    sender_email VARCHAR(200),
    message TEXT,
    ai_response TEXT,                 -- AI 자동 답변
    ai_confidence FLOAT,             -- AI 신뢰도 (0~1)
    status ENUM('OPEN', 'AI_ANSWERED', 'ESCALATED', 'RESOLVED', 'CLOSED'),
    task_id BIGINT,                  -- 에스컬레이션 시 연결된 업무
    resolved_by ENUM('AI', 'HUMAN'),
    created_at TIMESTAMP,
    resolved_at TIMESTAMP
);

-- 문의 대화 이력
inquiry_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inquiry_id BIGINT,
    role ENUM('USER', 'AI', 'AGENT'),  -- 사용자/AI/담당자
    message TEXT,
    created_at TIMESTAMP
);

-- 업무
tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(500),
    description TEXT,
    type VARCHAR(50),                 -- DEVELOPMENT, ACCESS, INFRA...
    urgency ENUM('LOW', 'NORMAL', 'HIGH', 'CRITICAL'),
    status ENUM('WAITING', 'IN_PROGRESS', 'REVIEW', 'DONE'),
    requester_name VARCHAR(100),
    requester_email VARCHAR(200),
    assignee_id BIGINT,              -- 담당자
    inquiry_id BIGINT,               -- 원본 문의 (있으면)
    ai_summary TEXT,                 -- AI 요약
    due_date DATE,
    estimated_hours FLOAT,
    actual_hours FLOAT,
    sla_response_deadline TIMESTAMP, -- SLA 응답 기한
    sla_resolve_deadline TIMESTAMP,  -- SLA 해결 기한
    sla_responded_at TIMESTAMP,      -- 실제 첫 응답 시간
    sla_response_breached BOOLEAN DEFAULT FALSE,
    sla_resolve_breached BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- 체크리스트
task_checklists (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT,
    item_text VARCHAR(500),
    is_done BOOLEAN DEFAULT FALSE,
    sort_order INT
);

-- 팀원
members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(200),
    role ENUM('AGENT', 'ADMIN'),
    skills JSON,                     -- {"firewall": 0.8, "infra": 0.9}
    is_active BOOLEAN DEFAULT TRUE
);

-- 지식 문서
knowledge_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source VARCHAR(50),              -- confluence, manual, resolved_inquiry
    source_id VARCHAR(200),          -- 원본 문서 ID
    title VARCHAR(500),
    content TEXT,
    url VARCHAR(1000),
    tags JSON,
    embedding VECTOR(768),           -- pgvector
    synced_at TIMESTAMP,
    created_at TIMESTAMP
);

-- 지식 커넥터 설정
knowledge_connectors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_type VARCHAR(50),
    config JSON,                     -- 접속 정보 (암호화)
    sync_interval_min INT DEFAULT 60,
    last_synced_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- 업무 태그
task_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT,
    tag VARCHAR(100)
);

-- 업무 연결 (의존 관계)
task_relations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_task_id BIGINT,
    target_task_id BIGINT,
    relation_type ENUM('BLOCKS', 'RELATED', 'DUPLICATE')
);

-- 파일 첨부
attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type ENUM('INQUIRY', 'TASK'),
    entity_id BIGINT,
    filename VARCHAR(500),
    file_path VARCHAR(1000),
    file_size BIGINT,
    mime_type VARCHAR(100),
    uploaded_by BIGINT,
    created_at TIMESTAMP
);

-- SLA 규칙
sla_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    urgency ENUM('LOW', 'NORMAL', 'HIGH', 'CRITICAL'),
    response_minutes INT,            -- 응답 목표 (분)
    resolve_minutes INT,             -- 해결 목표 (분)
    is_active BOOLEAN DEFAULT TRUE
);

-- 답변 템플릿
reply_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200),
    category VARCHAR(100),
    content TEXT,                     -- {{이름}}, {{서버명}} 등 변수 지원
    usage_count INT DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 업무 구독 (워치)
task_watchers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT,
    member_id BIGINT,
    created_at TIMESTAMP
);

-- 내부 코멘트 (담당자끼리만)
task_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT,
    author_id BIGINT,
    content TEXT,
    is_internal BOOLEAN DEFAULT FALSE,  -- true면 담당자만 볼 수 있음
    created_at TIMESTAMP
);

-- 업무 로그 (히스토리)
task_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT,
    action VARCHAR(50),              -- CREATED, ASSIGNED, STATUS_CHANGED, COMMENTED
    actor_id BIGINT,
    details JSON,
    created_at TIMESTAMP
);
```

---

## 7. LLM 추상화

### 7.1 Provider 인터페이스

```java
public interface LLMProvider {
    /**
     * 채팅 응답 생성
     */
    LLMResponse chat(String systemPrompt, String userMessage, List<Document> context);

    /**
     * 텍스트 임베딩 생성
     */
    float[] embed(String text);

    /**
     * 모델 정보
     */
    String getModelName();
    boolean isAvailable();
}

public record LLMResponse(
    String content,
    float confidence,      // 0~1
    Map<String, Object> metadata
) {}
```

### 7.2 구현체 (예시)

```
providers/
├── OllamaProvider.java       # 로컬 (기본값)
├── OpenAIProvider.java        # OpenAI API
├── ClaudeProvider.java        # Anthropic API
└── VllmProvider.java          # vLLM 서버
```

### 7.3 설정 (application.yml)

```yaml
breaddesk:
  llm:
    provider: ollama           # ollama | openai | claude | vllm
    ollama:
      url: http://localhost:11434
      model: llama3.1:8b
    openai:
      model: gpt-4o
      # api-key는 환경변수로
    claude:
      model: claude-sonnet-4-5-20250514
  embedding:
    provider: local            # local | openai
    local:
      model: all-MiniLM-L6-v2
  vector:
    provider: pgvector         # pgvector | milvus
```

---

## 8. 프로젝트 구조 (Monorepo)

```
breaddesk/
├── apps/
│   └── web/                    # Next.js 프론트엔드
│       ├── src/
│       │   ├── app/            # App Router
│       │   ├── features/       # 기능별 모듈
│       │   │   ├── inquiry/    # 문의 관리
│       │   │   ├── task/       # 업무/칸반
│       │   │   ├── knowledge/  # 지식베이스
│       │   │   ├── dashboard/  # 대시보드/통계
│       │   │   └── settings/   # 설정
│       │   ├── components/     # 공통 컴포넌트
│       │   └── lib/            # 유틸, API 클라이언트
│       ├── package.json
│       └── next.config.js
│
├── services/
│   └── api/                    # Spring Boot 백엔드
│       ├── src/main/java/com/breadlab/breaddesk/
│       │   ├── inquiry/        # 문의 도메인
│       │   ├── task/           # 업무 도메인
│       │   ├── knowledge/      # 지식베이스 도메인
│       │   │   ├── connector/  # 커넥터 플러그인
│       │   │   │   ├── KnowledgeConnector.java
│       │   │   │   ├── ConfluenceConnector.java
│       │   │   │   └── ...
│       │   │   └── vector/     # 벡터 검색
│       │   ├── ai/             # AI/LLM 추상화
│       │   │   ├── LLMProvider.java
│       │   │   ├── OllamaProvider.java
│       │   │   └── ...
│       │   ├── stats/          # 통계
│       │   ├── auth/           # 인증
│       │   └── common/         # 공통
│       ├── src/main/resources/
│       │   └── application.yml
│       ├── build.gradle
│       └── Dockerfile
│
├── packages/
│   └── shared/                 # 프론트/백 공유 타입 (API 스키마 등)
│
├── infrastructure/
│   ├── docker-compose.dev.yml  # 로컬 개발 (PostgreSQL, Redis, Ollama)
│   └── k8s/                    # K8s 매니페스트
│
├── specs/                      # 스펙 문서
│   └── breaddesk/
│       └── DESIGN.md           # 이 파일
│
├── turbo.json
├── package.json
└── README.md
```

---

## 9. 개발 단계

### Phase 1 — MVP (2~3주)
> AI 문의 답변 + 기본 업무 관리 + 핵심 편의 기능

- [ ] 프로젝트 스켈레톤 (Monorepo + 개발환경)
- [ ] DB 스키마 + Flyway 마이그레이션
- [ ] 문의 접수 API + AI 자동 답변
- [ ] 업무 CRUD + 칸반 뷰 + **보류(PENDING) 상태**
- [ ] 수동 업무 할당 + **재할당(전달)**
- [ ] **AI 작업 가이드** (할당 시 자동 제공)
- [ ] 에스컬레이션 (AI 미해결 → 업무 생성)
- [ ] 파일 첨부 (문의/업무)
- [ ] 태그/라벨
- [ ] 답변 템플릿 (CRUD + 변수 치환)
- [ ] **통합 검색** (문의/업무/지식)
- [ ] 관리자 대시보드 (기본)
- [ ] Docker Compose 로컬 실행

### Phase 2 — 지식베이스 + SLA + 알림 (1~2주)
> RAG 파이프라인 + 커넥터 + SLA 관리 + 알림/개인화

- [ ] Confluence 커넥터 (첫 번째)
- [ ] 벡터 검색 (pgvector)
- [ ] 임베딩 파이프라인 (로컬 모델)
- [ ] 증분 동기화
- [ ] 지식베이스 관리 UI
- [ ] SLA 규칙 설정 + 타이머 + **보류 시 일시정지**
- [ ] SLA 초과 알림
- [ ] **알림 센터** (인앱 + 외부 연동)
- [ ] **개인 대시보드 (My Page)**
- [ ] **개인 메모**
- [ ] 유사 문의 감지 (벡터 유사도)
- [ ] 반복 문의 패턴 감지

### Phase 3 — 채널 통합 (1~2주)
> n8n 연동 + 멀티채널

- [ ] n8n 웹훅 연동
- [ ] Slack 워크플로우 (n8n)
- [ ] Teams 워크플로우 (n8n)
- [ ] 답변 역전달 (BreadDesk → n8n → 사용자)

### Phase 4 — 고도화
> 통계, 협업, Jira, 추가 커넥터

- [ ] 통계 대시보드 (AI 성과, 팀 현황, SLA 준수율)
- [ ] 주간 리포트 자동 생성
- [ ] CSV 내보내기
- [ ] 업무 구독/워치 + 알림
- [ ] 내부 코멘트 (담당자끼리만)
- [ ] 업무 연결 (의존/관련/중복)
- [ ] 캘린더 뷰 (마감일 기준)
- [ ] AI 할당 추천
- [ ] **우선순위 자동 조정** (반복 문의/SLA 기반)
- [ ] **Jira 이슈 매핑** (양방향 동기화, n8n 경유)
- [ ] ArgoCD / AWS / Datadog 커넥터
- [ ] K8s 배포
- [ ] 성능 최적화

---

## 10. 비기능 요구사항

| 항목 | 목표 |
|------|------|
| 응답시간 | API P95 < 500ms (AI 제외) |
| AI 응답 | < 10초 (로컬 LLM 기준) |
| 동시 사용자 | 50명+ |
| 가용성 | 99% (업무 시간) |
| 보안 | JWT 인증, 사내망 전용 |
| 데이터 보존 | 문의/업무 이력 1년+ |

---

## 11. 안 넣을 것 (오버스펙 방지)

- ❌ 복잡한 승인 워크플로우 (결재선 등)
- ❌ ITIL 프로세스
- ❌ 멀티 테넌트 (단일 팀용)
- ❌ 실시간 채팅 (비동기 문의/답변)
- ❌ 모바일 앱 (웹 반응형으로 대체)

---

*프로젝트: BreadDesk*
*작성일: 2026-03-29*
*버전: v1.1*
*상태: 설계 확정 대기*
