import React, { useState, useEffect } from "react";
import "./DeckEditor.css";

function App() {
  const [decks, setDecks] = useState([]);

  useEffect(() => {
    fetchDecksFromBackend(); // Aufruf der Methode zum Abrufen der Decks vom Backend
  }, []);

  // Methode zum Abrufen der Decks vom Backend
  const fetchDecksFromBackend = () => {
    // Hier müsstest du den tatsächlichen API-Aufruf implementieren, um die Decks vom Backend abzurufen
    // Beispiel: fetch('/api/decks')
    //          .then(response => response.json())
    //          .then(data => setDecks(data))
    //          .catch(error => console.error('Error fetching decks:', error));
    const fetchedDecks = [
      { id: 1, name: "Deck 1" },
      { id: 2, name: "Deck 2" },
      { id: 3, name: "Deck 3" }
    ];
    setDecks(fetchedDecks);
  };

  // Methode zum Löschen eines Decks vom Backend
  const deleteDeckFromBackend = (id) => {
    // Hier müsstest du den tatsächlichen API-Aufruf implementieren, um ein Deck vom Backend zu löschen
    // Beispiel: fetch(`/api/decks/${id}`, { method: 'DELETE' })
    //          .then(response => {
    //              if (!response.ok) {
    //                  throw new Error('Failed to delete deck');
    //              }
    //              return response.json();
    //          })
    //          .then(() => fetchDecksFromBackend())
    //          .catch(error => console.error('Error deleting deck:', error));
    const updatedDecks = decks.filter((deck) => deck.id !== id);
    setDecks(updatedDecks);
  };

  const handleDeleteDeck = (id) => {
    deleteDeckFromBackend(id); // Aufruf der Methode zum Löschen eines Decks vom Backend
  };

  const handleEditDeck = (id) => {
    // Hier könntest du die Logik implementieren, um zur Bearbeitungsseite des Decks zu navigieren.
    console.log(`Navigiere zur Bearbeitungsseite von Deck ${id}`);
  };

  const handleCreateNewDeck = () => {
    if (decks.length >= 3) {
      alert("Bereits maximale Anzahl an Decks erreicht");
    } else {
      // Hier könntest du den Code für die Navigation zur Seite für die Erstellung eines neuen Decks implementieren
      console.log("Navigiere zur Seite für die Erstellung eines neuen Decks");
    }
  };

  return (
    <div className="AppDeck">
      <h1>Meine Decks</h1>
      <button onClick={handleCreateNewDeck}>Neues Deck erstellen</button>
      <div className="deck-container">
        {decks.map((deck) => (
          <div key={deck.id} className="deck">
            <a href={`/decks/${deck.id}`}>{deck.name}</a>
            <button onClick={() => handleDeleteDeck(deck.id)}>Löschen</button>
            <button onClick={() => handleEditDeck(deck.id)}>Bearbeiten</button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default App;
