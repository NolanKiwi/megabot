import { create } from 'zustand';
import type { BotScript } from '@megabot/shared';
import * as api from '../api/scripts';

interface ScriptState {
  scripts: BotScript[];
  selectedScript: BotScript | null;
  loading: boolean;
  fetchScripts: () => Promise<void>;
  selectScript: (script: BotScript | null) => void;
  createScript: (name: string, code: string, targetPackages: string[]) => Promise<BotScript>;
  updateScript: (id: string, updates: Partial<BotScript>) => Promise<void>;
  deleteScript: (id: string) => Promise<void>;
}

export const useScriptStore = create<ScriptState>()((set, get) => ({
  scripts: [],
  selectedScript: null,
  loading: false,

  fetchScripts: async () => {
    set({ loading: true });
    try {
      const scripts = await api.getScripts();
      set({ scripts, loading: false });
    } catch {
      set({ loading: false });
    }
  },

  selectScript: (script) => set({ selectedScript: script }),

  createScript: async (name, code, targetPackages) => {
    const script = await api.createScript({ name, code, targetPackages });
    set((s) => ({ scripts: [script, ...s.scripts] }));
    return script;
  },

  updateScript: async (id, updates) => {
    const updated = await api.updateScript(id, updates);
    set((s) => ({
      scripts: s.scripts.map((sc) => (sc.id === id ? updated : sc)),
      selectedScript: s.selectedScript?.id === id ? updated : s.selectedScript,
    }));
  },

  deleteScript: async (id) => {
    await api.deleteScript(id);
    set((s) => ({
      scripts: s.scripts.filter((sc) => sc.id !== id),
      selectedScript: s.selectedScript?.id === id ? null : s.selectedScript,
    }));
  },
}));
