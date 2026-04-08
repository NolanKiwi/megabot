import { Router, Request, Response, NextFunction } from 'express';
import { authService } from '../services/auth.service';
import { validate } from '../middleware/validation';
import { loginSchema, registerSchema } from '@megabot/shared';
import { authenticate } from '../middleware/auth';

const router = Router();

router.post('/register', validate(registerSchema), async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { email, password, displayName } = req.body;
    const user = await authService.register(email, password, displayName);
    const { accessToken, refreshToken } = await authService.login(email, password);
    res.status(201).json({
      user: user.toJSON(),
      tokens: { accessToken, refreshToken },
    });
  } catch (err: any) {
    if (err.message === 'Email already registered') {
      res.status(409).json({ error: err.message });
      return;
    }
    next(err);
  }
});

router.post('/login', validate(loginSchema), async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { email, password } = req.body;
    const { user, accessToken, refreshToken } = await authService.login(email, password);
    res.json({
      user: user.toJSON(),
      tokens: { accessToken, refreshToken },
    });
  } catch (err: any) {
    if (err.message === 'Invalid email or password') {
      res.status(401).json({ error: err.message });
      return;
    }
    next(err);
  }
});

router.post('/refresh', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { refreshToken } = req.body;
    if (!refreshToken) {
      res.status(400).json({ error: 'Refresh token required' });
      return;
    }
    const tokens = await authService.refreshTokens(refreshToken);
    res.json({ tokens });
  } catch (err: any) {
    res.status(401).json({ error: 'Invalid refresh token' });
  }
});

router.get('/me', authenticate, async (req: Request, res: Response) => {
  const { User } = await import('../models/User');
  const user = await User.findById(req.user!.userId);
  if (!user) {
    res.status(404).json({ error: 'User not found' });
    return;
  }
  res.json({ user: user.toJSON() });
});

export default router;
