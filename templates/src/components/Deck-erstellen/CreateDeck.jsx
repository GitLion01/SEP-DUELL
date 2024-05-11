import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import './CreateDeck.css';

function CreateDeck() {
    const { id } = useParams();
    const [decks, setDecks] = useState([]);
    const [myCards, setMyCards] = useState([]);
    const [activeDeck, setActiveDeck] = useState(null);
    const [deckName, setDeckName] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [isEditing, setIsEditing] = useState(false);  // Zustand zum Überwachen, ob Änderungen gemacht wurden
    const [updateTrigger, setUpdateTrigger] = useState(0); // Zählt, wie oft Updates notwendig waren
    const [formData, setFormData] = useState({
        userID: id,
        name:"",
        cardNames:[]
    });

    const handleInputChange = (event) => {
        const { name, value} = event.target;
        setFormData(prev => ({
            ...prev,
            [name]: value // Aktualisiere den entsprechenden Wert im formData Objekt
        }));
    };


    // Karten von der Datenbank abrufen
    useEffect(() => {

        // Abrufen der Karten des Spielers
        axios.get(`http://localhost:8080/cards`)
            .then(response => setMyCards(response.data))
            .catch(error => console.error('Fehler beim Abrufen der Karten:', error));

        // Abrufen der Decks
        axios.get(`http://localhost:8080/decks/getUserDecks/${id}`)
            .then(response => setDecks(response.data))
            .catch(error => console.error('Fehler beim Abrufen der Decks:', error));

    }, [id, updateTrigger]);

    // Neues Deck hinzufügen
    const handleCreateNewDeck = async () => {
        if (decks.length >= 3) {
            setErrorMessage('Es können maximal drei Decks erstellt werden.');
            return;
        }
        if (isEditing) {
            setErrorMessage('Bitte speichern Sie die Änderungen am aktiven Deck, bevor Sie ein neues Deck erstellen.');
            return;
        }

        // Erstelle neues Deck-Objekt
        const newDeck = {
            userID: id, //
            name: `Deck ${decks.length + 1}`,
            cardNames: [""]
        };

        try {
            // Senden der Anfrage an das Backend
            const response = await axios.post(`http://localhost:8080/decks/create`, JSON.stringify(newDeck),{
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            console.log('Deck erfolgreich erstellt', response.data);

        }
        catch (error) {
            console.error('Fehler beim Erstellen des Decks', error);
            setErrorMessage('Fehler beim Erstellen des Decks: ' + (error.response?.data.message || error.message));
        }
    };
    

    // Karte zu einem aktiven Deck hinzufügen und an den Server senden
    const handleAddCardToDeck = (card) => {
        if (activeDeck === null) {
            setErrorMessage('Kein aktives Deck ausgewählt.');
            return;
        }
    
        const deckToUpdate = decks[activeDeck];
        if (!deckToUpdate) {
            setErrorMessage('Aktives Deck konnte nicht identifiziert werden.');
            return;
        }
    
        const dataToSend = {
            deckName: deckToUpdate.name,
            cardsToAdd: [card.name]
        };
    
        axios.put(`http://localhost:8080/decks/addCards`, JSON.stringify(dataToSend))
            .then(response => {
                console.log('Karte erfolgreich zum Deck hinzugefügt', response.data);
                setUpdateTrigger(prev => prev + 1); // Trigger die useEffect Hook, um die Decks neu zu laden
                setErrorMessage(''); // Klare Fehlermeldung, wenn Erfolg
            })
            .catch(error => {
                console.error('Fehler beim Hinzufügen der Karte zum Deck:', error);
                setErrorMessage('Fehler beim Hinzufügen der Karte: ' + (error.response?.data.message || error.message));
            });
    };
    

    // Decknamen ändern
    const handleChangeDeckName = (e) => {
        setIsEditing(true);
        setDeckName(e.target.value);
        const updatedDecks = [...decks];
        updatedDecks[activeDeck].name = e.target.value;
        setDecks(updatedDecks);
    };

    const handleDeleteDeck = () => {
        if (activeDeck !== null) {
            const deckName = decks[activeDeck].name; // Festhalten der ID des aktiven Decks
            axios.delete(`http://localhost:8080/${id}decks/${deckName}`)
                .then(() => {
                    // Nach erfolgreichem Löschen im Backend, entferne das Deck auch lokal
                    const updatedDecks = decks.filter((_, index) => index !== activeDeck);
                    setDecks(updatedDecks);
                    setActiveDeck(null); // Setzt das aktive Deck zurück
                    setErrorMessage(''); // Zurücksetzen der Fehlermeldung
                    setIsEditing(false); // Beenden des Edit-Zustands
                    alert('Deck erfolgreich gelöscht.');
                })
                .catch(error => {
                    // Fehlerbehandlung, falls das Löschen im Backend fehlschlägt
                    setErrorMessage('Fehler beim Löschen des Decks: ' + (error.response?.data.message || error.message));
                });
        } else {
            setErrorMessage('Kein Deck ausgewählt zum Löschen.');
        }
    };

    // Fertig mit der Bearbeitung
    const handleFinish = () => {
        if (!deckName.trim()) {
            setErrorMessage('Der Deckname darf nicht leer sein');
            return;
        }



        axios.post(`http://localhost:8080/${id}/decks/`, decks[activeDeck])
            .then(() => {
                console.log('Deck gespeichert!');
                setActiveDeck(null);
                setIsEditing(false);
            })
            .catch(error => console.error('Fehler beim Speichern des Decks:', error));
    };

    const clearErrorMessage = () => setErrorMessage('');

    return (
        <div className="container">
            <h1>Meine Decks</h1>
            {errorMessage && (
                <div className="error-message" onClick={clearErrorMessage}>
                    {errorMessage}
                </div>
            )}
            <button onClick={handleCreateNewDeck} disabled={isEditing}>Neues Deck erstellen</button>
            <div className="deck-list">
                {decks.map((deck, index) => (
                    <div key={index} className="card" onClick={() => {
                        if (!isEditing) {
                        setActiveDeck(index);
                        setDeckName(deck.name);
                        }
                        else {
                            setErrorMessage('Bitte speichern oder verwerfen Sie die Änderungen bevor Sie das Deck wechseln.');
                        }
                    }}>
                        {deck.name}
                    </div>
                ))}
            </div>
            {activeDeck !== null && (
                <div className="cards-container">
                    <div className="cards-left">
                        <h2>Verfügbare Karten</h2>
                        {myCards.map(card => (
                            <div key={card.id} className="card" onClick={() => handleAddCardToDeck(card)}>
                                {card.name}
                            </div>
                        ))}
                    </div>
                    <div className="active-deck-container">
                        <h2>Aktives Deck: {decks[activeDeck].name}</h2>
                        <input type="text" value={deckName} onChange={handleChangeDeckName} />
                        <button onClick={handleDeleteDeck}>Deck Löschen</button>
                        {decks[activeDeck].cards.map((card, index) => (
                            <div key={index} className="card">{card.name}</div>
                        ))}
                        <button onClick={handleFinish} disabled={!deckName.trim()}>Fertig</button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default CreateDeck;
