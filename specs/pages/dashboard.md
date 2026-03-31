# 대시보드 페이지

## 경로
`/dashboard` 또는 `/`

## 인증
필요 (AGENT, ADMIN)

## 주요 컴포넌트
- **요약 카드**: 문의 건수, 업무 건수, SLA 준수율, AI 성능
- **최근 문의 목록**: 최근 5건 (링크 클릭 시 상세 페이지로 이동)
- **내 업무 목록**: 현재 사용자에게 할당된 미완료 업무
- **SLA 위반 경고**: 위반 건수 및 긴급 업무 표시
- **차트**: 문의 추이, 업무 상태 분포 (recharts 또는 chart.js)

## 데이터 소스
- `GET /api/v1/dashboard` - 대시보드 통계 조회
- Polling 또는 WebSocket으로 실시간 업데이트 (30초 간격)

## 사용자 인터랙션
- 요약 카드 클릭 → 해당 목록 페이지로 이동 (예: "문의 150건" 클릭 → `/inquiries`)
- 최근 문의 클릭 → `/inquiries/{id}` 상세 페이지
- 내 업무 클릭 → `/tasks/{id}` 상세 페이지
- SLA 위반 경고 클릭 → `/tasks?filter=sla_breached`

## 상태 관리
- React Query로 대시보드 데이터 캐싱
- 30초마다 자동 refetch

## 구현 참고
- `apps/web/src/app/dashboard/page.tsx`
- `apps/web/src/services/dashboard.ts`
