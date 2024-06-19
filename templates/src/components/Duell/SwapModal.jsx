// SwapModal.jsx
import React from 'react';
import Modal from 'react-modal';
import Card from "../card";
import './SwapModal.css';

const SwapModal = ({ isOpen, onRequestClose, onConfirm, title, selectedCards, setSelectedCards, playerCards, selectedHandCard, handCards, setSelectedHandCard, requiredFieldCards }) => {
    const toggleSelectCard = (Id) => {
        setSelectedCards((prevSelected) =>
            prevSelected.includes(Id) ? prevSelected.filter(cardId => cardId !== Id) : [...prevSelected, Id]
        );
    };

    return (
        <Modal
            isOpen={isOpen}
            onRequestClose={onRequestClose}
            contentLabel={title}
            className="swap-modal-content"
            overlayClassName="swap-modal-overlay"
        >
            <h3>{title}</h3>
            <div className="field-cards">
                <h4>Karten auf dem Feld</h4>
                {playerCards.map((playerCard) => (
                    <div
                        key={playerCard.id}
                        className={`card-slot ${selectedCards.includes(playerCard.id) ? 'selected' : ''}`}
                    >
                        <Card card={playerCard} onCardClick={() => toggleSelectCard(playerCard.id)} />
                    </div>
                ))}
            </div>
            <div className="hand-cards">
                <h4>Karten auf der Hand</h4>
                {handCards.map((playerCard) => (
                    <div
                        key={playerCard.id}
                        className={`card ${playerCard.id === selectedHandCard ? 'selected' : ''}`}
                    >
                        <Card card={playerCard} onCardClick={() => setSelectedHandCard(playerCard.id)} />
                    </div>
                ))}
            </div>
            <button onClick={onConfirm}>Fertig</button>
            <button onClick={onRequestClose}>Abbrechen</button>
        </Modal>
    );
};

export default SwapModal;
