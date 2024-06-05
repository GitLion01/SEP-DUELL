// Admin.jsx
import React, { Component } from 'react';
import axiosInstance from '../../api/axios';
import './Admin.css';
import Card from '../card';
import BackButton from '../BackButton';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

class Admin extends Component {
    state = {
        cards: [],
        selectedCards: [],
        searchTerm: '',
        loading: false,
        error: null,
        rarityFilter: ''
    }

    componentDidMount() {
        this.fetchCards();
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
            console.log('Response data:', response.data);
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
        if (this.state.selectedCards.length === 0) {
            toast.warn('Bitte wählen Sie mindestens eine Karte zum Löschen aus.');
            return;
        }
        this.setState({ loading: true, error: null });
        try {
            const response = await axiosInstance.post('/cards/delete', this.state.selectedCards);
            toast.success('Karten erfolgreich gelöscht.');
            this.setState({ selectedCards: [] });
            this.fetchCards();
        } catch (error) {
            this.setState({ loading: false, error: 'Fehler beim Löschen der Karten' });
            toast.error('Fehler beim Löschen der Karten');
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

    handleCardSelect = (cardName, isSelected) => {
        this.setState(prevState => {
            const selectedCards = isSelected
                ? [...prevState.selectedCards, cardName]
                : prevState.selectedCards.filter(name => name !== cardName);
            return { selectedCards };
        });
    };

    handleCheckboxChange = (cardName) => (event) => {
        const { checked } = event.target;
        this.handleCardSelect(cardName, checked);
    };

    handleSelectAll = () => {
        const { cards, selectedCards } = this.state
        if (selectedCards.length === cards.length) {
            this.setState({ selectedCards: [] });
        }

        else {
            const allCardNames = cards.map(card => card.name) 
            this.setState({ selectedCards: allCardNames });
        }
    }

    render() {
        const { cards, searchTerm, loading, rarityFilter, selectedCards } = this.state;
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
                        <button type="button" onClick={this.handleSelectAll}>
                            {selectedCards.length === cards.length ? 'Alle Abwählen' : 'Alle Auswählen'}
                        </button>
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
                                <div key={index} className="cardWithCheckbox">
                                    <input
                                        type="checkbox"
                                        checked={selectedCards.includes(card.name)}
                                        onChange={this.handleCheckboxChange(card.name)}
                                    />
                                    <Card card={card} />
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        );
    }
}

export default Admin;
