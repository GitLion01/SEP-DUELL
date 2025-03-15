import React, { Component } from 'react';
import './TwoFactorAuthentication.css';
import axiosInstance from '../../api/axios';
import { Navigate } from 'react-router-dom';

class TwoFaktorAuthenfication extends Component {
    state = {
        pincode: '',
        isPinCodeCorrect: false,
        errorMessage: ''
    };

    handlePincodeChange = (event) => {
        this.setState({ pincode: event.target.value });
    };

    handleSubmit = async (event) => {
        event.preventDefault();

        try {
            const userId = localStorage.getItem('id');
            const response = await axiosInstance.post('/login/verify', {
                token: this.state.pincode,
                userId: userId
            });

            console.log('Antwort vom Server:', response.data);
            if (response.data === "Login verified successfully with Super Code" || response.data === "Login verified successfully") {
                this.setState({ isPinCodeCorrect: true });
                await this.fetchAndStoreClanId(userId);
            } else {
                this.setState({ errorMessage: 'Falscher PIN-Code' });
            }
        } catch (error) {
            console.error('Fehler beim Überprüfen des PIN-Codes:', error.message);
            alert('PIN-Code falsch oder abgelaufen');
        }
    };

    fetchAndStoreClanId = async (userId) => {
        try {
            const response = await fetch(`http://localhost:8080/getClanId?userId=${userId}`);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            localStorage.setItem('clanId', data); // Store clanId in localStorage
        } catch (error) {
            console.error('There was a problem with the fetch operation:', error);
        }
    };

    render() {
        const { pincode } = this.state;
        const isPincodeComplete = pincode.length === 6;

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
