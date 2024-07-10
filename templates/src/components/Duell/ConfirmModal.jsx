import React from 'react';
import './ConfirmModal.css';

const ConfirmModal = ({ show, onConfirm, onCancel, message }) => {
    if (!show) {
        return null;
    }

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h2>Bestätigung</h2>
                <p>{message}</p>
                <div className="modal-actions">
                    <button onClick={onConfirm}>Ja</button>
                    <button onClick={onCancel}>Nein</button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmModal;
