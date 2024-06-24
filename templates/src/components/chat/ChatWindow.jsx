import React, { useState, useEffect, useContext, useRef } from 'react'; //Zustände verwalten, ,WebsocketClient zugreifen, 
import { WebSocketContext } from "../../WebSocketProvider";
import './ChatWindow.css';
import './ChatBubble.css';

function ChatWindow({ chatTarget, type, chatId }) {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [editingMessageId, setEditingMessageId] = useState(null);
  const userId = parseInt(localStorage.getItem('id'), 10);
  const { chatClient } = useContext(WebSocketContext);
  const messagesEndRef = useRef(null);

  const normalizeMessage = (message) => { //formatiert Nachrichten in einer einheitlichen Struktur
    if (!message.senderId || !message.chatId) {
      return null;
    }
    return {
      id: message.id,
      message: message.message,
      chat: { id: message.chatId },
      sender: { id: message.senderId, username: message.senderName },
      read: message.read
    };
  };

  const fetchMessages = async () => {
    try {
      const response = await fetch(`http://localhost:8080/get.messages?chatId=${chatId}&userID=${userId}`);
      if (response.ok) {
        const data = await response.json(); //konvertiert die Anwort von JSON-formatierten Text in ein JavaScript-Objekt um 
        const normalizedMessages = data.map(normalizeMessage);
        const sortedMessages = normalizedMessages
          .filter(message => message.chat.id === chatId)    
          .sort((a, b) => a.id - b.id);   //Die Nachrichten werden dadurch soritert. 
        setMessages(sortedMessages);
      } else {
        console.error('Failed to fetch messages:', response.statusText);
      }
    } catch (error) {
      console.error('Error fetching messages:', error);
    }
  };

  useEffect(() => { //?
    if (chatClient && chatId) {
      const subscription = chatClient.subscribe(`/user/${userId}/queue/messages`, (message) => {
        const messageBody = message.body;
        const messageObject = JSON.parse(messageBody); //JSON-String-Nachricht in ein JavaSript-Objekt zu konvertieren
        if (messageObject.onChat === "on Chat") {
          const message = {
            id: messageObject.id,
            message: messageObject.message,
            chat: { id: messageObject.chatId },
            sender: { id: messageObject.senderId, username: messageObject.senderName }
          };
          chatClient.publish({
            destination: '/chat/onChat',
            body: JSON.stringify(message), //in einen JSON-String konvertieren
            headers: {
              'userId': userId.toString(),
              'chatId': chatId.toString()
            }
          });
        } else { 
          const receivedMessage = JSON.parse(messageBody);
          console.log(receivedMessage);
          const normalizedMessage = normalizeMessage(receivedMessage);
          if (normalizedMessage && normalizedMessage.chat.id === chatId) {
            setMessages((prevMessages) => {
              const messageIndex = prevMessages.findIndex(msg => msg.id === normalizedMessage.id);
              if (messageIndex !== -1) { 
                console.log(messages);
                const updatedMessages = [...prevMessages];
                updatedMessages[messageIndex] = normalizedMessage;
                return updatedMessages;
              } else {
                const newMessages = [...prevMessages, normalizedMessage].sort((a, b) => a.id - b.id);
                console.log(messages);
                return newMessages;
              }
            });
          } else {
            console.error("Received message is missing required properties or is invalid:", receivedMessage);
          }
        }
      });

      fetchMessages();

      return () => {
        subscription.unsubscribe();
      };
    }
  }, [chatClient, chatId, userId]);

  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages]);

  const handleSendMessage = () => {
    if (newMessage.trim() !== '') {
      const message = {
        id: null,
        message: newMessage,
        chat: { id: chatId },
        sender: { id: userId }
      };
      console.log('Sending message:', JSON.stringify(message)); // Log the message being sent

      chatClient.publish({ destination: '/chat/sendMessage', body: JSON.stringify(message) });
      setNewMessage('');
    }
  };

  const handleEditMessage = (messageId, currentMessage) => {
    setEditingMessageId(messageId);
    setNewMessage(currentMessage);
  };

  const handleUpdateMessage = () => {
    if (editingMessageId && newMessage.trim() !== '') {
      const message = {
        id: editingMessageId,
        message: newMessage,
        chat: { id: chatId },
        sender: { id: userId }
      };

      chatClient.publish({ destination: '/chat/editMessage', body: JSON.stringify(message) });
      setNewMessage('');
      setEditingMessageId(null);
    }
  };

  const handleDeleteMessage = (messageId) => {
    const message = {
      id: messageId,
      chat: { id: chatId },
      sender: { id: userId }
    };

    chatClient.publish({ destination: '/chat/deleteMessage', body: JSON.stringify(message) });

    setTimeout(fetchMessages, 100);
  };

  const handleKeyPress = (event) => {
    if (event.key === 'Enter') {
      if (editingMessageId) {
        handleUpdateMessage();
      } else {
        handleSendMessage();
      }
    }
  };

  if (!chatTarget || !chatTarget.id) {
    return <div>Wählen Sie einen Freund oder eine Gruppe aus, um den Chat zu starten</div>;
  }

  return (
    <div className="chat-window">
      <h2>{type === 'friend' ? 'Chat mit: ' + chatTarget.username : 'Gruppe: ' + chatTarget.name}</h2>
      <div className="chat-messages imessage">
        {messages.length === 0 ? (
          <p>Keine Nachrichten vorhanden.</p>
        ) : (
          messages.filter(message => message.message.trim() !== '').map((message, index) => (
            <p
              key={index}
              className={message.sender && message.sender.id === userId ? 'from-me' : 'from-them'}
            >
              {type === 'group' && message.sender && message.sender.id !== userId && (
                <span className="message-sender">{message.sender.username}: </span>
              )}
              <span className="message-text">{message.message}</span>
              <div className="message-actions">
                {message.sender.id === userId && !message.read && (
                  <>
                    <button onClick={() => handleEditMessage(message.id, message.message)}>
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
                    <button onClick={() => handleDeleteMessage(message.id)}>
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
                  </>
                )}
                {message.sender.id === userId && message.read && (
                  <span className="message-read-status">Gelesen</span>
                )}
                <span className="message-timestamp">{message.timestamp}</span>
              </div>
            </p>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>
      <div className="chat-input">
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Nachricht..."
        />
        <button onClick={editingMessageId ? handleUpdateMessage : handleSendMessage}>
          {editingMessageId ? 'Update' : 'Send'}
        </button>
      </div>
    </div>
  );
}

export default ChatWindow;
