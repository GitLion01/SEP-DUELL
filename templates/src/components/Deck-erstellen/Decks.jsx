import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './Decks.css';
import BackButton from '../BackButton';
import Card from '../card';
import Modal from '../Modal';

function Decks() {
    const [id, setId] = useState(null);
    const [decks, setDecks] = useState([]);
    const [myCards, setMyCards] = useState([]);
    const [activeDeck, setActiveDeck] = useState(null);
    const [deckName, setDeckName] = useState('');
    const [originalDeckName, setCurrentDeckName] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [deckCards, setDeckCards] = useState([]);
    const [isEditing, setIsEditing] = useState(false);  // Zustand zum Überwachen, ob Änderungen gemacht wurden


    //Lädt die userID aus dem LocalStorage beim ersten Render der Komponente
    useEffect(() => {
        const loadId = async () => {
            const loadedId = localStorage.getItem('id');
            if (loadedId) {
                console.log('Geladene ID aus LocalStorage:', loadedId);  // Debugging
                setId(loadedId);
            } else {
                console.error('Keine userID im LocalStorage gefunden');
            }
        };
        loadId();
    }, []);

    // Karten und Decks von der Datenbank abrufen
    useEffect(() => {
        const loadData = async () => {
            if (id !== null) {
                try {
                    const cardsResponse = await axios.get(`http://localhost:8080/decks/cards/${id}`);
                    setMyCards(cardsResponse.data);
                } catch (error) {
                    console.error('Fehler beim Abrufen der Karten:', error);
                }

                try {
                    const decksResponse = await axios.get(`http://localhost:8080/decks/getUserDecks/${id}`);
                    setDecks(decksResponse.data);
                } catch (error) {
                    console.error('Fehler beim Abrufen der Decks:', error);
                }
            }
        };
        loadData();
    }, [id]);

    useEffect(() => {
        if (activeDeck !== null) {
            loadDeckCards(decks[activeDeck]?.name);
        }
    }, [decks, activeDeck]);

    const loadDecks = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/decks/getUserDecks/${id}`);
            setDecks(response.data);
        } catch (error) {
            console.error('Fehler beim Abrufen der Decks:', error);
        }
    };

    const loadCards = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/decks/cards/${id}`);
            setMyCards(response.data);
        } catch (error) {
            console.error('Fehler beim Abrufen der Karten:', error);
        }
    };
    useEffect(() => {
        if (id) {
            loadDecks();
            loadCards();
        }
    }, [id]);

    useEffect(() => {
        console.log('Updated decks:', decks);
    }, [decks]);

    const loadDeckCards = async (deckName) => {
        try {
            const response = await axios.get(`http://localhost:8080/decks/cards/${deckName}/${id}`);
            setDeckCards(response.data);
        } catch (error) {
            console.error('Fehler beim Abrufen der Deckkarten:', error);
        }
    };

    // Funktion zur Generierung eines zufälligen Namens
    const generateRandomName = () => {
        const pres = ["Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Theta", "Kappa", "Lambda", "Omega"];
        const nouns = ["Phoenix", "Dragon", "Tiger", "Unicorn", "Griffin", "Hyena"];
        const pre = pres[Math.floor(Math.random() * pres.length)];
        const noun = nouns[Math.floor(Math.random() * nouns.length)];
        return `${pre} ${noun}`;
    };


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

        // Erstelle neues Deck-Objekt als FormData
        const newDeck = {
            userID: id, //
            name: generateRandomName(),
            cardNames: []
        };

        try {
            // Senden der Anfrage an das Backend mit der FormData als JSON-String
            const response = await axios.post(`http://localhost:8080/decks/create`, JSON.stringify(newDeck),{
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            // Lade nach erfolgreichem Erstellen Decks und Karten neu
            await loadDecks();
            await loadCards();

            console.log('Deck erfolgreich erstellt', response.data);

        }
        catch (error) {
            console.error('Fehler beim Erstellen des Decks', error);
            if (error.response) {
                // Der Server antwortet mit einem Statuscode außerhalb des Bereichs 2xx
                console.error(error.response.data);
                console.error(error.response.status);
                console.error(error.response.headers);
                setErrorMessage('Fehler beim Erstellen des Decks: ' + (error.response?.data.message || error.message));
            }
            else if (error.request) {
                // Der Request wurde abgesetzt, aber es kam keine Antwort
                console.error(error.request);
                setErrorMessage('Keine Antwort vom Server beim Versuch, das Deck zu erstellen');
            }
            else {
                // Anderer Fehler beim Setup des Requests
                console.error('Error', error.message);
                setErrorMessage('Fehler beim Erstellen des Decks: ' + error.message);
            }
        }
    };


    // Karte zu einem aktiven Deck hinzufügen und an den Server senden
    const handleAddCardToDeck = async (card) => {
        if (activeDeck === null) {
            setErrorMessage('Kein aktives Deck ausgewählt.');
            return;
        }

        const deckToUpdate = decks[activeDeck];
        if (!deckToUpdate) {
            setErrorMessage('Aktives Deck konnte nicht identifiziert werden.');
            return;
        }

        const cardCountInDeck = deckCards.filter(c => c.name === card.name).length;
        const cardCountInMyCards = myCards.filter(c => c.name === card.name).length;

        if (cardCountInDeck >= cardCountInMyCards) {
            setErrorMessage('Sie können diese Karte nicht häufiger zu Ihrem Deck hinzufügen, als Sie sie besitzen.');
            return;
        }

        // Formulardaten definieren zum Senden an Backend
        const dataToSend = {
            userID: id,
            name: deckToUpdate.name,
            cardNames: [card.name]
        };

        // Senden des Formulars an den Server als JSON-String
        try {
            const response = await axios.put(`http://localhost:8080/decks/addCards`, JSON.stringify(dataToSend), {
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            console.log('Response after adding card:', response.data);  // Log the response

            // Laden der Decks und Karten nach erfolgreichem Hinzufügen
            await loadDecks();
            await loadCards();

            console.log("Karte erfolgreich hinzugefügt", response.data);
            console.log(decks[activeDeck]);

        }
        catch (error) {
            console.error('Fehler beim Hinzufügen der Karte', error);
            setErrorMessage('Fehler beim Hinzufügen der Karte: ' + (error.response?.data.message || error.message));
        }

    };

    // Wird aufgerufen, wenn ein Deck in der UI ausgewählt wird
    const selectDeck = (index) => {
        if (!isEditing) {
            setActiveDeck(index);
            setCurrentDeckName(decks[index].name);
            setDeckName(decks[index].name);
            setIsEditing(true);
        } else {
            setErrorMessage('Bitte speichern oder verwerfen Sie die Änderungen bevor Sie das Deck wechseln.');
            console.log(errorMessage)
        }
    };

    // Behandlung der Deck-Namensänderung
    const handleSaveDeckName = async () => {

        // überprüft, dass der neue Deckname nicht leer ist
        if (!deckName.trim()) {
            setErrorMessage('Der Deckname darf nicht leer sein');
            return;
        }

        // wenn Deckname nicht geändert wurde, soll nichts passieren
        if (deckName === originalDeckName) {
            return;
        }

        // sendet Anfrage an den Server, den alten Decknamen mit dem Neuen zu ersetzen
        try {
            // Senden der Anfrage zum Backend, um den Namen zu aktualisieren
            const response = await axios.put(`http://localhost:8080/decks/updateName/${id}/${originalDeckName}/${deckName}`);
            console.log('Deckname erfolgreich geändert!', response.data);

            // nach erfolgreichem ändern sollen Decks und Karten neu geladen und der Bearbeitungsmodus beendet werden
            await loadDecks();
            await loadCards();
            setActiveDeck(null);
            setIsEditing(false);

        } catch (error) {
            console.error('Fehler beim Ändern des Decknamens:', error);
            setErrorMessage('Fehler beim Ändern des Decknamens: ' + error.message);
        }
    };

    // Funktion für das Drücken des Fertig-Buttons
    const handleFinishEditing = () => {
        // Stellt den ursprünglichen Namen wieder her, wenn nicht gespeichert wurde
        setDeckName(originalDeckName);
        setActiveDeck(null);
        setIsEditing(false);
    }

    // Funktion für das Löschen des aktiven Decks
    const handleDeleteDeck = async () => {

        const deckToDelete = decks[activeDeck];
        if (!deckToDelete) {
            setErrorMessage('Zu löschendes Deck konnte nicht identifiziert werden.');
            return;
        }

        // Daten werden als ID-Deckname- Paar gespeichert
        const dataToSend = {
            [id]: deckToDelete.name,
        };

        // senden der DELETE-Anfrage an Server
        try {
            const response = await axios.delete(`http://localhost:8080/decks/delete`, {
                headers: {
                    'Content-Type': 'application/json'
                },
                data: dataToSend
            });

            // Beenden des Bearbeitungsmodus
            setActiveDeck(null);
            setIsEditing(false);

            await loadDecks();
            await loadCards();
            await loadDeckCards(deckName);

            console.log("Deck erfolgreich gelöscht", response.data);

        }
        catch (error) {
            console.error('Fehler beim Löschen des Decks', error);
            setErrorMessage('Fehler beim Löschen des Decks: ' + (error.response?.data.message || error.message));
        }
    };

    // Karten aus dem Deck entfernen
    const handleRemoveCardFromDeck = async(cardName) => {

        const deckToUpdate = decks[activeDeck];
        if (!deckToUpdate) {
            setErrorMessage('Aktives Deck konnte nicht identifiziert werden.');
            return;
        }

        // Formulardaten definieren
        const dataToSend = {
            userID: id,
            name: deckToUpdate.name,
            cardNames: [cardName]
        };

        // Senden der Anfrage an den Server mit Formulardaten
        try {
            const response = await axios.put(`http://localhost:8080/decks/removeCard`, JSON.stringify(dataToSend), {
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            await loadDecks();
            await loadCards();

            await loadDeckCards(deckName);


            console.log("Karte erfolgreich entfernt", response.data);

        }
        catch (error) {
            console.error('Fehler beim Entfernen der Karte', error);
            setErrorMessage('Fehler beim Entfernen der Karte: ' + (error.response?.data.message || error.message));
        }

    }

    // Fehlermeldung zurücksetzen
    const clearErrorMessage = () => setErrorMessage('');

    return (

        <div className="container">
            <BackButton />
            <h1>Meine Decks</h1>
            {errorMessage && (
                <Modal message={errorMessage} onClose={clearErrorMessage}>
                    {errorMessage}
                </Modal>
            )}
            <button onClick={handleCreateNewDeck} disabled={isEditing}>Neues Deck erstellen</button>
            <div className="wrap">
                <div className="deck-list">
                    {decks && decks.length > 0 && decks.map((deck, index) => (

                        <div key={index}
                             className="deck"
                             onClick={() => selectDeck(index)}>
                            {deck.name}
                        </div>

                    ))}
                </div>
                <div className="inside">
                    <div className="cards-left">
                        <h2>Verfügbare Karten</h2>
                        <div className="card-list">
                            {myCards.reduce((acc, card) => {
                                const existingCard = acc.find(c => c.name === card.name);
                                if (existingCard) {
                                    existingCard.count += 1;
                                } else {
                                    acc.push({...card, count: 1});
                                }
                                return acc;
                            }, []).map(card => {
                                let availableCount = myCards.filter(c => c.name === card.name).length;
                                if (activeDeck !== null && deckCards) {
                                    const cardCountInDeck = deckCards.filter(c => c.name === card.name).length;
                                    availableCount -= cardCountInDeck;
                                }

                                return (
                                    <div key={card.id} className="card">
                                        <Card card={card} onCardClick={() => {
                                            if (activeDeck != null) {
                                                handleAddCardToDeck(card);
                                            }
                                        }}/>
                                        <span>Verfügbar: {availableCount}</span>
                                    </div>
                                );
                            })}
                        </div>
                    </div>

                    {activeDeck !== null && (

                        <div className="active-deck-container">
                            <h2>Aktives Deck: {decks[activeDeck].name}</h2>
                            <input type="text" value={deckName} onChange={(e) => setDeckName(e.target.value)}/>
                            <div className="button-container">
                                <button onClick={handleFinishEditing}>Fertig</button>
                                <button onClick={handleDeleteDeck}>Deck Löschen</button>
                                <button onClick={handleSaveDeckName}
                                        disabled={!deckName.trim() || deckName === originalDeckName}>Name speichern
                                </button>
                            </div>
                            <div className="card-container">
                                {deckCards.map((card, index) => (
                                    <div key={index} className="card">
                                        <Card card={card} onCardClick={() => handleRemoveCardFromDeck(card.name)}/>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                    <div>
                    </div>
                </div>
            </div>
        </div>

    );
}
export default Decks;