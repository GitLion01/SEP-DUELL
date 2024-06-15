import React, { useState, useCallback } from 'react';
import FriendListForChat from './FriendListForChat';
import ChatWindow from './ChatWindow';
import CreateGroupForm from './CreateGroupForm';
import './ChatPage.css';
import BackButton from '../BackButton';

function ChatPage() {
  const [selectedChat, setSelectedChat] = useState({ chatTarget: null, type: null });
  const [creatingGroup, setCreatingGroup] = useState(false);
  const [chatId, setChatId] = useState(null);
  const userId = localStorage.getItem('id');

  const fetchGroups = useCallback(async () => {
    const url = `http://localhost:8080/get.groups?userId=${userId}`;
    const response = await fetch(url);
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
  }, [userId]);

  const handleSelect = async (chatTarget, type) => {
    setSelectedChat({ chatTarget, type });
    setCreatingGroup(false);
    if (type === 'friend') {
      await createChat(localStorage.getItem('id'), chatTarget.id);
    } else if (type === 'group') {
      setChatId(chatTarget.id); // Setze die Chat-ID für die Gruppe
    }
  };

  const handleCreateGroupClick = () => {
    setCreatingGroup(true);
  };

  const handleCreateGroup = async (newGroup) => {
    const response = await fetch(`http://localhost:8080/create.group?groupName=${encodeURIComponent(newGroup.name)}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(newGroup.userIds),
    });

    if (response.ok) {
      setCreatingGroup(false);
      await fetchGroups(); // Aktualisiere die Gruppenliste nach erfolgreicher Erstellung
    } else {
      console.error('Fehler beim Erstellen der Gruppe');
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
      const chatId = await response.json();
      setChatId(chatId);
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
          fetchGroups={fetchGroups} // Übergabe von fetchGroups als Prop
        />
        {creatingGroup ? (
          <CreateGroupForm onCreateGroup={handleCreateGroup}
          fetchGroups ={fetchGroups} />
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
