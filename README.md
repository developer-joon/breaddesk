# 🍞 BreadDesk

> AI Service Desk + Task Management

전사 문의를 AI가 1차 대응하고, 미해결 건은 담당자가 직접 처리하며, 모든 업무를 칸반으로 추적하는 시스템.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Monorepo | Turborepo + Bun |
| Frontend | Next.js + TypeScript |
| Backend | Java 25 + Spring Boot 3.x |
| Database | PostgreSQL + pgvector |
| AI | LLM 추상화 (Ollama, OpenAI, Claude 교체 가능) |
| Automation | n8n (멀티채널 통합) |

## 프로젝트 구조

```
breaddesk/
├── apps/
│   ├── web/           # Next.js 프론트엔드
│   └── api/           # Spring Boot 백엔드
├── docs/              # 설계 문서
├── k8s/               # K8s 매니페스트 (배포 시)
├── docker-compose.yml # 로컬 개발 인프라
└── turbo.json
```

## 로컬 개발

### 준비물
- JDK 21+
- Docker Desktop
- Node.js 22+

### 백엔드 (IntelliJ)
1. IntelliJ → `Open` → `breaddesk/` 루트 선택
2. `BreadDeskApplication.java` → ▶️ Run
3. PostgreSQL, Ollama가 **자동으로 기동됨** (spring-boot-docker-compose)
4. 앱 종료 시 컨테이너도 **자동 정지**

### 프론트엔드
```bash
cd apps/web
npm install
npm run dev
```

## 배포

### CI/CD 파이프라인
- **CI** (`.github/workflows/ci.yml`): PR/Push 시 자동 테스트
- **CD** (`.github/workflows/cd.yml`): main 머지 시 자동 배포

### 필수 설정
1. GitHub Secrets에 `KUBECONFIG` 추가 (Base64 인코딩)
2. K8s 클러스터에 `breaddesk` 네임스페이스 생성
3. GHCR Pull Secret 설정

자세한 내용은 [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) 참고.

## 모니터링 & 로깅

- **Metrics**: Prometheus + Grafana (기존)
- **Logs**: Loki + Promtail + Grafana
- **Tracing**: Correlation ID (X-Correlation-ID 헤더)

로그는 프로덕션에서 JSON 포맷으로 출력되며, Loki에서 파싱하여 Grafana Explore에서 조회 가능.

## 설계 원칙

- **의존 최소화**: LLM, 채널, DB 전부 교체 가능
- **확장 최대화**: 커넥터/프로바이더 플러그인 구조

## License

Private
