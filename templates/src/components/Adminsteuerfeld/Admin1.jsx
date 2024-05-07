import React, { Component } from 'react'; // Importiere React und Component von react
import './Admin.css';

class AdminPanel extends Component { // Definiere eine Klasse AdminPanel, die von Component erbt
  state = { // Initialisiere den Komponentenzustand
    cardName: "", // Zustand für den Kartennamen
    rarity: "", // Zustand für die Seltenheit
    attackPoints: "", // Zustand für die Angriffspunkte
    defensePoints: "", // Zustand für die Verteidigungspunkte
    description: "", // Zustand für die Beschreibung
    image: null, // Zustand für das Bild
    cards: [] // Zustand für die Liste der Karten
  };

  handleInputChange = (event) => { // Methode zum Aktualisieren des Zustands basierend auf Eingabefeldern
    const { name, value } = event.target; // Extrahiert den Namen und Wert des Eingabefelds
    this.setState({ [name]: value }); // Aktualisiert den Zustand mit dem neuen Wert
  };

  handleFileChange = (event) => { // Methode zum Aktualisieren des Zustands basierend auf einer ausgewählten Datei
    this.setState({ image: event.target.files[0] }); // Aktualisiert den Zustand mit der ausgewählten Datei
  };

  handleUpload = async (event) => {
    event.preventDefault();
  
    const file = event.target.files[0]; // CSV-Datei aus dem Eingabefeld erhalten
  
    // Prüfen, ob eine Datei ausgewählt wurde und ob es sich um eine CSV-Datei handelt
    if (file && file.type === "text/csv") {
      const reader = new FileReader(); // FileReader zum Lesen der Datei erstellen
  
      reader.onload = async (event) => { // Event-Handler, der ausgeführt wird, wenn die Datei gelesen wurde
        const csvData = event.target.result; // CSV-Daten aus der Datei lesen
        const cards = []; // Leere Liste für die Kartendaten erstellen
  
        // CSV-Datei parsen und Kartenattribute extrahieren
        csvData.split('\n').forEach((line, index) => { // Trennt die CSV-Daten in Zeilen auf
          if (index === 0) return; // Header-Zeile überspringen
  
          const [cardName, rarity, attackPoints, defensePoints, description, image] = line.split(','); // Trennt die Zeile in einzelne Attribute auf
  
          // Karte zur Liste hinzufügen
          cards.push({
            cardName,
            rarity,
            attackPoints,
            defensePoints,
            description,
            image
          });
        });
  
        // Aktualisiere den Zustand mit den extrahierten Karten
        this.setState({ cards });
  
        // Setze das Eingabefeld zurück
        event.target.value = null;
      };
  
      reader.readAsText(file); // Datei als Text einlesen
    } else {
      console.error('Bitte wählen Sie eine CSV-Datei aus.'); // Fehlermeldung, wenn keine oder falsche Datei ausgewählt wurde
    }
  };
  

  render() { // Methode zum Rendern der Komponente
    return (
      <div>
        <h1>Admin Panel</h1> {/* Überschrift für das Admin-Panel */}
        <form onSubmit={this.handleUpload}> {/* Formular zum Hochladen einer CSV-Datei */}
          <input type="file" accept=".csv" onChange={this.handleFileChange} /> {/* Eingabefeld für die CSV-Datei */}
          <button type="submit">CSV-Datei hochladen</button> {/* Button zum Hochladen der CSV-Datei */}
        </form>
        <div> {/* Bereich zur Anzeige vorhandener Kartentypen */}
          <h2>Existing Card Types</h2> {/* Überschrift für die vorhandenen Kartentypen */}
          <ul> {/* Liste der vorhandenen Kartentypen */}
            {this.state.cards.map(card => ( // Durchläuft die Liste der Kartentypen und rendert jeden einzelnen
              <li key={card.id}>
                {card.name} - {card.rarity} {/* Anzeige des Kartennamens und der Seltenheit */}
                <button>Remove</button> {/* Button zum Entfernen des Kartentyps */}
              </li>
            ))}
          </ul>
        </div>
      </div>
    );
  }
}

export default AdminPanel; // Exportiere die Komponente AdminPanel