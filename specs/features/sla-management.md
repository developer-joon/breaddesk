# SLA 타이머/스케줄러/위반감지

## 개요
업무(Task)의 SLA(Service Level Agreement)를 자동 계산하고, 주기적으로 위반 여부를 감지하여 알림을 발송합니다.

## 사용자 스토리
- 업무 생성 시 긴급도에 따라 SLA 기한이 자동 설정됨
- 스케줄러가 주기적으로 SLA 기한을 확인하여 위반 전 경고, 위반 시 알림
- 보류(PENDING) 상태에서는 SLA 타이머가 일시정지됨

## 플로우

### 1. SLA 자동 계산 (Task 생성 시)

```
POST /api/v1/tasks
  ↓
TaskService.createTask()
  ↓
SlaService.calculateDeadlines(task)
  ↓
- urgency에 따라 SlaRule 조회
- sla_response_deadline = createdAt + responseMinutes
- sla_resolve_deadline = createdAt + resolveMinutes
  ↓
Task 저장
```

### 2. SLA 체크 스케줄러

```
@Scheduled(fixedDelay = 300000) // 5분마다
SlaCheckScheduler.checkSlaViolations()
  ↓
1. 모든 미완료 Task 조회 (status != DONE)
  ↓
2. 각 Task에 대해:
   - 보류 중(TaskHold.ended_at = NULL)이면 스킵
   - 현재 시각 > sla_response_deadline && sla_responded_at = NULL
     → sla_response_breached = true
     → SLA_BREACHED 알림 발송
   - 현재 시각 > sla_resolve_deadline && status != DONE
     → sla_resolve_breached = true
     → SLA_BREACHED 알림 발송
  ↓
3. 30분 전 경고
   - 현재 시각 + 30분 > deadline
     → SLA_WARNING 알림 발송
```

### 3. 보류 시 SLA 일시정지

```
POST /api/v1/tasks/{id}/hold
  ↓
TaskService.holdTask(taskId, reason)
  ↓
TaskHold 생성 (started_at = now, ended_at = NULL)
  ↓
Task.status = PENDING
  ↓
(SLA 체크 시 보류 중이면 스킵)
```

### 4. 재개 시 SLA 타이머 재개

```
POST /api/v1/tasks/{id}/resume
  ↓
TaskService.resumeTask(taskId)
  ↓
TaskHold.ended_at = now
  ↓
sla_paused_minutes = (ended_at - started_at) / 60
  ↓
sla_response_deadline += sla_paused_minutes
sla_resolve_deadline += sla_paused_minutes
  ↓
Task.status = IN_PROGRESS
```

## 관련 API
- `GET /api/v1/sla/rules` - SLA 규칙 조회
- `PUT /api/v1/sla/rules/{id}` - SLA 규칙 수정
- `GET /api/v1/sla/stats` - SLA 통계 조회

## 관련 엔티티
- [SlaRule](../data-model/ENTITIES.md#14-slarule-sla-규칙)
- [Task](../data-model/ENTITIES.md#4-task-업무)
- [TaskHold](../data-model/ENTITIES.md#9-taskhold-보류-이력)

## 비즈니스 규칙

### SLA 규칙 (기본값)

| Urgency | 응답 기한 | 해결 기한 |
|---------|----------|----------|
| CRITICAL | 30분 | 4시간 |
| HIGH | 2시간 | 24시간 |
| NORMAL | 4시간 | 3일 |
| LOW | 24시간 | 5일 |

### SLA 응답 기준
- 첫 번째 상담원 댓글 또는 상태 변경(WAITING → IN_PROGRESS) 시각
- `sla_responded_at` 필드에 기록

### SLA 해결 기준
- 상태가 DONE으로 변경된 시각
- `completed_at` 필드와 동일

### 보류 시 일시정지
- `TaskHold.ended_at = NULL`인 동안 SLA 체크 스킵
- 재개 시 보류 시간만큼 기한 연장

### 위반 후 처리
- `sla_response_breached` 또는 `sla_resolve_breached` = true 설정
- ADMIN에게 SLA_BREACHED 알림 발송
- 대시보드에 위반 건수 표시

## 엣지 케이스

1. **주말/공휴일**: 현재는 24/7 계산, 향후 업무 시간만 계산 옵션 추가
2. **시간대 차이**: 서버 시간 기준, 향후 고객 시간대 고려
3. **보류 후 미재개**: 일정 기간 후 자동 재개 또는 ADMIN 알림
4. **SLA 규칙 변경**: 기존 Task의 SLA 기한은 변경 안 됨 (생성 시점 기준)

## 구현 클래스

- `SlaService.calculateDeadlines()`: SLA 기한 계산
- `SlaCheckScheduler.checkSlaViolations()`: 주기적 SLA 체크 (@Scheduled)
- `TaskService.holdTask()`: 보류 처리
- `TaskService.resumeTask()`: 재개 처리
- `SlaTimerService.pauseTimer()`: SLA 타이머 일시정지 (미구현 시 구현 필요)
- `SlaTimerService.resumeTimer()`: SLA 타이머 재개 (미구현 시 구현 필요)

## 성능 최적화

- 스케줄러 쿼리 최적화: 인덱스 활용 (`status`, `sla_response_deadline`, `sla_resolve_deadline`)
- 배치 처리: 알림 발송을 한 번에 처리
- 캐싱: SLA 규칙을 메모리에 캐싱

## 향후 개선 방향

1. **업무 시간 기반 SLA**: 주말/공휴일 제외
2. **동적 SLA**: 고객 등급(VIP 등)에 따라 SLA 조정
3. **SLA 위반 분석**: 위반 원인 분석 (담당자 부족, 복잡도 과소평가 등)
4. **예측 알림**: ML 기반으로 위반 가능성 예측
