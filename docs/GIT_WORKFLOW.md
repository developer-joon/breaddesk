# Git Workflow - BreadDesk

## 브랜치 전략: Git Flow + Worktree

### 브랜치 구조
```
main ──────────────────── 프로덕션 (배포 버전만)
  │
develop ───────────────── 개발 통합 (기본 브랜치)
  │
  ├── feature/*  ──────── 기능 개발
  ├── release/*  ──────── 릴리스 준비
  └── hotfix/*   ──────── 긴급 수정 (main에서 분기)
```

### 브랜치 규칙
| 브랜치 | 분기 from | merge to | 네이밍 |
|--------|----------|----------|--------|
| feature | develop | develop | `feature/inquiry-api`, `feature/kanban-ui` |
| release | develop | main + develop | `release/v0.1.0` |
| hotfix | main | main + develop | `hotfix/sla-timer-fix` |

---

## Worktree 사용법

### 왜 worktree?
- 브랜치 스위칭 없이 **병렬 작업** 가능
- 너(IntelliJ)와 에이전트(서버)가 동시에 다른 feature 작업
- stash/checkout 불필요

### 디렉토리 구조
```
~/projects/
├── breaddesk/                 ← main (기본 clone)
├── breaddesk-develop/         ← develop (주 작업)
├── breaddesk-feat-xxx/        ← feature 브랜치
└── breaddesk-release-xxx/     ← release 브랜치
```

### 초기 세팅
```bash
# 클론 (한 번만)
git clone https://github.com/developer-joon/breaddesk.git
cd breaddesk

# develop worktree 생성
git worktree add ../breaddesk-develop develop
```

### feature 작업
```bash
# feature 브랜치 생성 + worktree
cd breaddesk   # main 디렉토리에서
git worktree add ../breaddesk-feat-task-api -b feature/task-api develop

# 작업은 해당 디렉토리에서
cd ../breaddesk-feat-task-api
# ... 코딩 ...

# 커밋 & 푸시
git add -A
git commit -m "feat: 업무 CRUD API 구현"
git push -u origin feature/task-api

# PR 생성 (develop으로)
gh pr create --base develop --title "feat: 업무 CRUD API"

# 작업 완료 후 정리
cd ../breaddesk
git worktree remove ../breaddesk-feat-task-api
git branch -d feature/task-api
```

### release 작업
```bash
# develop에서 release 분기
git worktree add ../breaddesk-release-v010 -b release/v0.1.0 develop

# 버전 수정, 최종 테스트 후
# → main으로 PR (merge)
# → develop으로도 merge (변경사항 반영)

# 정리
git worktree remove ../breaddesk-release-v010
```

### hotfix 작업
```bash
# main에서 hotfix 분기
git worktree add ../breaddesk-hotfix-xxx -b hotfix/sla-fix main

# 수정 후
# → main으로 PR
# → develop으로도 merge

# 정리
git worktree remove ../breaddesk-hotfix-xxx
```

### 유용한 명령어
```bash
git worktree list              # 현재 worktree 목록
git worktree prune             # 삭제된 디렉토리 정리
```

---

## 커밋 컨벤션: Conventional Commits

### 형식
```
<type>: <subject>

[body]
```

### 타입
| 타입 | 용도 | 예시 |
|------|------|------|
| `feat` | 새 기능 | `feat: 문의 접수 API 구현` |
| `fix` | 버그 수정 | `fix: SLA 타이머 계산 오류` |
| `refactor` | 리팩토링 | `refactor: LLM Provider 구조 개선` |
| `test` | 테스트 | `test: 업무 CRUD 단위 테스트` |
| `docs` | 문서 | `docs: API 스펙 업데이트` |
| `chore` | 기타 | `chore: Gradle 의존성 업데이트` |
| `spec` | 스펙 | `spec: 문의 접수 플로우 정의` |

---

## PR 규칙

### 기본 흐름
```
feature/* → PR → 코드리뷰 → develop merge
```

### 리뷰
- **에이전트 자동 리뷰**: 모든 PR
- **사용자 리뷰 조건**: 
  - 파일 10개 이상 변경
  - 보안 관련 코드
  - DB 스키마 변경

### PR 제목
```
feat: 문의 접수 API 및 AI 자동 답변 (#12)
```
