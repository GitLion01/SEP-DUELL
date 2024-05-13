import React, { Component } from 'react';
import './Startseite.css';

class Startseite extends Component {
  constructor(props) {
    super(props);
    this.state = {
      loggedIn: true,

    };
  }


  handleLogout = () => {
    this.setState({ loggedIn: false });
  };


  render() {
    const { loggedIn  } = this.state;

    if (!loggedIn) {
      console.log("Benutzer ausgeloggt, Weiterleitung zur Login-Seite");
    }



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
                <a href="/profil/:id"><h2>MEIN PROFIL</h2></a>
              </section>
              <section className="meindeck">
                <a href="/create-deck"><h2>MEIN DECK</h2></a>
              </section>
              <section className="freundesliste">
                <a href="/freundelist"> <h2>MEINE FREUNDESLISTE</h2></a>
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