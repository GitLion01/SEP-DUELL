import React, { createContext } from 'react';

export const DuelContext = createContext();

export const DuelProvider = ({ children }) => {
  return (
    <DuelContext.Provider value={{}}>
      {children}
    </DuelContext.Provider>
  );
};
