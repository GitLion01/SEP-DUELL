import './App.css';
import React, { Component } from 'react';
import Register from './Registrierung/Register';

import DeckEditor from './Deckarbeiten/DeckEditor';


// <Register></Register>

class App extends Component {
  state = {  } 
  render() { 
    return <React.Fragment>
        <DeckEditor></DeckEditor>

    </React.Fragment>
    
    
  }
}
 
export default App;


