const { test, expect } = require('@playwright/test');

const ID = '123';
const INFO_URL = `**/api/journalpost/${ID}`;
const SEND_URL = `**/api/journalpost/${ID}/send`;

const SEND_BUTTON = { name: 'Send brevet' };
const SUCCESS_TEXT = 'Journalpost er nå sendt.';
const ALREADY_DISTRIBUTED_TEXT = 'Journalposten er allerede distribuert.';
const NOT_SENT_HEADING = { name: 'Brevet er ikke sendt' };
const INFO_FAILED_HEADING = { name: 'Kunne ikke hente journalposten' };

async function expectNotSent(page, feilmelding) {
    await expect(page.getByRole('heading', NOT_SENT_HEADING)).toBeVisible();
    await expect(page.getByText('Det oppstod en feil, og brevet ble ikke sendt.', { exact: false })).toBeVisible();
    await expect(page.getByText(`Feilmelding: ${feilmelding}`)).toBeVisible();
    await expect(page.getByText(SUCCESS_TEXT)).toHaveCount(0);
    await expect(page.getByText(ALREADY_DISTRIBUTED_TEXT)).toHaveCount(0);
}

function mockInfo(page, { status = 200, body = { journalpostId: ID, fritekst: false } } = {}) {
    const calls = [];
    return page.route(INFO_URL, (route) => {
        calls.push(route.request());
        return route.fulfill({ status, contentType: 'application/json', body: JSON.stringify(body) });
    }).then(() => calls);
}

function mockSend(page, handler) {
    const calls = [];
    return page.route(SEND_URL, async (route) => {
        calls.push(route.request());
        return handler(route);
    }).then(() => calls);
}

const fulfillJson = (status, body) => (route) =>
    route.fulfill({ status, contentType: 'application/json', body: body === undefined ? '' : JSON.stringify(body) });

function countPageLoads(page) {
    const loads = { count: 0 };
    page.on('load', () => loads.count++);
    return loads;
}

async function trackPageErrors(page) {
    const errors = [];
    page.on('pageerror', (error) => errors.push(error.message));
    return errors;
}

