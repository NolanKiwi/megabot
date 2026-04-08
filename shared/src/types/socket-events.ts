import { DeviceRegistration, DeviceHeartbeat, DevicePermissions } from './device';
import { IncomingMessage, OutgoingMessage, CallSmsLog } from './message';
import { ScriptCompileResult } from './script';

// ── Server → Android Device ──
export interface ServerToDeviceEvents {
  'script:deploy': (payload: { scriptId: string; code: string; name: string }) => void;
  'script:toggle': (payload: { scriptId: string; enabled: boolean }) => void;
  'script:compile': (payload: { scriptId: string; code: string }) => void;
  'command:phone': (payload: { action: 'call' | 'dial'; number: string }) => void;
  'command:sms': (payload: { numbers: string[]; message: string }) => void;
}

// ── Android Device → Server ──
export interface DeviceToServerEvents {
  'device:register': (payload: DeviceRegistration) => void;
  'device:heartbeat': (payload: DeviceHeartbeat) => void;
  'device:permissions': (payload: { deviceId: string; permissions: DevicePermissions }) => void;
  'log:entry': (payload: LogEntry) => void;
  'message:received': (payload: IncomingMessage) => void;
  'message:replied': (payload: OutgoingMessage) => void;
  'call-sms:executed': (payload: CallSmsLog) => void;
  'script:compileResult': (payload: ScriptCompileResult) => void;
}

// ── Server → Web Dashboard ──
export interface ServerToWebEvents {
  'log:stream': (payload: LogEntry) => void;
  'device:statusChange': (payload: { deviceId: string; status: 'online' | 'offline' | 'error' }) => void;
  'message:new': (payload: IncomingMessage | OutgoingMessage) => void;
  'script:compiled': (payload: ScriptCompileResult) => void;
}

// ── Web Dashboard → Server ──
export interface WebToServerEvents {
  'script:save': (payload: { scriptId: string; code: string }) => void;
  'script:compile': (payload: { scriptId: string }) => void;
  'script:toggle': (payload: { scriptId: string; enabled: boolean }) => void;
  'command:phone': (payload: { deviceId: string; action: 'call' | 'dial'; number: string }) => void;
  'command:sms': (payload: { deviceId: string; numbers: string[]; message: string }) => void;
  'log:subscribe': (payload: { deviceId: string }) => void;
  'log:unsubscribe': (payload: { deviceId: string }) => void;
}

// ── Shared Log Entry ──
export interface LogEntry {
  deviceId: string;
  scriptId?: string;
  scriptName?: string;
  level: 'debug' | 'info' | 'warn' | 'error';
  message: string;
  timestamp: string;
}
