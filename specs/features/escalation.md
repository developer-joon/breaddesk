# 에스컬레이션 플로우

## 개요
AI 자동답변으로 해결되지 않은 문의를 업무(Task)로 에스컬레이션하여 상담원이 처리합니다.

## 사용자 스토리
- 고객 문의에 AI가 답변했으나 고객이 만족하지 못하거나
- AI 신뢰도가 낮아 자동 답변 실패 시
- 상담원이 수동으로 업무로 전환

## 플로우

```
1. 에스컬레이션 조건 판단
   - AI 신뢰도 < 0.8
   - 고객이 "도움이 안 됨" 피드백
   - 상담원이 수동으로 "업무로 전환" 클릭
   ↓
2. POST /api/v1/inquiries/{id}/convert-to-task
   ↓
3. Task 생성
   - title = "[{channel}] {inquiry.senderName} 문의"
   - description = {inquiry.message}
   - urgency = AI가 추천 (긴급 키워드 감지)
   - requesterName = inquiry.senderName
   - requesterEmail = inquiry.senderEmail
   - inquiry_id = inquiry.id (연결)
   ↓
4. AI 담당자 추천 (선택)
   - GET /api/v1/tasks/{taskId}/recommend-assignee
   - 상담원이 선택하거나 자동 할당
   ↓
5. Task.assigneeId 설정
   ↓
6. Inquiry.status = ESCALATED
   ↓
7. Inquiry.task_id = task.id
   ↓
8. 알림 발송
   - 담당자에게 TASK_ASSIGNED 알림
   - 구독자에게 INQUIRY_ESCALATED 알림
   ↓
9. 채널에 "상담원이 곧 연락드리겠습니다" 메시지 전송
```

## 관련 API
- `POST /api/v1/inquiries/{id}/convert-to-task`
- `GET /api/v1/tasks/{taskId}/recommend-assignee`

## 관련 엔티티
- [Inquiry](../data-model/ENTITIES.md#2-inquiry-문의)
- [Task](../data-model/ENTITIES.md#4-task-업무)

## 비즈니스 규칙

1. **중복 에스컬레이션 방지**: 이미 ESCALATED 상태면 에러 반환
2. **SLA 자동 적용**: Task 생성 시 urgency에 따라 SLA 마감일 자동 계산
3. **AI 가이드 생성**: Task 생성 시 TaskGuide 자동 생성
4. **채널 연동**: Task 처리 후 댓글이 채널로 자동 전송 (예: Slack 스레드 답글)

## 엣지 케이스

1. **담당자 부재**: 자동 할당 실패 시 ADMIN에게 할당 또는 WAITING 상태로 보류
2. **동시 에스컬레이션**: 두 명의 상담원이 동시에 에스컬레이션 시도 → 먼저 성공한 요청만 처리
3. **Task 삭제 후**: Inquiry.task_id = NULL로 변경, status는 ESCALATED 유지

## 향후 개선 방향

1. **자동 에스컬레이션**: aiConfidence < threshold 시 자동으로 Task 생성
2. **SLA 위반 예측**: 예상 처리 시간 > SLA 시 우선순위 상향
3. **Jira 자동 연동**: 에스컬레이션 시 Jira 이슈 자동 생성
