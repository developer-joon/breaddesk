# 팀원 관리 API

## 개요
팀원(Member) 계정을 생성, 조회, 수정, 삭제합니다.

## 엔드포인트

### GET /api/v1/members
**설명**: 전체 팀원 목록을 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "김철수",
      "email": "kim@company.com",
      "role": "AGENT",
      "skills": {"languages": ["한국어", "영어"], "expertise": ["결제", "환불"]},
      "isActive": true,
      "createdAt": "2026-03-01T12:00:00"
    }
  ]
}
```

---

### GET /api/v1/members/{id}
**설명**: 특정 팀원의 상세 정보를 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "김철수",
    "email": "kim@company.com",
    "role": "AGENT",
    "skills": {"languages": ["한국어", "영어"], "expertise": ["결제", "환불"]},
    "isActive": true,
    "createdAt": "2026-03-01T12:00:00",
    "updatedAt": "2026-03-31T10:00:00"
  }
}
```

**에러 코드**:
- `404 Not Found`: 팀원을 찾을 수 없음

---

### POST /api/v1/members
**설명**: 새로운 팀원을 생성합니다.

**인증**: 필요 (ADMIN)

**Request Body**:
```json
{
  "name": "이영희",
  "email": "lee@company.com",
  "password": "password123",
  "role": "AGENT",
  "skills": {"languages": ["한국어"], "expertise": ["회원가입"]}
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 2,
    "name": "이영희",
    "email": "lee@company.com",
    "role": "AGENT",
    "isActive": true,
    "createdAt": "2026-03-31T12:00:00"
  }
}
```

**에러 코드**:
- `400 Bad Request`: 이메일 중복 또는 필수 필드 누락
- `403 Forbidden`: ADMIN 권한 필요

---

### PUT /api/v1/members/{id}
**설명**: 팀원 정보를 수정합니다.

**인증**: 필요 (ADMIN 또는 본인)

**Request Body**:
```json
{
  "name": "이영희",
  "email": "lee@company.com",
  "role": "ADMIN",
  "skills": {"languages": ["한국어", "영어"], "expertise": ["회원가입", "결제"]},
  "isActive": true
}
```

**Response**: 수정된 팀원 정보 반환

**비즈니스 규칙**:
- role 변경은 ADMIN만 가능
- 본인 정보 수정 시 name, skills만 변경 가능

**에러 코드**:
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 팀원을 찾을 수 없음

---

### DELETE /api/v1/members/{id}
**설명**: 팀원을 삭제합니다. (실제로는 isActive=false로 변경하여 soft delete 권장)

**인증**: 필요 (ADMIN)

**Response**:
```json
{
  "success": true,
  "data": null
}
```

**에러 코드**:
- `403 Forbidden`: ADMIN 권한 필요
- `404 Not Found`: 팀원을 찾을 수 없음

---

## 비즈니스 규칙

1. **역할**: AGENT(일반 상담원), ADMIN(관리자)
2. **스킬 포맷**: JSONB로 자유 형식 저장, AI 담당자 추천에 활용
3. **비밀번호**: BCrypt 해시로 저장, 응답에 포함 안 됨
4. **이메일 고유성**: 중복 불가

---

## 관련 엔티티

- [Member](../data-model/ENTITIES.md#1-member-팀원)
