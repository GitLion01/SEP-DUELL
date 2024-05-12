import React, { Component } from 'react';
import axios from 'axios';
import { Navigate } from 'react-router-dom';

class PinVerification extends Component {
    state = {
        pin: '',
        isPinValid: false,
        errorMessage: ''
    };

    handlePinChange = (event) => {
        this.setState({ pin: event.target.value });
    };

    handleSubmit = async (event) => {
        event.preventDefault();

        try {
            const response = await axios.post('http://localhost:8080/login/verify', { token: this.state.pin });

            if (response.data=== "Login verified successfully with Super Code" || response.data=== "Login verified successfully") {
                this.setState({ isPinValid: true });
            } else {
                this.setState({ errorMessage: 'Invalid PIN' });
            }
        } catch (error) {
            console.error('Error verifying PIN:', error);
        }
    };

    render() {
        if (this.state.isPinValid) {
            console.log("done");
            return <Navigate to="/startsite" />;
        }

        return (
            <div className='TwoFact__body'>
                <div className="TwoFact">
                    <form onSubmit={this.handleSubmit}>
                        <h1>Zwei-Faktor-Verifizierung</h1>
                        <div className="form__group form__pincode">
                            <label id="textstelle">Gebe dein 6-stelligen Code ein</label>
                            <input
                                type="text"
                                maxLength="6"
                                value={this.state.pin}
                                onChange={this.handlePinChange}
                            />
                        </div>
                        <div className="TwoFact__form__buttons">
                            <button type="submit" className="button button--primary" disabled={this.state.pin.length !== 6}>
                                Continue
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        );
    }
}

export default PinVerification;
