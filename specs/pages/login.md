# 로그인 페이지

## 경로
`/login`

## 인증
불필요 (공개 페이지)

## 주요 컴포넌트
- 이메일 입력 필드
- 비밀번호 입력 필드
- 로그인 버튼
- 에러 메시지 표시 영역

## 데이터 소스
- `POST /api/v1/auth/login` - 로그인 요청

## 사용자 인터랙션

### 로그인 프로세스
1. 이메일/비밀번호 입력
2. "로그인" 버튼 클릭
3. API 호출: `POST /api/v1/auth/login`
4. 성공 시:
   - accessToken, refreshToken 저장 (메모리 또는 쿠키)
   - `/dashboard`로 리다이렉트
5. 실패 시:
   - 에러 메시지 표시 ("이메일 또는 비밀번호가 올바르지 않습니다")

### 자동 로그인
- 페이지 진입 시 accessToken 유효성 검사
- 유효하면 `/dashboard`로 자동 리다이렉트
- 만료됐으면 refreshToken으로 갱신 시도

## 상태 관리
- `apps/web/src/stores/auth.ts` (Zustand)
- `login()`, `logout()`, `refreshToken()` 함수

## 구현 참고
- `apps/web/src/app/login/page.tsx`
- `apps/web/src/services/auth.ts` (미구현 시 추가 필요)
