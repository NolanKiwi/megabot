import { create } from 'zustand';
import type { Device } from '@megabot/shared';
import * as api from '../api/devices';

interface DeviceState {
  devices: Device[];
  loading: boolean;
  fetchDevices: () => Promise<void>;
  updateDeviceStatus: (deviceId: string, status: Device['status']) => void;
  deleteDevice: (id: string) => Promise<void>;
}

export const useDeviceStore = create<DeviceState>()((set) => ({
  devices: [],
  loading: false,

  fetchDevices: async () => {
    set({ loading: true });
    try {
      const devices = await api.getDevices();
      set({ devices, loading: false });
    } catch {
      set({ loading: false });
    }
  },

  updateDeviceStatus: (deviceId, status) =>
    set((s) => ({
      devices: s.devices.map((d) =>
        d.id === deviceId ? { ...d, status } : d
      ),
    })),

  deleteDevice: async (id) => {
    await api.deleteDevice(id);
    set((s) => ({ devices: s.devices.filter((d) => d.id !== id) }));
  },
}));
