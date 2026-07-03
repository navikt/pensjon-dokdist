const { defineConfig, devices } = require('@playwright/test');

const port = 8092;

module.exports = defineConfig({
    testDir: './e2e',
    fullyParallel: true,
    reporter: 'list',
    use: {
        baseURL: `http://localhost:${port}`,
        trace: 'on-first-retry',
    },
    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'] },
        },
    ],
    webServer: {
        command: `npm run build && node e2e/server.js`,
        url: `http://localhost:${port}/journalpost/123`,
        env: { PORT: String(port) },
        reuseExistingServer: !process.env.CI,
        timeout: 120_000,
    },
});
