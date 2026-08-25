import { defineConfig, devices } from '@playwright/test';

const isCI = typeof process !== 'undefined' ? !!process.env.CI : false;

export default defineConfig({
	testDir: './tests',
	testMatch: '**/*.e2e.{ts,js}',
	timeout: 30 * 1000,
	expect: { timeout: 5000 },
	fullyParallel: true,
	retries: isCI ? 2 : 0,
	workers: isCI ? 1 : undefined,
	reporter: 'html',
	use: {
		baseURL: 'http://localhost:5173',
		trace: 'on-first-retry',
		video: 'retain-on-failure',
	},
	projects: [
		{ name: 'chromium', use: { ...devices['Desktop Chrome'] } },
		{ name: 'firefox', use: { ...devices['Desktop Firefox'] } },
		{ name: 'webkit', use: { ...devices['Desktop Safari'] } } // <-- ¡WEBKIT RESTAURADO!
	],
	webServer: {
		command: 'npm run dev',
		port: 5173,
		reuseExistingServer: true,
		timeout: 120 * 1000,
	}
});
