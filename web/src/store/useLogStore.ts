import { create } from 'zustand';
import type { LogEntry } from '@megabot/shared';

interface LogState {
  logs: LogEntry[];
  maxLogs: number;
  addLog: (entry: LogEntry) => void;
  clearLogs: () => void;
}

export const useLogStore = create<LogState>()((set) => ({
  logs: [],
  maxLogs: 500,

  addLog: (entry) =>
    set((s) => ({
      logs: [entry, ...s.logs].slice(0, s.maxLogs),
    })),

  clearLogs: () => set({ logs: [] }),
}));
