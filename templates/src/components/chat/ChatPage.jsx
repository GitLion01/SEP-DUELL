// components/chat/ChatPage.jsx
import React, { useState } from 'react';
import FriendListForChat from './FriendListForChat';
import ChatWindow from './ChatWindow';
import './ChatPage.css';

function ChatPage() {
  const [selectedFriend, setSelectedFriend] = useState(null);

  const handleFriendSelect = (friend) => {
    setSelectedFriend(friend);
  };

  return (
    <div className="chat-page">
      <h1>Chat</h1>
      <div className="chat-container">
        <FriendListForChat onFriendSelect={handleFriendSelect} />
        {selectedFriend ? (
          <ChatWindow friend={selectedFriend} />
        ) : (
          <div className="chat-window">
            <p>Wähle einen Freund aus, um zu chatten</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default ChatPage;
