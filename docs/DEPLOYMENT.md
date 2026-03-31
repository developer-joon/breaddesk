# BreadDesk 배포 가이드

## 목차
1. [GitHub Actions CI/CD 설정](#github-actions-cicd-설정)
2. [Loki 로깅 스택 설치](#loki-로깅-스택-설치)
3. [Grafana에 Loki 데이터소스 추가](#grafana에-loki-데이터소스-추가)
4. [로그 확인 및 쿼리](#로그-확인-및-쿼리)

---

## GitHub Actions CI/CD 설정

### 필수 GitHub Secrets 설정

Repository Settings → Secrets and variables → Actions에서 다음 Secret 추가:

#### 1. `KUBECONFIG` (필수)
Kubernetes 클러스터 접근을 위한 kubeconfig 파일 (Base64 인코딩)

```bash
# kubeconfig 파일을 Base64로 인코딩
cat ~/.kube/config | base64 -w 0
```

위 명령어 출력 결과를 GitHub Secret `KUBECONFIG`에 저장.

**주의:**
- kubeconfig에는 클러스터 인증 정보가 포함되므로 절대 공개하지 말 것
- 권한이 제한된 ServiceAccount 사용 권장 (아래 참고)

#### 2. ServiceAccount 기반 kubeconfig 생성 (권장)

배포 전용 ServiceAccount를 생성하여 최소 권한 부여:

```bash
# ServiceAccount 및 Role 생성
kubectl apply -f - <<EOF
apiVersion: v1
kind: ServiceAccount
metadata:
  name: github-deployer
  namespace: breaddesk
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: github-deployer
  namespace: breaddesk
rules:
  - apiGroups: ["apps"]
    resources: ["deployments"]
    verbs: ["get", "list", "patch", "update"]
  - apiGroups: [""]
    resources: ["pods", "services"]
    verbs: ["get", "list"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: github-deployer
  namespace: breaddesk
subjects:
  - kind: ServiceAccount
    name: github-deployer
    namespace: breaddesk
roleRef:
  kind: Role
  name: github-deployer
  apiGroup: rbac.authorization.k8s.io
EOF

# ServiceAccount용 kubeconfig 생성
CLUSTER_NAME=$(kubectl config current-context)
SERVER=$(kubectl config view -o jsonpath="{.clusters[?(@.name=='$CLUSTER_NAME')].cluster.server}")
CA_CERT=$(kubectl config view --raw -o jsonpath="{.clusters[?(@.name=='$CLUSTER_NAME')].cluster.certificate-authority-data}")
TOKEN=$(kubectl create token github-deployer -n breaddesk --duration=87600h)  # 10년 유효

cat <<EOF > github-deployer-kubeconfig.yaml
apiVersion: v1
kind: Config
clusters:
  - name: $CLUSTER_NAME
    cluster:
      server: $SERVER
      certificate-authority-data: $CA_CERT
contexts:
  - name: github-deployer
    context:
      cluster: $CLUSTER_NAME
      user: github-deployer
      namespace: breaddesk
current-context: github-deployer
users:
  - name: github-deployer
    user:
      token: $TOKEN
EOF

# Base64 인코딩
cat github-deployer-kubeconfig.yaml | base64 -w 0

# 파일 삭제 (보안)
rm github-deployer-kubeconfig.yaml
```

출력된 Base64 문자열을 GitHub Secret `KUBECONFIG`에 저장.

### CI/CD 워크플로우

#### CI (`.github/workflows/ci.yml`)
- **트리거:** `develop`, `main` 브랜치 Push 또는 PR
- **작업:**
  - Backend: JDK 21 + Gradle 테스트 및 빌드
  - Frontend: Node.js 22 + npm lint 및 빌드
- **아티팩트:** 테스트 리포트 업로드 (7일 보관)

#### CD (`.github/workflows/cd.yml`)
- **트리거:** `main` 브랜치 Push (머지)
- **작업:**
  1. Docker 이미지 빌드 및 `ghcr.io/botjoonwoo` 레지스트리에 푸시
     - 태그: `<git-sha>`, `latest`
  2. Kubernetes 배포
     - `kubectl set image` 방식으로 롤링 업데이트
     - 롤아웃 상태 확인 (5분 타임아웃)

### 배포 플로우

```
develop 브랜치 작업 → PR → CI 테스트 → main 머지 → CD 자동 배포
```

### 로컬에서 이미지 빌드 테스트

```bash
# Backend
cd apps/api
docker build -t breaddesk-api:test .

# Frontend
cd ../..
docker build -f Dockerfile.web -t breaddesk-web:test .
```

---

## Loki 로깅 스택 설치

### 사전 요구사항

- Kubernetes 클러스터 (kubeadm)
- Helm 3.x 설치

#### Helm 설치 (미설치 시)

```bash
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

### 1. Helm 저장소 추가

```bash
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
```

### 2. Loki 설치 (SingleBinary 모드)

```bash
helm install loki grafana/loki \
  -n monitoring --create-namespace \
  -f infrastructure/k8s/loki-values.yaml
```

**설명:**
- `monitoring` 네임스페이스에 설치
- SingleBinary 모드: 단일 파드로 실행 (작은 클러스터에 적합)
- 파일시스템 스토리지 사용 (10Gi PVC)

### 3. Promtail 설치 (로그 수집)

```bash
helm install promtail grafana/promtail \
  -n monitoring \
  -f infrastructure/k8s/promtail-values.yaml
```

**설명:**
- DaemonSet으로 모든 노드에 배포
- Kubernetes 파드 로그를 Loki로 전송
- JSON 로그 파싱 설정 포함 (Spring Boot용)

### 4. 설치 확인

```bash
# Loki 파드 확인
kubectl get pods -n monitoring -l app.kubernetes.io/name=loki

# Promtail 파드 확인 (노드 수만큼 실행)
kubectl get pods -n monitoring -l app.kubernetes.io/name=promtail

# Loki 서비스 확인
kubectl get svc -n monitoring loki
```

### 5. Loki 접근 테스트

```bash
# 포트 포워딩
kubectl port-forward -n monitoring svc/loki 3100:3100

# 다른 터미널에서 테스트
curl http://localhost:3100/ready
# 응답: "ready"
```

---

## Grafana에 Loki 데이터소스 추가

### 방법 1: Grafana UI에서 수동 추가

1. Grafana 대시보드 접속 (`https://grafana.k6s.app`)
2. Configuration (⚙️) → Data sources
3. **Add data source** → **Loki** 선택
4. 설정:
   - **Name:** Loki
   - **URL:** `http://loki.monitoring:3100`
   - **Timeout:** 60초
5. **Save & test**

### 방법 2: ConfigMap으로 자동 추가 (선택사항)

Grafana가 Helm으로 설치된 경우:

```bash
helm upgrade grafana grafana/grafana -n monitoring \
  --set datasources."datasources\.yaml".apiVersion=1 \
  --set datasources."datasources\.yaml".datasources[0].name=Loki \
  --set datasources."datasources\.yaml".datasources[0].type=loki \
  --set datasources."datasources\.yaml".datasources[0].url=http://loki.monitoring:3100 \
  --set datasources."datasources\.yaml".datasources[0].access=proxy \
  --reuse-values
```

---

## 로그 확인 및 쿼리

### Grafana Explore에서 로그 확인

1. Grafana → Explore (🔍)
2. 데이터소스: **Loki** 선택
3. LogQL 쿼리 예시:

#### 1. BreadDesk API 로그 조회
```logql
{namespace="breaddesk", app="breaddesk-api"}
```

#### 2. 특정 레벨 필터 (ERROR만)
```logql
{namespace="breaddesk", app="breaddesk-api"} |= "ERROR"
```

#### 3. JSON 로그 파싱 및 필터
```logql
{namespace="breaddesk"} | json | level="ERROR"
```

#### 4. Correlation ID로 요청 추적
```logql
{namespace="breaddesk"} | json | correlationId="a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

#### 5. 최근 5분간 에러 카운트
```logql
sum(count_over_time({namespace="breaddesk"} |= "ERROR" [5m]))
```

#### 6. 로거별 로그 집계
```logql
{namespace="breaddesk"} | json | logger="com.breadlab.breaddesk.ticket.service.TicketService"
```

### 유용한 LogQL 패턴

```logql
# 특정 사용자 관련 로그
{namespace="breaddesk"} |~ "userId.*12345"

# 느린 요청 (latency 포함 시)
{namespace="breaddesk"} | json | duration > 1000

# 특정 엔드포인트
{namespace="breaddesk"} |~ "/api/tickets" | json
```

### 대시보드 생성

1. Grafana → Dashboards → New dashboard
2. Add panel → Query:
   ```logql
   sum by (level) (count_over_time({namespace="breaddesk"} | json [1m]))
   ```
3. Visualization: **Bar gauge** 또는 **Time series**
4. 패널 이름: "BreadDesk 로그 레벨별 분포"

---

## Spring Boot 로그 설정

### application.yml 프로파일 설정

프로덕션 배포 시 환경변수로 프로파일 활성화:

```yaml
# K8s Deployment에서
env:
  - name: SPRING_PROFILES_ACTIVE
    value: "prod"
```

### 로그 포맷

- **개발 환경:** 사람이 읽기 쉬운 콘솔 로그
- **프로덕션:** JSON 포맷 (Loki/Promtail 자동 파싱)

### Correlation ID

모든 HTTP 요청에 자동으로 `X-Correlation-ID` 헤더 생성:
- 클라이언트가 제공하지 않으면 UUID 자동 생성
- 로그에 `correlationId` 필드로 포함
- 응답 헤더에도 반환

**클라이언트 예시:**
```bash
curl -H "X-Correlation-ID: my-request-123" https://api.breaddesk.app/tickets
```

---

## 트러블슈팅

### Loki 파드가 시작되지 않음
```bash
kubectl describe pod -n monitoring loki-<pod-name>
kubectl logs -n monitoring loki-<pod-name>
```

일반적 원인: PVC 바인딩 실패
```bash
kubectl get pvc -n monitoring
```

### Promtail이 로그를 수집하지 않음
```bash
kubectl logs -n monitoring promtail-<pod-name>
```

Loki 연결 확인:
```bash
kubectl exec -n monitoring promtail-<pod-name> -- wget -qO- http://loki.monitoring:3100/ready
```

### Grafana에서 "Data source connected, but no labels received"
- Loki에 아직 로그가 없음 (애플리케이션 실행 후 재시도)
- Promtail이 제대로 작동하지 않음 (위 확인)

### GitHub Actions 배포 실패
```bash
# Secret 확인
echo ${{ secrets.KUBECONFIG }} | base64 -d | kubectl --kubeconfig=- get nodes
```

권한 확인:
```bash
kubectl auth can-i update deployments --namespace=breaddesk --as=system:serviceaccount:breaddesk:github-deployer
```

---

## 참고 자료

- [Loki 공식 문서](https://grafana.com/docs/loki/latest/)
- [LogQL 치트시트](https://grafana.com/docs/loki/latest/logql/)
- [Promtail 설정 가이드](https://grafana.com/docs/loki/latest/clients/promtail/)
- [GitHub Actions 문서](https://docs.github.com/en/actions)
