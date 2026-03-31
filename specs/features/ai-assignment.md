# AI 담당자 추천

## 개요
업무(Task) 생성 또는 담당자 변경 시, AI가 팀원의 스킬과 과거 유사 업무 처리 이력을 분석하여 최적의 담당자를 추천합니다.

## 사용자 스토리
- 업무 생성 시 "담당자 추천" 버튼 클릭
- AI가 팀원별로 적합도 점수를 계산하여 상위 3명 제시
- 상담원이 추천 결과를 참고하여 담당자 선택

## 플로우

```
1. 업무 생성 또는 담당자 변경 화면
   ↓
2. "담당자 추천" 버튼 클릭
   ↓
3. GET /api/v1/tasks/{taskId}/recommend-assignee
   ↓
4. AIAssignmentService.recommendAssignees(taskId)
   ↓
5. 추천 로직 실행
   ├─ 업무 내용 임베딩 생성
   ├─ 유사 업무 검색 (벡터 유사도)
   ├─ 각 팀원별 점수 계산
   │  ├─ 스킬 매칭 점수
   │  ├─ 유사 업무 처리 경험 점수
   │  └─ 현재 업무 부하 점수
   └─ 점수 합산 및 정렬
   ↓
6. 상위 3명 반환
   ↓
7. UI에 추천 결과 표시
   - 팀원 이름
   - 적합도 점수
   - 추천 사유
   ↓
8. 상담원이 선택하여 assigneeId 설정
```

## 관련 API
- `GET /api/v1/tasks/{taskId}/recommend-assignee`

## 관련 엔티티
- [Task](../data-model/ENTITIES.md#4-task-업무)
- [Member](../data-model/ENTITIES.md#1-member-팀원)

## 추천 알고리즘

### 1. 스킬 매칭 점수 (40%)

```
- Member.skills JSONB 파싱
- Task.description에서 키워드 추출
- 매칭도 계산
  예: skills = {"expertise": ["결제", "환불"]}
      description = "결제 오류 처리"
      → "결제" 키워드 매칭 → 점수 +40
```

### 2. 유사 업무 처리 경험 점수 (40%)

```
- Task.description 임베딩 생성
- 과거 완료된 Task 중 assigneeId별 유사 업무 검색
- 유사도 가중 평균
  예: 김철수가 유사 업무 5건 처리 (평균 유사도 0.85)
      → 점수 +34
```

### 3. 현재 업무 부하 점수 (20%)

```
- 각 팀원의 진행 중 업무 수 조회
- 업무 수가 적을수록 높은 점수
  예: 김철수: 5건, 이영희: 2건
      → 이영희 점수 +20, 김철수 점수 +8
```

### 종합 점수 계산

```
score = (skillMatchScore * 0.4) +
        (experienceScore * 0.4) +
        (workloadScore * 0.2)
```

### 추천 사유 생성

```
reason = ""
if skillMatchScore > 30:
    reason += "스킬: {matched_skills}, "
if experienceScore > 30:
    reason += "유사 업무 {count}건 처리 경험, "
if workloadScore > 15:
    reason += "현재 업무 부하 낮음"
```

## 비즈니스 규칙

1. **최소 점수**: 종합 점수 50 이상만 추천 (낮으면 "적합한 담당자 없음" 메시지)
2. **isActive=false 제외**: 비활성 팀원은 추천 안 함
3. **role 제한**: AGENT만 추천 (ADMIN은 관리자 업무만)
4. **최대 추천 수**: 상위 3명

## 엣지 케이스

1. **팀원 없음**: "담당자를 추가해주세요" 메시지
2. **모든 팀원 점수 낮음**: 수동 할당 안내
3. **유사 업무 없음**: 스킬과 업무 부하만으로 추천
4. **스킬 정보 없음**: 유사 업무와 업무 부하만으로 추천

## 구현 클래스

- `AIAssignmentService.recommendAssignees()`
- `EmbeddingService.embed()` - 업무 내용 임베딩
- `TaskRepository.findSimilarTasksByAssignee()` - 유사 업무 검색 (pgvector)
- `MemberRepository.findAllByIsActive(true)` - 활성 팀원 조회

## 프론트엔드 구현

### UI 예시

```tsx
<Button onClick={loadRecommendations}>
  🤖 AI 담당자 추천
</Button>

{recommendations && (
  <RecommendationList>
    {recommendations.map(rec => (
      <Card>
        <Name>{rec.memberName}</Name>
        <Score>{rec.score.toFixed(0)}점</Score>
        <Reason>{rec.reason}</Reason>
        <AssignButton onClick={() => assignTo(rec.memberId)}>
          할당
        </AssignButton>
      </Card>
    ))}
  </RecommendationList>
)}
```

## 성능 최적화

- **벡터 검색 최적화**: pgvector 인덱스 활용
- **캐싱**: 팀원 스킬 정보를 메모리에 캐싱
- **배치 처리**: 여러 업무의 추천을 한 번에 계산

## 향후 개선 방향

1. **학습 기반 추천**: 과거 할당 결과(성공/실패)를 학습하여 개선
2. **시간대 고려**: 팀원의 근무 시간대 고려
3. **선호도 반영**: 팀원이 선호하는 업무 타입 고려
4. **자동 할당**: AI 추천 1위를 자동으로 할당하는 옵션