test.describe('Henting av journalpostinfo', () => {
    test('viser laster mens journalpostinfo hentes', async ({ page }) => {
        let release;
        const pending = new Promise((resolve) => { release = resolve; });
        await page.route(INFO_URL, async (route) => {
            await pending;
            await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ journalpostId: ID, fritekst: false }) });
        });

        await page.goto(`/journalpost/${ID}?status=FS`);

        await expect(page.getByRole('img', { name: 'venter...' })).toBeVisible();
        await expect(page.getByRole('button', SEND_BUTTON)).toHaveCount(0);

        release();

        await expect(page.getByRole('button', SEND_BUTTON)).toBeVisible();
        await expect(page.getByRole('img', { name: 'venter...' })).toHaveCount(0);
    });

    test('viser kun send-knapp for vanlig brev', async ({ page }) => {
        const errors = await trackPageErrors(page);
        await mockInfo(page);

        await page.goto(`/journalpost/${ID}?status=FS`);

        await expect(page.getByRole('heading', { name: `Journalpost med ID: ${ID}` })).toBeVisible();
        await expect(page.getByRole('button', SEND_BUTTON)).toBeEnabled();
        await expect(page.getByRole('radiogroup')).toHaveCount(0);
        await expect(page.getByText('Du må velge varseltype')).toHaveCount(0);
        expect(errors).toEqual([]);
    });

    test('viser valg av varseltype for fritekstbrev', async ({ page }) => {
        await mockInfo(page, { body: { journalpostId: ID, fritekst: true } });

        await page.goto(`/journalpost/${ID}?status=FS`);

        await expect(page.getByRole('heading', { name: 'Du må velge varseltype' })).toBeVisible();
        for (const name of ['Vedtak', 'Viktig', 'Annet']) {
            await expect(page.getByRole('radio', { name })).not.toBeChecked();
        }
        await expect(page.getByRole('button', SEND_BUTTON)).toBeEnabled();
    });

    test('kan åpne hjelpetekst om varseltyper', async ({ page }) => {
        await mockInfo(page, { body: { journalpostId: ID, fritekst: true } });

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', { name: 'Slik skal de forskjellige varslingstypene brukes' }).click();

        await expect(page.getByText('Bruk alltid denne ved vedtak.')).toBeVisible();
        await expect(page.getByRole('link', { name: 'dette rutinedokumentet' })).toHaveAttribute('target', '_blank');
    });

    for (const status of [400, 403, 404, 500, 503]) {
        test(`viser feilmelding når henting feiler med ${status}`, async ({ page }) => {
            await mockInfo(page, { status, body: { message: `noe gikk galt (${status})` } });

            await page.goto(`/journalpost/${ID}?status=FS`);

            await expect(page.getByRole('heading', INFO_FAILED_HEADING)).toBeVisible();
            await expect(page.getByText(`Feilmelding: noe gikk galt (${status})`)).toBeVisible();
            await expect(page.getByRole('button', SEND_BUTTON)).toHaveCount(0);
            await expect(page.getByRole('img', { name: 'venter...' })).toHaveCount(0);
        });
    }

    test('logger ut og laster siden på nytt ved 401', async ({ page }) => {
        const loads = countPageLoads(page);
        let unauthorized = true;
        await page.route(INFO_URL, (route) => {
            const respondUnauthorized = unauthorized;
            unauthorized = false;
            return respondUnauthorized
                ? route.fulfill({ status: 401, contentType: 'application/json', body: '{}' })
                : route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ journalpostId: ID, fritekst: false }) });
        });
        const logoutRequest = page.waitForRequest('**/logout');
        await page.route('**/logout', (route) => route.fulfill({ status: 200, body: '' }));

        await page.goto(`/journalpost/${ID}?status=FS`);

        await logoutRequest;
        await expect(page.getByRole('button', SEND_BUTTON)).toBeVisible();
        expect(loads.count).toBe(2);
    });

    test('laster siden på nytt når henting gir redirect (utløpt sesjon)', async ({ page }) => {
        const loads = countPageLoads(page);
        let redirect = true;
        await page.route(INFO_URL, (route) => {
            const respondRedirect = redirect;
            redirect = false;
            return respondRedirect
                ? route.fulfill({ status: 302, headers: { location: '/oauth2/authorization/aad' } })
                : route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ journalpostId: ID, fritekst: false }) });
        });

        await page.goto(`/journalpost/${ID}?status=FS`);

        await expect(page.getByRole('button', SEND_BUTTON)).toBeVisible();
        expect(loads.count).toBe(2);
    });
});

