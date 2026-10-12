import { logger } from '../utils/logger';

export interface GlobalWebSocketListener {
  onMessage(frame: any): void;
  onConnected?(): void;
  onDisconnected?(reason: string): void;
}

export interface GlobalWebSocketChannel {
  registerListener(listener: GlobalWebSocketListener): void;
  unregisterListener(listener: GlobalWebSocketListener): void;
  send(payload: any): boolean;
  isConnected(): boolean;
}

export interface WebSocketConnectionListener {
  onConnected(): void;
  onDisconnected(reason: string): void;
}

export interface WebSocketMessageListener {
  onMessage(frame: any): void;
}

class GlobalWebSocketService implements GlobalWebSocketChannel {
  private readonly wsUrl = (() => {
    const envWsUrl = (import.meta as any).env?.VITE_WS_URL;
    if (envWsUrl) return envWsUrl;
    if (typeof window !== 'undefined' && window.location.hostname) {
      const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      if (window.location.port === '9011') {
        return `${proto}//${window.location.host}/ws/app`;
      }
      return `${proto}//${window.location.hostname}:9011/ws/app`;
    }
    return 'ws://localhost:9011/ws/app';
  })();

  private socket: WebSocket | null = null;
  private isConnectedState = false;
  private reconnectTimer: any = null;

  private listeners = new Set<GlobalWebSocketListener>();
  private connectionListeners = new Set<WebSocketConnectionListener>();
  private messageListeners = new Set<WebSocketMessageListener>();

  constructor() {
    this.connect();
  }

  public connect(): void {
    if (this.socket && (this.socket.readyState === WebSocket.CONNECTING || this.socket.readyState === WebSocket.OPEN)) {
      return;
    }

    logger.info('globalWebSocket.ts', 'connect', `Connecting to WebSocket endpoint: ${this.wsUrl}`);

    try {
      this.socket = new WebSocket(this.wsUrl);

      this.socket.onopen = () => {
        logger.info('globalWebSocket.ts', 'onopen', 'WebSocket connection established successfully');
        this.isConnectedState = true;
        this.notifyConnected();
      };

      this.socket.onmessage = (event) => {
        try {
          const frame = JSON.parse(event.data);
          this.notifyMessage(frame);
        } catch (err) {
          logger.warn('globalWebSocket.ts', 'onmessage', 'Failed to parse incoming WebSocket message JSON', err);
        }
      };

      this.socket.onclose = (event) => {
        const reason = event.reason || `Code ${event.code}`;
        logger.warn('globalWebSocket.ts', 'onclose', `WebSocket connection closed: ${reason}`);
        this.isConnectedState = false;
        this.notifyDisconnected(reason);
        this.scheduleReconnect();
      };

      this.socket.onerror = (error) => {
        logger.error('globalWebSocket.ts', 'onerror', 'WebSocket error encountered', error);
      };
    } catch (err) {
      logger.error('globalWebSocket.ts', 'connect', 'Failed to initialize WebSocket instance', err);
      this.scheduleReconnect();
    }
  }

  private scheduleReconnect(): void {
    if (this.reconnectTimer) return;
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      logger.info('globalWebSocket.ts', 'scheduleReconnect', 'Attempting WebSocket reconnection...');
      this.connect();
    }, 3000);
  }

  // GlobalWebSocketChannel implementation (SpringBoot symmetry)
  public registerListener(listener: GlobalWebSocketListener): void {
    this.listeners.add(listener);
    if (this.isConnectedState && listener.onConnected) {
      listener.onConnected();
    }
  }

  public unregisterListener(listener: GlobalWebSocketListener): void {
    this.listeners.delete(listener);
  }

  public isConnected(): boolean {
    return this.isConnectedState && this.socket !== null && this.socket.readyState === WebSocket.OPEN;
  }

  public getIsConnected(): boolean {
    return this.isConnected();
  }

  // Backward compatibility with legacy listeners
  public registerConnectionListener(listener: WebSocketConnectionListener): void {
    this.connectionListeners.add(listener);
    if (this.isConnectedState) {
      listener.onConnected();
    }
  }

  public unregisterConnectionListener(listener: WebSocketConnectionListener): void {
    this.connectionListeners.delete(listener);
  }

  public registerMessageListener(listener: WebSocketMessageListener): void {
    this.messageListeners.add(listener);
  }

  public unregisterMessageListener(listener: WebSocketMessageListener): void {
    this.messageListeners.delete(listener);
  }

  public subscribe(callback: (frame: any) => void): () => void {
    const listener: GlobalWebSocketListener = { onMessage: callback };
    this.registerListener(listener);
    return () => this.unregisterListener(listener);
  }

  private notifyConnected(): void {
    this.listeners.forEach((listener) => {
      try {
        if (listener.onConnected) listener.onConnected();
      } catch (err) {
        logger.error('globalWebSocket.ts', 'notifyConnected', 'Error in global listener onConnected', err);
      }
    });
    this.connectionListeners.forEach((listener) => {
      try {
        listener.onConnected();
      } catch (err) {
        logger.error('globalWebSocket.ts', 'notifyConnected', 'Error in connection listener', err);
      }
    });
  }

  private notifyDisconnected(reason: string): void {
    this.listeners.forEach((listener) => {
      try {
        if (listener.onDisconnected) listener.onDisconnected(reason);
      } catch (err) {
        logger.error('globalWebSocket.ts', 'notifyDisconnected', 'Error in global listener onDisconnected', err);
      }
    });
    this.connectionListeners.forEach((listener) => {
      try {
        listener.onDisconnected(reason);
      } catch (err) {
        logger.error('globalWebSocket.ts', 'notifyDisconnected', 'Error in connection listener', err);
      }
    });
  }

  private notifyMessage(frame: any): void {
    // Unfiltered broadcast to all registered listeners
    this.listeners.forEach((listener) => {
      try {
        listener.onMessage(frame);
      } catch (err) {
        logger.error('globalWebSocket.ts', 'notifyMessage', 'Error in global listener onMessage', err);
      }
    });
    this.messageListeners.forEach((listener) => {
      try {
        listener.onMessage(frame);
      } catch (err) {
        logger.error('globalWebSocket.ts', 'notifyMessage', 'Error in message listener', err);
      }
    });
  }

  public send(payload: any): boolean {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      logger.warn('globalWebSocket.ts', 'send', 'Cannot send payload: WebSocket is not connected');
      return false;
    }

    try {
      const jsonStr = typeof payload === 'string' ? payload : JSON.stringify(payload);
      this.socket.send(jsonStr);
      logger.info('globalWebSocket.ts', 'send', 'Transmitted frame over WebSocket wire', payload);
      return true;
    } catch (err) {
      logger.error('globalWebSocket.ts', 'send', 'Error sending WebSocket payload', err);
      return false;
    }
  }
}

export const globalWebSocket: GlobalWebSocketChannel & GlobalWebSocketService = new GlobalWebSocketService();
