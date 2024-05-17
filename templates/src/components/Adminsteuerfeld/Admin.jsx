import React, { Component } from 'react'; // Importiert React und die Component-Klasse aus der React-Bibliothek
import axiosInstance from '../../api/axios'; // Importiert eine axios-Instanz für HTTP-Anfragen
import './Admin.css'; // Importiert die CSS-Datei für das Styling der Admin-Komponente
import Card from '../card'; // Importiert die Card-Komponente
import BackButton from '../BackButton';

class Admin extends Component {
    state = { 
        cards: [], // Initialisiert den State mit einem leeren Array für Karten
        searchTerm:'' // Initialisiert den State mit einem leeren String für den Suchbegriff
    } 

    componentDidMount() {
        this.fetchCards(); // Ruft die Funktion fetchCards auf, sobald die Komponente in den DOM eingefügt wird
    }

    fetchCards = async () => {
        try {
            const response = await axiosInstance.get('/cards'); // Führt eine GET-Anfrage aus, um Karten vom Server zu holen
            // Wir gehen davon aus, dass die Karten im response.data-Array enthalten sind
            this.setState({ cards: response.data }); // Aktualisiert den State mit den erhaltenen Karten
        } catch (error) {
            console.error('Fehler beim Abrufen der Karten:', error); // Gibt einen Fehler aus, falls die Anfrage fehlschlägt
        }
    };
    
    handleUpload = async (event) => {
        event.preventDefault(); // Verhindert das Standardverhalten des Formulars
        const file = event.target.files[0]; // Holt die hochgeladene Datei aus dem Event-Target
        const formData = new FormData(); // Erstellt ein neues FormData-Objekt
        formData.append('file', file); // Fügt die Datei zum FormData-Objekt hinzu
    
        try {
            const response = await axiosInstance.post('/cards/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data' // Setzt den Content-Type Header
                }
            });
            console.log('Response data:', response.data); // Überprüfen, was genau vom Server zurückkommt
            console.log('Datei erfolgreich hochgeladen.');
            // Die Karten vom Server neu laden, um die neuesten Karten anzuzeigen
            this.fetchCards(); // Ruft erneut fetchCards auf, um die Kartenliste zu aktualisieren
        } catch (error) {
            console.error('Fehler beim Hochladen der Datei:', error); // Gibt einen Fehler aus, falls das Hochladen fehlschlägt
            alert('Fehler beim hochladen der Datei')
        }
    };

    handleSearchChange = (event) => {
        this.setState({ searchTerm: event.target.value }); // Aktualisiert den Suchbegriff im State, wenn sich der Wert des Eingabefelds ändert
    };

    handleSearch = async (event) => {
        event.preventDefault(); // Verhindert das Standardverhalten des Formulars
        try {
            const response = await axiosInstance.get(`/cards/findByName/${this.state.searchTerm}`); // Führt eine GET-Anfrage aus, um eine Karte nach Namen zu suchen
            if (response.data) {
                this.setState({ cards: [response.data] }); // Setzt die Karten neu basierend auf der Suche
            } else {
                this.setState({ cards: [] }); // Leere die Kartenliste, wenn keine Karte gefunden wurde
            }
        } catch (error) { 
            console.error('Fehler beim Suchen:', error); // Gibt einen Fehler aus, falls die Suchanfrage fehlschlägt
            this.setState({ cards: [] }); // Leere die Kartenliste, wenn ein Fehler auftritt
        }
    };

    handleDelete = async (event) => {
        event.preventDefault(); // Verhindert das Standardverhalten des Formulars
        if (this.state.searchTerm) {
            try {
                // Annahme, dass der Endpunkt eine POST-Anforderung erwartet
                const response = await axiosInstance.post('/cards/delete', [this.state.searchTerm]); // Führt eine POST-Anfrage aus, um eine Karte zu löschen
                console.log('Löschvorgang:', response.data); // Gibt den Löschvorgang in der Konsole aus
                // Aktualisiere die Kartenliste, falls das Löschen erfolgreich war
                this.fetchCards(); // Ruft erneut fetchCards auf, um die Kartenliste zu aktualisieren
            } catch (error) {
                console.error('Fehler beim Löschen der Karte:', error); // Gibt einen Fehler aus, falls das Löschen fehlschlägt
            }
        } else {
            console.error('Kein Kartenname eingegeben'); // Gibt einen Fehler aus, falls kein Kartenname eingegeben wurde
        }
    };

    render() { 
        console.log(this.state.cards); // Konsolenausgabe hinzugefügt, um die Karten im State anzuzeigen
        console.log(Array.isArray(this.state.cards)); // Überprüft, ob cards ein Array ist

        return (
            <body className='AdminPanel__body'> {/* Fügt eine Klasse für das Body-Element hinzu */}
                  <BackButton />
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
                    </form>
                    <h2>Vorhandene Kartentypen</h2>
                    <form onSubmit={this.handleSearch}> {/* Formular zum Suchen von Karten */}
                        <input type="text" placeholder="Karte suchen" value={this.state.searchTerm} onChange={this.handleSearchChange} required />
                        <button type="submit">Suchen</button> {/* Button zum Ausführen der Suchanfrage */}
                        <button type="button" onClick={this.handleDelete}>Löschen</button> {/* Button zum Löschen einer Karte */}
                        <button type="button" onClick={this.fetchCards}>Alle anzeigen</button> {/* Button zum Anzeigen aller Karten */}
                    </form>
                    <div id="existingCards" style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '20px' }}>
                        {/* Liste der Kartentypen */}
                        {this.state.cards.map((card, index) =>( 
                            <Card key={index} card={card} /> // Rendern der Card-Komponente für jede Karte im State
                        ))}
                    </div>  
                </div>
            </body>
        );
    }
}

document.body.classList.add('AdminPanel__body'); // Fügt eine Klasse zum Body-Element hinzu
export default Admin; // Exportiert die Admin-Komponente als Standardexport