test.describe('Distribusjon av vanlig brev', () => {
    test.beforeEach(async ({ page }) => {
        await mockInfo(page);
    });

    for (const status of [200, 204]) {
        test(`viser suksess når distribusjon svarer ${status}`, async ({ page }) => {
            const errors = await trackPageErrors(page);
            const sendCalls = await mockSend(page, fulfillJson(status, status === 200 ? { bestillingsId: '9911' } : undefined));

            await page.goto(`/journalpost/${ID}?status=FS`);
            await page.getByRole('button', SEND_BUTTON).click();

            await expect(page.getByText(SUCCESS_TEXT)).toBeVisible();
            await expect(page.getByText(ALREADY_DISTRIBUTED_TEXT)).toHaveCount(0);
            await expect(page.getByRole('heading', NOT_SENT_HEADING)).toHaveCount(0);
            await expect(page.getByRole('button', SEND_BUTTON)).toHaveCount(0);
            expect(sendCalls).toHaveLength(1);
            expect(errors).toEqual([]);
        });
    }

    test('viser at brevet ikke er sendt når mottaker er død (410)', async ({ page }) => {
        const sendCalls = await mockSend(page, fulfillJson(410));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();

        await expect(page.getByRole('heading', NOT_SENT_HEADING)).toBeVisible();
        await expect(page.getByText('Mottakeren er død, så brevet kan ikke distribueres.')).toBeVisible();
        await expect(page.getByText(SUCCESS_TEXT)).toHaveCount(0);
        await expect(page.getByRole('button', SEND_BUTTON)).toHaveCount(0);
        await expect(page.getByRole('button', { name: 'Prøv å sende igjen' })).toHaveCount(0);
        expect(sendCalls).toHaveLength(1);
    });

    test('sender journalpostId og status fra URL, uten distribusjonstype', async ({ page }) => {
        const sendCalls = await mockSend(page, fulfillJson(200, { bestillingsId: '9911' }));

        await page.goto(`/journalpost/${ID}?status=FL`);
        await page.getByRole('button', SEND_BUTTON).click();
        await expect(page.getByText(SUCCESS_TEXT)).toBeVisible();

        expect(sendCalls).toHaveLength(1);
        expect(sendCalls[0].method()).toBe('POST');
        expect(sendCalls[0].headers()['content-type']).toBe('application/json');
        expect(sendCalls[0].postDataJSON()).toEqual({ journalpostId: ID, status: 'FL' });
    });

    test('viser at brevet sendes og hindrer dobbeltklikk mens distribusjon pågår', async ({ page }) => {
        let release;
        const pending = new Promise((resolve) => { release = resolve; });
        const sendCalls = await mockSend(page, async (route) => {
            await pending;
            await fulfillJson(200, { bestillingsId: '9911' })(route);
        });

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();

        const loadingButton = page.getByRole('button', { name: 'Venter…' });
        await expect(loadingButton).toBeDisabled();
        await loadingButton.click({ force: true });

        release();

        await expect(page.getByText(SUCCESS_TEXT)).toBeVisible();
        expect(sendCalls).toHaveLength(1);
    });

    test('viser at journalposten allerede er distribuert ved 409', async ({ page }) => {
        const errors = await trackPageErrors(page);
        await mockSend(page, fulfillJson(409));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();

        await expect(page.getByText(ALREADY_DISTRIBUTED_TEXT)).toBeVisible();
        await expect(page.getByText(SUCCESS_TEXT)).toHaveCount(0);
        await expect(page.getByRole('heading', NOT_SENT_HEADING)).toHaveCount(0);
        await expect(page.getByRole('button', SEND_BUTTON)).toHaveCount(0);
        expect(errors).toEqual([]);
    });

    for (const status of [400, 403, 404, 500, 502, 503]) {
        test(`viser feilmelding når distribusjon feiler med ${status}`, async ({ page }) => {
            await mockSend(page, fulfillJson(status, { message: `teknisk feil (${status})` }));

            await page.goto(`/journalpost/${ID}?status=FS`);
            await page.getByRole('button', SEND_BUTTON).click();

            await expectNotSent(page, `teknisk feil (${status})`);
            await expect(page.getByRole('button', SEND_BUTTON)).toHaveCount(0);
        });
    }

    test('viser at brevet ikke er sendt også når feilsvaret ikke er JSON', async ({ page }) => {
        const errors = await trackPageErrors(page);
        await mockSend(page, (route) => route.fulfill({ status: 502, contentType: 'text/html', body: '<html>Bad Gateway</html>' }));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();

        await expectNotSent(page, 'Ukjent feil (HTTP 502)');
        expect(errors).toEqual([]);
    });

    test('viser at brevet ikke er sendt når feilsvaret mangler melding', async ({ page }) => {
        await mockSend(page, fulfillJson(500, {}));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();

        await expectNotSent(page, 'Ukjent feil (HTTP 500)');
    });

    test('logger ut og laster siden på nytt når distribusjon svarer 401', async ({ page }) => {
        const sendCalls = await mockSend(page, fulfillJson(401, {}));
        const logoutRequest = page.waitForRequest('**/logout');
        await page.route('**/logout', (route) => route.fulfill({ status: 200, body: '' }));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();

        await logoutRequest;
        await page.waitForEvent('load');
        await expect(page.getByRole('button', SEND_BUTTON)).toBeEnabled();
        await expect(page.getByText(SUCCESS_TEXT)).toHaveCount(0);
        expect(sendCalls).toHaveLength(1);
    });

    test('laster siden på nytt når distribusjon feiler på nettverksnivå', async ({ page }) => {
        const sendCalls = await mockSend(page, (route) => route.abort('connectionreset'));

        await page.goto(`/journalpost/${ID}?status=FS`);
        const reload = page.waitForEvent('load');
        await page.getByRole('button', SEND_BUTTON).click();
        await reload;

        await expect(page.getByRole('button', SEND_BUTTON)).toBeEnabled();
        await expect(page.getByText(SUCCESS_TEXT)).toHaveCount(0);
        expect(sendCalls).toHaveLength(1);
    });
});

