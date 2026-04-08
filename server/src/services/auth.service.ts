import jwt from 'jsonwebtoken';
import { User, IUser } from '../models/User';
import { env } from '../config/env';
import { AuthPayload } from '../middleware/auth';

export class AuthService {
  async register(email: string, password: string, displayName: string): Promise<IUser> {
    const existing = await User.findOne({ email });
    if (existing) {
      throw new Error('Email already registered');
    }
    const user = new User({ email, passwordHash: password, displayName });
    await user.save();
    return user;
  }

  async login(email: string, password: string): Promise<{ user: IUser; accessToken: string; refreshToken: string }> {
    const user = await User.findOne({ email });
    if (!user || !(await user.comparePassword(password))) {
      throw new Error('Invalid email or password');
    }

    const accessToken = this.generateAccessToken(user);
    const refreshToken = this.generateRefreshToken(user);

    return { user, accessToken, refreshToken };
  }

  async refreshTokens(refreshToken: string): Promise<{ accessToken: string; refreshToken: string }> {
    try {
      const payload = jwt.verify(refreshToken, env.jwt.refreshSecret) as AuthPayload;
      const user = await User.findById(payload.userId);
      if (!user) throw new Error('User not found');

      return {
        accessToken: this.generateAccessToken(user),
        refreshToken: this.generateRefreshToken(user),
      };
    } catch {
      throw new Error('Invalid refresh token');
    }
  }

  private generateAccessToken(user: IUser): string {
    const payload: AuthPayload = { userId: user._id.toString(), role: user.role };
    return jwt.sign(payload, env.jwt.accessSecret, { expiresIn: env.jwt.accessExpiresIn as jwt.SignOptions['expiresIn'] });
  }

  private generateRefreshToken(user: IUser): string {
    const payload: AuthPayload = { userId: user._id.toString(), role: user.role };
    return jwt.sign(payload, env.jwt.refreshSecret, { expiresIn: env.jwt.refreshExpiresIn as jwt.SignOptions['expiresIn'] });
  }
}

export const authService = new AuthService();
