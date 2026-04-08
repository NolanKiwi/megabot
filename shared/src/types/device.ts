export interface DevicePermissions {
  notificationAccess: boolean;
  callPhone: boolean;
  sendSms: boolean;
  readSms: boolean;
  readPhoneState: boolean;
}

export interface Device {
  id: string;
  ownerId: string;
  name: string;
  androidId: string;
  model: string;
  osVersion: string;
  appVersion: string;
  status: 'online' | 'offline' | 'error';
  lastSeenAt: string;
  registeredAt: string;
  permissions: DevicePermissions;
}

export interface DeviceRegistration {
  pairingCode: string;
  androidId: string;
  name: string;
  model: string;
  osVersion: string;
  appVersion: string;
}

export interface DeviceHeartbeat {
  deviceId: string;
  battery: number;
  activeScripts: number;
}
