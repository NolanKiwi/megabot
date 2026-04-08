import client from './client';
import type { Device } from '@megabot/shared';

export async function getDevices(): Promise<Device[]> {
  const { data } = await client.get('/devices');
  return data.devices;
}

export async function generatePairingCode(): Promise<string> {
  const { data } = await client.post('/devices/pairing-code');
  return data.pairingCode;
}

export async function deleteDevice(id: string): Promise<void> {
  await client.delete(`/devices/${id}`);
}
