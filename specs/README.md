# BreadDesk 스펙 문서

이 디렉토리는 BreadDesk의 구현된 기능을 기반으로 역추적하여 작성된 스펙 문서입니다.

## 문서 구조

### 📊 Data Model
- [ENTITIES.md](./data-model/ENTITIES.md) - 전체 엔티티 목록, 필드, 관계도

### 🔌 API 스펙
- [auth.md](./api/auth.md) - 인증 API (login, refresh, JWT)
- [inquiries.md](./api/inquiries.md) - 문의 관리 API
- [tasks.md](./api/tasks.md) - 업무 관리 API (CRUD, 칸반, 체크리스트, 댓글, 태그, 구독, 연결)
- [knowledge.md](./api/knowledge.md) - 지식베이스 API (문서, 커넥터, RAG 검색)
- [sla.md](./api/sla.md) - SLA 규칙/통계 API
- [channels.md](./api/channels.md) - 채널 설정/웹훅 API
- [members.md](./api/members.md) - 팀원 관리 API
- [notifications.md](./api/notifications.md) - 알림 API
- [dashboard.md](./api/dashboard.md) - 대시보드/통계 API
- [templates.md](./api/templates.md) - 답변 템플릿 API
- [attachments.md](./api/attachments.md) - 첨부파일 API
- [search.md](./api/search.md) - 통합 검색 API
- [export.md](./api/export.md) - CSV 내보내기 API
- [personal-notes.md](./api/personal-notes.md) - 개인 메모 API

### ⚙️ 주요 기능
- [ai-auto-response.md](./features/ai-auto-response.md) - AI 자동답변 (RAG 기반)
- [escalation.md](./features/escalation.md) - 에스컬레이션 플로우
- [sla-management.md](./features/sla-management.md) - SLA 타이머/스케줄러/위반감지
- [channel-integration.md](./features/channel-integration.md) - 멀티채널 통합 (웹훅, Slack, Teams, Email)
- [kanban-workflow.md](./features/kanban-workflow.md) - 칸반 업무 워크플로우
- [ai-assignment.md](./features/ai-assignment.md) - AI 담당자 추천

### 📄 페이지 스펙
- [login.md](./pages/login.md) - 로그인 페이지
- [dashboard.md](./pages/dashboard.md) - 대시보드 페이지
- [inquiries.md](./pages/inquiries.md) - 문의 관리 페이지
- [tasks.md](./pages/tasks.md) - 업무(칸반) 페이지
- [knowledge.md](./pages/knowledge.md) - 지식베이스 페이지
- [templates.md](./pages/templates.md) - 템플릿 페이지
- [my.md](./pages/my.md) - 내 업무 페이지
- [settings.md](./pages/settings.md) - 설정 페이지

## 스펙 작성 원칙

1. **실제 코드 기반**: 추측이나 예상이 아닌, 실제 구현된 코드에서 추출
2. **정확성**: 백엔드 API와 프론트엔드 구현 간 불일치 사항 명시
3. **완전성**: 누락된 기능은 "미구현" 표시
4. **유지보수성**: 코드 변경 시 스펙도 함께 업데이트 필요

## 업데이트 이력

- 2026-03-31: 초기 스펙 문서 생성 (Phase 1~4 코드 기반)
