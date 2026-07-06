// Enkel testserver for e2e-tester: serverer det bygde produksjonsbundlet
// (src/main/resources/static) med SPA-fallback for React Router, og et
// mocket /api-endepunkt slik at frontend kan testes uten et ekte backend.
const express = require('express');
const path = require('path');

const staticDir = path.resolve(__dirname, '..', 'src', 'main', 'resources', 'static');
const port = process.env.PORT || 8092;

const app = express();

app.get('/api/journalpost/:id', (req, res) => {
    res.status(200).json({ status: 'OK', journalpostId: req.params.id, fritekst: true });
});

app.use(express.static(staticDir));

// SPA-fallback: alle ukjente ruter skal serveres index.html slik at
// React Router kan håndtere klientsiderouting.
app.use((req, res) => {
    res.sendFile(path.join(staticDir, 'index.html'));
});

app.listen(port, () => console.log(`e2e test server listening on port ${port}`));
