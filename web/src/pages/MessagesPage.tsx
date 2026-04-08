import { useEffect, useState } from 'react';
import { getSocket } from '../socket/socketClient';
import { useDeviceStore } from '../store/useDeviceStore';

interface Message {
  direction: 'in' | 'out';
  packageName: string;
  sender: string;
  room: string;
  content: string;
  isGroupChat: boolean;
  timestamp: number;
}

export default function MessagesPage() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [smsNumber, setSmsNumber] = useState('');
  const [smsMessage, setSmsMessage] = useState('');
  const [callNumber, setCallNumber] = useState('');
  const { devices, fetchDevices } = useDeviceStore();

  const onlineDevice = devices.find((d) => d.status === 'online');

  useEffect(() => {
    fetchDevices();
  }, [fetchDevices]);

  useEffect(() => {
    const socket = getSocket();
    const handler = (msg: Message) => {
      setMessages((prev) => [{ ...msg, timestamp: Date.now() }, ...prev].slice(0, 200));
    };
    socket.on('message:new', handler);
    return () => { socket.off('message:new', handler); };
  }, []);

  const handleSendSms = () => {
    if (!onlineDevice || !smsNumber || !smsMessage) return;
    getSocket().emit('command:sms', {
      deviceId: onlineDevice.id,
      numbers: [smsNumber],
      message: smsMessage,
    });
    setSmsNumber('');
    setSmsMessage('');
  };

  const handleCall = () => {
    if (!onlineDevice || !callNumber) return;
    getSocket().emit('command:phone', {
      deviceId: onlineDevice.id,
      action: 'call',
      number: callNumber,
    });
    setCallNumber('');
  };

  const getAppLabel = (pkg: string) => {
    const map: Record<string, string> = {
      'com.kakao.talk': 'KakaoTalk',
      'jp.naver.line.android': 'LINE',
      'com.facebook.orca': 'Messenger',
      'org.telegram.messenger': 'Telegram',
    };
    return map[pkg] || pkg;
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-white mb-6">Messages & Actions</h1>

      {/* Quick actions */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        {/* SMS */}
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-5">
          <h3 className="text-white font-semibold mb-3">Send SMS</h3>
          <div className="space-y-2">
            <input
              value={smsNumber}
              onChange={(e) => setSmsNumber(e.target.value)}
              placeholder="Phone number"
              className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm"
            />
            <textarea
              value={smsMessage}
              onChange={(e) => setSmsMessage(e.target.value)}
              placeholder="Message"
              rows={2}
              className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm resize-none"
            />
            <button
              onClick={handleSendSms}
              disabled={!onlineDevice}
              className="w-full py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm rounded-lg"
            >
              Send SMS
            </button>
          </div>
        </div>

        {/* Phone Call */}
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-5">
          <h3 className="text-white font-semibold mb-3">Phone Call</h3>
          <div className="space-y-2">
            <input
              value={callNumber}
              onChange={(e) => setCallNumber(e.target.value)}
              placeholder="Phone number"
              className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm"
            />
            <button
              onClick={handleCall}
              disabled={!onlineDevice}
              className="w-full py-2 bg-green-600 hover:bg-green-700 disabled:opacity-50 text-white text-sm rounded-lg"
            >
              Make Call
            </button>
            {!onlineDevice && (
              <p className="text-xs text-red-400">No device online</p>
            )}
          </div>
        </div>
      </div>

      {/* Live message stream */}
      <div className="bg-slate-800 border border-slate-700 rounded-xl p-5">
        <h3 className="text-white font-semibold mb-3">Live Messages</h3>
        {messages.length === 0 ? (
          <p className="text-slate-400 text-sm">No messages yet. Messages will appear here in real-time.</p>
        ) : (
          <div className="space-y-2 max-h-96 overflow-auto">
            {messages.map((msg, i) => (
              <div
                key={i}
                className={`p-3 rounded-lg text-sm ${
                  msg.direction === 'in' ? 'bg-slate-700/50' : 'bg-blue-600/20'
                }`}
              >
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-xs px-1.5 py-0.5 bg-slate-600 rounded text-slate-300">
                    {getAppLabel(msg.packageName)}
                  </span>
                  <span className="text-white font-medium">{msg.sender}</span>
                  {msg.isGroupChat && (
                    <span className="text-xs text-slate-400">in {msg.room}</span>
                  )}
                  <span className="text-xs text-slate-500 ml-auto">
                    {new Date(msg.timestamp).toLocaleTimeString()}
                  </span>
                </div>
                <p className="text-slate-300">{msg.content}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
