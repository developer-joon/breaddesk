# 개인 메모 API

## 개요
팀원별 개인 메모를 작성하고 관리합니다. 다른 팀원은 볼 수 없습니다.

## 엔드포인트

### GET /api/v1/personal-notes
**설명**: 현재 사용자의 개인 메모를 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "content": "오늘 할 일: PG사 API 확인, SLA 규칙 업데이트",
      "createdAt": "2026-03-31T09:00:00",
      "updatedAt": "2026-03-31T12:00:00"
    }
  ]
}
```

---

### POST /api/v1/personal-notes
**설명**: 새로운 개인 메모를 생성합니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "content": "오늘 할 일: PG사 API 확인"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "content": "오늘 할 일: PG사 API 확인",
    "createdAt": "2026-03-31T09:00:00"
  }
}
```

---

### PUT /api/v1/personal-notes/{id}
**설명**: 개인 메모를 수정합니다.

**인증**: 필요 (AGENT, ADMIN, 본인 메모만)

**Request Body**:
```json
{
  "content": "오늘 할 일: PG사 API 확인, SLA 규칙 업데이트"
}
```

**Response**: 수정된 메모 반환

**에러 코드**:
- `403 Forbidden`: 다른 사람의 메모 수정 시도
- `404 Not Found`: 메모를 찾을 수 없음

---

### DELETE /api/v1/personal-notes/{id}
**설명**: 개인 메모를 삭제합니다.

**인증**: 필요 (AGENT, ADMIN, 본인 메모만)

**Response**:
```json
{
  "success": true,
  "data": null
}
```

---

## 비즈니스 규칙

1. **프라이버시**: 본인의 메모만 조회/수정/삭제 가능
2. **용도**: To-Do 리스트, 개인 메모, 학습 노트 등
3. **마크다운 지원**: 프론트엔드에서 마크다운 렌더링 권장

---

## 프론트엔드 구현 참고

- `apps/web/src/app/my/page.tsx`: 내 업무 페이지에서 개인 메모 표시
- 마크다운 에디터: `react-markdown` 또는 `react-mde`

---

## 관련 엔티티

- [PersonalNote](../data-model/ENTITIES.md#16-personalnote-개인-메모)
