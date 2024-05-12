import { SET_USER_ID } from './actions'; // Importiere den Aktionstyp für das Setzen der Benutzer-ID

// Definiere den Anfangszustand des Redux-Stores
const initialState = {
  userId: null // Initialisiere die Benutzer-ID mit null
};

// Reduzierer-Funktion, die den Zustand des Stores verwaltet
const rootReducer = (state = initialState, action) => {
  switch (action.type) { // Überprüfe den Typ der Aktion
    case SET_USER_ID:
      return {
        ...state, // Erhalte den vorherigen Zustand
        userId: action.payload // Setze die Benutzer-ID im Zustand mit den Daten aus der Aktion
      };
    // Weitere Reduziererfälle können hier hinzugefügt werden...
    default:
      return state; // Gib den vorherigen Zustand zurück, wenn der Aktionstyp nicht übereinstimmt
  }
};

export default rootReducer
