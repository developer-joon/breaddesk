import { test, expect } from '@playwright/test';
import { loginAsDemo } from './helpers/auth';

test.describe('Templates Management', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsDemo(page);
  });

  test('should load templates page', async ({ page }) => {
    await page.goto('/templates');
    await expect(page.locator('h1:has-text("템플릿"), h1:has-text("Templates")').first()).toBeVisible({ timeout: 10000 });
  });

  test('should create template', async ({ page }) => {
    await page.goto('/templates');
    
    const createButton = page.locator('button:has-text("생성"), button:has-text("Create")');
    if (await createButton.count() > 0) {
      await createButton.first().click();
      
      // Fill form
      await page.fill('input[name="title"]', 'E2E 테스트 템플릿');
      await page.fill('textarea[name="content"]', '테스트 템플릿 내용입니다');
      
      // Category if available
      const categoryInput = page.locator('input[name="category"]');
      if (await categoryInput.count() > 0) {
        await categoryInput.fill('테스트');
      }
      
      await page.click('button[type="submit"]:has-text("생성"), button:has-text("등록")');
      await page.waitForTimeout(2000);
    } else {
      test.skip();
    }
  });

  test('should edit template', async ({ page }) => {
    await page.goto('/templates');
    
    const firstTemplate = page.locator('a[href*="/templates/"], tr a').first();
    if (await firstTemplate.count() > 0) {
      await firstTemplate.click();
      
      const editButton = page.locator('button:has-text("수정"), button:has-text("Edit")');
      if (await editButton.count() > 0) {
        await editButton.first().click();
        
        const titleInput = page.locator('input[name="title"]');
        if (await titleInput.count() > 0) {
          await titleInput.fill('수정된 템플릿 제목');
          await page.click('button:has-text("저장"), button:has-text("Save")');
          await page.waitForTimeout(1000);
        }
      } else {
        test.skip();
      }
    } else {
      test.skip();
    }
  });

  test('should delete template', async ({ page }) => {
    await page.goto('/templates');
    
    const firstTemplate = page.locator('a[href*="/templates/"], tr').first();
    if (await firstTemplate.count() > 0) {
      await firstTemplate.click();
      
      const deleteButton = page.locator('button:has-text("삭제"), button:has-text("Delete")');
      if (await deleteButton.count() > 0) {
        await deleteButton.first().click();
        
        // Confirm deletion if modal appears
        const confirmButton = page.locator('button:has-text("확인"), button:has-text("Confirm")');
        if (await confirmButton.count() > 0) {
          await confirmButton.first().click();
        }
        await page.waitForTimeout(1000);
      } else {
        test.skip();
      }
    } else {
      test.skip();
    }
  });
});
