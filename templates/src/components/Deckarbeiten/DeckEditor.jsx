
import React, { useState } from 'react';
import './Deckbearbeiten.css';

function Card({ card, onClick }) {
  return (
    <div className="card" onClick={onClick}>
      {card}
    </div>
  );
}

function DeckEditor() {
  const [cards, setCards] = useState(['A', 'K', 'Q', 'J', '10']);
  const [selectedCard, setSelectedCard] = useState(null);

  function handleCardClick(index) {
    if (selectedCard !== null) {
      const newCards = [...cards];
      newCards[index] = selectedCard;
      setCards(newCards);
      setSelectedCard(null);
    }
  }

  function handleSelectChange(event) {
    setSelectedCard(event.target.value);
  }

  return (
    <div className="deck-container">
      <h1>DECK BEARBEITEN</h1>
      <div className="instruction">
        <label htmlFor="card-select">Wählen Sie eine Karte:</label>
        <select id="card-select" onChange={handleSelectChange} value={selectedCard || ''}>
          <option value="">Bitte wählen Sie eine Karte</option>
          {['A', 'K', 'Q', 'J', '10'].map((card, index) => (
            <option key={index} value={card}>
              {card}
            </option>
          ))}
        </select>
      </div>
      <div className="deck">
        {cards.map((card, index) => (
          <Card key={index} card={card} onClick={() => handleCardClick(index)} />
        ))}
      </div>
    </div>
  );
}

export default DeckEditor;


