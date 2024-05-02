import React, { Component } from 'react';
import './Register.css';

// Define the Registrierung component separately
class Registrierung extends React.Component {


  handleSubmit = async (event) => {
    event.preventDefault();
    const formData = new FormData(event.target);
    const requestData = {
      firstName: formData.get("firstName"),
      lastName: formData.get("lastName"),
      dateOfBirth: formData.get("dateOfBirth"),
      email: formData.get("email"),
      password: formData.get("password")
    };

    try {
      const response = await fetch('http://localhost:8080/registration', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'

        },
        body: JSON.stringify(requestData)
      });

      if (!response.ok) {
        throw new Error('Registrierung fehlgeschlagen');
      }

      console.log('Registrierung erfolgreich');
      // Hier könntest du zur Login-Seite weiterleiten oder andere Aktionen nach erfolgreicher Registrierung durchführen
    } catch (error) {
      console.error('Fehler bei der Registrierung:', error.message);
    }
  };

  render() {
    return (
      <div className="container">
        <h2>Registrierung</h2>
        <form onSubmit={this.handleSubmit}>
          <div className="form-group">
            <label htmlFor="firstName">Vorname:</label>
            <input type="text" id="firstName" name="firstName" required />
          </div>
          <div className="form-group">
            <label htmlFor="lastName">Nachname:</label>
            <input type="text" id="lastName" name="lastName" required />
          </div>
          <div className="form-group">
            <label htmlFor="dateOfBirth">Geburtsdatum:</label>
            <input type="date" id="dateOfBirth" name="dateOfBirth" required />
          </div>
          <div className="form-group">
            <label htmlFor="email">E-Mail:</label>
            <input type="email" id="email" name="email" required />
          </div>
          <div className="form-group">
            <label htmlFor="password">Passwort:</label>
            <input type="password" id="password" name="password" required />
          </div>
          <button type="submit">Registrieren</button>
        </form>
      </div>
    );
  }
}

// Define the Register component and render the Registrierung component
class Register extends Component {
  render() {
    return <Registrierung />;
  }
}

export default Register;

