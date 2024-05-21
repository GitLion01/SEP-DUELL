import React, { Component } from 'react';
import axiosInstance from '../../api/axios';
import './Admin.css';
import Card from '../card';
import BackButton from '../BackButton';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

class Admin extends Component {
    state = {
        cards: [], // Initialisiert den State mit einem leeren Array für Karten
        searchTerm:'', // Initialisiert den State mit einem leeren String für den Suchbegriff
        loading: false,
        error: null,
        rarityFilter: ''
    }

    componentDidMount() {
        this.fetchCards(); // Ruft die Funktion fetchCards auf, sobald die Komponente in den DOM eingefügt wird
    }

    fetchCards = async () => {
        this.setState({ loading: true, error: null });
        try {
            const response = await axiosInstance.get('/cards');
            this.setState({ cards: response.data, loading: false });
        } catch (error) {
            this.setState({ loading: false, error: 'Fehler beim Abrufen der Karten' });
            toast.error('Fehler beim Abrufen der Karten');
        }
    };

    handleUpload = async (event) => {
        event.preventDefault();
        const file = event.target.files[0];
        const formData = new FormData();
        formData.append('file', file);

        this.setState({ loading: true, error: null });

        try {
            const response = await axiosInstance.post('/cards/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            console.log('Response data:', response.data); // Überprüfen, was genau vom Server zurückkommt
            console.log('Datei erfolgreich hochgeladen.');
            toast.success('Datei erfolgreich hochgeladen.');
            event.target.value = null;
            this.fetchCards();
        } catch (error) {
            this.setState({ loading: false, error: 'Fehler beim Hochladen der Datei' });
            toast.error('Fehler beim Hochladen der Datei');
        }
    };

    handleSearchChange = (event) => {
        this.setState({ searchTerm: event.target.value });
    };

    handleSearch = async (event) => {
        event.preventDefault();
        if (!this.state.searchTerm.trim()) {
            toast.warn('Bitte geben Sie einen Suchbegriff ein.');
            return;
        }
        this.setState({ loading: true, error: null });
        try {
            const response = await axiosInstance.get(`/cards/findByName/${this.state.searchTerm}`);
            this.setState({ cards: response.data ? [response.data] : [], loading: false });
        } catch (error) {
            this.setState({ loading: false, error: 'Fehler beim Suchen' });
            toast.error('Fehler beim Suchen');
        }
    };

    handleDelete = async (event) => {
        event.preventDefault();
        if (!this.state.searchTerm.trim()) {
            toast.warn('Bitte geben Sie einen Kartenname zum Löschen ein.');
            return;
        }
        this.setState({ loading: true, error: null });
        try {
            const response = await axiosInstance.post('/cards/delete', [this.state.searchTerm]);
            toast.success('Karte erfolgreich gelöscht.');
            this.fetchCards();
        } catch (error) {
            this.setState({ loading: false, error: 'Fehler beim Löschen der Karte' });
            toast.error('Fehler beim Löschen der Karte');
        }
    };

    handleRarityFilterChange = (event) => {
        this.setState({ rarityFilter: event.target.value });
    };

    getFilteredCards = () => {
        const { cards, rarityFilter } = this.state;
        if (!rarityFilter) return cards;
        return cards.filter(card => card.rarity === rarityFilter);
    };

    render() {
        const { cards, searchTerm, loading, rarityFilter } = this.state;
        console.log(cards);
        const filteredCards = this.getFilteredCards();

        return (
            <div className='AdminPanel__body'>
                <ToastContainer />
                <BackButton />
                <div className="AdminPanel">
                    <h1>Admin Panel</h1>
                    <form onSubmit={this.handleUpload}>
                        <label htmlFor="csvFile">Wählen Sie eine CSV-Datei aus:</label>
                        <input
                            type="file"
                            id="csvFile"
                            name="csvFile"
                            accept=".csv"
                            onChange={this.handleUpload}
                            required
                        />
                    </form>
                    <h2>Vorhandene Kartentypen</h2>
                    <form className='Sachen'>
                        <input
                            type="text"
                            placeholder="Karte suchen"
                            value={searchTerm}
                            onChange={this.handleSearchChange}
                            required
                        />
                        <button type="submit" onClick={this.handleSearch}>Suchen</button>
                        <button type="button" onClick={this.handleDelete}>Löschen</button>
                        <button type="button" onClick={this.fetchCards}>Alle anzeigen</button>
                        <label htmlFor="rarityFilter">Seltenheit filtern:</label>
                        <select id="rarityFilter" className="select-style" value={rarityFilter} onChange={this.handleRarityFilterChange}>
                            <option value="">Alle</option>
                            <option value="NORMAL">Normal</option>
                            <option value="RARE">Selten</option>
                            <option value="LEGENDARY">Legendär</option>
                        </select>
                    </form>
                        {loading ? (
                            <p>Loading...</p>
                        ) : (
                            <div id="existingCards" style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '20px' }}>
                                {filteredCards.map((card, index) => (
                                    <Card key={index} card={card} />
                                ))}
                            </div>
                        )}
                </div>
            </div>
        );
    }
}

export default Admin;
