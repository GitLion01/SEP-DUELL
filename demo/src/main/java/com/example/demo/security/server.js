/*


const express = require('express');
const cors = require('cors');

const app = express();


app.use(cors());


app.use(express.json());


app.post("*", (req, res) => {

    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE');
    res.header('Access-Control-Allow-Headers', 'Content-Type'); //

});


app.listen(8080, () => {
    console.log('Server läuft auf Port 8080');
});*/

/*const express = require('express');
const cors = require('cors');
const session = require('express-session');
const SessionManagement = require('./SessionManagement');
const {join} = require("node:path");

const app = express();
const sessionManagement = new SessionManagement();

app.use(cors());
app.use(express.json());

sessionManagement.configureSessionMiddleware(app);

app.post("*", (req, res) => {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE');
    res.header('Access-Control-Allow-Headers', 'Content-Type');
});

app.get('/startseite', (req, res) => {
    if (isAuthenticated(req)) {
        res.sendFile(join(__dirname, 'public', 'startseite.html')); // Beispiel: Rendern der Startseite aus einer HTML-Datei
    } else {
        res.status(401).send('Unauthorized'); // Wenn der Benutzer nicht authentifiziert ist, sende einen 401-Statuscode
    }
});

app.listen(8080, () => {
    console.log('Server läuft auf Port 8080');
});*/

// Importiere die erforderlichen Pakete
const express = require('express');
const jwt = require('jsonwebtoken');
const path = require('path');

// Erstelle eine Instanz der Express-Anwendung
const app = express();

// Geheimer Schlüssel für die Signatur des Tokens (kann auch in einer Umgebungsvariablen gespeichert werden)
const secretKey = 'dein_geheimer_schluessel';

// Middleware zum Überprüfen des JWT und Extrahieren der Benutzerdaten
function isAuthenticated(req, res, next) {
    // Hole das Token aus dem Header, der Anfrageparameter oder dem Cookie
    const token = req.headers.authorization ? req.headers.authorization.split(' ')[1] : null;
    if (!token) {
        return res.status(401).send('Unauthorized'); // Wenn kein Token vorhanden ist, sende einen 401-Statuscode
    }

    // Überprüfe das Token und extrahiere die Benutzerdaten
    jwt.verify(token, secretKey, (err, decoded) => {
        if (err) {
            return res.status(401).send('Unauthorized'); // Wenn das Token ungültig ist, sende einen 401-Statuscode
        } else {
            // Füge die Benutzerdaten zum Request-Objekt hinzu, damit sie in den nachfolgenden Routen verfügbar sind
            req.user = decoded;
            next(); // Rufe die nächste Middleware oder die Route auf, wenn die Authentifizierung erfolgreich ist
        }
    });
}

// Beispielroute, die isAuthenticated verwendet
app.get('/startseite', isAuthenticated, (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'startseite.html')); // Beispiel: Rendern der Startseite aus einer HTML-Datei
});

// Starte den Server und lausche auf einem bestimmten Port
const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
    console.log(`Server läuft auf Port ${PORT}`);
});



