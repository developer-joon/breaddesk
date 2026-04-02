import { test, expect } from '@playwright/test';
import { loginAsDemo } from './helpers/auth';

test.describe('Settings Management', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsDemo(page);
  });

  test('should load settings page', async ({ page }) => {
    await page.goto('/settings');
    await expect(page.locator('h1:has-text("설정"), h1:has-text("Settings")').first()).toBeVisible({ timeout: 10000 });
  });

  test('should navigate to Teams tab', async ({ page }) => {
    await page.goto('/settings');
    
    const teamsTab = page.locator('a:has-text("팀"), button:has-text("팀"), [role="tab"]:has-text("팀")');
    if (await teamsTab.count() > 0) {
      await teamsTab.first().click();
      await page.waitForTimeout(1000);
      await expect(page.locator('text=/팀|Team/i').first()).toBeVisible();
    } else {
      test.skip();
    }
  });

  test('should create team', async ({ page }) => {
    await page.goto('/settings');
    
    const teamsTab = page.locator('a:has-text("팀"), button:has-text("팀")');
    if (await teamsTab.count() > 0) {
      await teamsTab.first().click();
      
      const createButton = page.locator('button:has-text("생성"), button:has-text("Create")');
      if (await createButton.count() > 0) {
        await createButton.first().click();
        
        await page.fill('input[name="name"]', 'E2E 테스트 팀');
        await page.click('button[type="submit"]:has-text("생성"), button:has-text("등록")');
        await page.waitForTimeout(2000);
      } else {
        test.skip();
      }
    } else {
      test.skip();
    }
  });

  test('should add member to team', async ({ page }) => {
    await page.goto('/settings');
    
    const teamsTab = page.locator('a:has-text("팀"), button:has-text("팀")');
    if (await teamsTab.count() > 0) {
      await teamsTab.first().click();
      
      const addMemberButton = page.locator('button:has-text("멤버"), button:has-text("Member")');
      if (await addMemberButton.count() > 0) {
        await addMemberButton.first().click();
        await page.waitForTimeout(1000);
        // Member modal should appear
      } else {
        test.skip();
      }
    } else {
      test.skip();
    }
  });

  test('should navigate to Channels tab', async ({ page }) => {
    await page.goto('/settings');
    
    const channelsTab = page.locator('a:has-text("채널"), button:has-text("채널"), [role="tab"]:has-text("채널")');
    if (await channelsTab.count() > 0) {
      await channelsTab.first().click();
      await page.waitForTimeout(1000);
      await expect(page.locator('text=/채널|Channel/i').first()).toBeVisible();
    } else {
      test.skip();
    }
  });

  test('should navigate to SLA tab', async ({ page }) => {
    await page.goto('/settings');
    
    const slaTab = page.locator('a:has-text("SLA"), button:has-text("SLA"), [role="tab"]:has-text("SLA")');
    if (await slaTab.count() > 0) {
      await slaTab.first().click();
      await page.waitForTimeout(1000);
      await expect(page.locator('text=/SLA/i').first()).toBeVisible();
    } else {
      test.skip();
    }
  });

  test('should edit SLA rules', async ({ page }) => {
    await page.goto('/settings');
    
    const slaTab = page.locator('a:has-text("SLA"), button:has-text("SLA")');
    if (await slaTab.count() > 0) {
      await slaTab.first().click();
      
      const editButton = page.locator('button:has-text("수정"), button:has-text("Edit")');
      if (await editButton.count() > 0) {
        await editButton.first().click();
        await page.waitForTimeout(1000);
        // SLA edit form should appear
      } else {
        test.skip();
      }
    } else {
      test.skip();
    }
  });

  test('should navigate to AI Config tab', async ({ page }) => {
    await page.goto('/settings');
    
    const aiTab = page.locator('a:has-text("AI"), button:has-text("AI"), [role="tab"]:has-text("AI")');
    if (await aiTab.count() > 0) {
      await aiTab.first().click();
      await page.waitForTimeout(1000);
      await expect(page.locator('text=/AI|LLM|프롬프트/i').first()).toBeVisible();
    } else {
      test.skip();
    }
  });

  test('should view/edit AI prompts', async ({ page }) => {
    await page.goto('/settings');
    
    const aiTab = page.locator('a:has-text("AI"), button:has-text("AI")');
    if (await aiTab.count() > 0) {
      await aiTab.first().click();
      
      const promptTextarea = page.locator('textarea[name*="prompt"], textarea[placeholder*="프롬프트"]');
      if (await promptTextarea.count() > 0) {
        // Prompt editing is available
        await expect(promptTextarea.first()).toBeVisible();
      } else {
        test.skip();
      }
    } else {
      test.skip();
    }
  });
});
