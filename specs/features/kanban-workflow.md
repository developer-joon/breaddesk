# 칸반 업무 워크플로우

## 개요
업무(Task)를 칸반 보드 형태로 시각화하여 상태별로 관리합니다. WAITING → IN_PROGRESS → REVIEW → DONE 흐름으로 진행됩니다.

## 칸반 컬럼

| 상태 | 설명 | 다음 상태 |
|------|------|-----------|
| WAITING | 대기 중 (미할당 또는 시작 전) | IN_PROGRESS |
| IN_PROGRESS | 진행 중 | PENDING, REVIEW, DONE |
| PENDING | 보류 (외부 대기, 추가 정보 필요) | IN_PROGRESS |
| REVIEW | 검토 중 (QA, 승인 대기) | IN_PROGRESS, DONE |
| DONE | 완료 | - |

## 플로우

### 1. 업무 생성

```
POST /api/v1/tasks
{
  "title": "홍길동 고객 결제 오류 해결",
  "urgency": "HIGH",
  "assigneeId": 5
}
  ↓
Task.status = WAITING
  ↓
칸반 WAITING 컬럼에 표시
```

### 2. 작업 시작

```
드래그 앤 드롭: WAITING → IN_PROGRESS
  ↓
PATCH /api/v1/tasks/{id}/status
{ "status": "IN_PROGRESS" }
  ↓
Task.startedAt = now
Task.sla_responded_at = now (첫 응답)
  ↓
TaskLog 기록: STATUS_CHANGED
  ↓
알림 발송: 구독자에게 TASK_STATUS_CHANGED
```

### 3. 보류

```
드래그 앤 드롭: IN_PROGRESS → PENDING
  ↓
POST /api/v1/tasks/{id}/hold
{ "reason": "고객 추가 정보 대기 중" }
  ↓
Task.status = PENDING
TaskHold 생성 (started_at = now)
  ↓
SLA 타이머 일시정지
```

### 4. 재개

```
드래그 앤 드롭: PENDING → IN_PROGRESS
  ↓
POST /api/v1/tasks/{id}/resume
  ↓
Task.status = IN_PROGRESS
TaskHold.ended_at = now
  ↓
SLA 타이머 재개 (보류 시간만큼 기한 연장)
```

### 5. 검토 요청

```
드래그 앤 드롭: IN_PROGRESS → REVIEW
  ↓
PATCH /api/v1/tasks/{id}/status
{ "status": "REVIEW" }
  ↓
알림 발송: ADMIN에게 TASK_REVIEW_REQUESTED
```

### 6. 완료

```
드래그 앤 드롭: REVIEW → DONE
  ↓
PATCH /api/v1/tasks/{id}/status
{ "status": "DONE" }
  ↓
Task.completedAt = now
SLA 해결 타이머 종료
  ↓
Inquiry 연결 시: Inquiry.status = RESOLVED, resolvedBy = HUMAN
  ↓
알림 발송: 구독자에게 TASK_COMPLETED
  ↓
채널로 "문제가 해결되었습니다" 메시지 전송
```

## 관련 API
- `GET /api/v1/tasks/kanban` - 칸반 보드 조회
- `PATCH /api/v1/tasks/{id}/status` - 상태 변경
- `POST /api/v1/tasks/{id}/hold` - 보류
- `POST /api/v1/tasks/{id}/resume` - 재개

## 관련 엔티티
- [Task](../data-model/ENTITIES.md#4-task-업무)
- [TaskHold](../data-model/ENTITIES.md#9-taskhold-보류-이력)
- [TaskLog](../data-model/ENTITIES.md#8-tasklog-업무-이력)

## 비즈니스 규칙

### 상태 전환 제한
- DONE → 다른 상태: 금지 (재오픈 시 새 Task 생성)
- WAITING → REVIEW: 금지 (IN_PROGRESS 거쳐야 함)

### 드래그 앤 드롭
- 같은 컬럼 내: 정렬 순서만 변경 (sortOrder 업데이트)
- 다른 컬럼으로: 상태 변경 API 호출

### SLA 타이머
- WAITING → IN_PROGRESS: SLA 응답 타이머 시작
- IN_PROGRESS → PENDING: SLA 타이머 일시정지
- PENDING → IN_PROGRESS: SLA 타이머 재개
- REVIEW → DONE: SLA 해결 타이머 종료

### 우선순위 표시
- urgency에 따라 카드 색상 변경
  - CRITICAL: 빨강
  - HIGH: 주황
  - NORMAL: 파랑
  - LOW: 회색

### SLA 경고
- SLA 기한 임박 시 카드에 경고 배지 표시
- SLA 위반 시 카드 테두리 빨간색

## 프론트엔드 구현

### 기술 스택
- **Drag & Drop**: react-beautiful-dnd 또는 dnd-kit
- **상태 관리**: Zustand 또는 React Query
- **UI**: Tailwind CSS

### 칸반 보드 레이아웃

```tsx
<Board>
  <Column title="대기 중" status="WAITING">
    {waitingTasks.map(task => (
      <TaskCard task={task} draggable />
    ))}
  </Column>
  <Column title="진행 중" status="IN_PROGRESS">
    {inProgressTasks.map(task => (
      <TaskCard task={task} draggable />
    ))}
  </Column>
  <Column title="보류" status="PENDING">
    {pendingTasks.map(task => (
      <TaskCard task={task} draggable />
    ))}
  </Column>
  <Column title="검토" status="REVIEW">
    {reviewTasks.map(task => (
      <TaskCard task={task} draggable />
    ))}
  </Column>
  <Column title="완료" status="DONE">
    {doneTasks.map(task => (
      <TaskCard task={task} draggable={false} />
    ))}
  </Column>
</Board>
```

### TaskCard 컴포넌트

```tsx
<Card>
  <Badge color={urgencyColor}>{task.urgency}</Badge>
  {task.slaResolveBreached && <WarningIcon />}
  <Title>{task.title}</Title>
  <Assignee>{task.assigneeName}</Assignee>
  <Deadline>마감: {task.slaResolveDeadline}</Deadline>
  <Tags>{task.tags.map(tag => <Tag>{tag}</Tag>)}</Tags>
</Card>
```

## 엣지 케이스

1. **동시 상태 변경**: 두 명이 동시에 드래그 → Optimistic UI + 충돌 시 새로고침
2. **BLOCKS 관계**: source_task가 DONE이 아니면 target_task를 IN_PROGRESS로 이동 불가 → 경고 메시지
3. **SLA 위반 후 완료**: 완료해도 sla_resolve_breached는 true 유지 (통계에 반영)
4. **담당자 변경 중 상태 변경**: 담당자 이전과 상태 변경을 동시에 하면 로그에 모두 기록

## 성능 최적화

- **Polling 대신 WebSocket**: 실시간 칸반 업데이트
- **Lazy Loading**: 각 컬럼에 가상 스크롤 (100개 이상 시)
- **Caching**: React Query로 칸반 데이터 캐싱

## 향후 개선 방향

1. **커스텀 컬럼**: 팀별로 상태 커스터마이징
2. **필터링**: 담당자, 긴급도, 태그로 필터
3. **WIP 제한**: 진행 중 업무 수 제한 (예: IN_PROGRESS 최대 5개)
4. **사이클 타임 분석**: 각 상태별 평균 체류 시간 통계
