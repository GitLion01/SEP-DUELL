const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');

const app = express();

// CORS für alle Ursprünge aktivieren
app.use(cors());

// Middleware zum Parsen von JSON-Anfragen
app.use(bodyParser.json());

// Route für die Benutzerregistrierung
app.post('/registration', (req, res) => {
    // Hier die Logik für die Benutzerregistrierung
});

// Server starten
app.listen(8080, () => {
    console.log('Server läuft auf Port 8080');
});
