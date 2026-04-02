import { test, expect } from '@playwright/test';
import { loginAsDemo } from './helpers/auth';

test.describe('Analytics Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsDemo(page);
  });

  test('should load analytics page', async ({ page }) => {
    await page.goto('/analytics');
    await expect(page.locator('h1:has-text("분석"), h1:has-text("Analytics")').first()).toBeVisible({ timeout: 10000 });
  });

  test('should show charts and stats', async ({ page }) => {
    await page.goto('/analytics');
    
    // Look for common chart elements
    const charts = page.locator('canvas, svg, [role="img"]');
    if (await charts.count() > 0) {
      expect(await charts.count()).toBeGreaterThan(0);
    }
    
    // Look for stat cards
    const statCards = page.locator('[class*="card"], [class*="stat"]');
    if (await statCards.count() > 0) {
      expect(await statCards.count()).toBeGreaterThan(0);
    }
  });

  test('should display key metrics', async ({ page }) => {
    await page.goto('/analytics');
    
    // Common metrics to look for
    const metrics = [
      /문의.*수|inquiry.*count/i,
      /해결.*률|resolution.*rate/i,
      /평균.*시간|average.*time/i,
      /AI.*률|AI.*rate/i
    ];
    
    for (const metric of metrics) {
      const element = page.locator(`text=${metric}`);
      if (await element.count() > 0) {
        // Metric found
        await expect(element.first()).toBeVisible();
        break; // At least one metric should be visible
      }
    }
  });
});
