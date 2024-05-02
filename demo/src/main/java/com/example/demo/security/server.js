/*
const express = require('express');
const cors = require('cors');

const app = express();

// CORS für alle Ursprünge aktivieren
app.use(cors());

// Middleware zum Parsen von JSON-Anfragen
app.use(express.json());

// Route für die Benutzerregistrierung
app.post('/registration', (req, res) => {
    // Hier die Logik für die Benutzerregistrierung
});

// Server starten
app.listen(8080, () => {
    console.log('Server läuft auf Port 8080');
});
*/


const express = require('express');
const cors = require('cors');

const app = express();

// CORS für alle Ursprünge aktivieren
app.use(cors());

// Middleware zum Parsen von JSON-Anfragen
app.use(express.json());

// Route für die Benutzerregistrierung
app.post('/registration', (req, res) => {
    // Hier die Logik für die Benutzerregistrierung
    // Setze die CORS-Header in der Antwort
    res.header('Access-Control-Allow-Origin', '*'); // Erlaubt Anfragen von allen Ursprüngen
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE'); // Erlaubt bestimmte HTTP-Methoden
    res.header('Access-Control-Allow-Headers', 'Content-Type'); // Erlaubt bestimmte Header in Anfragen
    // Hier kannst du deine Antwort senden
});

// Server starten
app.listen(8080, () => {
    console.log('Server läuft auf Port 8080');
});
