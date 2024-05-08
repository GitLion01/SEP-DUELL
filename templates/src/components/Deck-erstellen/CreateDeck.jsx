import React, { useState, useEffect } from 'react';
import axios from 'axios';

function CreateDeck() {
 
    const [decks, setDecks] = useState([[], [], []]); // 3 leere Decks
    const [mycards, setMyCards] = useState([]); // Array der Karten in Besitz
    const [activeDeck, setActiveDeck] = useState(null);

    // Karten in Besitz von Datenbank abrufen
    async function fetchMyCards() {
        try {
            const response = await axios.get('http://localhost:8080/cards');
            return response.data;
        } catch (error) {
            alert('Fehler beim Abrufen der Karten');
        }
    }

    // Karten in mycards Array hinzufügen
    useEffect(() => {
        fetchMyCards().then(mycards => setMyCards(mycards));
    }, []);

    function handleSelectDeck(deckIndex) {
        setActiveDeck(deckIndex);
    }

    // Hinzufügen von Karten in das aktive Deck
    function handleAddCard(card) {
        if (activeDeck !== null) {
            axios.post('http://localhost:8080/decks', {cardName: card.name})
                .then(response => {
                    setDecks(prevDecks => {
                        const newDecks = [...prevDecks]; // Kopie des aktuellen State
                    newDecks[activeDeck] = response.data; // aktualisierte Liste von Karten im Deck
                    });
                })
                .catch(error => {
                    alert(error.response.data.message); // Fehlermeldung vom Server
                });
        }    
    }


    /* 
    function handleRemoveCard(index) {

    }
    */

    return (
        <div>
            <h1>Deck Erstellen</h1>
            <div className="deck-auswahl">
                {decks.map((deck, deckIndex) => (
                    <div key={deckIndex}>
                      <button onClick={() => handleSelectDeck(deckIndex)}>
                        Deck {deckIndex + 1}
                      </button>
                      {activeDeck === deckIndex && (
                        <div className="mycards-liste">
                        <h2>Meine Karten</h2>
                        <ul>
                            {mycards.map((card) => (
                                <li key={card.id}>
                                    <span>{card.name}</span>    
                                    <button onClick={() => handleAddCard(card)}>Deck hinzufügen</button>
                                </li>
                            ))}
                        </ul>
                        </div>
                      )}
                      </div>
                ))}
            </div>
        </div>
    )
    

    
}

export default CreateDeck;