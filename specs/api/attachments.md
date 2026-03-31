# 첨부파일 API

## 개요
문의(Inquiry) 또는 업무(Task)에 파일을 첨부하고 조회합니다.

## 엔드포인트

### POST /api/v1/attachments
**설명**: 파일을 업로드합니다. multipart/form-data 형식입니다.

**인증**: 필요 (AGENT, ADMIN) 또는 불필요 (문의 첨부 시)

**Request (multipart/form-data)**:
- `file`: 파일
- `entityType`: `INQUIRY` 또는 `TASK`
- `entityId`: 대상 엔티티 ID

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "entityType": "TASK",
    "entityId": 42,
    "filename": "screenshot.png",
    "filePath": "/uploads/2026/03/31/abc123.png",
    "fileSize": 102400,
    "mimeType": "image/png",
    "uploadedBy": 5,
    "uploadedByName": "김철수",
    "createdAt": "2026-03-31T12:00:00"
  }
}
```

**에러 코드**:
- `400 Bad Request`: 파일 크기 초과 (최대 10MB) 또는 유효하지 않은 파일 형식
- `404 Not Found`: entityId가 존재하지 않음

---

### GET /api/v1/attachments/{id}
**설명**: 첨부파일 메타데이터를 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "entityType": "TASK",
    "entityId": 42,
    "filename": "screenshot.png",
    "filePath": "/uploads/2026/03/31/abc123.png",
    "fileSize": 102400,
    "mimeType": "image/png",
    "uploadedBy": 5,
    "createdAt": "2026-03-31T12:00:00"
  }
}
```

---

### GET /api/v1/attachments/{id}/download
**설명**: 파일을 다운로드합니다. 실제 파일 스트림을 반환합니다.

**인증**: 필요 (AGENT, ADMIN) 또는 불필요 (문의 첨부 시)

**Response**: 파일 스트림 (Content-Type: 원본 MIME 타입, Content-Disposition: attachment)

**에러 코드**:
- `404 Not Found`: 파일을 찾을 수 없음

---

### DELETE /api/v1/attachments/{id}
**설명**: 첨부파일을 삭제합니다. 실제 파일도 함께 삭제됩니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": null
}
```

---

### GET /api/v1/attachments?entityType={type}&entityId={id}
**설명**: 특정 엔티티의 첨부파일 목록을 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Query Parameters**:
- `entityType`: `INQUIRY` 또는 `TASK`
- `entityId`: 대상 엔티티 ID

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "filename": "screenshot.png",
      "fileSize": 102400,
      "mimeType": "image/png",
      "uploadedBy": 5,
      "createdAt": "2026-03-31T12:00:00"
    }
  ]
}
```

---

## 비즈니스 규칙

1. **저장 경로**: `/uploads/YYYY/MM/DD/` 디렉토리에 UUID 파일명으로 저장
2. **파일 크기 제한**: 최대 10MB (설정 가능)
3. **허용 MIME 타입**: 이미지, PDF, 문서 파일 (exe, bat 등 실행파일 차단)
4. **삭제 시 연쇄**: 엔티티 삭제 시 첨부파일도 함께 삭제 (ON DELETE CASCADE)

---

## 프론트엔드 구현 참고

- `apps/web/src/services/attachments.ts`: API 호출 함수 (미구현 시 구현 필요)
- Drag & Drop 업로드: react-dropzone 등
- 이미지 미리보기: `<img src="/api/v1/attachments/{id}/download" />`

---

## 관련 엔티티

- [Attachment](../data-model/ENTITIES.md#17-attachment-첨부파일)
