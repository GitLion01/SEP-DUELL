import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { Provider } from 'react-redux'; // Importiere Provider aus react-redux
import store from './redux/store'; // Importiere den Redux-Store
import 'bootstrap/dist/css/bootstrap.min.css';


const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
    <Provider store={store}>
    <App /> 
    </Provider>
)   

