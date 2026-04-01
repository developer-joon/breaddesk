# Changelog

All notable changes to BreadDesk will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### 계획 중 (Phase 1)
- RAG 기반 AI 자동응답 (pgvector + 임베딩)
- LLM Provider 추상화 (Ollama/OpenAI/Claude)
- AI 응답 추천 (Copilot)
- AI 자동 분류/라우팅

### 계획 중 (Phase 2+)
- 칸반보드 드래그앤드롭
- 팀/멀티 테넌트
- 옴니채널 수신 (이메일, 웹 채팅, 카카오톡)
- CSAT 자동 발송

---

## [0.1.0] - 2026-04-01

### Phase 0 완료 - 스켈레톤 + 기본 CRUD

#### Added - 프로젝트 기반
- **Monorepo 구조**: Turborepo + npm workspaces
- **프론트엔드**: Next.js 15 + TypeScript + React 19
- **백엔드**: Spring Boot 3.x + Java 25 + Gradle
- **데이터베이스**: PostgreSQL 17 + Flyway 마이그레이션
- **DevOps**: Docker Compose(개발) + Kubernetes(프로덕션)
- **CI/CD**: GitHub Actions (빌드/테스트/배포)

#### Added - 핵심 도메인
- **문의 관리** (`/api/v1/inquiries`)
  - 문의 접수 (웹훅 수신)
  - 문의 목록/상세 조회
  - 담당자 직접 답변
  - 사용자 피드백 (해결/미해결)
  - 상태 관리 (OPEN, AI_ANSWERED, ESCALATED, RESOLVED, CLOSED)

- **업무 관리** (`/api/v1/tasks`)
  - 업무 CRUD (생성/조회/수정/삭제)
  - 상태 전환 (WAITING → IN_PROGRESS → REVIEW → DONE)
  - 담당자 할당 (수동)
  - 체크리스트 관리
  - 태그 관리
  - 업무 연결 (BLOCKS, RELATED, DUPLICATE)
  - 칸반 뷰 (`/kanban`)

- **지식베이스** (`/api/v1/knowledge`)
  - 문서 CRUD
  - 커넥터 등록 (Confluence 등)
  - 수동 동기화 트리거
  - 벡터 검색 (테스트용)

- **SLA 관리** (`/api/v1/sla`)
  - SLA 규칙 CRUD
  - 타이머 자동 시작
  - 위반 감지 및 알림

- **팀원 관리** (`/api/v1/members`)
  - 팀원 CRUD
  - 역할 관리 (AGENT, ADMIN)
  - 스킬 관리

- **답변 템플릿** (`/api/v1/templates`)
  - 템플릿 CRUD
  - 변수 치환 (`{{이름}}`, `{{서버명}}` 등)

- **첨부파일** (`/api/v1/attachments`)
  - 파일 업로드/다운로드/삭제
  - 이미지 인라인 미리보기

- **채널 연동** (`/api/v1/channels`, `/api/v1/webhooks`)
  - 채널 설정 CRUD
  - 웹훅 인바운드/아웃바운드
  - 멀티채널 메타데이터 관리

- **검색** (`/api/v1/search`)
  - 통합 검색 (문의/업무)
  - 키워드 기반 필터링

- **알림** (`/api/v1/notifications`)
  - 알림 목록 조회
  - 읽음 처리

- **개인 메모** (`/api/v1/personal-notes`)
  - 개인 메모 CRUD (담당자용)

- **대시보드** (`/api/v1/dashboard`)
  - 요약 통계 (문의/업무/SLA/AI 성능)
  - 최근 문의 목록
  - 내 업무 목록

- **통계** (`/api/v1/stats`)
  - 전체 현황 (`/overview`)
  - AI 성능 (`/ai`)
  - 팀원별 통계 (`/team`)
  - 주간 리포트 (`/weekly-report`)

- **데이터 내보내기** (`/api/v1/export`)
  - CSV 내보내기 (문의/업무)

- **인증** (`/api/v1/auth`)
  - JWT 기반 인증
  - 로그인/로그아웃
  - 토큰 갱신

- **기능 플래그** (`/api/v1/features`)
  - 런타임 기능 활성화/비활성화

#### Added - 인프라
- **Docker Compose**: 로컬 개발 환경 (PostgreSQL)
- **Kubernetes 매니페스트**: `infrastructure/k8s/`
  - Deployment, Service, Ingress
  - ConfigMap, Secret
  - HPA (수평 확장)
- **GitHub Actions 워크플로우**:
  - CI: 빌드/테스트 자동화
  - CD: main 브랜치 머지 시 K8s 배포
- **Flyway 마이그레이션**: DB 스키마 버전 관리
- **Swagger/OpenAPI**: API 문서 자동 생성

#### Added - 테스트
- 단위 테스트 (주요 서비스/컨트롤러)
  - `InquiryServiceTest`
  - `TaskServiceTest`
  - `MemberServiceTest`
  - `SlaServiceTest`
  - `DashboardServiceTest`
  - `ExportServiceTest`
  - `NotificationServiceTest`
  - `SearchServiceTest`
  - `ChannelConfigServiceTest`
  - `AttachmentServiceTest`
  - 등

#### Added - 문서
- `README.md`: 프로젝트 소개, 로컬 개발, 배포
- `docs/DESIGN.md`: 시스템 설계 (아키텍처, 데이터 모델, API 설계)
- `docs/DEPLOYMENT.md`: 배포 가이드
- `docs/GIT_WORKFLOW.md`: Git 워크플로우
- `docs/TEAM.md`: 팀 정보
- `DEVOPS_SETUP_SUMMARY.md`: DevOps 설정 요약
- `specs/api/*.md`: API 명세서 (15개 도메인)
  - `inquiries.md`, `tasks.md`, `knowledge.md`, `sla.md`, `members.md`, `templates.md`, `attachments.md`, `channels.md`, `search.md`, `notifications.md`, `personal-notes.md`, `dashboard.md`, `stats.md`, `export.md`, `auth.md`, `features.md`

#### Fixed
- MVP 버그 12건 수정 진행 중 (4/1 기준)

---

## 버전 정책

- **Major (1.0.0)**: Breaking changes (API 변경, DB 스키마 대규모 변경)
- **Minor (0.1.0)**: 새 기능 추가 (Phase 완료 시)
- **Patch (0.0.1)**: 버그 수정, 문서 개선

---

## 참고 링크

- [프로젝트 설계서](docs/DESIGN.md)
- [배포 가이드](docs/DEPLOYMENT.md)
- [API 명세서](specs/api/)
- [프로덕션 환경](https://breaddesk.k6s.app)
