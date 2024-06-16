// SwapModal.jsx
import React from 'react';
import Modal from 'react-modal';
import Card from "../card";

const SwapModal = ({ isOpen, onRequestClose, onConfirm, title, selectedCards, setSelectedCards, playerCards, selectedHandCard, handCards, setSelectedHandCard, requiredFieldCards }) => {
    const toggleSelectCard = (index) => {
        setSelectedCards((prevSelected) =>
            prevSelected.includes(index) ? prevSelected.filter(i => i !== index) : [...prevSelected, index]
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
                {playerCards.map((card, index) => (
                    <div
                        key={index}
                        className={`card-slot ${selectedCards.includes(index) ? 'selected' : ''}`}
                    >
                        <Card card={card} onCardClick={() => toggleSelectCard(index)}/>
                    </div>
                ))}
            </div>
            <div className="hand-cards">
                <h4>Karten auf der Hand</h4>
                {handCards.map((card, index) => (
                    <div
                        key={index}
                        className={`card ${index === selectedHandCard ? 'selected' : ''}`}

                    >
                        <Card card={card} onCardClick={() => setSelectedHandCard(index)} />
                    </div>
                ))}
            </div>
            <button onClick={onConfirm}>Fertig</button>
            <button onClick={onRequestClose}>Abbrechen</button>
        </Modal>
    );
};

export default SwapModal;
