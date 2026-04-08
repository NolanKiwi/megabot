import { io, Socket } from 'socket.io-client';
import { useAuthStore } from '../store/useAuthStore';

let socket: Socket | null = null;

export function getSocket(): Socket {
  if (!socket) {
    socket = io('/', {
      auth: {
        token: useAuthStore.getState().accessToken,
        type: 'web',
      },
      autoConnect: false,
    });
  }
  return socket;
}

export function connectSocket(): void {
  const s = getSocket();
  s.auth = {
    token: useAuthStore.getState().accessToken,
    type: 'web',
  };
  s.connect();
}

export function disconnectSocket(): void {
  socket?.disconnect();
  socket = null;
}
