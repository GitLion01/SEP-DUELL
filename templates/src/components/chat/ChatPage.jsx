import React, { useState } from 'react';
import FriendListForChat from './FriendListForChat';
import ChatWindow from './ChatWindow';
import CreateGroupForm from './CreateGroupForm';
import './ChatPage.css';
import BackButton from '../BackButton';
import { WebSocketProvider } from '../../WebSocketProvider';

function ChatPage() {
  const [selectedChat, setSelectedChat] = useState({ chatTarget: null, type: null });
  const [creatingGroup, setCreatingGroup] = useState(false);
  const [chatId, setChatId] = useState(null); // Zustand für Chat-ID

  const handleSelect = async (chatTarget, type) => {
    setSelectedChat({ chatTarget, type });
    setCreatingGroup(false);
    if (type === 'friend') {
      await createChat(localStorage.getItem('id'), chatTarget.id);
    }
  };

  const handleCreateGroupClick = () => {
    setCreatingGroup(true);
  };

  const handleCreateGroup = async (newGroup) => {
    const response = await fetch('http://localhost:8080/groups', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(newGroup),
    });

    if (response.ok) {
      setCreatingGroup(false);
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
      const chatId = await response.json(); // Erwartet die Chat-ID als Antwort
      setChatId(chatId); // Setze die Chat-ID
      console.log('Chat wurde erstellt oder gefunden:', chatId);
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
          <CreateGroupForm onCreateGroup={handleCreateGroup} />
        ) : (
          selectedChat.chatTarget && selectedChat.type && chatId && (
            <ChatWindow chatTarget={selectedChat.chatTarget} type={selectedChat.type} chatId={chatId} />
          )
        )}
      </div>
    </div>
  );
}

export default ChatPage;
