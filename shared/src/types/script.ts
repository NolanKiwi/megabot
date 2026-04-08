export interface BotScript {
  id: string;
  ownerId: string;
  name: string;
  code: string;
  enabled: boolean;
  compiledAt: string | null;
  compileError: string | null;
  targetPackages: string[];
  deviceId: string | null;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface ScriptCreateInput {
  name: string;
  code: string;
  targetPackages: string[];
  deviceId?: string;
}

export interface ScriptUpdateInput {
  name?: string;
  code?: string;
  enabled?: boolean;
  targetPackages?: string[];
  deviceId?: string;
}

export interface ScriptCompileResult {
  scriptId: string;
  success: boolean;
  error?: string;
  compiledAt: string;
}
