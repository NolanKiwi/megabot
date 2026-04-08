import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import { env } from './config/env';
import { errorHandler } from './middleware/errorHandler';
import authRoutes from './routes/auth.routes';
import scriptRoutes from './routes/script.routes';
import deviceRoutes from './routes/device.routes';
import logRoutes from './routes/log.routes';

const app = express();

// Security
app.use(helmet());
app.use(cors({ origin: env.corsOrigin, credentials: true }));
app.use(express.json({ limit: '1mb' }));

// Rate limiting
const authLimiter = rateLimit({ windowMs: 60_000, max: 30, message: { error: 'Too many requests' } });
const apiLimiter = rateLimit({ windowMs: 60_000, max: 100, message: { error: 'Too many requests' } });

// Routes
app.get('/api/health', (_req, res) => res.json({ status: 'ok', timestamp: new Date().toISOString() }));
app.use('/api/auth', authLimiter, authRoutes);
app.use('/api/scripts', apiLimiter, scriptRoutes);
app.use('/api/devices', apiLimiter, deviceRoutes);
app.use('/api/logs', apiLimiter, logRoutes);

// Error handling
app.use(errorHandler);

export default app;
