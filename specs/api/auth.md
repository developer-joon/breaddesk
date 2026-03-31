# 인증 API

## 개요
JWT 기반 인증을 제공합니다. Access Token과 Refresh Token을 발급하며, Refresh Token으로 Access Token을 갱신할 수 있습니다.

## 엔드포인트

### POST /api/v1/auth/login
**설명**: 이메일/비밀번호로 로그인하여 JWT 토큰을 발급받습니다.

**인증**: 불필요

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**에러 코드**:
- `400 Bad Request`: 유효하지 않은 이메일 형식 또는 필수 필드 누락
- `401 Unauthorized`: 이메일/비밀번호 불일치
- `404 Not Found`: 사용자를 찾을 수 없음

---

### POST /api/v1/auth/refresh
**설명**: Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.

**인증**: 불필요 (Refresh Token 검증)

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**에러 코드**:
- `400 Bad Request`: Refresh Token 누락
- `401 Unauthorized`: 유효하지 않거나 만료된 Refresh Token
- `404 Not Found`: 사용자를 찾을 수 없음

---

## JWT 토큰 구조

### Access Token
- **유효기간**: 구현에 따라 다름 (일반적으로 15분~1시간)
- **Payload**:
  - `sub`: 사용자 이메일
  - `role`: 사용자 역할 (AGENT, ADMIN)
  - `exp`: 만료 시각

### Refresh Token
- **유효기간**: 구현에 따라 다름 (일반적으로 7일~30일)
- **Payload**:
  - `sub`: 사용자 이메일
  - `role`: 사용자 역할
  - `exp`: 만료 시각

---

## 인증 방식

모든 보호된 API는 HTTP 헤더에 Access Token을 포함해야 합니다:

```
Authorization: Bearer {accessToken}
```

Access Token이 만료되면 Refresh Token을 사용하여 `/api/v1/auth/refresh`를 호출하여 갱신합니다.

---

## 비즈니스 규칙

1. **비밀번호 저장**: 평문이 아닌 해시로 저장 (BCrypt 등)
2. **Refresh Token 재사용**: Refresh 요청 시 새로운 Refresh Token도 함께 발급 (Rotation 방식)
3. **역할 기반 접근 제어**: ADMIN 전용 API는 role=ADMIN 토큰만 허용
4. **토큰 검증**: 서명, 만료 시각, 발행자 검증

---

## 프론트엔드 구현 참고

- `apps/web/src/stores/auth.ts`: Zustand 기반 인증 상태 관리
- Access Token은 메모리에, Refresh Token은 httpOnly 쿠키에 저장 권장 (XSS 방어)
- 401 응답 시 자동으로 Refresh Token으로 갱신 시도
