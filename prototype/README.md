# 🍞 BreadDesk UI 프로토타입

통합 데스크 시스템 UI 프로토타입 - Tailwind CSS 기반 단일 페이지 애플리케이션

## 🚀 배포 정보

- **URL**: https://breaddesk.k6s.app
- **환경**: Kubernetes (K8s)
- **웹서버**: nginx:1.27-alpine
- **빌드**: 불필요 (CDN 기반)

## 📋 구현된 화면 (6개)

### 1. 레이아웃 (공통)
- 왼쪽 사이드바 (240px): 로고 + 6개 메뉴
- 상단 헤더 (60px): 알림 벨, 사용자 정보
- SPA 방식 페이지 전환
- 반응형 디자인 (모바일 지원)

### 2. 대시보드
- 4개 통계 카드 (총 문의 127건, 미해결 12건, 오늘 접수 8건, AI 해결률 73%)
- 최근 문의 5건 리스트
- 업무 현황 바 차트

### 3. 업무 칸반
- 필터 (유형, 긴급도, 검색)
- 4개 컬럼 (대기/진행중/리뷰/완료)
- 칸반 카드: 제목, 유형, 긴급도, 태그, 담당자, SLA
- 카드 클릭 → 상세 모달

### 4. 업무 상세 모달
- 제목, 설명 편집
- 상태/긴급도/유형/담당자 드롭다운
- SLA 타이머 (색상 표시)
- 태그 관리
- 체크리스트
- 첨부파일
- 활동 로그 타임라인
- 코멘트

### 5. 문의 상세 (채팅 UI)
- 좌측: 문의 목록 + 필터
- 우측: 채팅 영역
  - 사용자 메시지 (회색)
  - AI 메시지 (연파랑, 신뢰도 표시)
  - 담당자 메시지 (연녹색)
- 답변 입력 + 템플릿/첨부 버튼
- 액션 버튼 (해결됨, 업무로 전환)

### 6. 답변 템플릿
- 카테고리 탭 (전체/권한/VPN/배포/기타)
- 템플릿 카드 그리드
- 사용 횟수, 최종 사용 시간 표시

## 🎨 색상 체계

- **긴급도**
  - CRITICAL: #EF4444 (빨강)
  - HIGH: #F97316 (주황)
  - NORMAL: #EAB308 (노랑)
  - LOW: #3B82F6 (파랑)

- **메시지 배경**
  - 사용자: #F3F4F6 (연회색)
  - AI: #EFF6FF (연파랑)
  - 담당자: #F0FDF4 (연녹색)

- **SLA**
  - 정상: #22C55E (녹색)
  - 임박: #EAB308 (노랑)
  - 초과: #EF4444 (빨강)

- **사이드바**: #1E293B (다크)
- **선택된 메뉴**: #3B82F6 (파랑)
- **배경**: #F8FAFC

## 🛠 기술 스택

- HTML5
- Tailwind CSS (CDN)
- Vanilla JavaScript
- 단일 파일 (`index.html`)

## 📁 파일 구조

```
breaddesk/prototype/
├── index.html     # 메인 프로토타입 (단일 파일)
└── README.md      # 본 문서
```

## 🔧 K8s 배포 방법

```bash
# 1. Deployment 생성
kubectl create deployment breaddesk-proto --image=nginx:1.27-alpine

# 2. Service 노출
kubectl expose deployment breaddesk-proto --port=80

# 3. Ingress 생성
kubectl create ingress breaddesk-proto --rule="breaddesk.k6s.app/*=breaddesk-proto:80"

# 4. 파일 복사
POD=$(kubectl get pod -l app=breaddesk-proto -o jsonpath='{.items[0].metadata.name}')
kubectl cp index.html $POD:/usr/share/nginx/html/index.html

# 5. 권한 설정
kubectl exec $POD -- chmod 644 /usr/share/nginx/html/index.html
```

## ✨ 주요 기능

- ✅ 완전한 SPA 네비게이션
- ✅ 모달 열기/닫기
- ✅ 반응형 레이아웃
- ✅ 실제 같은 더미 데이터 (한국어)
- ✅ 호버 효과 및 트랜지션
- ✅ 채널 배지, 상태 배지
- ✅ 아바타, 아이콘
- ✅ 색상 코딩 (긴급도, SLA)

## 🚧 향후 개발 사항

- 백엔드 API 연동
- 실시간 데이터 업데이트 (WebSocket)
- 드래그 앤 드롭 (칸반)
- 파일 업로드 실제 구현
- 템플릿 편집기
- 사용자 권한 관리

---

**생성일**: 2026-03-29  
**버전**: v1.0 (프로토타입)
