import React, { useState, useEffect } from 'react';
import webSocketService from '../Service/WebSocketService'; 
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

  const chatId = friend && friend.id ? generateChatId(userId, friend.id) : null;

  // Nachrichten vom Server laden
  useEffect(() => {
    if (chatId) {
      const fetchMessages = async () => {
        try {
          const response = await fetch(`http://localhost:8080/messages/${chatId}`);
          const data = await response.json();
          setMessages((prevMessages) => ({
            ...prevMessages,
            [chatId]: Array.isArray(data) ? data : [], // Ensure data is an array
          }));
        } catch (error) {
          console.error('Fehler beim Laden der Nachrichten:', error);
        }
      };

      fetchMessages();
    }
  }, [chatId]);

  // WebSocket-Verbindung herstellen und Nachrichten empfangen
  useEffect(() => {
    webSocketService.connect();

    return () => {
      webSocketService.close();
    };
  }, []);

  useEffect(() => {
    const handleMessage = (messageEvent) => {
      const message = JSON.parse(messageEvent.body);
      setMessages((prevMessages) => {
        const updatedMessages = { ...prevMessages };
        if (!Array.isArray(updatedMessages[message.chatId])) {
          updatedMessages[message.chatId] = [];
        }
        updatedMessages[message.chatId].push(message);
        return updatedMessages;
      });
    };

    if (webSocketService.client && webSocketService.connected) {
      webSocketService.client.subscribe(`/topic/${chatId}`, handleMessage);
    }

    return () => {
      if (webSocketService.client && webSocketService.connected) {
        webSocketService.client.unsubscribe(`/topic/${chatId}`);
      }
    };
  }, [webSocketService.connected, chatId]);

  // Nachricht senden und speichern
  const handleSendMessage = () => {
    if (newMessage.trim() !== '') {
      const message = {
        sender: userId,
        receiver: type === 'friend' ? friend.id : null,
        group: type === 'group' ? friend.id : null,
        text: newMessage,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        chatId: chatId,
        read: false,
      };
      webSocketService.sendMessage(`/app/chat/${chatId}`, JSON.stringify(message));

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
      webSocketService.sendMessage(`/app/chat/${chatId}`, JSON.stringify({ ...updatedMessage, action: 'edit' }));

      setMessages((prevMessages) => {
        const updatedMessages = { ...prevMessages };
        updatedMessages[chatId] = updatedMessages[chatId].map((msg) =>
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
    webSocketService.sendMessage(`/app/chat/${chatId}`, JSON.stringify({ ...message, action: 'delete' }));

    setMessages((prevMessages) => {
      const updatedMessages = { ...prevMessages };
      updatedMessages[chatId] = updatedMessages[chatId].filter((msg) => msg.timestamp !== message.timestamp);
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
      webSocketService.sendMessage(`/app/chat/${chatId}`, JSON.stringify({ ...message, action: 'read' }));

      setMessages((prevMessages) => {
        const updatedMessages = { ...prevMessages };
        updatedMessages[chatId] = updatedMessages[chatId].map((msg) =>
          msg.timestamp === message.timestamp ? { ...msg, read: true } : msg
        );
        return updatedMessages;
      });
    }
  };

  const chatMessages = Array.isArray(messages[chatId]) ? messages[chatId] : [];

  if (!friend || !friend.id) {
    return <div>Wählen Sie einen Freund oder eine Gruppe aus, um den Chat zu starten</div>;
  }

  return (
    <div className="chat-window">
      <h2>Chat mit {type === 'friend' ? friend.username : friend.name}</h2>
      <div className="chat-messages imessage">
        {chatMessages.map((message, index) => (
          <p
            key={index}
            className={message.sender === userId ? 'from-me' : 'from-them'}
            onClick={() => handleReadMessage(message)}
          >
            {type === 'group' && message.sender !== userId && (
              <span className="message-sender">{message.senderName}</span>
            )}
            <span className="message-text">{message.text}</span>
            <div className="message-actions">
              <button onClick={() => handleEditMessage(message)}>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  fill="currentColor"
                  className="bi bi-pencil-fill"
                  viewBox="0 0 16 16"
                >
                  <path d="M12.854.146a.5.5 0 0 0-.707 0L10.5 1.793 14.207 5.5l1.647-1.646a.5.5 0 0 0 0-.708zm.646 6.061L9.793 2.5 3.293 9H3.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.207zm-7.468 7.468A.5.5 0 0 1 6 13.5V13h-.5a.5.5 0 0 1-.5-.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.5-.5V10h-.5a.5.5 0 0 1-.175-.032l-.179.178a.5.5 0 0 0-.11.168l-2 5a.5.5 0 0 0 .65.65l5-2a.5.5 0 0 0 .168-.11z" />
                </svg>
              </button>
              <button onClick={() => handleDeleteMessage(message)}>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  fill="currentColor"
                  className="bi bi-trash-fill"
                  viewBox="0 0 16 16"
                >
                  <path d="M2.5 1a1 1 0 0 0-1 1v1a1 1 0 0 0 1 1H3v9a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V4h.5a1 1 0 0 0 1-1V2a1 1 0 0 0-1-1H10a1 1 0 0 0-1-1H7a1 1 0 0 0-1 1zm3 4a.5.5 0 0 1 .5.5v7a.5.5 0 0 1-1 0v-7a.5.5 0 0 1 .5-.5M8 5a.5.5 0 0 1 .5.5v7a.5.5 0 0 1-1 0v-7A.5.5 0 0 1 8 5m3 .5v7a.5.5 0 0 1-1 0v-7a.5.5 0 0 1 1 0" />
                </svg>
              </button>
              <span className="message-timestamp">{message.timestamp}</span>
            </div>
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
