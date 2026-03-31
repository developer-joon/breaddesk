# 답변 템플릿 API

## 개요
자주 사용하는 답변 템플릿을 관리합니다.

## 엔드포인트

### GET /api/v1/templates
**설명**: 템플릿 목록을 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Query Parameters**:
- `category`: 카테고리 필터 (선택)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "결제 오류 안내",
      "category": "결제",
      "content": "안녕하세요. 결제 오류는 다음과 같이 해결할 수 있습니다...",
      "usageCount": 15,
      "createdBy": "김철수",
      "createdAt": "2026-03-01T12:00:00",
      "updatedAt": "2026-03-15T10:00:00"
    }
  ]
}
```

---

### GET /api/v1/templates/{id}
**설명**: 특정 템플릿의 상세 정보를 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "결제 오류 안내",
    "category": "결제",
    "content": "안녕하세요. 결제 오류는 다음과 같이 해결할 수 있습니다...",
    "usageCount": 15,
    "createdBy": "김철수",
    "createdAt": "2026-03-01T12:00:00",
    "updatedAt": "2026-03-15T10:00:00"
  }
}
```

---

### POST /api/v1/templates
**설명**: 새로운 템플릿을 생성합니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "title": "환불 절차 안내",
  "category": "환불",
  "content": "환불은 다음 절차로 진행됩니다..."
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 2,
    "title": "환불 절차 안내",
    "category": "환불",
    "content": "환불은 다음 절차로 진행됩니다...",
    "usageCount": 0,
    "createdAt": "2026-03-31T12:00:00"
  }
}
```

---

### PUT /api/v1/templates/{id}
**설명**: 템플릿을 수정합니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**: POST와 동일

**Response**: 수정된 템플릿 반환

---

### DELETE /api/v1/templates/{id}
**설명**: 템플릿을 삭제합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": null
}
```

---

### POST /api/v1/templates/{id}/use
**설명**: 템플릿 사용 시 usageCount를 증가시킵니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "usageCount": 16
  }
}
```

---

## 비즈니스 규칙

1. **사용 횟수 추적**: 템플릿 선택 시 usageCount 증가
2. **카테고리**: 자유 형식 (결제, 환불, 회원가입 등)
3. **변수 치환**: 프론트엔드에서 `{{customer_name}}` 등을 실제 값으로 치환

---

## 프론트엔드 구현 참고

- `apps/web/src/services/templates.ts`: API 호출 함수
- `apps/web/src/app/templates/page.tsx`: 템플릿 관리 페이지
- 문의/업무 답변 작성 시 템플릿 선택 UI

---

## 관련 엔티티

- [ReplyTemplate](../data-model/ENTITIES.md#18-replytemplate-답변-템플릿)