test.describe('Distribusjon av fritekstbrev', () => {
    test.beforeEach(async ({ page }) => {
        await mockInfo(page, { body: { journalpostId: ID, fritekst: true } });
    });

    test('krever at varseltype velges før sending', async ({ page }) => {
        const sendCalls = await mockSend(page, fulfillJson(200, { bestillingsId: '9911' }));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();

        await expect(page.getByText('Varslingsmetode må fylles inn')).toBeVisible();
        await expect(page.getByRole('button', SEND_BUTTON)).toBeEnabled();
        expect(sendCalls).toHaveLength(0);
    });

    test('kan sende etter å ha rettet manglende varseltype', async ({ page }) => {
        const sendCalls = await mockSend(page, fulfillJson(200, { bestillingsId: '9911' }));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();
        await expect(page.getByText('Varslingsmetode må fylles inn')).toBeVisible();

        await page.getByRole('radio', { name: 'Viktig' }).check();
        await page.getByRole('button', SEND_BUTTON).click();

        await expect(page.getByText(SUCCESS_TEXT)).toBeVisible();
        await expect(page.getByText('Varslingsmetode må fylles inn')).toHaveCount(0);
        expect(sendCalls).toHaveLength(1);
        expect(sendCalls[0].postDataJSON()).toEqual({ journalpostId: ID, status: 'FS', distribusjonstype: 'VIKTIG' });
    });

    for (const [label, distribusjonstype] of [['Vedtak', 'VEDTAK'], ['Viktig', 'VIKTIG'], ['Annet', 'ANNET']]) {
        test(`sender valgt varseltype ${distribusjonstype}`, async ({ page }) => {
            const sendCalls = await mockSend(page, fulfillJson(200, { bestillingsId: '9911' }));

            await page.goto(`/journalpost/${ID}?status=FS`);
            await page.getByRole('radio', { name: label }).check();
            await page.getByRole('button', SEND_BUTTON).click();

            await expect(page.getByText(SUCCESS_TEXT)).toBeVisible();
            expect(sendCalls[0].postDataJSON()).toEqual({ journalpostId: ID, status: 'FS', distribusjonstype });
        });
    }

    test('låser varseltype mens distribusjon pågår og skjuler valget etterpå', async ({ page }) => {
        let release;
        const pending = new Promise((resolve) => { release = resolve; });
        await mockSend(page, async (route) => {
            await pending;
            await fulfillJson(200, { bestillingsId: '9911' })(route);
        });

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('radio', { name: 'Vedtak' }).check();
        await page.getByRole('button', SEND_BUTTON).click();

        await expect(page.getByRole('radio', { name: 'Viktig' })).toBeDisabled();

        release();

        await expect(page.getByText(SUCCESS_TEXT)).toBeVisible();
        await expect(page.getByRole('radio')).toHaveCount(0);
    });

    test('viser at journalposten allerede er distribuert ved 409', async ({ page }) => {
        await mockSend(page, fulfillJson(409));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('radio', { name: 'Vedtak' }).check();
        await page.getByRole('button', SEND_BUTTON).click();

        await expect(page.getByText(ALREADY_DISTRIBUTED_TEXT)).toBeVisible();
        await expect(page.getByRole('radio')).toHaveCount(0);
    });

    test('viser feilmelding når distribusjon feiler', async ({ page }) => {
        await mockSend(page, fulfillJson(500, { message: 'teknisk feil' }));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('radio', { name: 'Annet' }).check();
        await page.getByRole('button', SEND_BUTTON).click();

        await expectNotSent(page, 'teknisk feil');
        await expect(page.getByRole('radio')).toHaveCount(0);
    });
});

