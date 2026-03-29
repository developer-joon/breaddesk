# Attachment API 명세 - 파일 첨부 관리

## 개요
문의 및 업무에 파일 첨부, 다운로드, 삭제 API

**Base URL**: `/api/v1`

---

## 1. 파일 업로드

### `POST /attachments`

#### Request
**Content-Type**: `multipart/form-data`

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `file` | File | Y | 업로드할 파일 |
| `entityType` | String | Y | `INQUIRY` 또는 `TASK` |
| `entityId` | Long | Y | 문의 ID 또는 업무 ID |

**예시 (cURL)**
```bash
curl -X POST https://api.breaddesk.com/api/v1/attachments \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@screenshot.png" \
  -F "entityType=INQUIRY" \
  -F "entityId=42"
```

#### Response 201 Created
```json
{
  "id": 7,
  "entityType": "INQUIRY",
  "entityId": 42,
  "filename": "screenshot.png",
  "originalFilename": "스크린샷 2026-03-29 오후 3.45.12.png",
  "mimeType": "image/png",
  "fileSize": 524288,
  "uploadedBy": {
    "id": 3,
    "name": "이영희",
    "email": "yhlee@company.com"
  },
  "uploadedAt": "2026-03-29T15:45:30Z",
  "url": "/api/v1/attachments/7/download",
  "thumbnailUrl": "/api/v1/attachments/7/thumbnail"  // 이미지만 제공
}
```

#### 파일 제한
- **최대 파일 크기**: 10MB
- **최대 파일 수**: 5개/건 (문의 또는 업무당)
- **허용 확장자**:
  - 이미지: `.jpg`, `.jpeg`, `.png`, `.gif`, `.webp`
  - 문서: `.pdf`, `.txt`, `.md`, `.doc`, `.docx`, `.xls`, `.xlsx`
  - 압축: `.zip`, `.tar`, `.gz`
  - 로그: `.log`

#### Error 400 Bad Request
```json
{
  "error": "FILE_TOO_LARGE",
  "message": "File size exceeds 10MB limit",
  "details": {
    "filename": "large-log.zip",
    "size": 15728640,
    "maxSize": 10485760
  }
}
```

#### Error 400 Bad Request (확장자 제한)
```json
{
  "error": "INVALID_FILE_TYPE",
  "message": "File type not allowed",
  "details": {
    "filename": "script.exe",
    "mimeType": "application/x-msdownload",
    "allowedTypes": ["image/*", "application/pdf", "text/*", "application/zip"]
  }
}
```

#### Error 400 Bad Request (파일 수 초과)
```json
{
  "error": "MAX_FILES_EXCEEDED",
  "message": "Maximum 5 files per inquiry/task",
  "details": {
    "currentCount": 5,
    "maxCount": 5
  }
}
```

#### Error 404 Not Found
```json
{
  "error": "ENTITY_NOT_FOUND",
  "message": "Inquiry with id 999 not found"
}
```

---

## 2. 첨부파일 목록 조회

### `GET /attachments`

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `entityType` | String | Y | `INQUIRY` 또는 `TASK` |
| `entityId` | Long | Y | 문의 ID 또는 업무 ID |

#### Response 200 OK
```json
{
  "attachments": [
    {
      "id": 7,
      "entityType": "INQUIRY",
      "entityId": 42,
      "filename": "screenshot.png",
      "originalFilename": "스크린샷 2026-03-29 오후 3.45.12.png",
      "mimeType": "image/png",
      "fileSize": 524288,
      "uploadedBy": {
        "id": 3,
        "name": "이영희"
      },
      "uploadedAt": "2026-03-29T15:45:30Z",
      "url": "/api/v1/attachments/7/download",
      "thumbnailUrl": "/api/v1/attachments/7/thumbnail"
    },
    {
      "id": 8,
      "entityType": "INQUIRY",
      "entityId": 42,
      "filename": "error-log.txt",
      "originalFilename": "error.log",
      "mimeType": "text/plain",
      "fileSize": 8192,
      "uploadedBy": {
        "id": 3,
        "name": "이영희"
      },
      "uploadedAt": "2026-03-29T15:46:00Z",
      "url": "/api/v1/attachments/8/download",
      "thumbnailUrl": null
    }
  ]
}
```

---

## 3. 파일 다운로드

### `GET /attachments/{id}/download`

#### Response 200 OK
**Content-Type**: 원본 파일의 MIME 타입  
**Content-Disposition**: `attachment; filename="screenshot.png"`

바이너리 파일 데이터 반환

#### Error 404 Not Found
```json
{
  "error": "ATTACHMENT_NOT_FOUND",
  "message": "Attachment with id 999 not found"
}
```

---

## 4. 썸네일 조회 (이미지만)

### `GET /attachments/{id}/thumbnail`

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `size` | Int | N | 썸네일 크기 (기본: 200, 최대: 800) |

#### Response 200 OK
**Content-Type**: `image/jpeg`

썸네일 이미지 반환 (JPEG로 변환)

#### Error 400 Bad Request
```json
{
  "error": "NOT_AN_IMAGE",
  "message": "Thumbnail is only available for image files",
  "details": {
    "mimeType": "text/plain"
  }
}
```

---

## 5. 파일 삭제

### `DELETE /attachments/{id}`

#### Response 204 No Content

