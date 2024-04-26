import React from 'react';
import './LoginPage.css';
import { Link  } from 'react-router-dom';


class LoginPage extends React.Component {
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
          <button type="submit">Anmelden</button>
          </form>
          <div className="register-link"> 
            <p>Noch kein Account?</p>
          <Link to="/register">
          <button type="button">Registrieren</button>
          </Link>
        </div>
      </div>
    );
  }
}

export default LoginPage;
