    import React, { Component } from 'react';
    import 'bootstrap/dist/css/bootstrap.css';
    import './card.css';
    //Teilweise ein paar Sachen von Bootstrap

    class Card extends Component {
      constructor(props) {
        super(props);
        this.state = {
          showBack: false
        };
      }

      toggleCard = (event) => {
        event.stopPropagation(); // Verhindert das Auslösen des übergeordneten onClick-Events
        this.setState(prevState => ({ showBack: !prevState.showBack }));
      }

      handleCardClick = () => {
        const { onCardClick, card } = this.props;
        if (onCardClick) {
          onCardClick(card);
        }
      }

      render() {
        const { card } = this.props; // onCardClick wird aus den Props entnommen
        const { name, rarity, attackPoints, defensePoints, description, image } = this.props.card;
        const { showBack } = this.state;

        return (
          <div className="card" onClick={this.handleCardClick}>
            <div className="card-rarity">
              <h6>{rarity}</h6>
            </div>
            <div className="card-body d-flex flex-column">
              {showBack ? (
                <div className="card-description">{description}</div>
              ) : (
                <>
                  <img src={`data:image/jpeg;base64,${image}`} className="card-img-top" alt={name} />
                <div style= {{ position: 'absolute', bottom: '35px', left:0, right:0, textAlign: 'center'}}>
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
            <div style={{ position: 'absolute', bottom: '-1px', left: 0, right: 0, textAlign: 'center' }}>
                <button onClick={this.toggleCard} className="btn btn-primary btn-sm" style={{ marginTop: '5px' }}>
                  {showBack ? 'Vorderseite' : 'Rückseite'}
                </button>
            </div>
          </div>
        );
      }
    }

    export default Card;
