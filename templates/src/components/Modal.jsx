import React from 'react';
import './Modal.css';

const Modal = ({ message, onClose }) => {
    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h2>Error</h2>
                <p>{message}</p>
                <button onClick={onClose}>OK</button>
            </div>
        </div>
    );
};

export default Modal;
