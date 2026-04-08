import { z } from 'zod';
import { ALL_PACKAGE_NAMES } from '../constants/packageNames';

// ── Auth ──
export const loginSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
});

export const registerSchema = loginSchema.extend({
  displayName: z.string().min(2, 'Display name must be at least 2 characters').max(50),
});

// ── Script ──
export const scriptCreateSchema = z.object({
  name: z.string().min(1).max(100),
  code: z.string().max(500_000),
  targetPackages: z.array(z.string()).min(1),
  deviceId: z.string().optional(),
});

export const scriptUpdateSchema = z.object({
  name: z.string().min(1).max(100).optional(),
  code: z.string().max(500_000).optional(),
  enabled: z.boolean().optional(),
  targetPackages: z.array(z.string()).optional(),
  deviceId: z.string().nullable().optional(),
});

// ── Device ──
export const deviceRegistrationSchema = z.object({
  pairingCode: z.string().length(6),
  androidId: z.string().min(1),
  name: z.string().min(1).max(100),
  model: z.string().min(1),
  osVersion: z.string().min(1),
  appVersion: z.string().min(1),
});

// ── Phone / SMS Commands ──
export const phoneCommandSchema = z.object({
  deviceId: z.string().min(1),
  action: z.enum(['call', 'dial']),
  number: z.string().regex(/^\+?[\d\-\s()]+$/, 'Invalid phone number'),
});

export const smsCommandSchema = z.object({
  deviceId: z.string().min(1),
  numbers: z.array(z.string().regex(/^\+?[\d\-\s()]+$/)).min(1).max(50),
  message: z.string().min(1).max(1000),
});

// ── Type exports ──
export type LoginInput = z.infer<typeof loginSchema>;
export type RegisterInput = z.infer<typeof registerSchema>;
export type ScriptCreateInput = z.infer<typeof scriptCreateSchema>;
export type ScriptUpdateInput = z.infer<typeof scriptUpdateSchema>;
export type DeviceRegistrationInput = z.infer<typeof deviceRegistrationSchema>;
export type PhoneCommandInput = z.infer<typeof phoneCommandSchema>;
export type SmsCommandInput = z.infer<typeof smsCommandSchema>;
