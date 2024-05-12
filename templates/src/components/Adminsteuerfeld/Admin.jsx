import React, { Component } from 'react';
import axiosInstance from '../../api/axios';
import './Admin.css';
import Card from '../card';



class Admin extends Component {
    state = { 
        cards: [] 
     } 

     componentDidMount() {
        this.fetchCards(); 
     }

     fetchCards = async () => {
        try {
          const response = await axiosInstance.get('/cards');
          // Wir gehen davon aus, dass die Karten im response.data-Array enthalten sind
          this.setState({ cards: response.data });
        } catch (error) {
          console.error('Fehler beim Abrufen der Karten:', error);
        }
      };
      


    handleUpload = async (event) => {
        console.log('handleUpload wurde aufgerufen.'); // Konsolenausgabe hinzufügen
        event.preventDefault();
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

    




    render() { 
        return (
            <body className='AdminPanel__body'>
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
                <div id="existingCards">
                <h2>Vorhandene Kartentypen</h2>
                {/* Liste der Kartentypen */}
                {this.state.cards.map((card, index) =>( 
                    <Card key={index} card={card} />
                ))}
                </div>  
            </div>
        </body>
        );
    }
}

document.body.classList.add('AdminPanel__body');
export default Admin;