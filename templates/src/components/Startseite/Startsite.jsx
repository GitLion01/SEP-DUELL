import React, { Component } from 'react';
import './Startsite.css';
 
class Startseite extends Component {
  constructor(props) {
    super(props);
    this.state = {
      loggedIn: true,
      friends: [], // Zustand für die Freundesliste
      searchTerm: '', // Zustand für die Suchanfrage
    };
  }
 
  componentDidMount() {
    // Hier rufe die Funktion zum Abrufen der Freundesliste auf
    this.fetchFriends();
  }
 
  fetchFriends = async () => {
    try {
      // Backend-Endpunkt für die Freundesliste anpassen
      const response = await fetch('/api/friends');//  den Backend-Endpunkt /api/friends entsprechend deiner Backend-Konfiguration anpasst, um die Freundesliste abzurufen
      const data = await response.json();
      this.setState({ friends: data });
    } catch (error) {
      console.error('Error fetching friends:', error);
    }
  };
 
  handleLogout = () => {
    this.setState({ loggedIn: false });
  };
 
  handleSearch = (event) => {
    this.setState({ searchTerm: event.target.value });
  };
 
  render() {
    const { loggedIn, friends, searchTerm } = this.state;
 
    if (!loggedIn) {
      console.log("Benutzer ausgeloggt, Weiterleitung zur Login-Seite");
    }
 
    // Filtere die Freundesliste basierend auf dem Suchbegriff
    const filteredFriends = friends.filter((friend) =>
      friend.name.toLowerCase().includes(searchTerm.toLowerCase())
    );
 
    return (
      <div className="AppStart">
        <header>
          <h1>STARTSEITE</h1>
        </header>
        <main>
          <div className="centered-content">
            <section className="spiel">
              <a href="/spielen"><h2>SPIEL START</h2></a>
            </section>
            <section className="meinprofile">
              <a href="/profile"><h2>MEIN PROFIL</h2></a>
            </section>
            <section className="meindeck">
              <a href="/create-deck"><h2>MEIN DECK</h2></a>
            </section>
            <section className="freundesliste">
              <h2>MEINE FREUNDESLISTE</h2>
              <input
                type="text"
                placeholder="Freund suchen"
                value={searchTerm}
                onChange={this.handleSearch}
              />
              <div className="friend-list">
                <ul>
                  {filteredFriends.map((friend) => (
                    <li key={friend.id}>{friend.name}</li>
                  ))}
                </ul>
              </div>
            </section>
            <section className="adminsteuerfeld">
              <a href="/admin"><h2>MEIN ADMINSTEUERFELD</h2></a>
            </section>
          </div>
        </main>
        <footer>
          <nav>
            <ul>
              <li><a href="/" onClick={this.handleLogout}>Abmelden</a></li>
            </ul>
          </nav>
        </footer>
      </div>
    );
  }
}
 
export default Startseite;