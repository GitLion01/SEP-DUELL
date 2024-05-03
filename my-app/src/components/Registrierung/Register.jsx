import React, { Component } from 'react';
import './Register.css';

class Registrierung extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      image: null,
      previewImage: null,
      formData: { // Neues formData-Objekt im state
        username: '',
        firstName: '',
        lastName: '',
        dateOfBirth: '',
        email: '',
        password: '', // Passwortfeld im formData hinzugefügt
        role: ""
      },
    };
  }


  handleSubmit = async (event) => {
    event.preventDefault();
    const formData = this.state.formData;
    // Hier kannst du die Logik für die Datenverarbeitung einfügen
    try {

      formData.role = this.state.role ? 'ADMIN' : 'USER';
      const response = await fetch('http://localhost:8080/registration', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
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

  //HIER GHETS UM DIE VERÄNDERUNG DES BILDES UND DIE DIE BILD EINFÜGUNG
  handleImageChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        this.setState({
          image: file,
          previewImage: reader.result
        });
      };
      reader.readAsDataURL(file);
    }
  };

  //HIER GEHT ES UM DIE VERÄNDERUNG DER CHECKBOX!
  //HIER GEHT ES UM DIE VERÄNDERUNG DER CHECKBOX!
  handleRoleChange = () => {
    this.setState(prevState => ({

        ...prevState.formData,
        role: prevState.formData.role === 'ADMIN' ? 'USER' : 'ADMIN'

    }));
  };





  handleChange = (event) => {
    const { name, value } = event.target;
    this.setState(prevState => ({
      formData: {
        ...prevState.formData,
        [name]: value
      }
    }));
  };




  render() {
    return (

        /*HIER WIRD DER HAUPT CONTAINER DER REGISTRIERUNG ERSTELLT*/

        <div className="container">

          {/* ÜBERSCHRIFT  CONTAINER*/}
          <h2>REGISTRIERUNG</h2>
          <form onSubmit={this.handleSubmit}>

            { /*PROFILBILD LABEL & EINGABEFELD & AUSSEHEN */}
            <div className="form-group">
              <label htmlFor="image">Profilbild:</label>
              <input type="file" id="image" name="image" accept="image/*" onChange={this.handleImageChange} />
              {this.state.previewImage && (
                  <img src={this.state.previewImage} alt="Preview" style={{ width: '100px', marginTop: '10px', height: '100px', borderRadius: '100%' }} />
              )}
            </div>
            {/*USERNAME LABEL & EINGABEFELD*/}
            <div className="form-group">
              <label htmlFor="username">Username:</label>
              <input type="text" id="username" name="username" required  onChange={this.handleChange}/>
            </div>
            {/*VORNAME LABEL & EINGABEFELD*/}
            <div className="form-group">
              <label htmlFor="firstName">Vorname:</label>
              <input type="text" id="firstName" name="firstName" required onChange={this.handleChange}/>
            </div>

            {/*NACHNAME LABEL & EINGABEFELD*/}
            <div className="form-group">
              <label htmlFor="lastName">Nachname:</label>
              <input type="text" id="lastName" name="lastName" required onChange={this.handleChange}/>
            </div>

            {/*GEBURTSDATUM LABEL & EINGABEFELD*/}
            <div className="form-group">
              <label htmlFor="dateOfBirth">Geburtsdatum:</label>
              <input type="date" id="dateOfBirth" name="dateOfBirth" required onChange={this.handleChange}/>
            </div>

            {/*EMAIL LABEL & EINGABEFELD*/}
            <div className="form-group">
              <label htmlFor="email">E-Mail:</label>
              <input type="email" id="email" name="email" required onChange={this.handleChange}/>
            </div>

            {/*PASSWORT LABEL & EINGABEFELD*/}
            <div className="form-group">
              <label htmlFor="password">Passwort:</label>
              <input type="password" id="password" name="password" onChange={this.handleChange}/>
            </div>

            {/*ADMIN LABEL & CHECKBOX*/}
            <div className="form-group">
              <label htmlFor="role">Admin?</label>
              <input type="checkbox" id="role" checked={this.state.role} onChange={this.handleRoleChange} />
            </div>


            {/* BUTTON AM ENDE DER SEITE*/}
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