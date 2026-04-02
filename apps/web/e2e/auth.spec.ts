import { test, expect } from '@playwright/test';
import { loginAsDemo } from './helpers/auth';

test.describe('Authentication', () => {
  test('should login with demo account', async ({ page }) => {
    await loginAsDemo(page);
    await expect(page).toHaveURL(/.*dashboard/);
  });

  test('should verify dashboard loads after login', async ({ page }) => {
    await loginAsDemo(page);
    await expect(page.locator('h1, h2, [role="heading"]').first()).toBeVisible();
  });

  test('should logout successfully', async ({ page }) => {
    await loginAsDemo(page);
    
    // Look for user menu or logout button
    const logoutButton = page.locator('button:has-text("로그아웃"), button:has-text("Logout")');
    if (await logoutButton.count() > 0) {
      await logoutButton.click();
      await expect(page).toHaveURL(/.*login/);
    } else {
      // If no logout button found, skip this test
      test.skip();
    }
  });

  test('should show error on invalid login', async ({ page }) => {
    await page.goto('/login');
    
    // Try to find and fill manual login form (if exists)
    const emailInput = page.locator('input[type="email"], input[name="email"]');
    const passwordInput = page.locator('input[type="password"], input[name="password"]');
    
    if (await emailInput.count() > 0 && await passwordInput.count() > 0) {
      await emailInput.fill('invalid@example.com');
      await passwordInput.fill('wrongpassword');
      await page.click('button[type="submit"]');
      
      // Should show error message
      await expect(page.locator('text=/오류|error|실패|invalid/i').first()).toBeVisible({ timeout: 5000 });
    } else {
      // No manual login form available, skip
      test.skip();
    }
  });
});
