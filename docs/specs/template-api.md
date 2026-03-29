# Template API 명세 - 답변 템플릿 관리

## 개요
담당자가 자주 사용하는 답변 템플릿 CRUD 및 변수 치환 API

**Base URL**: `/api/v1`

---

## 1. 템플릿 목록 조회

### `GET /templates`

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `category` | String | N | 카테고리 필터 |
| `search` | String | N | 제목/내용 검색 |
| `sort` | String | N | 정렬 (기본: `usageCount,desc`) |
| `page` | Int | N | 페이지 번호 (0부터) |
| `size` | Int | N | 페이지 크기 (기본: 20) |

**정렬 옵션**
- `usageCount,desc` (기본) - 사용 빈도순
- `createdAt,desc` - 최신 등록순
- `title,asc` - 제목 오름차순

#### Response 200 OK
```json
{
  "content": [
    {
      "id": 1,
      "title": "VPN 클라이언트 재설치 가이드",
      "category": "VPN",
      "content": "안녕하세요, {{이름}}님.\n\nVPN 접속 문제는 다음 단계로 해결할 수 있습니다:\n\n1. 기존 VPN 클라이언트 완전 삭제\n2. {{다운로드_링크}}에서 최신 버전 다운로드\n3. 재설치 후 {{서버주소}}로 접속\n\n문제 지속 시 IT팀(내선 1234)으로 연락 주세요.",
      "usageCount": 127,
      "createdBy": {
        "id": 3,
        "name": "이영희"
      },
      "createdAt": "2026-01-15T10:00:00Z",
      "updatedAt": "2026-03-20T14:30:00Z"
    },
    {
      "id": 2,
      "title": "DB 권한 승인 안내",
      "category": "ACCESS",
      "content": "{{이름}}님의 {{DB명}} 읽기 권한 요청이 승인되었습니다.\n\n접속 정보:\n- Host: {{호스트}}\n- Port: {{포트}}\n- Database: {{DB명}}\n- Username: {{사용자명}}\n\n비밀번호는 별도 메일로 발송되었습니다.",
      "usageCount": 89,
      "createdBy": {
        "id": 5,
        "name": "김태호"
      },
      "createdAt": "2026-02-10T09:00:00Z",
      "updatedAt": "2026-02-10T09:00:00Z"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 23,
    "totalPages": 2
  }
}
```

---

## 2. 템플릿 상세 조회

### `GET /templates/{id}`

#### Response 200 OK
```json
{
  "id": 1,
  "title": "VPN 클라이언트 재설치 가이드",
  "category": "VPN",
  "content": "안녕하세요, {{이름}}님.\n\nVPN 접속 문제는 다음 단계로 해결할 수 있습니다:\n\n1. 기존 VPN 클라이언트 완전 삭제\n2. {{다운로드_링크}}에서 최신 버전 다운로드\n3. 재설치 후 {{서버주소}}로 접속\n\n문제 지속 시 IT팀(내선 1234)으로 연락 주세요.",
  "variables": [
    "이름",
    "다운로드_링크",
    "서버주소"
  ],
  "usageCount": 127,
  "createdBy": {
    "id": 3,
    "name": "이영희",
    "email": "yhlee@company.com"
  },
  "createdAt": "2026-01-15T10:00:00Z",
  "updatedAt": "2026-03-20T14:30:00Z"
}
```

**변수 자동 감지**
- 템플릿 내용에서 `{{변수명}}` 패턴을 자동으로 파싱하여 `variables` 배열 반환
- 프론트엔드에서 입력 폼 동적 생성 시 활용

#### Error 404 Not Found
```json
{
  "error": "TEMPLATE_NOT_FOUND",
  "message": "Template with id 999 not found"
}
```

---

## 3. 템플릿 생성

### `POST /templates`

#### Request
```json
{
  "title": "방화벽 규칙 추가 완료 안내",
  "category": "FIREWALL",
  "content": "{{이름}}님의 방화벽 규칙 요청이 완료되었습니다.\n\n출발지: {{출발지_IP}}\n목적지: {{목적지_IP}}\n포트: {{포트}}\n프로토콜: {{프로토콜}}\n\n적용 완료 시간: {{적용_시간}}"
}
```

**필드 설명**
- `title`: 템플릿 제목 (필수, 최대 200자)
- `category`: 카테고리 (선택, 최대 100자)
  - 권장: `VPN`, `ACCESS`, `FIREWALL`, `DEPLOY`, `GENERAL` 등
- `content`: 템플릿 내용 (필수)
  - `{{변수명}}` 형식으로 변수 삽입 가능

