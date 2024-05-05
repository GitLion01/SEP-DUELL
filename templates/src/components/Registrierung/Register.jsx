import React, { Component } from 'react'; // Importiere React und Component aus der 'react'-Bibliothek
import './Register.css'; // Importiere das Styling für diese Komponente




// Definiere die Register-Komponente und render die Registrierung-Komponente
class Register extends Component {
  render() { // Definiere die render-Methode
    return <Registrierung />; // Rendere die Registrierung-Komponente innerhalb von Register
  }
}




// Deklariere die Registrierungskomponente
//
class Registrierung extends React.Component {
  constructor(props) { // Konstruktor für die Registrierungskomponente 
    //props sind die Eigenschaften, die der Komponente übergeben werden können, wenn sie verwendet wird.
    super(props); // Aufruf des Konstruktors der Elternklasse
    this.state = { // Setze den Anfangszustand der Komponente fest 
      // Der Zustand (state) ist ein Objekt, das die Daten enthält, die die Komponente verwalten soll
      //state) ist ein zentrales Konzept in React und wird verwendet, um Daten zu speichern, 
        //die sich im Laufe der Zeit ändern können
          // und die das Aussehen und Verhalten der Benutzeroberfläche beeinflussen
      image: null, // Das ausgewählte Bild
      previewImage: null, // Die Vorschau des ausgewählten Bildes
      formData: { // Formulardaten
        username: '', // Benutzername
        firstName: '', // Vorname
        lastName: '', // Nachname
        dateOfBirth: '', // Geburtsdatum
        email: '', // E-Mail
        password: '', // Passwort
        role: "" // Rolle (Administrator oder Benutzer)
      },
    };
  }

  // Methode zum Verarbeiten des Formularabsendens
  handleSubmit = async (event) => { // Methode für das Absenden des Formulars
    event.preventDefault(); // Verhindere das Standardverhalten des Formulars
    const formData = this.state.formData; // Greife auf die Formulardaten im Zustand zu
    try {
      formData.role = this.state.role ? 'ADMIN' : 'USER'; // Setze die Rolle basierend auf dem Checkbox-Zustand
      const response = await fetch('http://localhost:8080/registration', { // Sende die Formulardaten an den Server
        method: 'POST', // HTTP-Methode
        headers: {
          'Content-Type': 'application/json' // Header für JSON-Daten
        },
        body: JSON.stringify(formData) // Formulardaten in JSON umwandeln und senden
      });

      if (!response.ok) { // Wenn die Antwort nicht erfolgreich ist, wirf einen Fehler
        throw new Error('Registrierung fehlgeschlagen');
      }

      console.log('Registrierung erfolgreich'); // Logge eine Erfolgsmeldung
      // Hier könntest du zur Login-Seite weiterleiten oder andere Aktionen nach erfolgreicher Registrierung durchführen
    } catch (error) {
      console.error('Fehler bei der Registrierung:', error.message); // Logge Fehlermeldungen
    }
  };

  // Methode zur Verarbeitung der Bildauswahl und Anzeige der Vorschau
  handleImageChange = (event) => { // Methode für die Änderung des Bildes
    const file = event.target.files[0]; // Das ausgewählte Bild
    if (file) {
      const reader = new FileReader(); // FileReader-Objekt zum Lesen von Dateien
      reader.onloadend = () => { // Wenn das Lesen abgeschlossen ist
        this.setState({
          image: file, // Setze das ausgewählte Bild
          previewImage: reader.result // Setze die Vorschau des ausgewählten Bildes
        });
      };
      reader.readAsDataURL(file); // Lese das ausgewählte Bild als Data-URL
    }
  };

  // Methode zur Verarbeitung der Änderung der Checkbox
  handleRoleChange = () => { // Methode für die Änderung der Rolle
    this.setState(prevState => ({
        ...prevState.formData, // Aktualisiere den vorherigen Zustand der Formulardaten
        role: prevState.formData.role === 'ADMIN' ? 'USER' : 'ADMIN' // Ändere die Rolle
    }));
  };

  // Methode zur Verarbeitung der Änderung von Formularelementen
  handleChange = (event) => { // Methode für die Änderung von Formularelementen
    const { name, value } = event.target; // Extrahiere Name und Wert des Elements
    this.setState(prevState => ({
      formData: {
        ...prevState.formData, // Aktualisiere den vorherigen Zustand der Formulardaten
        [name]: value // Setze den neuen Wert für das entsprechende Formularelement
      }
    }));
  };

  render() { // Methode zum Rendern der JSX
    return (
        /* Hauptcontainer für die Registrierung */
        <div className="container">
          {/* Überschrift für die Registrierung */}
          <h2>REGISTRIERUNG</h2>
          <form onSubmit={this.handleSubmit}>

            {/* Profilbildauswahl */}
            <div className="form-group">
              <label htmlFor="image">Profilbild:</label>
              <input type="file" id="image" name="image" accept="image/*" onChange={this.handleImageChange} />
              {this.state.previewImage && (
                  <img src={this.state.previewImage} alt="Preview" style={{ width: '100px', marginTop: '10px', height: '100px', borderRadius: '100%' }} />
              )}
            </div>

            {/* Benutzername */}
            <div className="form-group">
              <label htmlFor="username">Username:</label>
              <input type="text" id="username" name="username" required  onChange={this.handleChange}/>
            </div>

            {/* Vorname */}
            <div className="form-group">
              <label htmlFor="firstName">Vorname:</label>
              <input type="text" id="firstName" name="firstName" required onChange={this.handleChange}/>
            </div>

            {/* Nachname */}
            <div className="form-group">
              <label htmlFor="lastName">Nachname:</label>
              <input type="text" id="lastName" name="lastName" required onChange={this.handleChange}/>
            </div>

            {/* Geburtsdatum */}
            <div className="form-group">
              <label htmlFor="dateOfBirth">Geburtsdatum:</label>
              <input type="date" id="dateOfBirth" name="dateOfBirth" required onChange={this.handleChange}/>
            </div>

            {/* E-Mail */}
            <div className="form-group">
              <label htmlFor="email">E-Mail:</label>
              <input type="email" id="email" name="email" required onChange={this.handleChange}/>
            </div>

            {/* Passwort */}
            <div className="form-group">
              <label htmlFor="password">Passwort:</label>
              <input type="password" id="password" name="password" onChange={this.handleChange}/>
            </div>

            {/* Admin-Checkbox */}
            <div  className="form-group">
              <label htmlFor="role">Admin?</label>
              <input type="checkbox" id="role" checked={this.state.role} onChange={this.handleRoleChange} />
            </div>

            {/* Submit-Button */}
            <button type="submit">Registrieren</button>
          </form>
        </div>
    );
  }
}

export default Register; // Exportiere die Register-Komponente
