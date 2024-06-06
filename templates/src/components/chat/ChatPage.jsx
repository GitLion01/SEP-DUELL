import React, { useState, useEffect } from 'react';
import webSocketService from './WebSocketService';
import './ChatWindow.css';
import './ChatBubble.css';

const generateChatId = (userId, targetId) => {
  return [userId, targetId].sort().join('-');
};

function ChatWindow({ friend, type }) {
  const [messages, setMessages] = useState({});
  const [newMessage, setNewMessage] = useState('');
  const [editingMessage, setEditingMessage] = useState(null);
  const userId = localStorage.getItem('id');
  const chatId = generateChatId(userId, friend.id);

  // Nachrichten vom Server laden
  useEffect(() => {
    const fetchMessages = async () => {
      const response = await fetch(`http://localhost:8080/messages/${chatId}`);
      const data = await response.json();
      setMessages((prevMessages) => ({
        ...prevMessages,
        [chatId]: Array.isArray(data) ? data : [] // Ensure data is an array
      }));
    };

    fetchMessages();
  }, [chatId]);

  // WebSocket-Verbindung herstellen und Nachrichten empfangen
  useEffect(() => {
    webSocketService.connect();

    const handleMessage = (messageEvent) => {
      const message = JSON.parse(messageEvent.data);
      setMessages((prevMessages) => {
        const updatedMessages = { ...prevMessages };
        if (!Array.isArray(updatedMessages[message.chatId])) {
          updatedMessages[message.chatId] = [];
        }
        updatedMessages[message.chatId].push(message);
        return updatedMessages;
      });
    };

    webSocketService.socket.addEventListener('message', handleMessage);

    return () => {
      webSocketService.socket.removeEventListener('message', handleMessage);
      webSocketService.close();
    };
  }, []);

  // Nachricht senden und speichern
  const handleSendMessage = () => {
    if (newMessage.trim() !== '') {
      const message = {
        sender: userId,
        receiver: type === 'friend' ? friend.id : null, // Receiver ist nur für private Chats definiert
        group: type === 'group' ? friend.id : null, // Group ist nur für Gruppenchats definiert
        text: newMessage,
        timestamp: new Date().toLocaleTimeString(),
        chatId: chatId,
        read: false // Neue Nachrichten sind ungelesen
      };
      webSocketService.sendMessage(JSON.stringify(message));

      setMessages((prevMessages) => {
        const updatedMessages = { ...prevMessages };
        if (!Array.isArray(updatedMessages[chatId])) {
          updatedMessages[chatId] = [];
        }
        updatedMessages[chatId].push(message);
        return updatedMessages;
      });
      setNewMessage('');
    }
  };

  // Nachricht bearbeiten
  const handleEditMessage = (message) => {
    setEditingMessage(message);
    setNewMessage(message.text);
  };

  const handleUpdateMessage = () => {
    if (editingMessage && newMessage.trim() !== '') {
      const updatedMessage = { ...editingMessage, text: newMessage };
      webSocketService.sendMessage(JSON.stringify({ ...updatedMessage, action: 'edit' }));

      setMessages((prevMessages) => {
        const updatedMessages = { ...prevMessages };
        updatedMessages[chatId] = updatedMessages[chatId].map(msg => 
          msg.timestamp === editingMessage.timestamp ? updatedMessage : msg
        );
        return updatedMessages;
      });
      setEditingMessage(null);
      setNewMessage('');
    }
  };

  // Nachricht löschen
  const handleDeleteMessage = (message) => {
    webSocketService.sendMessage(JSON.stringify({ ...message, action: 'delete' }));

    setMessages((prevMessages) => {
      const updatedMessages = { ...prevMessages };
      updatedMessages[chatId] = updatedMessages[chatId].filter(msg => msg.timestamp !== message.timestamp);
      return updatedMessages;
    });
  };

  const handleKeyPress = (event) => {
    if (event.key === 'Enter') {
      if (editingMessage) {
        handleUpdateMessage();
      } else {
        handleSendMessage();
      }
    }
  };

  // Funktion zum Setzen des Lesestatus
  const handleReadMessage = (message) => {
    if (message.sender !== userId && !message.read) {
      message.read = true;
      webSocketService.sendMessage(JSON.stringify({ ...message, action: 'read' }));

      setMessages((prevMessages) => {
        const updatedMessages = { ...prevMessages };
        updatedMessages[chatId] = updatedMessages[chatId].map(msg =>
          msg.timestamp === message.timestamp ? { ...msg, read: true } : msg
        );
        return updatedMessages;
      });
    }
  };

  const chatMessages = Array.isArray(messages[chatId]) ? messages[chatId] : [];

  return (
    <div className="chat-window">
      <h2>Chat mit {type === 'friend' ? friend.username : friend.name}</h2>
      <div className="chat-messages imessage">
        {chatMessages.map((message, index) => (
          <p key={index}
             className={message.sender === userId ? 'from-me' : 'from-them'}
             onClick={() => handleReadMessage(message)}>
            {type === 'group' && message.sender !== userId && (
              <span className="message-sender">{message.senderName}</span>
            )}
            <span className="message-text">{message.text}</span>
            <span className="message-timestamp">{message.timestamp}</span>
            {message.sender === userId && !message.read && (
              <div className="message-actions">
                <button onClick={() => handleEditMessage(message)}>Bearbeiten</button>
                <button onClick={() => handleDeleteMessage(message)}>Löschen</button>
              </div>
            )}
            {message.read && <span className="message-read-status">Gelesen</span>}
          </p>
        ))}
      </div>
      <div className="chat-input">
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Nachricht..."
        />
        <button onClick={editingMessage ? handleUpdateMessage : handleSendMessage}>
          {editingMessage ? 'Update' : 'Send'}
        </button>
      </div>
    </div>
  );
}

export default ChatWindow;
