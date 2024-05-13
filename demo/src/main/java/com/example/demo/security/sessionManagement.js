const session = require('express-session');

class SessionManagement {
    constructor() {
        this.sessionConfig = {
            secret: 'geheimesGeheimnis', // Geheimnis zur Signierung der Session-Cookies
            resave: false, // Sessions nicht erneut speichern, wenn sie nicht geändert wurden
            saveUninitialized: false // Keine leeren Sessions speichern
        };
    }

    configureSessionMiddleware(app) {
        app.use(session(this.sessionConfig));
    }
}

module.exports = SessionManagement;
