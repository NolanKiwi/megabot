export interface IncomingMessage {
  id: string;
  deviceId: string;
  packageName: string;
  sender: string;
  room: string;
  content: string;
  isGroupChat: boolean;
  timestamp: string;
  imageBase64?: string;
}

export interface OutgoingMessage {
  id: string;
  deviceId: string;
  packageName: string;
  room: string;
  content: string;
  scriptId: string;
  timestamp: string;
}

export interface MessageLog {
  id: string;
  deviceId: string;
  direction: 'in' | 'out';
  packageName: string;
  sender: string;
  room: string;
  content: string;
  isGroupChat: boolean;
  scriptId?: string;
  timestamp: string;
}

export interface CallSmsLog {
  id: string;
  deviceId: string;
  type: 'call' | 'sms';
  target: string;
  message?: string;
  scriptId: string;
  timestamp: string;
}
