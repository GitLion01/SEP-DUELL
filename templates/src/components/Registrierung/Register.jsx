import React, { Component } from 'react';
import './Register.css';


// Definiere die Register-Komponente und render die Registrierung-Komponente
class Register extends Component {
  render() { // Definiere die render-Methode
    return <Registrierung />; // Rendere die Registrierung-Komponente innerhalb von Register
  }
}



class Registrierung extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      image: null,
      previewImage: null,
      formData: {
        username: '',
        firstName: '',
        lastName: '',
        dateOfBirth: '',
        email: '',
        password: '',
        role: false
      },
     role: false,
    };

    this.handleImageChange = this.handleImageChange.bind(this);
    this.handleRoleChange = this.handleRoleChange.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  // Methode zum Verarbeiten des Formularabsendens
  // Methode zum Verarbeiten des Formularabsendens
  /*handleSubmit = async (event) => { // Methode für das Absenden des Formulars
    event.preventDefault(); // Verhindere das Standardverhalten des Formulars
    const formData = this.state.formData; // Greife auf die Formulardaten im Zustand zu
    try {
      formData.role = this.state.role ? 'ADMIN' : 'USER'; // Setze die Rolle basierend auf dem Checkbox-Zustand
      const response = await fetch('http://localhost:8080/registration', { // Sende die Formulardaten an den Server
        method: 'POST', // HTTP-Methode
        headers: {
          'Content-Type': 'multipart/form-data' // Header für JSON-Daten
        },
        body: JSON.stringify(formData) // Formulardaten in JSON umwandeln und senden
      });


      if (!response.ok) { // Wenn die Antwort nicht erfolgreich ist, wirf einen Fehler
        throw new Error('Registrierung fehlgeschlagen');
      }


      window.location.href = '/';
      console.log('Registrierung erfolgreich'); // Logge eine Erfolgsmeldung
      // Hier könntest du zur Login-Seite weiterleiten oder andere Aktionen nach erfolgreicher Registrierung durchführen
    } catch (error) {
      console.error('Fehler bei der Registrierung:', error.message); // Logge Fehlermeldungen
    }
  };
*/

  handleSubmit = async (event) => {
    event.preventDefault();
    const formData = new FormData();

    // Append form data fields to the FormData object
    Object.entries(this.state.formData).forEach(([key, value]) => {
      // Konvertiere das Datumfeld in ein String-Format
      if (key === 'dateOfBirth' && value instanceof Date) {
        formData.append(key, value.toISOString().split('T')[0]);
      } else {
        formData.append(key, value);
      }
    });

    /*// Append the role separately
    formData.append('role', this.state.role ? 'ADMIN' : 'USER');*/
    // Rolle separat hinzufügen
    formData.append('role', this.state.role ? 'ADMIN' : 'USER');

    // Append image file if available
    if (this.state.image) {
      formData.append('image', this.state.image);
    }

    try {
      const response = await fetch('http://localhost:8080/registration', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error('Registrierung fehlgeschlagen');
      }

      window.location.href = '/';
      console.log('Registrierung erfolgreich');
    } catch (error) {
      console.error('Fehler bei der Registrierung:', error.message);
      alert('Fehler bei der Registrierung')
    }
  };






  handleImageChange(event) {
    const file = event.target.files[0];
    
    if (file.size >1048576) { // 1MB Limit
      alert('Die Datei ist zu groß. Die maximale Dateigröße beträgt 1MB. ');
      return
    }

    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        this.setState({
          image: file,
          previewImage: reader.result
        });
      };
      reader.readAsDataURL(file); // Lese das ausgewählte Bild als Data-URL
    }
  }


  /*handleRoleChange = () => {
    this.setState(prevState => ({
      role: !prevState.role,
      formData: {
        ...prevState.formData,
        role: !prevState.role ? 'ADMIN' : 'USER' // Setze die Rolle explizit als Zeichenfolge
      },
    }));
  };*/
  handleRoleChange = () => {
    this.setState(prevState => ({
      role: !prevState.role,
      formData: {
        ...prevState.formData,
        role: !prevState.role // Aktualisiere role im formData-Objekt mit dem neuen booleschen Wert
      },
    }));
  };




  handleChange(event) {
    const { name, value } = event.target;
    this.setState(prevState => ({
      formData: {
        ...prevState.formData,
        [name]: value
      }
    }));
  }

  /*handleSubmit = async (event) => {
    event.preventDefault();
    const formData = new FormData(); // Create a FormData object

    // Append form data to the FormData object
    Object.entries(this.state.formData).forEach(([key, value]) => {
      formData.append(key, value);
    });

    formData.append('role', this.state.role ? 'ADMIN' : 'USER'); // Append role separately

    // Append image file if available
    if (this.state.image) {
      formData.append('image', this.state.image);
    }

    try {
      const response = await fetch('http://localhost:8080/registration', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error('Registration failed');
      }

      window.location.href = '/';
      console.log('Registration successful');
    } catch (error) {
      console.error('Error during registration:', error.message);
    }
  };
*/


  render() {
    return (
        <div className="AppRegister">
          <h2>REGISTRATION</h2>
          <form onSubmit={this.handleSubmit}>
            <div className="form-group">
              <label htmlFor="image">Profile Picture:</label>
              <input type="file" id="image" name="image" accept="image/*" onChange={this.handleImageChange} />
              {this.state.previewImage && (
                  <img src={this.state.previewImage} alt="Preview" style={{ width: '100px', marginTop: '10px', height: '100px', borderRadius: '100%' }} />
              )}
            </div>

            <div className="form-group">
              <label htmlFor="username">Username:</label>
              <input type="text" id="username" name="username" required onChange={this.handleChange} />
            </div>

            <div className="form-group">
              <label htmlFor="firstName">First Name:</label>
              <input type="text" id="firstName" name="firstName" required onChange={this.handleChange} />
            </div>

            <div className="form-group">
              <label htmlFor="lastName">Last Name:</label>
              <input type="text" id="lastName" name="lastName" required onChange={this.handleChange} />
            </div>

            <div className="form-group">
              <label htmlFor="dateOfBirth">Date of Birth:</label>
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
              <input type="password" id="password" name="password"  required onChange={this.handleChange}/>
            </div>

            {/* Admin-Checkbox */}
            <div  className="form-group">
              <label htmlFor="role">Admin?</label>
              <input
                  type="checkbox"
                  id="role"
                  checked={this.state.role}
                  onChange={this.handleRoleChange}
              />
            </div>

            {/* Submit-Button */}
            <button type="submit">Registrieren</button>
          </form>
        </div>
    );
  }
}

export default Register; // Exportiere die Register-Komponente