import React, { Component } from 'react';
import './Register.css';

class Registrierung extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      profileImage: null,
      previewImage: null
    };
  }


  handleSubmit = (event) => {
    event.preventDefault();
    // Hier kannst du die Logik für die Datenverarbeitung einfügen
  };

  //HIER GHETS UM DIE VERÄNDERUNG DES BILDES UND DIE DIE BILD EINFÜGUNG
  handleImageChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        this.setState({
          profileImage: file,
          previewImage: reader.result
        });
      };
      reader.readAsDataURL(file);
    }
  };

  //HIER GEHT ES UM DIE VERÄNDERUNG DER CHECKBOX!
  handleAdminCheckboxChange = () => {
    this.setState(prevState => ({
      isAdmin: !prevState.isAdmin
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
            <label htmlFor="profilbild">Profilbild:</label>
            <input type="file" id="profilbild" name="profilbild" accept="image/*" onChange={this.handleImageChange} />
            {this.state.previewImage && (
              <img src={this.state.previewImage} alt="Preview" style={{ maxWidth: '100px', marginTop: '10px' }} />
            )}
          </div>
          {/*USERNAME LABEL & EINGABEFELD*/}
          <div className="form-group">
            <label htmlFor="username">Username:</label>
            <input type="text" id="username" name="username" required />
          </div>
          {/*VORNAME LABEL & EINGABEFELD*/}
          <div className="form-group">
            <label htmlFor="vorname">Vorname:</label>
            <input type="text" id="vorname" name="vorname" required />
          </div>

          {/*NACHNAME LABEL & EINGABEFELD*/}
          <div className="form-group">
            <label htmlFor="nachname">Nachname:</label>
            <input type="text" id="nachname" name="nachname" required />
          </div>

          {/*GEBURTSDATUM LABEL & EINGABEFELD*/}
          <div className="form-group">
            <label htmlFor="geburtsdatum">Geburtsdatum:</label>
            <input type="date" id="geburtsdatum" name="geburtsdatum" required />
          </div>

          {/*EMAIL LABEL & EINGABEFELD*/}
          <div className="form-group">
            <label htmlFor="email">E-Mail:</label>
            <input type="email" id="email" name="email" required />
          </div>

          {/*PASSWORT LABEL & EINGABEFELD*/}
          <div className="form-group">
            <label htmlFor="passwort">Passwort:</label>
            <input type="password" id="passwort" name="passwort" required />
          </div>

          {/*ADMIN LABEL & CHECKBOX*/}
          <div className="form-group">
          <label htmlFor="adminCheckbox">Admin?</label>
            <input type="checkbox" id="adminCheckbox" checked={this.state.isAdmin} onChange={this.handleAdminCheckboxChange} />
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
