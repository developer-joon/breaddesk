# BreadDesk Web (Frontend)

Next.js 15 + TypeScript + Tailwind CSS로 구현된 BreadDesk 프론트엔드입니다.

## 기술 스택

- **Framework**: Next.js 15 (App Router)
- **Language**: TypeScript (strict mode)
- **Styling**: Tailwind CSS
- **State**: React Hooks
- **API**: Fetch API 래퍼

## 프로젝트 구조

```
src/
├── app/                # Next.js App Router
│   ├── page.tsx        # 대시보드
│   ├── tasks/          # 업무 관리 (칸반, 상세)
│   ├── inquiries/      # 문의 관리 (목록, 상세)
│   ├── templates/      # 답변 템플릿
│   └── settings/       # 설정
├── components/         # 공통 컴포넌트
│   ├── Sidebar.tsx     # 사이드바 네비게이션
│   ├── Header.tsx      # 헤더
│   └── StatCard.tsx    # 통계 카드
├── lib/                # 유틸리티
│   └── api.ts          # API 클라이언트
└── types/              # 타입 정의
    └── index.ts        # 공통 타입
```

## 개발 환경 설정

### 환경 변수

`.env.local` 파일을 생성하고 백엔드 API URL을 설정하세요:

\`\`\`bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
\`\`\`

### 실행

\`\`\`bash
# 개발 서버 실행 (http://localhost:3000)
npm run dev

# 프로덕션 빌드
npm run build

# 프로덕션 서버 실행
npm run start

# 타입 체크
npm run type-check
\`\`\`

## 구현된 기능 (Phase 1)

### 1. 레이아웃
- ✅ 사이드바 네비게이션 (문의, 업무, 템플릿, 설정)
- ✅ 헤더 (로고 + 알림)
- ✅ 반응형 디자인 (모바일 햄버거 메뉴)

### 2. 대시보드
- ✅ 전체 현황 카드 (총 문의, 미해결, 오늘 접수, 처리율)
- ✅ 최근 문의 목록 (5건)
- ✅ 업무 상태별 요약

### 3. 업무 관리
- ✅ 칸반 보드 (WAITING / IN_PROGRESS / REVIEW / DONE)
- ✅ 업무 카드 (제목, 유형 배지, 긴급도, 담당자)
- ✅ 업무 상세 페이지
  - 제목, 설명, 상태, 긴급도, 담당자
  - 체크리스트
  - 태그
  - 코멘트/로그
  - SLA 정보

### 4. 문의 관리
- ✅ 문의 목록 (상태 필터, 검색)
- ✅ 문의 상세
  - 대화 이력 (사용자/AI/담당자 구분)
  - 담당자 답변 입력
  - 업무로 전환 (에스컬레이션)

### 5. 답변 템플릿
- ✅ 템플릿 목록
- ✅ 생성/수정/삭제
- ✅ 카테고리 분류
- ✅ 사용 횟수 통계

### 6. 설정
- ✅ LLM Provider 설정
- ✅ 업무 유형 관리
- ✅ SLA 규칙 표시

## API 연동

`lib/api.ts`에 모든 API 엔드포인트가 정의되어 있습니다:

- **Inquiry**: 문의 목록, 상세, 답변, 피드백
- **Task**: 업무 CRUD, 할당, 칸반 데이터
- **Template**: 템플릿 CRUD
- **Dashboard**: 통계 데이터
- **Knowledge**: 지식베이스 커넥터
- **Member**: 팀원 목록

## 다음 단계 (Phase 2+)

- [ ] 업무 드래그앤드롭 (칸반)
- [ ] 파일 첨부 UI
- [ ] 실시간 알림
- [ ] 지식베이스 검색 UI
- [ ] 통계 차트
- [ ] 다크 모드
- [ ] 키보드 단축키

## 주의사항

- 현재 빌드 시 Monorepo 환경으로 인한 모듈 해석 이슈가 있을 수 있습니다. 개발 모드(`npm run dev`)는 정상 작동합니다.
- 실제 API가 연결되기 전까지 로딩 상태나 빈 데이터가 표시됩니다.

## 라이선스

Proprietary - BreadLab