#### Response 201 Created
```json
{
  "id": 24,
  "title": "방화벽 규칙 추가 완료 안내",
  "category": "FIREWALL",
  "content": "{{이름}}님의 방화벽 규칙 요청이 완료되었습니다.\n\n출발지: {{출발지_IP}}\n목적지: {{목적지_IP}}\n포트: {{포트}}\n프로토콜: {{프로토콜}}\n\n적용 완료 시간: {{적용_시간}}",
  "variables": [
    "이름",
    "출발지_IP",
    "목적지_IP",
    "포트",
    "프로토콜",
    "적용_시간"
  ],
  "usageCount": 0,
  "createdBy": {
    "id": 5,
    "name": "김태호"
  },
  "createdAt": "2026-03-29T15:30:00Z",
  "updatedAt": "2026-03-29T15:30:00Z"
}
```

#### Error 400 Bad Request
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Title is required",
  "details": {
    "field": "title",
    "error": "must not be blank"
  }
}
```

---

## 4. 템플릿 수정

### `PUT /templates/{id}`

#### Request
```json
{
  "title": "방화벽 규칙 추가 완료 안내 (개선)",
  "category": "FIREWALL",
  "content": "{{이름}}님의 방화벽 규칙 요청이 완료되었습니다.\n\n**적용 정보**\n- 출발지: {{출발지_IP}}\n- 목적지: {{목적지_IP}}\n- 포트: {{포트}}\n- 프로토콜: {{프로토콜}}\n- 적용 완료: {{적용_시간}}\n\n변경 사항은 즉시 반영됩니다."
}
```

#### Response 200 OK
```json
{
  "id": 24,
  "title": "방화벽 규칙 추가 완료 안내 (개선)",
  "category": "FIREWALL",
  "content": "...",
  "variables": [
    "이름",
    "출발지_IP",
    "목적지_IP",
    "포트",
    "프로토콜",
    "적용_시간"
  ],
  "usageCount": 0,
  "createdBy": {
    "id": 5,
    "name": "김태호"
  },
  "createdAt": "2026-03-29T15:30:00Z",
  "updatedAt": "2026-03-29T15:45:00Z"
}
```

#### Error 404 Not Found
```json
{
  "error": "TEMPLATE_NOT_FOUND",
  "message": "Template with id 999 not found"
}
```

---

## 5. 템플릿 삭제

### `DELETE /templates/{id}`

#### Response 204 No Content

#### Error 404 Not Found
```json
{
  "error": "TEMPLATE_NOT_FOUND",
  "message": "Template with id 999 not found"
}
```

---

## 6. 템플릿 변수 치환 (미리보기)

### `POST /templates/{id}/preview`

#### Request
```json
{
  "variables": {
    "이름": "김철수",
    "출발지_IP": "10.0.1.100",
    "목적지_IP": "192.168.1.50",
    "포트": "3306",
    "프로토콜": "TCP",
    "적용_시간": "2026-03-29 15:50"
  }
}
```

#### Response 200 OK
```json
{
  "originalContent": "{{이름}}님의 방화벽 규칙 요청이 완료되었습니다.\n\n**적용 정보**\n- 출발지: {{출발지_IP}}\n- 목적지: {{목적지_IP}}\n- 포트: {{포트}}\n- 프로토콜: {{프로토콜}}\n- 적용 완료: {{적용_시간}}\n\n변경 사항은 즉시 반영됩니다.",
  "renderedContent": "김철수님의 방화벽 규칙 요청이 완료되었습니다.\n\n**적용 정보**\n- 출발지: 10.0.1.100\n- 목적지: 192.168.1.50\n- 포트: 3306\n- 프로토콜: TCP\n- 적용 완료: 2026-03-29 15:50\n\n변경 사항은 즉시 반영됩니다.",
  "missingVariables": [],
  "unusedVariables": []
}
```

**동작**
- 변수 치환 후 결과 미리보기
- 누락/미사용 변수 감지
- 프론트엔드에서 사용자가 변수 입력 시 실시간 미리보기 제공 가능

#### Error 400 Bad Request (변수 누락)
```json
{
  "error": "MISSING_VARIABLES",
  "message": "Required variables are missing",
  "details": {
    "missingVariables": ["포트", "프로토콜"]
  }
}
```

---

## 7. 템플릿 사용 (실제 답변 전송)

### `POST /templates/{id}/apply`

문의 또는 업무에 템플릿을 적용하여 답변 전송

#### Request
```json
{
  "targetType": "INQUIRY",      // INQUIRY | TASK
  "targetId": 42,                // 문의 ID 또는 업무 ID
  "variables": {
    "이름": "김철수",
    "다운로드_링크": "https://vpn.company.com/download",
    "서버주소": "vpn.company.com:443"
  },
  "additionalMessage": "추가로 궁금한 점 있으면 언제든 연락 주세요."  // optional
}
```

#### Response 200 OK
```json
{
  "templateId": 1,
  "targetType": "INQUIRY",
  "targetId": 42,
  "renderedContent": "안녕하세요, 김철수님.\n\nVPN 접속 문제는 다음 단계로 해결할 수 있습니다:\n\n1. 기존 VPN 클라이언트 완전 삭제\n2. https://vpn.company.com/download에서 최신 버전 다운로드\n3. 재설치 후 vpn.company.com:443로 접속\n\n문제 지속 시 IT팀(내선 1234)으로 연락 주세요.\n\n추가로 궁금한 점 있으면 언제든 연락 주세요.",
  "sentAt": "2026-03-29T16:00:00Z"
}
```

**동작**
1. 변수 치환하여 최종 메시지 생성
2. 대상 문의/업무에 답변으로 추가
   - 문의: `POST /inquiries/{id}/reply` 내부 호출
   - 업무: 코멘트로 추가 (향후 구현)
3. 템플릿 사용 횟수 +1 (`usageCount`)
4. 웹훅 트리거 (n8n → 사용자 채널)

#### Error 400 Bad Request
```json
{
  "error": "MISSING_VARIABLES",
  "message": "Required variables are missing",
  "details": {
    "missingVariables": ["서버주소"]
  }
}
```

---

## 8. 인기 템플릿 통계

### `GET /templates/stats/popular`

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `dateFrom` | Date | N | 시작일 (기본: 30일 전) |
| `dateTo` | Date | N | 종료일 (기본: 오늘) |
| `limit` | Int | N | 최대 결과 수 (기본: 10) |

#### Response 200 OK
```json
{
  "period": {
    "from": "2026-02-27",
    "to": "2026-03-29"
  },
  "popularTemplates": [
    {
      "id": 1,
      "title": "VPN 클라이언트 재설치 가이드",
      "category": "VPN",
      "usageCount": 127,
      "usageCountInPeriod": 23,
      "avgResolutionRate": 0.85      // 이 템플릿 사용 후 해결율
    },
    {
      "id": 3,
      "title": "비밀번호 초기화 안내",
      "category": "ACCESS",
      "usageCount": 98,
      "usageCountInPeriod": 18,
      "avgResolutionRate": 0.95
    }
  ]
}
```

---

## 9. 카테고리 목록 조회

### `GET /templates/categories`

#### Response 200 OK
```json
{
  "categories": [
    {
      "name": "VPN",
      "templateCount": 8,
      "usageCount": 256
    },
    {
      "name": "ACCESS",
      "templateCount": 12,
      "usageCount": 198
    },
    {
      "name": "FIREWALL",
      "templateCount": 5,
      "usageCount": 87
    },
    {
      "name": "DEPLOY",
      "templateCount": 3,
      "usageCount": 42
    },
    {
      "name": "GENERAL",
      "templateCount": 15,
      "usageCount": 321
    }
  ]
}
```

**용도**
- 프론트엔드에서 카테고리 필터 드롭다운 생성
- 카테고리별 통계 제공

---

## 변수 시스템 규칙

### 변수 명명 규칙
- 형식: `{{변수명}}`
- 변수명은 한글, 영문, 숫자, 언더스코어(`_`) 가능
- 공백은 언더스코어로 대체 권장 (예: `{{서버_주소}}`)

### 예약 변수 (자동 치환)
| 변수 | 설명 | 예시 |
|------|------|------|
| `{{요청자_이름}}` | 문의/업무 요청자 이름 | `김철수` |
| `{{요청자_이메일}}` | 문의/업무 요청자 이메일 | `cskim@company.com` |
| `{{담당자_이름}}` | 현재 로그인한 담당자 이름 | `이영희` |
| `{{현재_날짜}}` | 오늘 날짜 (YYYY-MM-DD) | `2026-03-29` |
| `{{현재_시간}}` | 현재 시간 (HH:mm) | `16:00` |

**자동 치환 동작**
- 예약 변수는 사용자가 입력하지 않아도 시스템이 자동으로 치환
- 사용자 정의 변수는 명시적 입력 필요

---

## 에러 코드 정리

| HTTP 상태 | 에러 코드 | 설명 |
|-----------|----------|------|
| 400 | `VALIDATION_ERROR` | 요청 데이터 검증 실패 |
| 400 | `MISSING_VARIABLES` | 필수 변수 누락 |
| 404 | `TEMPLATE_NOT_FOUND` | 템플릿 없음 |
| 404 | `TARGET_NOT_FOUND` | 대상 문의/업무 없음 |
| 500 | `INTERNAL_ERROR` | 서버 에러 |

---

## 향후 확장 (Phase 2+)

### AI 템플릿 추천
- 문의 내용 분석 → 적합한 템플릿 자동 추천
- 담당자가 템플릿 선택 시 변수만 입력

### 템플릿 공유
- 팀 내 템플릿 공유
- 관리자 승인 시 전사 템플릿으로 승격

### 다국어 지원
- 같은 템플릿의 다국어 버전 관리
- 요청자 언어 설정에 따라 자동 선택

---

**작성일**: 2026-03-29  
**버전**: v1.0
