# BreadDesk DevOps 설정 완료 요약

## 작업 완료 사항

### 1. GitHub Actions CI/CD 파이프라인 ✅

#### CI 워크플로우 (`.github/workflows/ci.yml`)
- **트리거**: `develop`, `main` 브랜치에 Push 또는 PR 생성
- **작업 내용**:
  - **Backend**: PostgreSQL 테스트 DB + JDK 21 + Gradle 빌드/테스트
  - **Frontend**: Node.js 22 + npm lint + 빌드
  - 테스트 리포트 아티팩트 자동 업로드 (7일 보관)

#### CD 워크플로우 (`.github/workflows/cd.yml`)
- **트리거**: `main` 브랜치에 Push (머지 완료 시)
- **작업 내용**:
  1. **Docker 이미지 빌드**
     - Backend: `ghcr.io/botjoonwoo/breaddesk-api:<sha>`
     - Frontend: `ghcr.io/botjoonwoo/breaddesk-web:<sha>`
     - 태그: Git SHA + `latest`
  2. **K8s 배포**
     - `kubectl set image`로 롤링 업데이트
     - 롤아웃 상태 확인 (5분 타임아웃)

#### ⚠️ 필수 설정 (사용자 수동 작업 필요)
1. GitHub Repository → Settings → Secrets and variables → Actions
2. `KUBECONFIG` Secret 추가 (Base64 인코딩된 kubeconfig)
   ```bash
   cat ~/.kube/config | base64 -w 0
   ```
   또는 ServiceAccount 기반 kubeconfig 생성 (권장, `docs/DEPLOYMENT.md` 참고)

---

### 2. Spring Boot JSON 로깅 설정 ✅

#### 파일 생성/수정
1. **`apps/api/src/main/resources/logback-spring.xml`** (신규 생성)
   - 개발 환경: 일반 콘솔 로그 (가독성 우선)
   - 프로덕션 환경: JSON 포맷 (Loki 파싱 용이)
   - 에러 로그 별도 파일 저장 (선택사항)

2. **`apps/api/src/main/java/com/breadlab/breaddesk/config/CorrelationIdFilter.java`** (신규 생성)
   - 모든 HTTP 요청에 Correlation ID 생성/추출
   - MDC에 저장하여 로그에 자동 포함
   - 응답 헤더에도 `X-Correlation-ID` 반환
   - 요청 추적 및 분산 트레이싱 지원

3. **`apps/api/build.gradle`** (의존성 추가)
   ```gradle
   implementation 'net.logstash.logback:logstash-logback-encoder:8.0'
   ```

#### 로그 포맷 예시
**개발 환경:**
```
2026-03-31 12:00:00.123 INFO [abc-123-def] --- [nio-8080-exec-1] c.b.b.ticket.TicketService : Ticket created: #1234
```

**프로덕션 (JSON):**
```json
{"timestamp":"2026-03-31 12:00:00.123","level":"INFO","correlationId":"abc-123-def","logger":"c.b.b.ticket.TicketService","message":"Ticket created: #1234","service":"breaddesk-api"}
```

---

### 3. Loki 로깅 스택 설정 ✅

#### Helm Values 파일 생성
1. **`infrastructure/k8s/loki-values.yaml`**
   - SingleBinary 모드 (경량화, 단일 파드)
   - Filesystem 스토리지 (10Gi PVC)
   - 모니터링/테스트 비활성화

2. **`infrastructure/k8s/promtail-values.yaml`**
   - DaemonSet으로 모든 노드에 배포
   - JSON 로그 자동 파싱 (Spring Boot 로그 대응)
   - Loki로 전송: `http://loki.monitoring:3100`

#### ⚠️ 설치 필요 (사용자 수동 작업)
Helm이 K8s 노드에 설치되어 있지 않으므로, 사용자가 직접 설치 필요:

```bash
# 1. Helm 설치 (미설치 시)
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# 2. Helm 저장소 추가
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update

# 3. Loki 설치
helm install loki grafana/loki \
  -n monitoring --create-namespace \
  -f infrastructure/k8s/loki-values.yaml

# 4. Promtail 설치
helm install promtail grafana/promtail \
  -n monitoring \
  -f infrastructure/k8s/promtail-values.yaml

# 5. 설치 확인
kubectl get pods -n monitoring
```

#### Grafana 데이터소스 추가
1. Grafana 접속 (`https://grafana.k6s.app`)
2. Configuration → Data sources → Add data source → **Loki**
3. URL: `http://loki.monitoring:3100`
4. Save & test

#### LogQL 쿼리 예시
```logql
# BreadDesk API 로그 전체
{namespace="breaddesk", app="breaddesk-api"}

# 에러만 필터
{namespace="breaddesk"} | json | level="ERROR"

# Correlation ID로 요청 추적
{namespace="breaddesk"} | json | correlationId="abc-123-def"

# 최근 5분간 에러 카운트
sum(count_over_time({namespace="breaddesk"} |= "ERROR" [5m]))
```

