import './Profile.css'; // Importiere CSS-Module
import React, { Component } from 'react';


class ProfileFriends extends Component {
  // Definiere den initialen Zustand mit userData als null
  state = {
    userData: null
  };

  // Wird aufgerufen, sobald die Komponente montiert ist
  componentDidMount() {
    // Lade Benutzerdaten, wenn die Komponente montiert ist
    this.fetchUserData();
  }

  // Methode zum Abrufen der Benutzerdaten über die API
  fetchUserData = () => {
    // API-Aufruf, um Benutzerdaten abzurufen
    fetch('/api/user')
      .then(response => response.json()) // Konvertiere die Antwort in JSON
      .then(data => {
        // Aktualisiere den Zustand mit den abgerufenen Benutzerdaten
        this.setState({ userData: data });
      })
      .catch(error => console.error('Error fetching user data:', error)); // Behandele Fehler beim Abrufen der Daten
  };
   render () {
    const { userData } = this.state;

  return (
    <div className={'profile-container'}> {/* Verwende das CSS-Modul */}
      <div className={'profile-header'}>
        <h2>Mein Profil</h2>
      </div>
      <div className={'profile-info'}>
        <img className={'profile-picture'} src={userData.profilePicture} alt="Profilbild" />
        <p><strong>Benutzername:</strong> {userData.username}</p>
        <p><strong>Vorname:</strong> {userData.firstName}</p>
        <p><strong>Nachname:</strong> {userData.lastName}</p>
        <p><strong>E-Mail:</strong> {userData.email}</p>
        <p><strong>Geburtsdatum:</strong> {userData.birthDate}</p>
        <p><strong>Leaderboard-Punkte:</strong> {userData.leaderboardPoints}</p>
        <p><strong>SEP-Coins:</strong> {userData.sepCoins}</p>
      </div>
    </div>
    );
  }
} 
export default ProfileFriends;
