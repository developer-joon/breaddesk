# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: auth.spec.ts >> Authentication >> should show error on invalid login
- Location: e2e/auth.spec.ts:29:7

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('text=/오류|error|실패|invalid/i').first()
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for locator('text=/오류|error|실패|invalid/i').first()

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
          - text: invalid@example.com
      - generic [ref=e12]:
        - generic [ref=e13]: 비밀번호
        - textbox "비밀번호" [ref=e14]:
          - /placeholder: ••••••••
          - text: wrongpassword
      - button "로그인" [ref=e15] [cursor=pointer]
    - generic [ref=e16]:
      - button "데모 계정으로 체험하기" [ref=e17] [cursor=pointer]
      - paragraph [ref=e18]: demo@breaddesk.com / demo1234
  - alert [ref=e19]
```

# Test source

```ts
  1  | import { test, expect } from '@playwright/test';
  2  | import { loginAsDemo } from './helpers/auth';
  3  | 
  4  | test.describe('Authentication', () => {
  5  |   test('should login with demo account', async ({ page }) => {
  6  |     await loginAsDemo(page);
  7  |     await expect(page).toHaveURL(/.*dashboard/);
  8  |   });
  9  | 
  10 |   test('should verify dashboard loads after login', async ({ page }) => {
  11 |     await loginAsDemo(page);
  12 |     await expect(page.locator('h1, h2, [role="heading"]').first()).toBeVisible();
  13 |   });
  14 | 
  15 |   test('should logout successfully', async ({ page }) => {
  16 |     await loginAsDemo(page);
  17 |     
  18 |     // Look for user menu or logout button
  19 |     const logoutButton = page.locator('button:has-text("로그아웃"), button:has-text("Logout")');
  20 |     if (await logoutButton.count() > 0) {
  21 |       await logoutButton.click();
  22 |       await expect(page).toHaveURL(/.*login/);
  23 |     } else {
  24 |       // If no logout button found, skip this test
  25 |       test.skip();
  26 |     }
  27 |   });
  28 | 
  29 |   test('should show error on invalid login', async ({ page }) => {
  30 |     await page.goto('/login');
  31 |     
  32 |     // Try to find and fill manual login form (if exists)
  33 |     const emailInput = page.locator('input[type="email"], input[name="email"]');
  34 |     const passwordInput = page.locator('input[type="password"], input[name="password"]');
  35 |     
  36 |     if (await emailInput.count() > 0 && await passwordInput.count() > 0) {
  37 |       await emailInput.fill('invalid@example.com');
  38 |       await passwordInput.fill('wrongpassword');
  39 |       await page.click('button[type="submit"]');
  40 |       
  41 |       // Should show error message
> 42 |       await expect(page.locator('text=/오류|error|실패|invalid/i').first()).toBeVisible({ timeout: 5000 });
     |                                                                         ^ Error: expect(locator).toBeVisible() failed
  43 |     } else {
  44 |       // No manual login form available, skip
  45 |       test.skip();
  46 |     }
  47 |   });
  48 | });
  49 | 
```