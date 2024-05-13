
// Definiere den Aktionstyp für das Setzen der Benutzer-ID
export const SET_USER_ID = 'SET_USER_ID';

// Action Creator-Funktion, die eine Aktion zurückgibt, um die Benutzer-ID im Store zu setzen
export const setUserId = (userId) => ({
  type: SET_USER_ID,  // Typ der Aktion, um den Reduzierer zu identifizieren
  payload: userId  // Nutzlast (Daten), die mit der Aktion übergeben werden sollen
});

// Weitere Aktionen können hier definiert werden...
