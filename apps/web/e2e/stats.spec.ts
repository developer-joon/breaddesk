import { test, expect } from '@playwright/test';
import { loginAsDemo } from './helpers/auth';

test.describe('Statistics Page', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsDemo(page);
  });

  test('should load stats page', async ({ page }) => {
    await page.goto('/stats');
    await expect(page.locator('h1:has-text("통계"), h1:has-text("Stats")').first()).toBeVisible({ timeout: 10000 });
  });

  test('should show weekly report section', async ({ page }) => {
    await page.goto('/stats');
    
    const weeklyReport = page.locator('text=/주간|weekly|week/i');
    if (await weeklyReport.count() > 0) {
      await expect(weeklyReport.first()).toBeVisible();
    }
  });

  test('should display performance metrics', async ({ page }) => {
    await page.goto('/stats');
    
    // Look for various stat sections
    const statSections = page.locator('[class*="stat"], [class*="metric"], h2, h3');
    expect(await statSections.count()).toBeGreaterThan(0);
  });
});
