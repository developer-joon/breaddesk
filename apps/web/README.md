# BreadDesk Web Frontend

Phase 1 프론트엔드 구현 (shadcn/ui 기반)

## 기술 스택

- **Framework**: Next.js 15 (App Router)
- **UI Components**: shadcn/ui (Base Nova 스타일)
- **Styling**: Tailwind CSS
- **Icons**: Lucide React
- **TypeScript**: Strict mode

## 주요 기능

- ✅ 대시보드 (통계 카드, 업무 현황, 최근 문의)
- ✅ Skeleton UI 로딩 상태
- ✅ 에러 처리 (모든 API 호출)
- ✅ 백엔드 ApiResponse<T> 타입 대응
- ✅ Responsive 레이아웃

## 설치 및 실행

```bash
# 의존성 설치
npm install

# 개발 서버 실행 (localhost:3000)
npm run dev

# 프로덕션 빌드
npm run build

# 프로덕션 실행
npm start
```

## 환경 변수

`.env.local` 파일에 다음 변수 설정:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
```

## 구조

```
src/
├── app/                  # Next.js App Router 페이지
│   ├── page.tsx         # 대시보드
│   ├── layout.tsx       # 루트 레이아웃 (Sidebar + Header)
│   └── globals.css      # shadcn/ui CSS 변수
├── components/          # 공통 컴포넌트
│   ├── ui/             # shadcn/ui 컴포넌트
│   ├── Header.tsx      # 헤더 (알림, 프로필)
│   ├── Sidebar.tsx     # 사이드바 네비게이션
│   └── StatCard.tsx    # 통계 카드
├── lib/
│   ├── api.ts          # API 클라이언트
│   └── utils.ts        # cn() 헬퍼
└── types/
    └── index.ts        # TypeScript 타입 정의
```

## Phase 1 완료 사항

### 1. shadcn/ui 적용
- Base Nova 스타일
- 라이트 모드 only
- Button, Card, Badge, Skeleton, Dialog 등 추가

### 2. 에러 처리
- API 호출 실패 시 에러 메시지 표시
- 에러 상태 UI (AlertCircle 아이콘)

### 3. 로딩 UX
- Skeleton UI 컴포넌트
- 데이터 로딩 중 깜빡임 없음

### 4. 빌드 검증
- TypeScript strict 모드 통과
- 빌드 에러 0개

### 5. API 응답 타입
- 백엔드 `{ success, data, message }` 구조 대응
- ApiResponse<T> 인터페이스 일치

## 주의사항

- **경로 별칭**: tsconfig의 `@/` 별칭이 Next.js 15에서 제대로 작동하지 않아 상대 경로 사용
- **모듈 해석**: webpack alias는 추가했지만 TypeScript 타입 체크에서는 상대 경로 필요
- **API 엔드포인트**: 백엔드 서버가 실행 중이어야 실제 데이터 로드 가능

## 다음 단계 (Phase 2)

- [ ] 문의 관리 페이지 (목록, 상세, 답변)
- [ ] 업무 관리 페이지 (칸반 보드, 상세)
- [ ] 답변 템플릿 관리
- [ ] 설정 페이지 (지식베이스, 팀원, 채널)
- [ ] 다크 모드 지원
- [ ] 실시간 업데이트 (WebSocket)
