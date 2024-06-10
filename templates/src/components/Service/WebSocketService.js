import { Client } from '@stomp/stompjs';

class WebSocketService {
    constructor() {
        this.client = null;
        this.connected = false;
    }

    connect() {
        this.client = new Client({
            brokerURL: 'ws://localhost:8080/game-websocket',
            onConnect: () => {
                console.log('STOMP connection established');
                this.connected = true;
                this.subscribeToTopics();
            },
            onStompError: (frame) => {
                console.error('STOMP error:', frame.headers['message'], frame.body);
            },
            onWebSocketClose: () => {
                console.log('WebSocket connection closed');
                this.connected = false;
            },
            onWebSocketError: (error) => {
                console.error('WebSocket error:', error);
                this.connected = false;
            },
        });

        this.client.activate();
    }

    subscribeToTopics() {
        if (this.connected) {
            this.client.subscribe('/topic/someTopic', (message) => {
                console.log('Received:', message.body);
                // Hier können Sie eingehende Nachrichten verarbeiten
            });
        } else {
            console.error('Cannot subscribe because the connection is not established');
        }
    }

    sendMessage(destination, message) {
        if (this.client && this.client.connected) {
            this.client.publish({ destination, body: message });
        } else {
            console.error('Cannot send message because there is no connection');
        }
    }

    close() {
        if (this.client) {
            this.client.deactivate();
        }
    }
}

const webSocketService = new WebSocketService();
export default webSocketService;