#### Error 403 Forbidden
```json
{
  "error": "DELETE_FORBIDDEN",
  "message": "You can only delete your own uploads"
}
```

#### Error 404 Not Found
```json
{
  "error": "ATTACHMENT_NOT_FOUND",
  "message": "Attachment with id 999 not found"
}
```

---

## 6. 파일 메타데이터 조회

### `GET /attachments/{id}`

#### Response 200 OK
```json
{
  "id": 7,
  "entityType": "INQUIRY",
  "entityId": 42,
  "filename": "screenshot.png",
  "originalFilename": "스크린샷 2026-03-29 오후 3.45.12.png",
  "mimeType": "image/png",
  "fileSize": 524288,
  "uploadedBy": {
    "id": 3,
    "name": "이영희",
    "email": "yhlee@company.com"
  },
  "uploadedAt": "2026-03-29T15:45:30Z",
  "url": "/api/v1/attachments/7/download",
  "thumbnailUrl": "/api/v1/attachments/7/thumbnail",
  "metadata": {
    "width": 1920,        // 이미지만
    "height": 1080,       // 이미지만
    "format": "PNG"       // 이미지만
  }
}
```

---

## 파일 저장 전략

### Phase 1 (MVP) - 로컬 파일시스템
```
/data/attachments/
├── inquiries/
│   ├── 42/
│   │   ├── 7_screenshot.png
│   │   └── 8_error-log.txt
│   └── 43/
│       └── 9_diagram.png
└── tasks/
    └── 123/
        └── 10_deployment-log.txt
```

**파일명 규칙**: `{attachmentId}_{sanitized_filename}`

### Phase 2+ - 클라우드 스토리지 (추상화)
```java
public interface StorageProvider {
    String store(InputStream file, String filename, String entityType, Long entityId);
    InputStream retrieve(String fileId);
    void delete(String fileId);
    String getPublicUrl(String fileId);
}

// 구현체
// - LocalFileStorageProvider (기본)
// - S3StorageProvider
// - MinioStorageProvider
// - AzureBlobStorageProvider
```

**설정 (application.yml)**
```yaml
breaddesk:
  storage:
    provider: local         # local | s3 | minio | azure
    local:
      basePath: /data/attachments
    s3:
      bucket: breaddesk-attachments
      region: ap-northeast-2
```

---

## 보안 고려사항

### 1. 파일 업로드 검증
- **MIME 타입 검증**: Content-Type 헤더 + 파일 시그니처 (magic bytes) 확인
- **확장자 화이트리스트**: 실행 파일(`.exe`, `.sh`, `.bat`) 차단
- **안티바이러스 스캔** (Phase 2+): ClamAV 통합

### 2. 다운로드 권한
- 업로드한 사용자 본인
- 해당 문의/업무의 담당자
- 관리자

### 3. 파일명 새니타이징
- 원본 파일명 저장 (표시용)
- 실제 저장 시 UUID 또는 안전한 파일명 사용
- 경로 탐색 공격 방지 (`../` 제거)

---

## 이미지 최적화 (Phase 2+)

### 자동 압축
- 업로드된 이미지 자동 최적화 (JPEG 품질 85%)
- 원본 유지 옵션 제공

### 썸네일 생성
- 200px, 400px, 800px 다중 크기 생성
- Lazy loading 지원

### WebP 변환
- 최신 브라우저용 WebP 포맷 제공
- 폴백: JPEG/PNG

---

## 에러 코드 정리

| HTTP 상태 | 에러 코드 | 설명 |
|-----------|----------|------|
| 400 | `VALIDATION_ERROR` | 요청 데이터 검증 실패 |
| 400 | `FILE_TOO_LARGE` | 파일 크기 초과 (10MB) |
| 400 | `INVALID_FILE_TYPE` | 허용되지 않은 파일 형식 |
| 400 | `MAX_FILES_EXCEEDED` | 파일 수 초과 (5개) |
| 400 | `NOT_AN_IMAGE` | 이미지 전용 기능에 비이미지 파일 사용 |
| 403 | `DELETE_FORBIDDEN` | 삭제 권한 없음 |
| 404 | `ATTACHMENT_NOT_FOUND` | 첨부파일 없음 |
| 404 | `ENTITY_NOT_FOUND` | 대상 문의/업무 없음 |
| 500 | `STORAGE_ERROR` | 파일 저장 실패 |
| 500 | `INTERNAL_ERROR` | 서버 에러 |

---

## 프론트엔드 통합 예시

### 파일 업로드 (React + Axios)
```typescript
const uploadFile = async (file: File, entityType: 'INQUIRY' | 'TASK', entityId: number) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('entityType', entityType);
  formData.append('entityId', entityId.toString());

  const response = await axios.post('/api/v1/attachments', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: (progressEvent) => {
      const percentCompleted = Math.round(
        (progressEvent.loaded * 100) / progressEvent.total!
      );
      console.log(`Upload progress: ${percentCompleted}%`);
    },
  });

  return response.data;
};
```

### 이미지 미리보기
```tsx
<img 
  src={`/api/v1/attachments/${attachment.id}/thumbnail?size=400`}
  alt={attachment.originalFilename}
  loading="lazy"
  onClick={() => window.open(`/api/v1/attachments/${attachment.id}/download`)}
/>
```

---

**작성일**: 2026-03-29  
**버전**: v1.0
