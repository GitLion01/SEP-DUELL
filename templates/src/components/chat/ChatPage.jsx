// components/chat/ChatPage.jsx
import React, { useState } from 'react'; // useEffect entfernt, da es nicht verwendet wird
import FriendListForChat from './FriendListForChat'; // Importiert die FriendListForChat-Komponente
import ChatWindow from './ChatWindow'; // Importiert die ChatWindow-Komponente
import CreateGroupForm from './CreateGroupForm'; // Importiert die CreateGroupForm-Komponente
import './ChatPage.css'; // Importiert die CSS-Datei für die ChatPage-Komponente
import BackButton from '../BackButton';
import { WebSocketProvider } from '../../WebSocketProvider';


function ChatPage() {
  const [selectedChat, setSelectedChat] = useState({ chatTarget: null, type: null });
  const [creatingGroup, setCreatingGroup] = useState(false); // Zustand für das Erstellen einer neuen Gruppe (true/false)

  // const [friends, setFriends] = useState([]); // Entfernt, da es nicht verwendet wird

  const handleSelect = (chatTarget, type) => {
    setSelectedChat({ chatTarget, type }); // Setzt den ausgewählten Chat (Freund oder Gruppe) und den Typ (friend/group)
    setCreatingGroup(false); // Schließt das Gruppen-Erstellen-Formular, wenn ein Chat ausgewählt wird
    console.log(localStorage.getItem('id'))
    if (type === 'friend') {
      createChat(localStorage.getItem('id'), chatTarget.id); 
    }
  };

  const handleCreateGroupClick = () => {
    setCreatingGroup(true); // Aktiviert das Gruppen-Erstellen-Formular
  };

  const handleCreateGroup = async (newGroup) => {
    // Backend-Anfrage zum Erstellen einer neuen Gruppe
    const response = await fetch('http://localhost:8080/groups', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(newGroup), // Sendet die neue Gruppe als JSON an das Backend
    });

    if (response.ok) {
      // const createdGroup = await response.json(); // Entfernt, da es nicht verwendet wird
      setCreatingGroup(false); // Schließt das Formular nach erfolgreicher Erstellung
      // Aktualisieren Sie die Gruppenliste
      // Sie müssen diese Logik möglicherweise anpassen, um die neuen Gruppen zu laden
    }
  };


  const createChat = async (userId1, userId2) => {
    const response = await fetch(`http://localhost:8080/create.chat?userId1=${userId1}&userId2=${userId2}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (response.ok) {
      console.log('Chat wurde erstellt');
    } else {
      console.error('Fehler beim Chat erstellen');
    }
  };

  return (
    <div className="chat-page">
      <BackButton />
      <h1>Chat</h1>
      <div className="chat-container">
        <FriendListForChat
          onSelect={handleSelect}
          onCreateGroupClick={handleCreateGroupClick}
        />
        {creatingGroup ? (
          <CreateGroupForm onCreateGroup={handleCreateGroup} /> //ruft die handleCreateGroup auf
        ) : (
          selectedChat.chatTarget && selectedChat.type && (
            <ChatWindow chatTarget={selectedChat.chatTarget} type={selectedChat.type} />
          )
        )}
      </div>
    </div>
  );
}

export default ChatPage; // Exportiert die ChatPage-Komponente als Standardexport
