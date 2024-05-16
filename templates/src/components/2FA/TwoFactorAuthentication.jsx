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
    handlePincodeChange = (event ,index, value) => { // Funktion zum Aktualisieren des PIN-Codes
        this.setState({ pincode: event.target.value });
    };
    
    // Funktion zum Überprüfen des PIN-Codes
    handleSubmit = async (event) => { // Funktion zum Behandeln des Formularabsendens
        event.preventDefault(); // Verhindert das Standardverhalten des Formulars (Seite neu laden)
        
        try {


            const userId = localStorage.getItem('id');// Benutzer-ID aus dem LocalStorage abrufen
            const response = await axiosInstance.post('/login/verify', { // POST-Anfrage an die Backend-Route '/verify-pin' senden
                token: this.state.pincode, // PIN-Code als Teil des Datenobjekts senden
                userId: userId
            });

            console.log('Antwort vom Server:', response.data); // Ausgabe der Antwort des Servers in der Konsole
            // Hier könntest du entsprechend auf die Antwort des Servers reagieren, z.B. eine Weiterleitung oder eine Benachrichtigung anzeigen
            if (response.data==="Login verified successfully with Super Code" ||response.data==="Login verified successfully" ) {
                this.setState({ isPinCodeCorrect: true }); // PIN-Code ist korrekt
            } else {
                this.setState({ errorMessage: 'Falscher PIN-Code' }); // Fehlermeldung anzeigen
            }
        } catch (error) {
            console.error('Fehler beim Überprüfen des PIN-Codes:', error.message); // Ausgabe eines Fehlers in der Konsole
            // Hier könntest du auf einen Fehler beim Überprüfen des PIN-Codes reagieren, z.B. eine Fehlermeldung anzeigen
            alert ('PIN-Code falsch oder abgelaufen')
        }
    };

    render() {
        const { pincode } = this.state; // Pincode aus dem state extrahieren
        const isPincodeComplete = pincode.length===6; 
        if (this.state.isPinCodeCorrect) {
            return <Navigate to="/startseite" />; 
            
        }
        return (
            <body className='TwoFact__body'> 
            <div className="TwoFact">
                <form id="form" onSubmit={this.handleSubmit}>
                    <h1>Zwei-Faktor-Verifizierung</h1>
                    <div className="form__group form__pincode">
                        <label id="textstelle">Gebe dein 6-stelligen Code ein</label>
                        <input
                            type="text"
                            id="pincode"
                            name="pincode"
                            onChange={this.handlePincodeChange}
                        />
                    </div>
                    <div className="TwoFact__form__buttons">
                        {/* Aktiviere den Button nur, wenn der PIN-Code vollständig ist */}
                        <button type="submit" className="primary" disabled={!isPincodeComplete}>
                            Continue
                        </button>
                    </div>
                </form>
            </div>
            </body>
        );
    }
}
document.body.classList.add('TwoFact__body');

export default TwoFaktorAuthenfication;