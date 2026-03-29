# Member API 명세 - 팀원 관리 및 인증

## 개요
팀원 CRUD, 인증(로그인/로그아웃), 권한 관리, 프로필 조회 API

**Base URL**: `/api/v1`

---

## 1. 인증

### 1.1 로그인

### `POST /auth/login`

#### Request
```json
{
  "email": "yhlee@company.com",
  "password": "SecureP@ssw0rd"
}
```

#### Response 200 OK
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,           // 초 (1시간)
  "member": {
    "id": 3,
    "name": "이영희",
    "email": "yhlee@company.com",
    "role": "AGENT",
    "isActive": true
  }
}
```

**JWT Claims**
```json
{
  "sub": "3",                    // 팀원 ID
  "email": "yhlee@company.com",
  "role": "AGENT",
  "iat": 1711695600,             // 발급 시간
  "exp": 1711699200              // 만료 시간 (1시간 후)
}
```

#### Error 401 Unauthorized
```json
{
  "error": "INVALID_CREDENTIALS",
  "message": "Invalid email or password"
}
```

#### Error 403 Forbidden
```json
{
  "error": "ACCOUNT_DISABLED",
  "message": "Your account has been disabled. Contact administrator."
}
```

---

### 1.2 토큰 갱신

### `POST /auth/refresh`

#### Request
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Response 200 OK
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### Error 401 Unauthorized
```json
{
  "error": "INVALID_REFRESH_TOKEN",
  "message": "Refresh token is invalid or expired"
}
```

---

### 1.3 로그아웃

### `POST /auth/logout`

**Authorization**: `Bearer {accessToken}` (헤더)

#### Response 204 No Content

**동작**
- Refresh Token을 블랙리스트에 추가 (Redis)
- 클라이언트는 로컬 스토리지에서 토큰 삭제

---

### 1.4 내 정보 조회

### `GET /auth/me`

**Authorization**: `Bearer {accessToken}` (헤더)

#### Response 200 OK
```json
{
  "id": 3,
  "name": "이영희",
  "email": "yhlee@company.com",
  "role": "AGENT",
  "skills": {
    "firewall": 0.8,
    "infra": 0.9,
    "vpn": 0.7
  },
  "isActive": true,
  "createdAt": "2025-11-20T09:00:00Z",
  "lastLoginAt": "2026-03-29T10:00:00Z"
}
```

---

## 2. 팀원 관리

### 2.1 팀원 목록 조회

### `GET /members`

**Authorization**: `Bearer {accessToken}` (헤더)

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `role` | String | N | 역할 필터 (`AGENT`, `ADMIN`) |
| `isActive` | Boolean | N | 활성 상태 필터 |
| `search` | String | N | 이름/이메일 검색 |
| `sort` | String | N | 정렬 (기본: `name,asc`) |
| `page` | Int | N | 페이지 번호 (0부터) |
| `size` | Int | N | 페이지 크기 (기본: 20) |

#### Response 200 OK
```json
{
  "content": [
    {
      "id": 3,
      "name": "이영희",
      "email": "yhlee@company.com",
      "role": "AGENT",
      "skills": {
        "firewall": 0.8,
        "infra": 0.9
      },
      "isActive": true,
      "createdAt": "2025-11-20T09:00:00Z",
      "lastLoginAt": "2026-03-29T10:00:00Z"
    },
    {
      "id": 5,
      "name": "김태호",
      "email": "thkim@company.com",
      "role": "AGENT",
      "skills": {
        "database": 0.9,
        "access": 0.7
      },
      "isActive": true,
      "createdAt": "2025-12-01T10:00:00Z",
      "lastLoginAt": "2026-03-29T09:30:00Z"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 12,
    "totalPages": 1
  }
}
```

---

### 2.2 팀원 상세 조회

### `GET /members/{id}`

**Authorization**: `Bearer {accessToken}` (헤더)

#### Response 200 OK
```json
{
  "id": 3,
  "name": "이영희",
  "email": "yhlee@company.com",
  "role": "AGENT",
  "skills": {
    "firewall": 0.8,
    "infra": 0.9,
    "vpn": 0.7
  },
  "isActive": true,
  "createdAt": "2025-11-20T09:00:00Z",
  "lastLoginAt": "2026-03-29T10:00:00Z",
  "statistics": {
    "totalTasksAssigned": 87,
    "completedTasks": 79,
    "avgCompletionTime": 5.2,    // 시간
    "currentTasks": 3,
    "slaComplianceRate": 0.92
  }
}
```

#### Error 404 Not Found
```json
{
  "error": "MEMBER_NOT_FOUND",
  "message": "Member with id 999 not found"
}
```

---

### 2.3 팀원 생성 (관리자 전용)

### `POST /members`

**Authorization**: `Bearer {accessToken}` (헤더, ADMIN만)

#### Request
```json
{
  "name": "박민수",
  "email": "mspark@company.com",
  "password": "InitialP@ssw0rd",
  "role": "AGENT",
  "skills": {
    "deploy": 0.7,
    "kubernetes": 0.6
  }
}
```

**필드 설명**
- `role`: `AGENT` | `ADMIN`
- `skills`: 업무 유형별 숙련도 (0.0~1.0)
  - 키: 업무 유형 (`firewall`, `infra`, `deploy` 등)
  - 값: 숙련도 (AI 할당 추천 시 활용)

#### Response 201 Created
```json
{
  "id": 12,
  "name": "박민수",
  "email": "mspark@company.com",
  "role": "AGENT",
  "skills": {
    "deploy": 0.7,
    "kubernetes": 0.6
  },
  "isActive": true,
  "createdAt": "2026-03-29T16:00:00Z",
  "lastLoginAt": null
}
```

#### Error 400 Bad Request
```json
{
  "error": "EMAIL_ALREADY_EXISTS",
  "message": "Email mspark@company.com is already registered"
}
```

#### Error 403 Forbidden
```json
{
  "error": "ADMIN_REQUIRED",
  "message": "Only administrators can create members"
}
```

---

### 2.4 팀원 수정 (관리자 전용)

### `PUT /members/{id}`

**Authorization**: `Bearer {accessToken}` (헤더, ADMIN만)

#### Request
```json
{
  "name": "박민수",
  "role": "ADMIN",
  "skills": {
    "deploy": 0.8,
    "kubernetes": 0.7,
    "argocd": 0.6
  },
  "isActive": true
}
```

#### Response 200 OK
```json
{
  "id": 12,
  "name": "박민수",
  "email": "mspark@company.com",
  "role": "ADMIN",
  "skills": {
    "deploy": 0.8,
    "kubernetes": 0.7,
    "argocd": 0.6
  },
  "isActive": true,
  "createdAt": "2026-03-29T16:00:00Z",
  "lastLoginAt": null
}
```

#### Error 403 Forbidden
```json
{
  "error": "ADMIN_REQUIRED",
  "message": "Only administrators can modify members"
}
```

---

### 2.5 팀원 비활성화/활성화 (관리자 전용)

### `PATCH /members/{id}/status`

**Authorization**: `Bearer {accessToken}` (헤더, ADMIN만)

#### Request
```json
{
  "isActive": false
}
```

#### Response 200 OK
```json
{
  "id": 12,
  "name": "박민수",
  "email": "mspark@company.com",
  "isActive": false,
  "updatedAt": "2026-03-29T16:30:00Z"
}
```

**동작**
- `isActive: false` 시
  - 로그인 차단
  - 기존 JWT 토큰 무효화 (블랙리스트)
  - 할당된 업무는 유지 (재할당은 관리자가 수동 처리)

---

### 2.6 비밀번호 변경

### `POST /members/me/change-password`

**Authorization**: `Bearer {accessToken}` (헤더)

#### Request
```json
{
  "currentPassword": "OldP@ssw0rd",
  "newPassword": "NewSecureP@ssw0rd"
}
```

#### Response 204 No Content

#### Error 400 Bad Request
```json
{
  "error": "INVALID_CURRENT_PASSWORD",
  "message": "Current password is incorrect"
}
```

#### Error 400 Bad Request
```json
{
  "error": "WEAK_PASSWORD",
  "message": "Password must be at least 8 characters with uppercase, lowercase, digit, and special character",
  "details": {
    "requirements": [
      "Minimum 8 characters",
      "At least 1 uppercase letter",
      "At least 1 lowercase letter",
      "At least 1 digit",
      "At least 1 special character (@$!%*?&)"
    ]
  }
}
```

---

### 2.7 비밀번호 초기화 요청 (관리자 전용)

### `POST /members/{id}/reset-password`

**Authorization**: `Bearer {accessToken}` (헤더, ADMIN만)

#### Response 200 OK
```json
{
  "temporaryPassword": "Temp#2026!xYz",
  "expiresAt": "2026-03-30T16:00:00Z",
  "message": "Temporary password has been sent to the member's email"
}
```

**동작**
- 임시 비밀번호 생성 (강력한 난수)
- 팀원 이메일로 발송
- 24시간 후 만료
- 첫 로그인 시 강제 비밀번호 변경

---

## 3. 역할 및 권한

### 역할 (Role)

| 역할 | 설명 | 권한 |
|------|------|------|
| `AGENT` | 담당자 | 업무 조회/수정/할당, 문의 답변, 템플릿 사용 |
| `ADMIN` | 관리자 | AGENT 권한 + 팀원 관리, 설정 변경, 통계 조회, 지식베이스 관리 |

### 권한 매트릭스

| 기능 | AGENT | ADMIN |
|------|-------|-------|
| 문의 조회/답변 | ✅ | ✅ |
| 업무 CRUD | ✅ | ✅ |
| 업무 할당 (자기 자신) | ✅ | ✅ |
| 업무 할당 (타인) | ❌ | ✅ |
| 템플릿 CRUD | ✅ | ✅ |
| 첨부파일 업로드/삭제 | ✅ (자기 파일만) | ✅ |
| 팀원 관리 | ❌ | ✅ |
| 통계 조회 | ❌ | ✅ |
| 지식베이스 관리 | ❌ | ✅ |
| LLM 설정 | ❌ | ✅ |
| SLA 규칙 설정 | ❌ | ✅ |

---

## 4. 팀원 통계

### 4.1 팀원별 업무 통계

### `GET /members/{id}/stats`

**Authorization**: `Bearer {accessToken}` (헤더)

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `dateFrom` | Date | N | 시작일 (기본: 30일 전) |
| `dateTo` | Date | N | 종료일 (기본: 오늘) |

#### Response 200 OK
```json
{
  "member": {
    "id": 3,
    "name": "이영희",
    "email": "yhlee@company.com"
  },
  "period": {
    "from": "2026-02-27",
    "to": "2026-03-29"
  },
  "tasks": {
    "total": 23,
    "byStatus": {
      "WAITING": 2,
      "IN_PROGRESS": 3,
      "REVIEW": 1,
      "DONE": 17
    },
    "byType": {
      "INFRA": 12,
      "FIREWALL": 8,
      "ACCESS": 3
    },
    "avgCompletionTime": 5.2,    // 시간
    "slaComplianceRate": 0.92
  },
  "inquiries": {
    "directReplies": 15,
    "avgResolutionTime": 1.8      // 시간
  }
}
```

---

### 4.2 팀 전체 통계 (관리자 전용)

### `GET /members/stats/team`

**Authorization**: `Bearer {accessToken}` (헤더, ADMIN만)

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `dateFrom` | Date | N | 시작일 (기본: 30일 전) |
| `dateTo` | Date | N | 종료일 (기본: 오늘) |

#### Response 200 OK
```json
{
  "period": {
    "from": "2026-02-27",
    "to": "2026-03-29"
  },
  "members": [
    {
      "id": 3,
      "name": "이영희",
      "tasksCompleted": 17,
      "currentTasks": 3,
      "avgCompletionTime": 5.2,
      "slaComplianceRate": 0.92
    },
    {
      "id": 5,
      "name": "김태호",
      "tasksCompleted": 14,
      "currentTasks": 2,
      "avgCompletionTime": 4.8,
      "slaComplianceRate": 0.88
    }
  ],
  "overall": {
    "totalTasksCompleted": 67,
    "avgCompletionTime": 5.5,
    "avgSlaComplianceRate": 0.89
  }
}
```

---

## 에러 코드 정리

| HTTP 상태 | 에러 코드 | 설명 |
|-----------|----------|------|
| 400 | `VALIDATION_ERROR` | 요청 데이터 검증 실패 |
| 400 | `EMAIL_ALREADY_EXISTS` | 이메일 중복 |
| 400 | `INVALID_CREDENTIALS` | 잘못된 이메일/비밀번호 |
| 400 | `INVALID_CURRENT_PASSWORD` | 현재 비밀번호 불일치 |
| 400 | `WEAK_PASSWORD` | 비밀번호 강도 부족 |
| 401 | `INVALID_REFRESH_TOKEN` | 유효하지 않은 Refresh Token |
| 401 | `UNAUTHORIZED` | 인증 필요 |
| 403 | `ACCOUNT_DISABLED` | 계정 비활성화 |
| 403 | `ADMIN_REQUIRED` | 관리자 권한 필요 |
| 404 | `MEMBER_NOT_FOUND` | 팀원 없음 |
| 500 | `INTERNAL_ERROR` | 서버 에러 |

---

## 보안 고려사항

### 1. 비밀번호 정책
- 최소 8자
- 대문자 1개 이상
- 소문자 1개 이상
- 숫자 1개 이상
- 특문 1개 이상 (`@$!%*?&`)

### 2. 비밀번호 저장
- bcrypt 해싱 (cost factor: 12)
- Salt 자동 생성

### 3. JWT 보안
- Access Token: 1시간 만료
- Refresh Token: 7일 만료
- 로그아웃 시 Refresh Token 블랙리스트 (Redis)
- HTTPS 전용 전송

### 4. Rate Limiting
- 로그인: 5회/분 (IP 기준)
- 비밀번호 변경: 3회/시간 (사용자 기준)

---

## 향후 확장 (Phase 2+)

### SSO 통합
- OAuth 2.0 (Google Workspace, Microsoft Azure AD)
- SAML 2.0

### 다중 인증 (MFA)
- TOTP (Google Authenticator, Authy)
- SMS 인증 코드

### 세션 관리
- 활성 세션 목록 조회
- 원격 세션 강제 종료

---

**작성일**: 2026-03-29  
**버전**: v1.0
