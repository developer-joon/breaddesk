# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: analytics.spec.ts >> Analytics Dashboard >> should show charts and stats
- Location: e2e/analytics.spec.ts:14:7

# Error details

```
Test timeout of 30000ms exceeded while running "beforeEach" hook.
```

```
Error: page.waitForURL: Test timeout of 30000ms exceeded.
=========================== logs ===========================
waiting for navigation to "**/dashboard" until "load"
============================================================
```

# Page snapshot

```yaml
- generic [active] [ref=e1]:
  - generic [ref=e3]:
    - generic [ref=e4]:
      - text: 🍞
      - heading "BreadDesk" [level=1] [ref=e5]
      - paragraph [ref=e6]: 통합 고객지원 플랫폼
    - generic [ref=e7]: Request failed with status code 429
    - generic [ref=e8]:
      - generic [ref=e9]:
        - generic [ref=e10]: 이메일
        - textbox "이메일" [ref=e11]:
          - /placeholder: your@email.com
          - text: demo@breaddesk.com
      - generic [ref=e12]:
        - generic [ref=e13]: 비밀번호
        - textbox "비밀번호" [ref=e14]:
          - /placeholder: ••••••••
          - text: demo1234
      - button "로그인" [ref=e15] [cursor=pointer]
    - generic [ref=e16]:
      - button "데모 계정으로 체험하기" [ref=e17] [cursor=pointer]
      - paragraph [ref=e18]: demo@breaddesk.com / demo1234
  - alert [ref=e19]
```

# Test source

```ts
  1 | import { Page } from '@playwright/test';
  2 | 
  3 | export async function loginAsDemo(page: Page) {
  4 |   await page.goto('/login');
  5 |   await page.click('button:has-text("데모 계정으로 체험하기")');
  6 |   await page.click('button:has-text("로그인")');
> 7 |   await page.waitForURL('**/dashboard');
    |              ^ Error: page.waitForURL: Test timeout of 30000ms exceeded.
  8 | }
  9 | 
```