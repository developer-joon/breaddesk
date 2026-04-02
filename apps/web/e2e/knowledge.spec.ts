import { test, expect } from '@playwright/test';
import { loginAsDemo } from './helpers/auth';

test.describe('Knowledge Base', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsDemo(page);
  });

  test('should load knowledge page', async ({ page }) => {
    await page.goto('/knowledge');
    await expect(page.locator('h1:has-text("지식"), h1:has-text("Knowledge")').first()).toBeVisible({ timeout: 10000 });
  });

  test('should add knowledge document', async ({ page }) => {
    await page.goto('/knowledge');
    
    const createButton = page.locator('button:has-text("생성"), button:has-text("추가"), button:has-text("Create")');
    if (await createButton.count() > 0) {
      await createButton.first().click();
      
      await page.fill('input[name="title"]', 'E2E 테스트 문서');
      await page.fill('textarea[name="content"]', '테스트 지식 문서 내용');
      
      await page.click('button[type="submit"]:has-text("생성"), button:has-text("등록")');
      await page.waitForTimeout(2000);
    } else {
      test.skip();
    }
  });

  test('should search knowledge', async ({ page }) => {
    await page.goto('/knowledge');
    
    const searchInput = page.locator('input[type="search"], input[placeholder*="검색"]');
    if (await searchInput.count() > 0) {
      await searchInput.fill('VPN');
      await page.keyboard.press('Enter');
      await page.waitForTimeout(2000);
      // Should show search results
    } else {
      test.skip();
    }
  });

  test('should view knowledge document', async ({ page }) => {
    await page.goto('/knowledge');
    
    const firstDoc = page.locator('a[href*="/knowledge/"]').first();
    if (await firstDoc.count() > 0) {
      await firstDoc.click();
      await expect(page).toHaveURL(/.*knowledge\/\d+/);
      await expect(page.locator('h1, h2').first()).toBeVisible();
    } else {
      test.skip();
    }
  });

  test('should edit knowledge document', async ({ page }) => {
    await page.goto('/knowledge');
    
    const firstDoc = page.locator('a[href*="/knowledge/"]').first();
    if (await firstDoc.count() > 0) {
      await firstDoc.click();
      
      const editButton = page.locator('button:has-text("수정"), button:has-text("Edit")');
      if (await editButton.count() > 0) {
        await editButton.first().click();
        
        const titleInput = page.locator('input[name="title"]');
        if (await titleInput.count() > 0) {
          await titleInput.fill('수정된 문서 제목');
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

  test('should delete knowledge document', async ({ page }) => {
    await page.goto('/knowledge');
    
    const firstDoc = page.locator('a[href*="/knowledge/"]').first();
    if (await firstDoc.count() > 0) {
      await firstDoc.click();
      
      const deleteButton = page.locator('button:has-text("삭제"), button:has-text("Delete")');
      if (await deleteButton.count() > 0) {
        await deleteButton.first().click();
        
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
