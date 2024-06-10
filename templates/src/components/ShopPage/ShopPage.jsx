import React, { Component } from 'react';
import axiosInstance from '../../api/axios';
import './ShopPage.css';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import BackButton from '../BackButton';
import Card from '../card';

class ShopPage extends Component {
    state = { 
        sepCoins: '', 
        newCards: [], 
        id: localStorage.getItem('id'), 
        cards: []
    };

    componentDidMount() {
        this.getsepCoins();
        this.fetchCards();
        this.checkAndRechargeCoins();
    }

    fetchCards = async () => {
        try {
            const response = await axiosInstance.get('/cards');
            this.setState({ cards: response.data });
        } catch {
            console.log('Fehler beim Laden der Karten');
        }
    }

    getsepCoins = async () => {
        try {
            const response = await axiosInstance.get(`/profile/${this.state.id}`);
            this.setState({ sepCoins: response.data.sepCoins }, this.checkAndRechargeCoins);
        } catch (error) {
            console.error(error);
            this.setState({ sepCoins: 10000 }); // Da für Admin nichts angezeigt wird
        }
    }

    checkAndRechargeCoins = async () => {
        if (this.state.sepCoins === 0) {
            try {
                await this.updateSepCoins(500);
                toast.info('SEP-Coins wurden automatisch auf 500 aufgeladen.');
            } catch (error) {
                console.error('Fehler beim automatischen Aufladen der Münzen', error);
            }
        }
    }
     
    openLootbox = async (type) => {
        const cost = type === 'bronze' ? 50 : type === 'silver' ? 150 : 250; 
        if (cost > this.state.sepCoins) {
            toast.error('Du hast nicht genug Münzen');
            return; 
        }
    
        const newCards = []; 
        for (let i = 0; i < 5; i++) {
            newCards.push(this.getRandomCard(type)); 
        }
    
        this.setState((prevState) => {
            const newSepCoins = prevState.sepCoins - cost;
            return {
                newCards: newCards,
                sepCoins: newSepCoins
            };
        }, () => {
            this.checkAndRechargeCoins();
            this.updateSepCoins(this.state.sepCoins);
        });
    
        await this.setUserCards(newCards);
    }
    

    updateSepCoins = async (sepCoins) => {
        try {
            const response = await axiosInstance.put(`/profile/${this.state.id}`, null, { params: { sepCoins } });
            this.getsepCoins(); 
        } catch (error) {
            console.error(error + ' Fehler beim Aktualisieren der Münzen');
        }
    }

    setUserCards = async (newCards) => {
        try {
            const cardNames = newCards.map(card => card.name);  // Extrahiere die Namen der Karten
            const response = await axiosInstance.post(`/cards/${this.state.id}/addCards`, cardNames);  // Sende die Namen der Karten
            console.log(response);
        } catch (error) {
            console.error(error + ' Fehler beim Senden der neuen Karten');
        }
    }

    getRandomCard = (type) => {
        const random = Math.random();
        let rarity;
        
        if (type === 'bronze') {
            rarity = random <= 0.05 ? 'LEGENDARY' : random <= 0.2 ? 'RARE' : 'NORMAL';
        } else if (type === 'silver') {
            rarity = random <= 0.1 ? 'LEGENDARY' : random <= 0.3 ? 'RARE' : 'NORMAL';
        } else if (type === 'gold') {
            rarity = random <= 0.15 ? 'LEGENDARY' : random <= 0.4 ? 'RARE' : 'NORMAL';
        }

        const cardsRarity = this.state.cards.filter(card => card.rarity === rarity); 
        const randomCard = cardsRarity[Math.floor(Math.random() * cardsRarity.length)];
        return randomCard; 
    }
    
    render() { 
        return (
            <div className='ShopPage_body'>
                <ToastContainer />
                <BackButton/>
                <h1>Karten Shop</h1>
                <div className="sep-coins">SEP-Coins: {this.state.sepCoins}</div>
                <button className="bronze" onClick={() => this.openLootbox('bronze')}>Kauf Bronze Pack <br />(50 SEP-Coins)</button>
                <button className="silver" onClick={() => this.openLootbox('silver')}>Kauf Silver Pack <br/> (150 SEP-Coins)</button>
                <button className="gold" onClick={() => this.openLootbox('gold')}>Kauf Gold Pack <br/> (250 SEP-Coins)</button>
                <div id="gezogeneCards" style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '20px' }}>
                    {this.state.newCards.map((card, index) => (
                        <Card key={index} card={card} />
                    ))}
                </div>
            </div>
        );
    }
}

export default ShopPage;
