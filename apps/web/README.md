# BreadDesk Web Frontend

Next.js 15 + TypeScript + Tailwind CSS로 구현된 BreadDesk 프론트엔드입니다.

## Phase 1 구현 완료

- ✅ JWT 인증 (로그인/자동갱신)
- ✅ 팀 현황 대시보드
- ✅ 문의 관리 (채팅 UI)
- ✅ 업무 칸반 보드
- ✅ 답변 템플릿
- ✅ 지식베이스 UI
- ✅ 내 업무 + 개인 메모
- ✅ 설정 페이지
- ✅ 모바일 반응형

## 개발 서버 실행

\`\`\`bash
npm run dev
\`\`\`

## 빌드

\`\`\`bash
npm run build
\`\`\`

## 구조

- \`src/app/\` - Next.js App Router 페이지
- \`src/components/\` - 재사용 가능한 React 컴포넌트
- \`src/lib/\` - API 클라이언트 및 유틸리티
- \`src/stores/\` - Zustand 상태 관리
- \`src/types/\` - TypeScript 타입 정의

## 백엔드 API

\`apps/api/\`의 Spring Boot API와 통신합니다.
기본 API URL: http://localhost:8080/api/v1

## 환경 변수

\`.env.local\` 파일에서 설정:
\`\`\`
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
\`\`\`
