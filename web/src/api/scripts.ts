import client from './client';
import type { BotScript, ScriptCreateInput, ScriptUpdateInput } from '@megabot/shared';

export async function getScripts(): Promise<BotScript[]> {
  const { data } = await client.get('/scripts');
  return data.scripts;
}

export async function getScript(id: string): Promise<BotScript> {
  const { data } = await client.get(`/scripts/${id}`);
  return data.script;
}

export async function createScript(input: ScriptCreateInput): Promise<BotScript> {
  const { data } = await client.post('/scripts', input);
  return data.script;
}

export async function updateScript(id: string, input: ScriptUpdateInput): Promise<BotScript> {
  const { data } = await client.patch(`/scripts/${id}`, input);
  return data.script;
}

export async function deleteScript(id: string): Promise<void> {
  await client.delete(`/scripts/${id}`);
}
