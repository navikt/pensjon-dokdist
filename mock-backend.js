const express = require('express');

const app = express();

app.get('/api/internal/isAlive', (req, res) => res.send('isAlive'));
app.get('/api/internal/isReady', (req, res) => res.send('isReady'));
app.post('/api/journalpost/:id/send', (req, res) => setTimeout(() => {
    if (Math.random() < 0.5) {
        res.status(200).json({ status: 'OK '});
    } else {
        res.status(500).json({ message: 'This error was intentionally triggered by the mock backend, to test failure situations.' });
    }
}, 600));

const port = 8081;
app.listen(port, () => console.log(`Mock backend listening on port ${port}`));
