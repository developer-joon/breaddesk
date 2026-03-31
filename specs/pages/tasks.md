# 업무(칸반) 페이지

## 경로
`/tasks`

## 인증
필요 (AGENT, ADMIN)

## 주요 컴포넌트
- **칸반 보드**: WAITING, IN_PROGRESS, PENDING, REVIEW, DONE 컬럼
- **TaskCard**: 제목, 긴급도 배지, 담당자, 마감일, SLA 경고, 태그
- **Drag & Drop**: react-beautiful-dnd
- **필터 바**: 담당자, 긴급도, 태그 필터
- **업무 생성 버튼**: 모달 열기
- **상세 모달**: 클릭 시 업무 상세 정보 표시

## 데이터 소스
- `GET /api/v1/tasks/kanban` - 칸반 보드 데이터
- `GET /api/v1/tasks/{id}` - 업무 상세
- `POST /api/v1/tasks` - 업무 생성
- `PATCH /api/v1/tasks/{id}/status` - 상태 변경 (드래그 앤 드롭)
- `POST /api/v1/tasks/{id}/checklists` - 체크리스트 추가
- `POST /api/v1/tasks/{id}/comments` - 댓글 추가
- `POST /api/v1/tasks/{id}/tags` - 태그 추가
- `POST /api/v1/tasks/{id}/watch` - 구독
- `GET /api/v1/tasks/{taskId}/recommend-assignee` - AI 담당자 추천

## 사용자 인터랙션

### 칸반 보드
- 드래그 앤 드롭 → `PATCH /api/v1/tasks/{id}/status`
- 카드 클릭 → 상세 모달 열기
- 필터 선택 → 클라이언트 사이드 필터링 (또는 API 재호출)

### 업무 생성
- "+ 업무 추가" 버튼 클릭
- 제목, 설명, 긴급도, 담당자 입력
- "AI 담당자 추천" 버튼 → 추천 결과 표시
- "생성" 버튼 → `POST /api/v1/tasks`

### 업무 상세
- **개요 탭**: 제목, 설명, 긴급도, 담당자, 마감일, SLA 정보
- **체크리스트 탭**: 체크리스트 추가/완료 토글
- **댓글 탭**: 댓글 추가/조회
- **이력 탭**: TaskLog 조회
- **연결 탭**: 관련 업무 표시, 연결 추가
- **구독 버튼**: Watch/Unwatch 토글

### SLA 경고
- SLA 기한 30분 남았을 때: 카드에 경고 배지
- SLA 위반 시: 카드 테두리 빨간색, "위반" 배지

## 상태 관리
- React Query: 칸반 데이터 캐싱, 실시간 업데이트
- Zustand: 필터 상태, 모달 열기/닫기

## 드래그 앤 드롭 로직

```tsx
const onDragEnd = (result) => {
  const { draggableId, source, destination } = result;
  if (!destination) return;
  
  const taskId = draggableId;
  const newStatus = destination.droppableId;
  
  // Optimistic UI update
  updateTaskStatus(taskId, newStatus);
  
  // API call
  patchTaskStatus(taskId, { status: newStatus })
    .catch(() => {
      // Revert on error
      refetchKanban();
    });
};
```

## 구현 참고
- `apps/web/src/app/tasks/page.tsx`
- `apps/web/src/services/tasks.ts`
- `react-beautiful-dnd` 또는 `dnd-kit`
