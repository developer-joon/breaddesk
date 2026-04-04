#!/bin/bash
set -e

REGISTRY="ghcr.io/developer-joon"
NAMESPACE="openclaw"

echo "🍞 BreadDesk Deploy Script"
echo "=========================="

# GHCR 로그인
echo "📦 GHCR 로그인..."
echo $(gh auth token) | podman login ghcr.io -u developer-joon --password-stdin 2>/dev/null

# 인자 확인
TARGET=${1:-all}

if [ "$TARGET" = "api" ] || [ "$TARGET" = "all" ]; then
  echo ""
  echo "🔧 Backend 빌드..."
  cd apps/api && ./gradlew build -x test --no-daemon && cd ../..
  
  echo "🐳 Backend 이미지 빌드..."
  podman build -t $REGISTRY/breaddesk-api:latest -f apps/api/Dockerfile apps/api/
  
  echo "📤 Backend 이미지 푸시..."
  podman push $REGISTRY/breaddesk-api:latest
  
  echo "🚀 Backend K8s 재배포..."
  kubectl rollout restart deployment/breaddesk-api -n $NAMESPACE
fi

if [ "$TARGET" = "web" ] || [ "$TARGET" = "all" ]; then
  echo ""
  echo "🔧 Frontend 빌드..."
  podman build --no-cache -t $REGISTRY/breaddesk-web:latest -f apps/web/Dockerfile .
  
  echo "📤 Frontend 이미지 푸시..."
  podman push $REGISTRY/breaddesk-web:latest
  
  echo "🚀 Frontend K8s 재배포..."
  kubectl rollout restart deployment/breaddesk-web -n $NAMESPACE
fi

echo ""
echo "⏳ Pod 상태 확인 (30초 대기)..."
sleep 30
kubectl get pods -n $NAMESPACE | grep breaddesk

echo ""
echo "✅ 배포 완료!"
echo "🔗 https://breaddesk.k6s.app"
