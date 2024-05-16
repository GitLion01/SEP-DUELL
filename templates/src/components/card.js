    import React, { Component } from 'react';
    import 'bootstrap/dist/css/bootstrap.css';
    import './card.css';

    class Card extends Component {
      constructor(props) {
        super(props);
        this.state = {
          showBack: false
        };
      }

      toggleCard = () => {
        this.setState(prevState => ({ showBack: !prevState.showBack }));
      }

      render() {
        const { name, rarity, attackPoints, defensePoints, description, image } = this.props.card;
        const { showBack } = this.state;

        return (
          <div className="card" style={{ width: "18rem", height: "25rem", position: 'relative' }}>
            <div className="card-rarity">
              <h6>Seltenheit: {rarity}</h6>
            </div>
            <div className="card-body d-flex flex-column">
              {showBack ? (
                <div className="card-text">{description}</div>
              ) : (
                <>
                  <img src={`data:image/jpeg;base64,${image}`} className="card-img-top" alt={name} />
                <div style= {{ position: 'absolute', bottom: '45px', left:0, right:0}}>
                  <h5 className="card-title text-center">{name}</h5>
                  </div>
                <div style={{ position: 'absolute', bottom: '10px', left: '10px' }} className="stats-circle defense-points">
                  {defensePoints}
                </div>
                <div style={{ position: 'absolute', bottom: '10px', right: '10px' }} className="stats-circle attack-points">
                  {attackPoints}
                </div>
                </>
              )}
            </div>
            <div style={{ position: 'absolute', bottom: '10px', left: 0, right: 0, textAlign: 'center' }}>
                <button onClick={this.toggleCard} className="btn btn-primary btn-sm">
                  {showBack ? 'Zurück zur Vorderseite' : 'Beschreibung'}
                </button>
            </div>
          </div>
        );
      }
    }

    export default Card;
