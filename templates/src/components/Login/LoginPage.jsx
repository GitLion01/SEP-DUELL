import React from 'react'; // Importieren der React-Bibliothek
import './LoginPage.css'; // Importieren der CSS-Datei für die Stilisierung der Login-Seite
import { Link, Navigate } from 'react-router-dom'; // Importieren von Link und Navigate für die Navigation
import axiosInstance from '../../api/axios'; // Importieren der Axios-Instanz für die Kommunikation mit dem Server
import axios from 'axios';

class LoginPage extends React.Component { // Definieren der LoginPage-Klasse als eine React-Komponente
  constructor(props) { // Konstruktor, der den initialen Zustand setzt
    super(props); // Aufruf des Konstruktors der übergeordneten Klasse
    this.state = { // Initialisierung des Zustands
      email: "", // Initialisieren Sie den Zustand mit leeren Zeichenketten für den Benutzernamen
      password: '', // Initialisieren Sie den Zustand mit leeren Zeichenketten für das Passwort
      redirectToHome: false, // Initialisieren Sie den Zustand für die Weiterleitung auf false
      error:''
    };
  }

  handleChange = (event) => { // Methode zum Aktualisieren des Zustands bei Änderungen in den Eingabefeldern
    this.setState({ // Setzen des Zustands mit dem neuen Wert aus dem Eingabefeld
      [event.target.name]: event.target.value
    });
  };

    handleSubmit = async (event) => {
        event.preventDefault();
        const { email, password } = this.state;
        axios.post('http://localhost:8080/login', { email, password })
            .then(res => {
                console.log(res);
                if (res.data && res.data.status === 'success') {
                    console.log("Login successful!");
                    // Benutzer-ID speichern
                    localStorage.setItem('userId', res.data.userId);
                    this.setState({ redirectToHome: true });
                } else if (res.data && res.data.status === 'error') {
                    this.setState({ error: res.data.message });
                    alert('Falsche Email oder Passwort')
                } else {
                    console.error("Unerwartete Serverantwort:", res);
                    alert('Unerwartete Serverantwort');
                }
            });
    };


  render() {
    // Redirect durch Navigate ersetzen
    if (this.state.redirectToHome) {
      return <Navigate to="/2fa" />;
    }

    return (

        <div className='login'>
          <h1>Login</h1>
          <form onSubmit={this.handleSubmit}>
            <div>
              <label htmlFor="email">Benutzername:</label>
              <input
                  type="text"
                  id="email"
                  name="email"
                  value={this.state.email}
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
    );
  }
}
document.body.classList.add('login__body');

export default LoginPage;