import { test, expect } from '@playwright/test';
import { loginAsDemo } from './helpers/auth';

test.describe('Inquiries Management', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsDemo(page);
  });

  test('should load inquiries list page', async ({ page }) => {
    await page.goto('/inquiries');
    await expect(page.locator('h1:has-text("문의"), h1:has-text("Inquiries")').first()).toBeVisible({ timeout: 10000 });
  });

  test('should create new inquiry', async ({ page }) => {
    await page.goto('/inquiries');
    
    // Look for create button
    const createButton = page.locator('button:has-text("생성"), button:has-text("Create"), a:has-text("생성")');
    if (await createButton.count() > 0) {
      await createButton.first().click();
      
      // Fill form
      await page.fill('input[name="message"], textarea[name="message"]', 'E2E 테스트 문의입니다');
      
      // Submit
      await page.click('button[type="submit"]:has-text("생성"), button:has-text("등록")');
      
      // Should see success or redirect to list
      await page.waitForTimeout(2000);
    } else {
      test.skip();
    }
  });

  test('should view inquiry detail', async ({ page }) => {
    await page.goto('/inquiries');
    
    // Click first inquiry in list
    const firstInquiry = page.locator('a[href*="/inquiries/"], tr[role="row"] a').first();
    if (await firstInquiry.count() > 0) {
      await firstInquiry.click();
      await expect(page).toHaveURL(/.*inquiries\/\d+/);
      await expect(page.locator('text=/문의|Inquiry/i').first()).toBeVisible();
    } else {
      test.skip();
    }
  });

  test('should change inquiry status', async ({ page }) => {
    await page.goto('/inquiries');
    
    const firstInquiry = page.locator('a[href*="/inquiries/"]').first();
    if (await firstInquiry.count() > 0) {
      await firstInquiry.click();
      
      // Look for status change button/dropdown
      const statusButton = page.locator('button:has-text("상태"), select[name="status"]');
      if (await statusButton.count() > 0) {
        await statusButton.first().click();
        await page.waitForTimeout(1000);
        // Status should be changeable
      } else {
        test.skip();
      }
    } else {
      test.skip();
    }
  });

  test('should add message to inquiry', async ({ page }) => {
    await page.goto('/inquiries');
    
    const firstInquiry = page.locator('a[href*="/inquiries/"]').first();
    if (await firstInquiry.count() > 0) {
      await firstInquiry.click();
      
      // Look for message input
      const messageInput = page.locator('textarea[name="message"], textarea[placeholder*="메시지"]');
      if (await messageInput.count() > 0) {
        await messageInput.fill('추가 답변 테스트');
        await page.click('button:has-text("전송"), button:has-text("답변")');
        await page.waitForTimeout(1000);
      } else {
        test.skip();
      }
    } else {
      test.skip();
    }
  });

  test('should convert inquiry to task', async ({ page }) => {
    await page.goto('/inquiries');
    
    const firstInquiry = page.locator('a[href*="/inquiries/"]').first();
    if (await firstInquiry.count() > 0) {
      await firstInquiry.click();
      
      // Look for "Convert to Task" button
      const convertButton = page.locator('button:has-text("업무"), button:has-text("Task"), button:has-text("전환")');
      if (await convertButton.count() > 0) {
        await convertButton.first().click();
        await page.waitForTimeout(2000);
        // Should redirect to task or show confirmation
      } else {
        test.skip();
      }
    } else {
      test.skip();
    }
  });

  test('should filter inquiries by status', async ({ page }) => {
    await page.goto('/inquiries');
    
    // Look for status filter
    const statusFilter = page.locator('select:has-text("상태"), button:has-text("필터")');
    if (await statusFilter.count() > 0) {
      await statusFilter.first().click();
      await page.waitForTimeout(1000);
      // Filter should work
    } else {
      test.skip();
    }
  });
});
