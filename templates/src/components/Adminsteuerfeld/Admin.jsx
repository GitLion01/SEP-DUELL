import React, { Component } from 'react';
import axiosInstance from '../../api/axios';
import './Admin.css'; // Stile für das Adminpanel
import Card from '../Card';

class AdminPanel extends Component {
  state = {
    cards: [], // Zustand für die Liste der Karten
    searchName:'', // Zustand für den eingegebenen Kartennamen
    images: {}, // Zustand für die hochgeladenen Bilder
    filteredCards: [] // Zustand für die gefilterten Karten hinzugefügt
  };

  componentDidMount() {
    this.fetchCards(); //Beim Laden der Komponente werden die Karten abgerufen 
  }

  handleUploadClick = () => {
    document.getElementById('csvFile').click(); // Klicken Sie auf das Dateieingabefeld, wenn der Button geklickt wird
  };

  // Methode zum Abrufen der Kartendaten vom Backend
  fetchCards = async () => {
    try {
      const response = await fetch('/cards'); // Abrufen der Kartenliste vom Backend
      if (!response.ok) {
        throw new Error('Fehler beim Abrufen der Kartendaten:', response.statusText);
      }
      const data = await response.json(); // Daten aus der Antwort extrahieren
      this.setState({ cards: data }); // Setzen des Zustands für die Kartenliste
    } catch (error) { 
      console.error('Fehler beim Abrufen der Kartendaten:', error); 
    }
  }
  

  // Methode zum Hochladen einer CSV-Datei mit Kartendaten
  handleUpload = async (event) => {
    console.log('handleUpload wurde aufgerufen.'); // Konsolenausgabe hinzufügen
    
    event.preventDefault();
  
    // Überprüfen, ob event.target.files definiert ist
    if (!event.target.files || event.target.files.length === 0) {
      console.error('Keine Datei ausgewählt.');
      return;
    }
  
    const file = event.target.files[0];
    const formData = new FormData();
    formData.append('file', file);
  
    try {
      const response = await axiosInstance.post('/cards/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      console.log('Datei erfolgreich hochgeladen.');
      this.setState({ cards: response.data });
    } catch (error) {
      console.error('Fehler beim Hochladen der Datei:', error);
    }
  };
  
  

  // Methode zum Aktualisieren des Suchbegriffs
  handleSearchChange = async (event) => {
    const searchName = event.target.value;
    this.setState({ searchName });

    try {
      const response = await axiosInstance.get(`/cards/findByName/${searchName}`); //Suche nach Karte mit dem angegebenen Namen 
      this.setState ({ filteredCards: response.data}); 
    }
    catch (error) {
      console.error('Fehler beim Suchen der Karte:', error);
  }
    
  };

  // Methode zum Löschen einer Karte
  handleDelete = async () => {
    const {searchName} = this.state 
    try {
      await axiosInstance.delete(`/delete/${searchName}`);
      console.log('Karte erfolgreich gelöscht.');
      this.fetchCards(); // Aktualisierung der Kartendaten nach dem Löschen
    } catch (error) {
      console.error('Fehler beim Löschen der Karte:', error);
    }
  }

  // Methode zum Hochladen eines Bildes für eine bestimmte Karte


  render() {
    // Filtern der Kartenliste basierend auf dem eingegebenen Kartennamen
  
    if (!this.state.cards) {
      return <div>Loading...</div>; // oder eine andere Ladeanzeige
    }
    return (
      <div className="AdminPanel">
        <h1>Admin Panel</h1> {/* Überschrift für das Admin-Panel */}
        <form onSubmit={this.handleUpload}> {/* Formular zum Hochladen einer CSV-Datei */}
          <label htmlFor="csvFile">Wählen Sie eine CSV-Datei aus:</label>
          <input 
            type="file" 
            id="csvFile" 
            name="csvFile" 
            accept=".csv" 
            onChange={this.handleUpload}
            required 
          />
          <button type="button" onClick={this.handleUploadClick}>CSV-Datei auswählen</button>
        </form>
        <div id="searchContainer">
          <label htmlFor="searchName">Kartenname suchen:</label>
          <input type="text" id="searchName" value={this.state.name} onChange={this.handleSearchChange} /> 
          <button onClick={this.handleDelete}>Löschen</button>
          <div id="searchResults">
            {/* Anzeige der Karten und Bildhochladefelder für die gefilterten Karten */}
        {/*    {filteredCards.map((card, index) => (
              <div key={index}>
                <Card card={card} />
                <input type="file" onChange={(event) => this.handleImageUpload(card.id, event)} />
        </div> 
            ))} */}
          </div>
        </div>
        <div id="existingCards">
          <h2>Vorhandene Kartentypen</h2>
          {/* Liste der Kartentypen */}
         
        </div>
      </div>
    );
  }
}

export default AdminPanel; // Exportiere die Komponente AdminPanel