test.describe('Prøv igjen og korrelasjons-ID', () => {
    const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;
    const RETRY_BUTTON = { name: 'Prøv å sende igjen' };
    const callIdOf = (request) => request.headers()['nav-call-id'];

    function sequence(...handlers) {
        let i = 0;
        return (route) => handlers[Math.min(i++, handlers.length - 1)](route);
    }

    test('viser korrelasjons-ID som ble sendt med distribusjonen', async ({ page }) => {
        await mockInfo(page);
        const sendCalls = await mockSend(page, fulfillJson(500, { message: 'teknisk feil' }));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();

        await expect(page.getByRole('heading', NOT_SENT_HEADING)).toBeVisible();
        expect(callIdOf(sendCalls[0])).toMatch(UUID);
        await expect(page.getByTestId('korrelasjons-id')).toHaveText(callIdOf(sendCalls[0]));
    });

    test('kopierer korrelasjons-ID til utklippstavlen', async ({ page, context }) => {
        await context.grantPermissions(['clipboard-read', 'clipboard-write']);
        await mockInfo(page);
        const sendCalls = await mockSend(page, fulfillJson(500, { message: 'teknisk feil' }));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();
        await page.getByRole('button', { name: 'Kopier ID' }).click();

        await expect(page.getByRole('button', { name: 'Kopiert' })).toBeVisible();
        expect(await page.evaluate(() => navigator.clipboard.readText())).toBe(callIdOf(sendCalls[0]));
    });

    test('prøv igjen sender brevet på nytt med ny korrelasjons-ID og viser suksess', async ({ page }) => {
        await mockInfo(page);
        const sendCalls = await mockSend(page, sequence(
            fulfillJson(500, { message: 'teknisk feil' }),
            fulfillJson(200, { bestillingsId: '9911' }),
        ));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();
        await page.getByRole('button', RETRY_BUTTON).click();

        await expect(page.getByText(SUCCESS_TEXT)).toBeVisible();
        await expect(page.getByRole('heading', NOT_SENT_HEADING)).toHaveCount(0);
        await expect(page.getByTestId('korrelasjons-id')).toHaveCount(0);
        await expect(page.getByRole('button', RETRY_BUTTON)).toHaveCount(0);
        expect(sendCalls).toHaveLength(2);
        expect(sendCalls[1].postDataJSON()).toEqual(sendCalls[0].postDataJSON());
        expect(callIdOf(sendCalls[1])).not.toBe(callIdOf(sendCalls[0]));
    });

    test('beholder feilmeldingen og viser venter på knappen mens prøv igjen pågår', async ({ page }) => {
        await mockInfo(page);
        let release;
        const pending = new Promise((resolve) => { release = resolve; });
        let attempt = 0;
        await mockSend(page, async (route) => {
            if (attempt++ === 0) return fulfillJson(500, { message: 'teknisk feil' })(route);
            await pending;
            return fulfillJson(200, { bestillingsId: '9911' })(route);
        });

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();
        await page.getByRole('button', RETRY_BUTTON).click();

        await expectNotSent(page, 'teknisk feil');
        await expect(page.getByRole('button', { name: 'Venter…' })).toBeDisabled();
        await expect(page.getByRole('button', SEND_BUTTON)).toHaveCount(0);

        release();

        await expect(page.getByText(SUCCESS_TEXT)).toBeVisible();
        await expect(page.getByRole('heading', NOT_SENT_HEADING)).toHaveCount(0);
    });

    test('prøv igjen som feiler på nytt viser korrelasjons-ID for siste forsøk', async ({ page }) => {
        await mockInfo(page);
        const sendCalls = await mockSend(page, sequence(
            fulfillJson(500, { message: 'første feil' }),
            fulfillJson(503, { message: 'andre feil' }),
        ));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();
        await expect(page.getByText('Feilmelding: første feil')).toBeVisible();
        await page.getByRole('button', RETRY_BUTTON).click();

        await expectNotSent(page, 'andre feil');
        await expect(page.getByTestId('korrelasjons-id')).toHaveText(callIdOf(sendCalls[1]));
        await expect(page.getByRole('button', RETRY_BUTTON)).toBeEnabled();
    });

    test('prøv igjen viser at brevet allerede er distribuert ved 409', async ({ page }) => {
        await mockInfo(page);
        await mockSend(page, sequence(
            fulfillJson(500, { message: 'teknisk feil' }),
            fulfillJson(409),
        ));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();
        await page.getByRole('button', RETRY_BUTTON).click();

        await expect(page.getByText(ALREADY_DISTRIBUTED_TEXT)).toBeVisible();
        await expect(page.getByRole('heading', NOT_SENT_HEADING)).toHaveCount(0);
    });

    test('prøv igjen beholder valgt varseltype for fritekstbrev', async ({ page }) => {
        await mockInfo(page, { body: { journalpostId: ID, fritekst: true } });
        const sendCalls = await mockSend(page, sequence(
            fulfillJson(500, { message: 'teknisk feil' }),
            fulfillJson(200, { bestillingsId: '9911' }),
        ));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('radio', { name: 'Annet' }).check();
        await page.getByRole('button', SEND_BUTTON).click();
        await page.getByRole('button', RETRY_BUTTON).click();

        await expect(page.getByText(SUCCESS_TEXT)).toBeVisible();
        expect(sendCalls[1].postDataJSON()).toEqual({ journalpostId: ID, status: 'FS', distribusjonstype: 'ANNET' });
    });

    test('viser korrelasjons-ID når henting av journalpostinfo feiler', async ({ page }) => {
        const infoCalls = await mockInfo(page, { status: 500, body: { message: 'saf nede' } });

        await page.goto(`/journalpost/${ID}?status=FS`);

        await expect(page.getByRole('heading', INFO_FAILED_HEADING)).toBeVisible();
        await expect(page.getByText('Feilmelding: saf nede')).toBeVisible();
        const callIds = infoCalls.map(callIdOf);
        expect(callIds.every((id) => UUID.test(id))).toBe(true);
        await expect(page.getByTestId('korrelasjons-id')).toHaveText(callIds[callIds.length - 1]);
    });

    test('viser feilmelding når henting av journalpostinfo gir ikke-JSON', async ({ page }) => {
        await page.route(INFO_URL, (route) => route.fulfill({ status: 502, contentType: 'text/html', body: '<html>Bad Gateway</html>' }));

        await page.goto(`/journalpost/${ID}?status=FS`);

        await expect(page.getByRole('heading', INFO_FAILED_HEADING)).toBeVisible();
        await expect(page.getByText('Feilmelding: Ukjent feil (HTTP 502)')).toBeVisible();
        await expect(page.getByTestId('korrelasjons-id')).toBeVisible();
    });

    test('viser ikke korrelasjons-ID ved valideringsfeil', async ({ page }) => {
        await mockInfo(page, { body: { journalpostId: ID, fritekst: true } });
        await mockSend(page, fulfillJson(500, { message: 'teknisk feil' }));

        await page.goto(`/journalpost/${ID}?status=FS`);
        await page.getByRole('button', SEND_BUTTON).click();

        await expect(page.getByText('Varslingsmetode må fylles inn')).toBeVisible();
        await expect(page.getByTestId('korrelasjons-id')).toHaveCount(0);
    });
});
