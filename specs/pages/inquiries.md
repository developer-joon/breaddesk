# 문의 관리 페이지

## 경로
`/inquiries`

## 인증
필요 (AGENT, ADMIN)

## 주요 컴포넌트
- **필터 바**: 상태, 채널, 날짜 범위 필터
- **문의 테이블**: 발신자명, 메시지, 상태, 생성일, 액션 버튼
- **페이지네이션**: 페이지 이동 (20개씩)
- **검색 바**: 키워드 검색 (발신자명, 메시지)
- **상세 모달**: 문의 클릭 시 대화 이력 표시

## 데이터 소스
- `GET /api/v1/inquiries?page=0&size=20` - 문의 목록
- `GET /api/v1/inquiries/{id}` - 문의 상세 (대화 이력 포함)
- `GET /api/v1/inquiries/{id}/similar` - 유사 문의
- `POST /api/v1/inquiries/{id}/messages` - 답변 추가
- `POST /api/v1/inquiries/{id}/convert-to-task` - 업무로 전환
- `PATCH /api/v1/inquiries/{id}/status` - 상태 변경

## 사용자 인터랙션

### 문의 조회
- 필터 선택 → API 재호출
- 검색어 입력 → debounce 후 API 호출
- 테이블 행 클릭 → 상세 모달 열기

### 답변 작성
- 상세 모달에서 답변 입력
- "전송" 버튼 → `POST /api/v1/inquiries/{id}/messages`
- 채널로 자동 전송 (Slack/Teams/Email)

### 업무 전환
- "업무로 전환" 버튼 클릭
- 제목, 긴급도, 담당자 선택 폼 표시
- "전환" 버튼 → `POST /api/v1/inquiries/{id}/convert-to-task`
- 성공 시 `/tasks/{taskId}`로 리다이렉트

### 유사 문의 조회
- 상세 모달에서 "유사 문의" 탭 클릭
- `GET /api/v1/inquiries/{id}/similar` 호출
- 유사도 순으로 표시

## 상태 관리
- React Query: 문의 목록 캐싱
- Zustand: 필터 상태 관리

## 구현 참고
- `apps/web/src/app/inquiries/page.tsx`
- `apps/web/src/services/inquiries.ts`