---

### 4. K8s 매니페스트 정리 ✅

#### 네임스페이스 통일
- **변경 전**: `openclaw` (일부 매니페스트에서 사용)
- **변경 후**: `breaddesk` (모든 리소스 통일)

#### 이미지 태그 업데이트
- **변경 전**: `ghcr.io/botjoonwoo/breaddesk-api:0.1.0`
- **변경 후**: `ghcr.io/botjoonwoo/breaddesk-api:latest`
  - CD 워크플로우에서 `kubectl set image`로 자동 업데이트

#### 환경변수 추가 (`03-backend.yaml`)
```yaml
env:
  - name: SPRING_PROFILES_ACTIVE
    value: "prod"  # JSON 로그 포맷 활성화
```

#### 컨테이너 이름 변경 (일관성 개선)
- **변경 전**: `containers.name: api`, `web`
- **변경 후**: `breaddesk-api`, `breaddesk-web`

---

### 5. 문서화 ✅

#### `docs/DEPLOYMENT.md` (신규 작성)
전체 8KB 분량의 배포 가이드:
- GitHub Actions CI/CD 설정 (Secret 생성 포함)
- Loki 설치 가이드 (Helm 명령어 포함)
- Grafana 데이터소스 추가 방법
- LogQL 쿼리 예시 및 트러블슈팅
- ServiceAccount 기반 kubeconfig 생성 (보안 강화)

#### `README.md` 업데이트
- 배포 섹션 추가 (CI/CD, 모니터링)
- 로컬 개발 환경 정확히 반영 (JDK 21, Node 22)
- 기술 스택 업데이트 (PostgreSQL, Loki)

---

## Git 커밋 & 푸시 ✅

```bash
git add .github/ apps/api/ infrastructure/k8s/ docs/ README.md
git commit -m "feat: GitHub Actions CI/CD + Loki 로깅 설정"
git push origin develop
```

커밋 SHA: `6fc5d9a`

---

## 다음 단계 (사용자 액션 필요)

### 1. GitHub Secret 설정
```bash
# kubeconfig Base64 인코딩
cat ~/.kube/config | base64 -w 0

# GitHub에 등록
# Repository → Settings → Secrets and variables → Actions
# New repository secret: KUBECONFIG
```

### 2. Loki 설치
```bash
# K8s 클러스터에 접속 후
helm install loki grafana/loki -n monitoring --create-namespace -f infrastructure/k8s/loki-values.yaml
helm install promtail grafana/promtail -n monitoring -f infrastructure/k8s/promtail-values.yaml
```

### 3. Grafana 데이터소스 추가
- URL: `http://loki.monitoring:3100`

### 4. CI/CD 테스트
```bash
# develop → main PR 생성 → CI 자동 실행 확인
# main 머지 → CD 자동 배포 확인
```

---

## 보안 고려사항

### GitHub Actions
- **kubeconfig 보호**: Repository Secret으로 저장 (Base64 인코딩)
- **ServiceAccount 권한 최소화**: 배포 전용 Role만 부여 (`docs/DEPLOYMENT.md` 참고)
- **Token 유효기간**: 10년 (필요 시 주기적 갱신)

### Loki
- **인증 비활성화**: 내부 클러스터 전용 (Ingress 노출 시 인증 추가 필요)
- **스토리지**: Filesystem (프로덕션에서는 S3/GCS 권장)

### 로그
- **민감 정보 필터링**: 로그에 비밀번호/토큰 등 출력 금지
- **Correlation ID**: UUID 사용 (예측 불가능)

---

## 파일 목록

### 신규 생성
- `.github/workflows/ci.yml`
- `.github/workflows/cd.yml`
- `apps/api/src/main/resources/logback-spring.xml`
- `apps/api/src/main/java/com/breadlab/breaddesk/config/CorrelationIdFilter.java`
- `infrastructure/k8s/loki-values.yaml`
- `infrastructure/k8s/promtail-values.yaml`
- `docs/DEPLOYMENT.md`

### 수정
- `apps/api/build.gradle` (logstash-encoder 의존성 추가)
- `infrastructure/k8s/01-secrets.yaml` (네임스페이스)
- `infrastructure/k8s/02-postgres.yaml` (네임스페이스)
- `infrastructure/k8s/03-backend.yaml` (네임스페이스, 이미지 태그, 환경변수, 컨테이너명)
- `infrastructure/k8s/04-frontend.yaml` (네임스페이스, 이미지 태그, 컨테이너명)
- `infrastructure/k8s/05-ingress.yaml` (네임스페이스)
- `README.md` (배포 및 모니터링 섹션)

---

## 참고 자료

- [GitHub Actions 공식 문서](https://docs.github.com/en/actions)
- [Loki 공식 문서](https://grafana.com/docs/loki/latest/)
- [LogQL 치트시트](https://grafana.com/docs/loki/latest/logql/)
- [Spring Boot Logging 가이드](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)
