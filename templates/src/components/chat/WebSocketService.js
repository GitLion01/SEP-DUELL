// services/WebSocketService.js
class WebSocketService {
    constructor() {
      this.socket = null;
    }
  
    connect() {
      this.socket = new WebSocket('ws://localhost:8080/ws');
  
      this.socket.onopen = () => {
        console.log('WebSocket connection established');
      };
  
      this.socket.onmessage = (message) => {
        console.log('Received:', message.data);
        // Hier können Sie eingehende Nachrichten verarbeiten
      };
  
      this.socket.onclose = () => {
        console.log('WebSocket connection closed');
      };
  
      this.socket.onerror = (error) => {
        console.error('WebSocket error:', error);
      };
    }
  
    sendMessage(message) {
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        this.socket.send(message);
      }
    }
  
    close() {
      if (this.socket) {
        this.socket.close();
      }
    }
  }
  
  const webSocketService = new WebSocketService();
  export default webSocketService;
  