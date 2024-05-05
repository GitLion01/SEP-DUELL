import React from 'react';
import './LoginPage.css';
import { Link, Navigate } from 'react-router-dom';
import axiosInstance from '../../api/axios'; // Richtiges Importieren der Axios-Klasse


class LoginPage extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      username: "", // Initialisieren Sie den Zustand mit leeren Zeichenketten
      password: '', // Initialisieren Sie den Zustand mit leeren Zeichenketten
      redirectToHome: false
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
      const response = await axiosInstance.post('/registration', {
        username, password
      });

      if (!response.ok) {
        throw new Error('Anmeldung fehlgeschlagen');
      }

      // Hier kannst du die Weiterleitung oder andere Aktionen nach erfolgreicher Anmeldung durchführen
      console.log('Anmeldung erfolgreich');

      // Redirect durch Navigate ersetzen
      this.setState({ redirectToHome: true });

    } catch (error) {
      console.error('Fehler bei der Anmeldung:', error.message);
      if (error.message === 'Anmeldung fehlgeschlagen') {
        alert('Falscher Benutzername oder Passwort')
      }
      // Hier kannst du eine Fehlermeldung für den Benutzer anzeigen
    }
  };

  render() {
    // Redirect durch Navigate ersetzen
    if (this.state.redirectToHome) {
      return <Navigate to="/2FA/TwoFactorAuthentication" />;
    }

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