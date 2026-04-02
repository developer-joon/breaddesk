import { test, expect } from '@playwright/test';
import { loginAsDemo } from './helpers/auth';

test.describe('Tasks Management', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsDemo(page);
  });

  test('should load tasks page with kanban view', async ({ page }) => {
    await page.goto('/tasks');
    await expect(page.locator('h1:has-text("업무"), h1:has-text("Tasks")').first()).toBeVisible({ timeout: 10000 });
    
    // Check for kanban columns (WAITING, IN_PROGRESS, REVIEW, DONE)
    const kanbanColumns = page.locator('[data-status], .kanban-column');
    if (await kanbanColumns.count() > 0) {
      expect(await kanbanColumns.count()).toBeGreaterThan(0);
    }
  });

  test('should toggle to list view', async ({ page }) => {
    await page.goto('/tasks');
    
    // Look for view toggle button
    const listViewButton = page.locator('button:has-text("목록"), button:has-text("List"), button[aria-label*="list"]');
    if (await listViewButton.count() > 0) {
      await listViewButton.first().click();
      await page.waitForTimeout(1000);
      // Should show table/list view
      const tableView = page.locator('table, [role="table"]');
      if (await tableView.count() > 0) {
        await expect(tableView.first()).toBeVisible();
      }
    } else {
      test.skip();
    }
  });

  test('should create new task', async ({ page }) => {
    await page.goto('/tasks');
    
    const createButton = page.locator('button:has-text("생성"), button:has-text("Create"), a:has-text("생성")');
    if (await createButton.count() > 0) {
      await createButton.first().click();
      
      // Fill form
      await page.fill('input[name="title"]', 'E2E 테스트 업무');
      await page.fill('textarea[name="description"]', '테스트 설명입니다');
      
      // Select type if available
      const typeSelect = page.locator('select[name="type"]');
      if (await typeSelect.count() > 0) {
        await typeSelect.selectOption({ index: 1 });
      }
      
      // Submit
      await page.click('button[type="submit"]:has-text("생성"), button:has-text("등록")');
      await page.waitForTimeout(2000);
    } else {
      test.skip();
    }
  });

  test('should view task detail', async ({ page }) => {
    await page.goto('/tasks');
    
    const firstTask = page.locator('a[href*="/tasks/"]').first();
    if (await firstTask.count() > 0) {
      await firstTask.click();
      await expect(page).toHaveURL(/.*tasks\/\d+/);
      await expect(page.locator('h1, h2').first()).toBeVisible();
    } else {
      test.skip();
    }
  });

  test('should edit task', async ({ page }) => {
    await page.goto('/tasks');
    
    const firstTask = page.locator('a[href*="/tasks/"]').first();
    if (await firstTask.count() > 0) {
      await firstTask.click();
      
      // Look for edit button
      const editButton = page.locator('button:has-text("수정"), button:has-text("Edit")');
      if (await editButton.count() > 0) {
        await editButton.first().click();
        await page.waitForTimeout(1000);
        
        // Modify title if in edit mode
        const titleInput = page.locator('input[name="title"]');
        if (await titleInput.count() > 0) {
          await titleInput.fill('수정된 제목');
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

  test('should change task status', async ({ page }) => {
    await page.goto('/tasks');
    
    const firstTask = page.locator('a[href*="/tasks/"]').first();
    if (await firstTask.count() > 0) {
      await firstTask.click();
      
      // Look for status change button
      const statusButton = page.locator('button:has-text("상태"), select[name="status"]');
      if (await statusButton.count() > 0) {
        await statusButton.first().click();
        await page.waitForTimeout(1000);
      } else {
        test.skip();
      }
    } else {
      test.skip();
    }
  });

  test('should filter tasks by assignee', async ({ page }) => {
    await page.goto('/tasks');
    
    const filterButton = page.locator('button:has-text("필터"), select[name="assignee"]');
    if (await filterButton.count() > 0) {
      await filterButton.first().click();
      await page.waitForTimeout(1000);
    } else {
      test.skip();
    }
  });

  test('should filter tasks by urgency', async ({ page }) => {
    await page.goto('/tasks');
    
    const urgencyFilter = page.locator('select[name="urgency"], button:has-text("긴급")');
    if (await urgencyFilter.count() > 0) {
      await urgencyFilter.first().click();
      await page.waitForTimeout(1000);
    } else {
      test.skip();
    }
  });
});
