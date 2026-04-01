# 기능 플래그 API

## 개요
시스템에서 현재 활성화된 기능 플래그를 조회합니다. 기능 플래그를 통해 특정 기능을 런타임에 활성화/비활성화할 수 있습니다.

## 엔드포인트

### GET /api/v1/features
**설명**: 현재 활성화된 기능 플래그 목록을 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "kanbanTasks": true,
  "internalNotes": true,
  "aiAssignment": false,
  "jiraIntegration": false
}
```

**기능 플래그 설명**:
- `kanbanTasks`: 칸반 보드 기능 활성화 여부
- `internalNotes`: 내부 코멘트(담당자끼리만 볼 수 있는 메모) 기능 활성화 여부
- `aiAssignment`: AI 기반 업무 자동 할당 기능 활성화 여부
- `jiraIntegration`: Jira 연동 기능 활성화 여부

**비즈니스 규칙**:
- 설정 파일(`application.yml`)에서 기능 플래그 관리
- 프론트엔드는 이 API를 호출하여 UI 조건부 렌더링에 사용
- Phase별 기능 출시 관리에 활용

**설정 예시 (application.yml)**:
```yaml
breaddesk:
  features:
    kanban-tasks: true
    internal-notes: true
    ai-assignment: false
    jira-integration: false
```

---

## 관련 파일

- `apps/api/src/main/java/com/breadlab/breaddesk/config/FeatureProperties.java`: 기능 플래그 설정 클래스
- `apps/api/src/main/java/com/breadlab/breaddesk/controller/FeatureController.java`: 컨트롤러

---

## 프론트엔드 구현 참고

- `apps/web/src/services/features.ts`: API 호출 함수
- 페이지/컴포넌트에서 조건부 렌더링에 사용
- 예: `{features.kanbanTasks && <KanbanBoard />}`
