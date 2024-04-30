import React from 'react';
import './LoginPage.css';
import { Link  } from 'react-router-dom';



class LoginPage extends React.Component {


  /*handleSubmit = async (event) => {
    event.preventDefault();
    const { username, password } = this.state;

    try {
      const response = await fetch('http://localhost:8080/registration', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
      });

      if (!response.ok) {
        throw new Error('Anmeldung fehlgeschlagen');
      }

      // Hier kannst du die Weiterleitung oder andere Aktionen nach erfolgreicher Anmeldung durchführen
      console.log('Anmeldung erfolgreich');
    } catch (error) {
      console.error('Fehler bei der Anmeldung:', error.message);
      // Hier kannst du eine Fehlermeldung für den Benutzer anzeigen
    }
  };*/





  render() {
    return (
      <div>
        <h1>Login</h1>
        <form>
          <div>
            <label htmlFor="username">Benutzername:</label>
            <input type="text" id="username" name="username" />
          </div>
          <div>
            <label htmlFor="password">Passwort:</label>
            <input type="password" id="password" name="password" />
          </div>
          <Link to="/startseite">
          <button type="submit">Anmelden</button>
          </Link>
          </form>
          <div className="register-link"> 
            <p>Noch kein Account?</p>
          <Link to="/register">
          <button type="button">Registrieren</button>
          </Link>
          <Link to="/2fa">
          <button type="button">2FA</button>
          </Link>
        </div>
      </div>
    );
  }
}

export default LoginPage;
