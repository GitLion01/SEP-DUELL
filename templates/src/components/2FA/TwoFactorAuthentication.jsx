import React, { Component } from 'react';
import './TwoFactorAuthentication.css';
import axiosInstance from '../../api/axios';
import { Link, Navigate } from 'react-router-dom'; // Importieren von Link und Navigate für die Navigation


class TwoFaktorAuthenfication extends Component {
    state = {
        pincode: '', // Zustand für den PIN-Code
        isPinCodeCorrect: false, 
        errorMessage: '' 

    };
    
    // Funktion zum Aktualisieren des PIN-Codes
    handlePincodeChange = (index, value) => { // Funktion zum Aktualisieren des PIN-Codes
        const newPincode = this.state.pincode.split(''); // Kopie des aktuellen PIN-Codes erstellen
        newPincode[index] = value; // Wert des Zeichens an der gegebenen Indexposition aktualisieren
        this.setState({ pincode: newPincode.join('') }); // PIN-Code im Zustand aktualisieren
    };
    
    // Funktion zum Überprüfen des PIN-Codes
    handleSubmit = async (event) => { // Funktion zum Behandeln des Formularabsendens
        event.preventDefault(); // Verhindert das Standardverhalten des Formulars (Seite neu laden)
        
        try {
            const response = await axiosInstance.post('/login/verify', { // POST-Anfrage an die Backend-Route '/verify-pin' senden
                pincode: this.state.pincode // PIN-Code als Teil des Datenobjekts senden
            });

            console.log('Antwort vom Server:', response.data); // Ausgabe der Antwort des Servers in der Konsole
            // Hier könntest du entsprechend auf die Antwort des Servers reagieren, z.B. eine Weiterleitung oder eine Benachrichtigung anzeigen
            if (response.data.isPinCodeCorrect) {
                this.setState({ isPinCodeCorrect: true }); // PIN-Code ist korrekt
            } else {
                this.setState({ errorMessage: 'Falscher PIN-Code' }); // Fehlermeldung anzeigen
            }
        } catch (error) {
            console.error('Fehler beim Überprüfen des PIN-Codes:', error.message); // Ausgabe eines Fehlers in der Konsole
            // Hier könntest du auf einen Fehler beim Überprüfen des PIN-Codes reagieren, z.B. eine Fehlermeldung anzeigen
        }
    };

    render() {
        const isPincodeComplete = this.state.pincode.length === 6;
        if (this.state.isPinCodeCorrect) {
            return <Navigate to="/startseite" />; 
        }
        return (
            <body className='TwoFact__body'> 
            <div className="TwoFact">
                <form id="form">
                    <h1>Zwei-Faktor-Verifizierung</h1>
                    <div className="form__group form__pincode">
                        <label id="textstelle">Gebe dein 6-stelligen Code ein</label>
                        <input type="tel" name="pincode-1" maxLength="1" pattern="[\d]*" tabIndex="1" placeholder="·" autoComplete="off" value={this.state.pincode[0]} onChange={(e) => this.handlePincodeChange(0, e.target.value)}/>
                        <input type="tel" name="pincode-2" maxLength="1" pattern="[\d]*" tabIndex="2" placeholder="·" autoComplete="off" value={this.state.pincode[1]} onChange={(e) => this.handlePincodeChange(1, e.target.value)}/>
                        <input type="tel" name="pincode-3" maxLength="1" pattern="[\d]*" tabIndex="3" placeholder="·" autoComplete="off" value={this.state.pincode[2]} onChange={(e) => this.handlePincodeChange(2, e.target.value)}/>
                        <input type="tel" name="pincode-4" maxLength="1" pattern="[\d]*" tabIndex="4" placeholder="·" autoComplete="off" value={this.state.pincode[3]} onChange={(e) => this.handlePincodeChange(3, e.target.value)}/>
                        <input type="tel" name="pincode-5" maxLength="1" pattern="[\d]*" tabIndex="5" placeholder="·" autoComplete="off" value={this.state.pincode[4]} onChange={(e) => this.handlePincodeChange(4, e.target.value)}/>
                        <input type="tel" name="pincode-6" maxLength="1" pattern="[\d]*" tabIndex="6" placeholder="·" autoComplete="off" value={this.state.pincode[5]} onChange={(e) => this.handlePincodeChange(5, e.target.value)}/>
                    </div>
                    <div className="TwoFact__form__buttons">
                        {/* Aktiviere den Button nur, wenn der PIN-Code vollständig ist */}
                        <button type="submit" className="button button--primary" disabled={!isPincodeComplete}>
                            Continue
                        </button>
                    </div>
                </form>
            </div>
            </body>
        );
    }
}
export default TwoFaktorAuthenfication;