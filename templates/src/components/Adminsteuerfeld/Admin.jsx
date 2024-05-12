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

  // Methode zum Abrufen der Kartendaten vom Backend
  fetchCards = async () => {
    try {
      const response = await axiosInstance.get('/cards'); // Abrufen der Kartenliste vom Backend
      this.setState({ cards: response.data }); // Setzen des Zustands für die Kartenliste
    } catch (error) { 
      console.error('Fehler beim Abrufen der Kartendaten:', error ); 
    }

  }

  // Methode zum Hochladen einer CSV-Datei mit Kartendaten
  handleUpload = async (event) => {
    event.preventDefault(); // Verhindert das Standardverhalten des Formulars (Seite neu laden)

    const file = event.target.files[0]; // CSV-Datei aus dem Eingabefeld erhalten
    const formData = new FormData(); // FormData erstellen, um die Datei zu senden
    formData.append('file', file); // Datei zum FormData hinzufügen

    // Senden der Datei an das Backend
    try {
      const response = await axiosInstance.post('/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data' // Setzen des Content-Type-Headers auf multipart/form-data für den Dateiupload
        }
      });
      console.log('Datei erfolgreich hochgeladen.');

      // Kartendaten im State aktualisieren
      this.setState({ cards: response.data });
    } catch (error) {
      console.error('Fehler beim Hochladen der Datei:', error); // Behandlung von Fehlern beim Hochladen der Datei
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
  handleImageUpload = async (cardId, event) => {
    const file = event.target.files[0]; // Bild aus dem Eingabefeld erhalten
    const formData = new FormData(); // FormData erstellen, um die Datei zu senden
    formData.append('file', file); // Datei zum FormData hinzufügen

    // Senden des Bildes an das Backend
    try {
      const response = await axiosInstance.post(`/upload/image/${cardId}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data' // Setzen des Content-Type-Headers auf multipart/form-data für den Dateiupload
        }
      });
      console.log(`Bild für Karte ${cardId} erfolgreich hochgeladen.`);

      // Aktualisieren des Bildzustands
      this.setState(prevState => ({
        images: {
          ...prevState.images,
          [cardId]: response.data.imageUrl
        }
      }));
    } catch (error) {
      console.error(`Fehler beim Hochladen des Bildes für Karte ${cardId}:`, error);
    }
  };

  render() {
    // Filtern der Kartenliste basierend auf dem eingegebenen Kartennamen
    const filteredCards = this.state.cards.filter(card =>
      card.name.toLowerCase().includes(this.state.searchName.toLowerCase())
    );

    return (
      <div className="AdminPanel">
        <h1>Admin Panel</h1> {/* Überschrift für das Admin-Panel */}
        <form onSubmit={this.handleUpload}> {/* Formular zum Hochladen einer CSV-Datei */}
          <label htmlFor="csvFile">Wählen Sie eine CSV-Datei aus:</label>
          <input type="file" id="csvFile" name="csvFile" accept=".csv" required />
          <button type="submit">CSV-Datei hochladen</button>
        </form>

        <div id="searchContainer">
          <label htmlFor="searchName">Kartenname suchen:</label>
          <input type="text" id="searchName" value={this.state.name} onChange={this.handleSearchChange} /> 
          <button onClick={this.handleDelete}>Löschen</button>
          <div id="searchResults">
            {/* Anzeige der Karten und Bildhochladefelder für die gefilterten Karten */}
            {filteredCards.map((card, index) => (
              <div key={index}>
                <Card card={card} />
                <input type="file" onChange={(event) => this.handleImageUpload(card.id, event)} />
              </div>
            ))}
          </div>
        </div>
        <div id="existingCards">
          <h2>Vorhandene Kartentypen</h2>
          {/* Liste der Kartentypen */}
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



