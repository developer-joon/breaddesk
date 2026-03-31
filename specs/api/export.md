# CSV 내보내기 API

## 개요
문의 또는 업무 데이터를 CSV 파일로 내보냅니다.

## 엔드포인트

### GET /api/v1/export/inquiries
**설명**: 문의 목록을 CSV로 내보냅니다.

**인증**: 필요 (AGENT, ADMIN)

**Query Parameters**:
- `status`: 상태 필터 (선택)
- `startDate`, `endDate`: 기간 필터 (YYYY-MM-DD 형식, 선택)

**Response**: CSV 파일 스트림
```
Content-Type: text/csv
Content-Disposition: attachment; filename="inquiries_20260331.csv"

id,channel,senderName,message,status,createdAt
1,slack,홍길동,결제가 안 돼요...,AI_ANSWERED,2026-03-31T12:00:00
```

---

### GET /api/v1/export/tasks
**설명**: 업무 목록을 CSV로 내보냅니다.

**인증**: 필요 (AGENT, ADMIN)

**Query Parameters**:
- `status`: 상태 필터 (선택)
- `assigneeId`: 담당자 필터 (선택)
- `startDate`, `endDate`: 기간 필터 (선택)

**Response**: CSV 파일 스트림
```
Content-Type: text/csv
Content-Disposition: attachment; filename="tasks_20260331.csv"

id,title,urgency,status,assigneeName,createdAt,completedAt
42,홍길동 고객 결제 오류 해결,HIGH,IN_PROGRESS,김철수,2026-03-31T12:00:00,
```

---

## CSV 필드

### inquiries.csv
- id, channel, senderName, senderEmail, message, aiResponse, aiConfidence, status, resolvedBy, createdAt, resolvedAt

### tasks.csv
- id, title, description, urgency, status, assigneeName, requesterName, dueDate, slaResponseDeadline, slaResolveDeadline, slaResponseBreached, slaResolveBreached, createdAt, startedAt, completedAt

---

## 비즈니스 규칙

1. **인코딩**: UTF-8 with BOM (Excel 호환성)
2. **날짜 형식**: ISO 8601 (YYYY-MM-DDTHH:mm:ss)
3. **큰따옴표 이스케이프**: 필드 내 쉼표/줄바꿈 처리
4. **최대 행 수**: 10,000개 (초과 시 분할 다운로드 안내)

---

## 프론트엔드 구현 참고

- 다운로드 버튼: `<a href="/api/v1/export/inquiries?status=OPEN" download>`
- 또는 Axios로 blob 다운로드:
  ```js
  axios.get('/api/v1/export/inquiries', { responseType: 'blob' })
    .then(res => {
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'inquiries.csv');
      link.click();
    });
  ```

---

## 관련 엔티티

- [Inquiry](../data-model/ENTITIES.md#2-inquiry-문의)
- [Task](../data-model/ENTITIES.md#4-task-업무)
