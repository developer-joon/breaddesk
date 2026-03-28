# 🍞 BreadDesk

> AI Service Desk + Task Management

전사 문의를 AI가 1차 대응하고, 미해결 건은 담당자가 직접 처리하며, 모든 업무를 칸반으로 추적하는 시스템.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Monorepo | Turborepo + Bun |
| Frontend | Next.js + TypeScript |
| Backend | Java 25 + Spring Boot 3.x |
| Database | MySQL + pgvector |
| Cache | Redis |
| AI | LLM 추상화 (Ollama, OpenAI, Claude 교체 가능) |
| Automation | n8n (멀티채널 통합) |

## 프로젝트 구조

```
breaddesk/
├── apps/web/          # Next.js 프론트엔드
├── services/api/      # Spring Boot 백엔드
├── packages/shared/   # 공유 타입
├── infrastructure/    # Docker, K8s
└── specs/             # 설계 문서
```

## 로컬 개발

### 인프라 실행
```bash
docker compose -f infrastructure/docker-compose.dev.yml up -d
```

### 프론트엔드
```bash
bun install
bun run dev
```

### 백엔드
```bash
cd services/api
./gradlew bootRun
```

## 설계 원칙

- **의존 최소화**: LLM, 채널, DB 전부 교체 가능
- **확장 최대화**: 커넥터/프로바이더 플러그인 구조

## License

Private
