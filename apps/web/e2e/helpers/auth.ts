import { Page } from '@playwright/test';

export async function loginAsDemo(page: Page) {
  await page.goto('/login');
  await page.click('button:has-text("데모 계정으로 체험하기")');
  await page.click('button:has-text("로그인")');
  await page.waitForURL('**/dashboard');
}
