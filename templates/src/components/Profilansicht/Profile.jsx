import React, { useState, useEffect } from 'react';
import {Link, useParams} from 'react-router-dom';
import Axios from 'axios';
import './Profile.css';

// Profil-Seite

function Profile() {
  const { id} = useParams();
  let [image, setProfilePicture] = useState('');
  const [username, setUsername] = useState('');
  const [firstName, setVorname] = useState('');
  const [lastName, setNachname] = useState('');
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('');
  const [dateOfBirth, setDateOfBirth] = useState('');
  const [sepCoins, setSepcoins] = useState('');
  const [leaderboardPoints, setLeaderboardPunkte] = useState('');
  const [role, setRole] =useState('');
  const [error, setError] = useState(null);
  let [userdata, setUserdata] = useState('');
  
  // Benutzerdaten von der API abrufen
  useEffect(() => {
    Axios.get(`http://localhost:8080/profile/${id}`)
      .then(response => {
        const userData = response.data;
        setUserdata(response.data);
        setProfilePicture(userData.image);
        setUsername(userData.username);
        setVorname(userData.firstName);
        setNachname(userData.lastName);
        setEmail(userData.email);
        setPassword(userData.password);
        setDateOfBirth(userData.dateOfBirth);
        setRole(userData.role);
        if (userData.role !== 'admin') {
          setSepcoins(userData.sepCoins);
          setLeaderboardPunkte(userData.leaderboardPoints);
        }
      })
      .catch(error => {
        console.error(error);
        setError('Ein Fehler ist aufgetreten')
      });
  }, []);


  // Update SEP-Coins wenn der Wert von SEP-Coins geändert wird
  useEffect(() => {
    if (role !== 'admin') {
      setSepcoins(userdata.sepCoins);
    }
  }, [role, userdata.sepCoins]);

  // Update Leaderboard-Punkte wenn der Wert von Leaderboard-Punkte geändert wird
  useEffect(() => {
    if (role !== 'admin') {
      setLeaderboardPunkte(userdata.leaderboardPoints);
    }
  }, [role, userdata.leaderboardPoints])

  // Profilseite rendern
  return (
    <>
    <div className="Profile">
      <h1 className="titel"> Mein Profil</h1>
      <div className="daten">
        {/* Profilbild anzeigen und Ändern */}
        <input type="file" accept="image/*" onChange={(event) => {
        const file = event.target.files[0];
        const formData = new FormData();
        formData.append('file', file);
        Axios.post('http://localhost:8080/image', formData)
        .then(response => {
            console.log('Profile picture uploaded successfully');
            setProfilePicture(response.data.image);
          })
        .catch(error => {
            console.error(error);
            setError('Ein Fehler ist aufgetreten');
          });
        }} style={{display: 'none'}} />
      <img className="profilbild" src={image ? image : require('./testbild.jpg')} alt="Profilbild" onClick={() => document.querySelector('input[type="file"]').click()} />
      <p><strong>Username:</strong> {username}</p>
      <p><strong>Vorname:</strong> {firstName}</p>
      <p><strong>Nachname:</strong> {lastName}</p>
      <p><strong>Email:</strong> {email}</p>
      <p><strong>Passwort:</strong> {password} </p>
      <p><strong>Date of Birth:</strong> {dateOfBirth}</p>
      {role !== 'admin' && <p><strong>SEP Coins:</strong> {sepCoins}</p>}
      {role !== 'admin' && <p><strong>Leaderboard Punkte:</strong> {leaderboardPoints}</p>}
      {error && <p className="error">{error}</p>} {/* Fehlermeldung anzeigen falls error != null */}
      </div>
    </div>
    <div>
      <Link to="/startseite">
        <button className="button" type="button">Home</button>
      </Link>
    </div>
    </>
  );
}

export default Profile;