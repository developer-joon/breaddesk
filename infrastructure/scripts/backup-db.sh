#!/bin/bash
# BreadDesk PostgreSQL 백업 스크립트
# 사용법: ./backup-db.sh [NAMESPACE]

set -e

NAMESPACE="${1:-openclaw}"
POD_SELECTOR="app=breaddesk-postgres"
BACKUP_DIR="/home/openclaw/.openclaw/workspace/breaddesk/backups"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=7

echo "🔍 BreadDesk DB 백업 시작..."
echo "   Namespace: $NAMESPACE"
echo "   Pod Selector: $POD_SELECTOR"
echo "   백업 디렉토리: $BACKUP_DIR"

# 백업 디렉토리 생성
mkdir -p "$BACKUP_DIR"

# PostgreSQL Pod 찾기
echo "🔍 PostgreSQL Pod 검색 중..."
POD=$(kubectl get pod -n "$NAMESPACE" -l "$POD_SELECTOR" -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)

if [ -z "$POD" ]; then
  echo "❌ PostgreSQL Pod를 찾을 수 없습니다 (Namespace: $NAMESPACE, Selector: $POD_SELECTOR)"
  exit 1
fi

echo "✅ Pod 발견: $POD"

# 백업 실행
BACKUP_FILE="$BACKUP_DIR/breaddesk_$DATE.sql.gz"
echo "📦 백업 실행 중: $BACKUP_FILE"

kubectl exec -n "$NAMESPACE" "$POD" -- pg_dump -U breaddesk breaddesk | gzip > "$BACKUP_FILE"

if [ $? -eq 0 ]; then
  FILE_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
  echo "✅ 백업 완료: $BACKUP_FILE ($FILE_SIZE)"
else
  echo "❌ 백업 실패"
  exit 1
fi

# 오래된 백업 삭제 (7일 이상)
echo "🧹 $RETENTION_DAYS일 이상 된 백업 삭제 중..."
DELETED=$(find "$BACKUP_DIR" -name "breaddesk_*.sql.gz" -mtime +$RETENTION_DAYS -delete -print | wc -l)

if [ "$DELETED" -gt 0 ]; then
  echo "   삭제된 백업: ${DELETED}개"
else
  echo "   삭제할 백업 없음"
fi

# 백업 목록 출력
echo ""
echo "📂 현재 백업 목록:"
ls -lh "$BACKUP_DIR"/breaddesk_*.sql.gz 2>/dev/null | awk '{print "   " $9 " (" $5 ")"}'

echo ""
echo "✅ 백업 완료!"
