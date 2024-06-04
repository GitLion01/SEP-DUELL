// components/chat/ChatWindow.jsx
import React, { useState, useEffect } from 'react';
import webSocketService from './WebSocketService';
import './ChatWindow.css';
import './ChatBubble.css'; // Import der neuen CSS-Datei

function ChatWindow({ friend }) {
  const [messages, setMessages] = useState({});
  const [newMessage, setNewMessage] = useState('');

  useEffect(() => {
    webSocketService.connect();

    const handleMessage = (messageEvent) => {
      const message = JSON.parse(messageEvent.data);
      setMessages((prevMessages) => {
        const updatedMessages = { ...prevMessages };
        if (!updatedMessages[message.friendId]) {
          updatedMessages[message.friendId] = [];
        }
        updatedMessages[message.friendId].push(message);
        return updatedMessages;
      });
    };

    webSocketService.socket.addEventListener('message', handleMessage);

    return () => {
      webSocketService.socket.removeEventListener('message', handleMessage);
      webSocketService.close();
    };
  }, []);

  const handleSendMessage = () => {
    if (newMessage.trim() !== '') {
      const message = {
        sender: 'You',
        text: newMessage,
        timestamp: new Date().toLocaleTimeString(),
        friendId: friend.id
      };
      webSocketService.sendMessage(JSON.stringify(message));
      setMessages((prevMessages) => {
        const updatedMessages = { ...prevMessages };
        if (!updatedMessages[friend.id]) {
          updatedMessages[friend.id] = [];
        }
        updatedMessages[friend.id].push(message);
        return updatedMessages;
      });
      setNewMessage('');
    }
  };

  const handleKeyPress = (event) => {
    if (event.key === 'Enter') {
      handleSendMessage();
    }
  };

  const friendMessages = messages[friend.id] || [];

  return (
    <div className="chat-window">
      <h2>Chat mit {friend.username}</h2>
      <div className="chat-messages imessage">
        {friendMessages.map((message, index) => (
          <p key={index} className={message.sender === 'You' ? 'from-me' : 'from-them'}>
            <span className="message-text">{message.text}</span>
            <span className="message-timestamp">{message.timestamp}</span>
          </p>
        ))}
      </div>
      <div className="chat-input">
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Type a message"
        />
        <button onClick={handleSendMessage}>Send</button>
      </div>
    </div>
  );
}

export default ChatWindow;
