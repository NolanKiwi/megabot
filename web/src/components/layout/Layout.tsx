import { useEffect } from 'react';
import Sidebar from './Sidebar';
import { connectSocket, disconnectSocket, getSocket } from '../../socket/socketClient';
import { useDeviceStore } from '../../store/useDeviceStore';
import { useLogStore } from '../../store/useLogStore';

export default function Layout({ children }: { children: React.ReactNode }) {
  useEffect(() => {
    connectSocket();
    const socket = getSocket();

    socket.on('device:statusChange', ({ deviceId, status }) => {
      useDeviceStore.getState().updateDeviceStatus(deviceId, status);
    });

    socket.on('log:stream', (entry) => {
      useLogStore.getState().addLog(entry);
    });

    return () => {
      disconnectSocket();
    };
  }, []);

  return (
    <div className="flex min-h-screen bg-slate-900">
      <Sidebar />
      <main className="flex-1 p-6 overflow-auto">{children}</main>
    </div>
  );
}
