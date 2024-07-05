import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Axios from 'axios';
import './Profile.css';
import testbild from './testbild.jpg';
import BackButton from '../BackButton';

// Profil-Seite

function Profile() {
  const id = localStorage.getItem('id');
  const navigate = useNavigate();
  const [profilePicture, setProfilePicture] = useState('');
  const [username, setUsername] = useState('');
  const [vorname, setVorname] = useState('');
  const [nachname, setNachname] = useState('');
  const [email, setEmail] = useState('');
  const [geburtsdatum, setDateOfBirth] = useState('');
  const [sepCoins, setSepcoins] = useState('');
  const [leaderbordpunkte, setLeaderbordPunkte] = useState('');
  const [role, setRole] =useState('');
  const [error, setError] = useState(null);
  const [userdata, setUserdata] = useState('');
  
  // Funktion, um ein Bild von einer URL zu laden und als File zurückzugeben
  const urlToFile = async (url, filename, mimeType) => {
    const response = await fetch(url);
    const blob = await response.blob();
    return new File([blob], filename, { type: mimeType });
  };

   // Funktion zum Hochladen des Testbilds
   const uploadTestbild = async () => {
    try {
      const file = await urlToFile(testbild, 'testbild.jpg', 'image/jpeg');
      const formData = new FormData();
      formData.append('image', file);

      await Axios.post(`http://localhost:8080/image/upload/${id}`, formData);
      fetchUpdatedImage();
      console.log('Testbild wurde als Profilbild hochgeladen');
    } catch (error) {
      setError('Ein Fehler ist aufgetreten');
      console.error(error);
    }
  };

  // Benutzerdaten von der API abrufen
  useEffect(() => {
    Axios.get(`http://localhost:8080/profile/${id}`)
      .then(response => {
          console.log(response.data);
        const userData = response.data;
        setUserdata(response.data);

        if (!userData.image) {
          uploadTestbild();
        } else {
          setProfilePicture(userData.image);
        }

        //setProfilePicture(userData.image);
        setUsername(userData.username);
        setVorname(userData.firstName);
        setNachname(userData.lastName);
        setEmail(userData.email);
        const date = new Date(userData.dateOfBirth);
        const formattedDate = `${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1).toString().padStart(2, '0')}.${date.getFullYear()}`;
        setDateOfBirth(formattedDate);
        setRole(userData.role);
          if (userData.role !== 'ADMIN') {
              setSepcoins(userData.sepCoins);
              setLeaderbordPunkte(userData.leaderboardPoints);
          }
      })
      .catch(error => {
        console.error(error);
        setError('Ein Fehler ist aufgetreten')
      });
  }, [id]);


  // Funktion zum Abrufen des neu hochgeladenen Profilbilds
  function fetchUpdatedImage() {
    Axios.get(`http://localhost:8080/profile/${id}`)
      .then(response => {
        setProfilePicture(response.data.image);
      })
      .catch(error => {
        setError('Ein Fehler ist aufgetreten');
      });
  }

  // Verarbeitung der Funktion, ein neues Profilbild hochzuladen
  const handleFileChange = event => {
    const file = event.target.files[0];
    if (file.size >1048576) { // 1MB Limit
      alert('Die Datei ist zu groß. Die maximale Dateigröße beträgt 1MB. ');
      return
    }

    // ausgewählte Datei mus ein image sein
    if (!file.type.startsWith('image/')) {
      alert('Bitte wählen Sie eine Bilddatei.');
      return;
    }

    // Senden des neuen Profilbilds an den Server
    const formData = new FormData();
    formData.append('image', file);
    Axios.post(`http://localhost:8080/image/upload/${id}`, formData)
      .then(response => {
        if (response.status === 200) {
          fetchUpdatedImage();
          console.log('Bild wurde aktualisiert')
        }
      })
      .catch(error => {
        setError('Ein Fehler ist aufgetreten');
      });
  };


  // Update SEP-Coins wenn der Wert von SEP-Coins geändert wird
  useEffect(() => {
    if (role !== 'ADMIN') {
      setSepcoins(userdata.sepCoins);
    }
  }, [role, userdata.sepCoins]);

  // Update Leaderboard-Punkte wenn der Wert von Leaderboard-Punkte geändert wird
  useEffect(() => {
    if (role !== 'ADMIN') {
        setLeaderbordPunkte(userdata.leaderboardPoints);
    }
  }, [role, userdata.leaderboardPoints])


  // Profilseite rendern
  return (
    <>
    <BackButton/>
      <div className="Profile">
        <h1 className="titel"> Mein Profil</h1>
          <div className="daten">

              {/* Profilbild anzeigen und Ändern */}
              <input type="file" accept="image/*" onChange={handleFileChange} style={{display: 'none'}}/>
              <img className="profilbild"

                  // Profilbild umwandeln von base64 in Bildformat
                   src={`data:image/jpeg;base64,${profilePicture}`}
                   alt={'Profilbild'} onClick={() => document.querySelector('input[type="file"]').click()}
              />

              <p><strong>Username:</strong> {username}</p>
              <p><strong>Vorname:</strong> {vorname}</p>
              <p><strong>Nachname:</strong> {nachname}</p>
              <p><strong>Email:</strong> {email}</p>
              {/*<p><strong>Passwort:</strong> {password} </p>*/}
              <p><strong>Geburtsdatum:</strong> {geburtsdatum}</p>
              {role !== 'ADMIN' && <p><strong>SEP Coins:</strong> {sepCoins}</p>}
              {role !== 'ADMIN' && <p><strong>Leaderboard Punkte:</strong> {leaderbordpunkte}</p>}
              {error && <p className="error">{error}</p>} {/* Fehlermeldung anzeigen falls error != null */}
              <button onClick={() => navigate('/duellhistorie')}>Duellhistorie ansehen</button>
          </div>
      </div>
    </>
  );
}

export default Profile;