import React from 'react'; // Importieren der React-Bibliothek
import './LoginPage.css'; // Importieren der CSS-Datei für die Stilisierung der Login-Seite
import { Link, Navigate } from 'react-router-dom'; // Importieren von Link und Navigate für die Navigation
import axiosInstance from '../../api/axios'; // Importieren der Axios-Instanz für die Kommunikation mit dem Server

class LoginPage extends React.Component { // Definieren der LoginPage-Klasse als eine React-Komponente
  constructor(props) { // Konstruktor, der den initialen Zustand setzt
    super(props); // Aufruf des Konstruktors der übergeordneten Klasse
    this.state = { // Initialisierung des Zustands
      username: "", // Initialisieren Sie den Zustand mit leeren Zeichenketten für den Benutzernamen
      password: '', // Initialisieren Sie den Zustand mit leeren Zeichenketten für das Passwort
      redirectToHome: false // Initialisieren Sie den Zustand für die Weiterleitung auf false
    };
  }

  handleChange = (event) => { // Methode zum Aktualisieren des Zustands bei Änderungen in den Eingabefeldern
    this.setState({ // Setzen des Zustands mit dem neuen Wert aus dem Eingabefeld
      [event.target.name]: event.target.value
    });
  };

  handleSubmit = async (event) => { // Methode zum Bearbeiten des Formulars beim Einreichen
    event.preventDefault(); // Verhindern des Standardverhaltens des Formulars
    const { username, password } = this.state; // Extrahieren von Benutzername und Passwort aus dem Zustand

    try { // Versuch, die Anmeldung durchzuführen
      const response = await axiosInstance.post('/registration', { // Senden der Anmeldeinformationen an den Server
        username, password
      });

      if (!response.ok) { // Überprüfen der Antwort des Servers
        throw new Error('Anmeldung fehlgeschlagen'); // Fehlermeldung, wenn die Anmeldung fehlgeschlagen ist
      }

      console.log('Anmeldung erfolgreich'); // Ausgabe in der Konsole, wenn die Anmeldung erfolgreich war

      // Redirect durch Navigate ersetzen
      this.setState({ redirectToHome: true }); // Setzen des Zustands für die Weiterleitung auf true

    } catch (error) { // Fangen von Fehlern
      console.error('Fehler bei der Anmeldung:', error.message); // Hier wird eine Fehlermeldung in der Konsole ausgegeben, die besagt, dass ein Fehler bei der Anmeldung aufgetreten ist. Die `error.message` enthält die spezifische Fehlermeldung, die vom Server zurückgegeben wurde.
      if (error.message === 'Anmeldung fehlgeschlagen') { // Überprüfen, ob der Fehler eine fehlgeschlagene Anmeldung war
        alert('Falscher Benutzername oder Passwort'); // Anzeigen einer Benachrichtigung für den Benutzer
      }
      // Hier kannst du eine Fehlermeldung für den Benutzer anzeigen
    }
  };

  render() {
    // Redirect durch Navigate ersetzen
    if (this.state.redirectToHome) {
      return <Navigate to="/2FA/TwoFactorAuthentication" />;
    }


    return (
      <body className='login__body'>
      <div className='login'>
        <h1>Login</h1>
        <form onSubmit={this.handleSubmit}>
          <div>
            <label htmlFor="username">Benutzername:</label>
            <input
              type="text"
              id="username"
              name="username"
              value={this.state.username}
              onChange={this.handleChange}
            />
          </div>
          <div>
            <label htmlFor="password">Passwort:</label>
            <input
              type="password"
              id="password"
              name="password"
              value={this.state.password}
              onChange={this.handleChange}
            />
          </div>
          <button type="submit">Anmelden</button>
        </form>
        <div className="register-link">
          <p>Noch kein Account?</p>
          <Link to="/registration">
            <button type="button">Registrieren</button>
          </Link>
        </div>
      </div>
      </body>
    );
  }
}

export default LoginPage;