import React, { Component } from 'react';
import './Startsite.css';


class Startseite extends Component {
  render() { // Definiere die render-Methode
    return (
      <div className="App">
        <header>
          <h1>STARTSEITE</h1>
        </header>
        <main>
          <div className="centered-content">
            <section className="deck">
              <h2>Mein Kartendeck</h2>
              {/* Hier könnten die Karten des Spielers angezeigt werden */}
            </section>
            <section className="friends">
              <h2>Meine Freunde</h2>
              {/* Hier könnten die Freunde des Spielers angezeigt werden */}
            </section>
          </div>
        </main>
        <footer>
          <nav>
            <ul>
              <li><a href="/profil">Mein Profil</a></li>
              {/* Weitere Navigationslinks könnten hier hinzugefügt werden */}
            </ul>
          </nav>
        </footer>
      </div>
    );

  }
}
 
export default Startseite;

