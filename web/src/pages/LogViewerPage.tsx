import { useEffect, useRef } from 'react';
import { useLogStore } from '../store/useLogStore';
import { useDeviceStore } from '../store/useDeviceStore';
import { getSocket } from '../socket/socketClient';

export default function LogViewerPage() {
  const { logs, clearLogs } = useLogStore();
  const { devices, fetchDevices } = useDeviceStore();
  const logEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchDevices();
  }, [fetchDevices]);

  useEffect(() => {
    // Subscribe to first online device logs
    const onlineDevice = devices.find((d) => d.status === 'online');
    if (onlineDevice) {
      const socket = getSocket();
      socket.emit('log:subscribe', { deviceId: onlineDevice.id });
      return () => {
        socket.emit('log:unsubscribe', { deviceId: onlineDevice.id });
      };
    }
  }, [devices]);

  const getLevelColor = (level: string) => {
    switch (level) {
      case 'error': return 'text-red-400';
      case 'warn': return 'text-yellow-400';
      case 'info': return 'text-blue-400';
      default: return 'text-slate-400';
    }
  };

  return (
    <div className="flex flex-col h-[calc(100vh-3rem)]">
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-2xl font-bold text-white">Live Logs</h1>
        <div className="flex gap-2">
          <span className="text-sm text-slate-400">{logs.length} entries</span>
          <button
            onClick={clearLogs}
            className="px-3 py-1 bg-slate-700 hover:bg-slate-600 text-white text-sm rounded"
          >
            Clear
          </button>
        </div>
      </div>

      <div className="flex-1 bg-slate-950 border border-slate-700 rounded-xl p-4 font-mono text-sm overflow-auto">
        {logs.length === 0 ? (
          <p className="text-slate-500">Waiting for log entries...</p>
        ) : (
          <div className="space-y-0.5">
            {logs.map((log, i) => (
              <div key={i} className="flex gap-2 hover:bg-slate-800/50 px-1 rounded">
                <span className="text-slate-600 shrink-0">
                  {new Date(log.timestamp).toLocaleTimeString()}
                </span>
                <span className={`shrink-0 uppercase w-12 ${getLevelColor(log.level)}`}>
                  [{log.level}]
                </span>
                <span className="text-slate-300">{log.message}</span>
              </div>
            ))}
            <div ref={logEndRef} />
          </div>
        )}
      </div>
    </div>
  );
}
