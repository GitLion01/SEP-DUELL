              import React, { Component } from 'react';
              import axiosInstance from '../../api/axios';
              import './Admin.css';
              import Card from '../card';



              class Admin extends Component {
                  state = { 
                      cards: [],
                      searchTerm:''
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
                        console.log('Response data:', response.data); // Überprüfen, was genau vom Server zurückkommt
                        console.log('Datei erfolgreich hochgeladen.');
                        // Die Karten vom Server neu laden, um die neuesten Karten anzuzeigen
                        this.fetchCards();
                      } catch (error) {
                        console.error('Fehler beim Hochladen der Datei:', error);
                      }
                  };
                  

                  handleSearchChange = (event) => {
                    this.setState({ searchTerm: event.target.value });
                  }; 

                  handleSearch = async (event) => {
                    event.preventDefault();
                    try {
                        const response = await axiosInstance.get(`/cards/findByName/${this.state.searchTerm}`);
                        if (response.data) {
                            this.setState({ cards: [response.data] }); // Setze die Karten neu basierend auf der Suche
                        } else {
                            this.setState({ cards: [] }); // Leere die Kartenliste, wenn keine Karte gefunden wurde
                        }
                    } catch (error) { 
                        console.error('Fehler beim Suchen:', error);
                        this.setState({ cards: [] });
                    }
                };
                
                
                
                handleDelete = async (event) => {
                  event.preventDefault();
                  if (this.state.searchTerm) {
                      try {
                          // Annahme, dass der Endpunkt eine POST-Anforderung erwartet
                          const response = await axiosInstance.post('/cards/delete', [this.state.searchTerm]);
                          console.log('Löschvorgang:', response.data);
                          // Aktualisiere die Kartenliste, falls das Löschen erfolgreich war
                          this.fetchCards();
                      } catch (error) {
                          console.error('Fehler beim Löschen der Karte:', error);
                      }
                  } else {
                      console.error('Kein Kartenname eingegeben');
                  }
              };
              


                  render() { 
                    console.log(this.state.cards); // Konsolenausgabe hinzugefügt
                    console.log(Array.isArray(this.state.cards));

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
                          <h2>Vorhandene Kartentypen</h2>
                          <form onSubmit={this.handleSearch}>
                                  <input type="text" placeholder="Karte suchen" value={this.state.searchTerm} onChange={this.handleSearchChange} required />
                                  <button type="submit">Suchen</button>
                                  <button type="button" onClick={this.handleDelete}>Löschen</button>
                                  <button type="button" onClick={this.fetchCards}>Alle anzeigen</button>
                              </form>

                          <div id="existingCards" style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '20px' }}>
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