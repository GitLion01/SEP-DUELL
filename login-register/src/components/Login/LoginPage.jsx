import React from 'react';
import './LoginPage.css';
import { Link  } from 'react-router-dom';



class LoginPage extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      username: "", // Initialisieren Sie den Zustand mit leeren Zeichenketten
      password: '' // Initialisieren Sie den Zustand mit leeren Zeichenketten
    };
  }

  handleChange = (event) => {
    this.setState({
      [event.target.name]: event.target.value
    });
  };



  handleSubmit = async (event) => {
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
  };





  render() {
    return (
        <div>
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
            <Link to="/2fa">
              <button type="button">2FA</button>
            </Link>
          </div>
        </div>
    );
  }
}

export default LoginPage;
