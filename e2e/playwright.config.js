const { defineConfig, devices } = require('@playwright/test');

const port = 8092;

module.exports = defineConfig({
    testDir: '.',
    fullyParallel: true,
    reporter: [
        ['list'],
        ['html', { outputFolder: 'e2e-report', open: 'never' }],
    ],
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
        command: `npm --prefix .. run build && node server.js`,
        url: `http://localhost:${port}/journalpost/123`,
        env: { PORT: String(port) },
        reuseExistingServer: !process.env.CI,
        timeout: 120_000,
    },
});
