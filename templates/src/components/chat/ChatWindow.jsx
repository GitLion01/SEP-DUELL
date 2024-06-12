import React, { useState, useEffect, useContext } from 'react';
import { WebSocketContext} from "../../WebSocketProvider";
import './ChatWindow.css';
import './ChatBubble.css';

const generateChatId = (userId, targetId) => {
  return [userId, targetId].sort().join('-');
};

function ChatWindow({ chatTarget, type }) {
  const [messages, setMessages] = useState({});
  const [newMessage, setNewMessage] = useState('');
  const [editingMessage, setEditingMessage] = useState(null);
  const userId = localStorage.getItem('id');
  const chatId = generateChatId(userId, chatTarget.id);
  const { chatClient } = useContext(WebSocketContext);

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
    if (chatClient && chatId) {
      chatClient.subscribe(`/topic/${chatId}`, (message) => {
        const receivedMessage = JSON.parse(message.body);
        setMessages((prevMessages) => {
          const updatedMessages = { ...prevMessages };
          if (!Array.isArray(updatedMessages[receivedMessage.chat.id])) {
            updatedMessages[receivedMessage.chat.id] = [];
          }
          updatedMessages[receivedMessage.chat.id].push(receivedMessage);
          return updatedMessages;
        });
      });
    }
  }, [chatClient, chatId]);

  // Nachricht senden und speichern
  const handleSendMessage = () => {
    if (newMessage.trim() !== '') {
      const message = {
        id: null, // ID wird vom Backend gesetzt
        message: newMessage,
        chat: { id: chatId },
        sender: { id: userId }
      };
      chatClient.publish({ destination: '/chat/sendMessage', body: JSON.stringify(message) });

      setMessages(prev => {
        const updatedMessages = { ...prev };
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
    setNewMessage(message.message);
  };

  const handleUpdateMessage = () => {
    if (editingMessage && newMessage.trim() !== '') {
      const updatedMessage = { ...editingMessage, message: newMessage };
      chatClient.publish({ destination: '/chat/editMessage', body: JSON.stringify(updatedMessage) });

      setMessages((prevMessages) => {
        const updatedMessages = { ...prevMessages };
        updatedMessages[chatId] = updatedMessages[chatId].map((msg) =>
          msg.id === editingMessage.id ? updatedMessage : msg
        );
        return updatedMessages;
      });
      setEditingMessage(null);
      setNewMessage('');
    }
  };

  // Nachricht löschen
  const handleDeleteMessage = (message) => {
    chatClient.publish({ destination: '/chat/deleteMessage', body: JSON.stringify(message) });

    setMessages((prevMessages) => {
      const updatedMessages = { ...prevMessages };
      updatedMessages[chatId] = updatedMessages[chatId].filter((msg) => msg.id !== message.id);
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
    if (message.sender.id !== userId && !message.read) {
      const updatedMessage = { ...message, read: true };
      chatClient.publish({ destination: '/chat/readMessage', body: JSON.stringify(updatedMessage) });

      setMessages((prevMessages) => {
        const updatedMessages = { ...prevMessages };
        updatedMessages[chatId] = updatedMessages[chatId].map((msg) =>
          msg.id === message.id ? { ...msg, read: true } : msg
        );
        return updatedMessages;
      });
    }
  };

  const chatMessages = Array.isArray(messages[chatId]) ? messages[chatId] : [];

  if (!chatTarget || !chatTarget.id) {
    return <div>Wählen Sie einen Freund oder eine Gruppe aus, um den Chat zu starten</div>;
  }

  return (
    <div className="chat-window">
      <h2>Chat mit {type === 'friend' ? chatTarget.username : chatTarget.name}</h2>
      <div className="chat-messages imessage">
        {chatMessages.map((message, index) => (
          <p
            key={index}
            className={message.sender.id === userId ? 'from-me' : 'from-them'}
            onClick={() => handleReadMessage(message)}
          >
            {type === 'group' && message.sender.id !== userId && (
              <span className="message-sender">{message.sender.username}</span>
            )}
            <span className="message-text">{message.message}</span>
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
