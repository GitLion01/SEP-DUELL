import React, { Component } from 'react';

class Card extends Component {
  render() {
    const { name, rarity, attackPoints, defensePoints, description, picture } = this.props.card;
    
{/* HTML Code von Bootstrap*/}
    return (
      <div className="card" style={{ width: "18rem" }}>
        <img src={picture} className="card-img-top" alt={name} />
        <div className="card-body">
          <h5 className="card-title">{name}</h5>
          <p className="card-text">{description}</p>
        </div>
        <ul className="list-group list-group-flush">
          <li className="list-group-item">Seltenheit: {rarity}</li>
          <li className="list-group-item">Angriffspunkte: {attackPoints}</li>
          <li className="list-group-item">Verteidigungspunkte: {defensePoints}</li>
        </ul>
    {/*    <div className="card-body">
          <a href="#" className="card-link">Card link</a>
          <a href="#" className="card-link">Another link</a>
        </div> */}
    </div> 
    );
  }
}

export default Card;
