import React, { Component } from 'react';
import './TwoFaktorAuthentication.css';

class TwoFaktorAuthenfication extends Component {
    state = {
        pincode: '' // Zustand für den PIN-Code
    };
    
    // Funktion zum Aktualisieren des PIN-Codes
    handlePincodeChange = (index, value) => { // Funktion zum Aktualisieren des PIN-Codes
        const newPincode = this.state.pincode.split(''); // Kopie des aktuellen PIN-Codes erstellen
        newPincode[index] = value; // Wert des Zeichens an der gegebenen Indexposition aktualisieren
        this.setState({ pincode: newPincode.join('') }); // PIN-Code im Zustand aktualisieren
    };
    
    // Funktion zum Überprüfen des PIN-Codes
    handleSubmit = (e) => { // Funktion zum Behandeln des Formularabsendens
        e.preventDefault(); // Verhindert das Standardverhalten des Formulars (Seite neu laden)
        // Hier kannst du die Überprüfung des PIN-Codes durchführen, z.B. eine API-Anfrage senden
        console.log('PIN-Code:', this.state.pincode); // Ausgabe des aktuellen PIN-Codes in der Konsole
    };

    render() {
        const isPincodeComplete = this.state.pincode.length === 6;

        return (
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
        );
    }
}
export default TwoFaktorAuthenfication;