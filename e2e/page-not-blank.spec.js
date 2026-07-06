const { test, expect } = require('@playwright/test');

// Denne testen dekker regresjonen vi fant ved Babel 8-oppgraderingen:
// et feil oppløst JSX-runtime-miljø (development/production) førte til at
// react/jsx-dev-runtime sitt `jsxDEV` var undefined i produksjonsbygget,
// noe som krasjet React før noe som helst ble rendret - siden ble blank.
test.describe('Journalpost-siden er ikke blank', () => {
    test('rendrer innhold og kaster ingen JS-feil', async ({ page }) => {
        const pageErrors = [];
        page.on('pageerror', (error) => pageErrors.push(error.message));

        await page.goto('/journalpost/123');

        // Vent på at hovedoverskriften faktisk vises i DOM-en.
        await expect(page.getByRole('heading', { name: /Journalpost med ID: 123/i })).toBeVisible();

        const rootHtml = await page.locator('#root').innerHTML();
        expect(rootHtml.trim().length).toBeGreaterThan(0);

        expect(pageErrors, `Uventede JS-feil i siden: ${pageErrors.join(', ')}`).toEqual([]);
    });
});
