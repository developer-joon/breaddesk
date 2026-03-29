# 에이전트 팀 구성 - BreadDesk

> 언제든 변경 가능. 프로젝트 단계별로 조정.

## 팀원

| # | 역할 | 담당 |
|---|---|---|
| 1 | **Architect** | 스펙 작성, 설계, 리팩토링 판단 |
| 2 | **Backend Dev** | Spring Boot API 구현 |
| 3 | **Frontend Dev** | Next.js UI 구현 |
| 4 | **AI Dev** | LLM/RAG 구현 |
| 5 | **QA** | 단위/통합/회귀/E2E 테스트 |
| 6 | **Reviewer** | 코드리뷰, 리팩토링 실행, 스펙 검증 |
| 7 | **사용자** | 방향 결정, 직접 코딩, 최종 승인 |

## 작업 흐름

```
Architect: 스펙 작성
    ↓
Backend/Frontend/AI Dev: 구현 (worktree 병렬)
    ↓
QA: 테스트 작성 + 실행
    ↓
Reviewer: 코드리뷰 + 스펙 일치 확인 + 리팩토링 제안
    ↓
사용자: 최종 승인 → merge
```

## 변경 이력

- **2026-03-29**: 초기 구성 (6 에이전트 + 사용자)
