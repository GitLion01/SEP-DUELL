import React, { useState } from 'react';
import './Register.css';

function Register() {
  return <Registrierung />;
}

function Registrierung() {
  const [formData, setFormData] = useState({
    username: '',
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    email: '',
    password: '',
    role: false
  });
  const [ setImage] = useState(null);
 // const [image, setImage] = useState(null);
  const [previewImage, setPreviewImage] = useState(null);
  const [registrationSuccess, setRegistrationSuccess] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      formData.role = formData.role ? 'ADMIN' : 'USER';
      // Your form submission logic
      console.log('Registrierung erfolgreich');
      // Set registration success to true
      setRegistrationSuccess(true);
    } catch (error) {
      console.error('Fehler bei der Registrierung:', error.message);
    }
  };

  const handleImageChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImage(file);
        setPreviewImage(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleRoleChange = () => {
    setFormData(prevState => ({
      ...prevState,
      role: !prevState.role
    }));
  };

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData(prevState => ({
      ...prevState,
      [name]: value
    }));
  };

  if (registrationSuccess) {
    return (
      <div>
        <p>Registration successful. Redirecting to login page...</p>
        {/* You can add a spinner or loading indicator here */}
        {setTimeout(() => {
          window.location.href = '/'; // Redirect to login page
        }, 2000)} {/* Redirect after 2 seconds */}
      </div>
    );
  }

  return (
    <div className="container">
      <h2>REGISTRIERUNG</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="image">Profilbild:</label>
          <input type="file" id="image" name="image" accept="image/*" onChange={handleImageChange} />
          {previewImage && (
            <img src={previewImage} alt="Preview" style={{ width: '100px', marginTop: '10px', height: '100px', borderRadius: '100%' }} />
          )}
        </div>
        <div className="form-group">
          <label htmlFor="username">Username:</label>
          <input type="text" id="username" name="username" required onChange={handleChange} />
        </div>
        <div className="form-group">
          <label htmlFor="firstName">Vorname:</label>
          <input type="text" id="firstName" name="firstName" required onChange={handleChange} />
        </div>
        <div className="form-group">
          <label htmlFor="lastName">Nachname:</label>
          <input type="text" id="lastName" name="lastName" required onChange={handleChange} />
        </div>
        <div className="form-group">
          <label htmlFor="dateOfBirth">Geburtsdatum:</label>
          <input type="date" id="dateOfBirth" name="dateOfBirth" required onChange={handleChange} />
        </div>
        <div className="form-group">
          <label htmlFor="email">E-Mail:</label>
          <input type="email" id="email" name="email" required onChange={handleChange} />
        </div>
        <div className="form-group">
          <label htmlFor="password">Passwort:</label>
          <input type="password" id="password" name="password" onChange={handleChange} />
        </div>
        <div className="form-group">
          <label htmlFor="role">Admin?</label>
          <input
            type="checkbox"
            id="role"
            checked={formData.role}
            onChange={handleRoleChange}
          />
        </div>
        <button type="submit">Registrieren</button>
      </form>
    </div>
  );
}

export default Register;