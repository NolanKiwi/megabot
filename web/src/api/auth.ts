import client from './client';
import type { AuthResponse } from '@megabot/shared';

export async function login(email: string, password: string): Promise<AuthResponse> {
  const { data } = await client.post('/auth/login', { email, password });
  return data;
}

export async function register(email: string, password: string, displayName: string): Promise<AuthResponse> {
  const { data } = await client.post('/auth/register', { email, password, displayName });
  return data;
}

export async function getMe() {
  const { data } = await client.get('/auth/me');
  return data.user;
}
