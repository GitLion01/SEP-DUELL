import React, { Component } from 'react';
import axiosInstance from '../../api/axios';
import './Admin.css'; // Stile für das Adminpanel
import Card from '../card'; 

class AdminPanel extends Component {
  state = {
    cards: [] // Zustand für die Liste der Karten
  };

  // Methode zum Hochladen einer CSV-Datei mit Kartendaten
  handleUpload = async (event) => {
    event.preventDefault(); // Verhindert das Standardverhalten des Formulars (Seite neu laden)

    const file = event.target.files[0]; // CSV-Datei aus dem Eingabefeld erhalten
    const formData = new FormData(); // FormData erstellen, um die Datei zu senden
    formData.append('file', file); // Datei zum FormData hinzufügen

    // Senden der Datei an das Backend
    try {
      await axiosInstance.post('/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data' // Setzen des Content-Type-Headers auf multipart/form-data für den Dateiupload
        }
      });
      console.log('Datei erfolgreich hochgeladen.');
      // Nach dem Hochladen der Datei kann eine Aktualisierung der Liste der Karten vom Backend erfolgen
    } catch (error) {
      console.error('Fehler beim Hochladen der Datei:', error); // Behandlung von Fehlern beim Hochladen der Datei
    }
  };

  render() {
    return (
      <div className="AdminPanel">
        <h1>Admin Panel</h1> {/* Überschrift für das Admin-Panel */}
        <form onSubmit={this.handleUpload}> {/* Formular zum Hochladen einer CSV-Datei */}
          <label htmlFor="csvFile">Wählen Sie eine CSV-Datei aus:</label>
          <input type="file" id="csvFile" name="csvFile" accept=".csv" required />
          <button type="submit">CSV-Datei hochladen</button>
        </form>

        <div id="existingCards">
          <h2>Vorhandene Kartentypen</h2>
          <div className="cardContainer">
            {this.state.cards.map((card, index) => (
              <Card key={index} card={card} />
            ))}
          </div>
        </div>
      </div>
    );
  }
}

export default AdminPanel; // Exportiere die Komponente AdminPanel

