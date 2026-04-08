import crypto from 'crypto';
import jwt from 'jsonwebtoken';
import { Device, IDevice } from '../models/Device';
import { User } from '../models/User';
import { env } from '../config/env';

export class DeviceService {
  async generatePairingCode(ownerId: string): Promise<string> {
    const code = crypto.randomInt(100000, 999999).toString();
    // Store temporarily - in production use Redis with TTL
    const device = new Device({
      ownerId,
      name: 'Pending',
      androidId: '',
      model: '',
      osVersion: '',
      appVersion: '',
      pairingCode: code,
    });
    await device.save();
    return code;
  }

  async pairDevice(
    pairingCode: string,
    androidId: string,
    name: string,
    model: string,
    osVersion: string,
    appVersion: string
  ): Promise<{ deviceId: string; token: string } | null> {
    const device = await Device.findOne({ pairingCode, androidId: '' });
    if (!device) return null;

    device.androidId = androidId;
    device.name = name;
    device.model = model;
    device.osVersion = osVersion;
    device.appVersion = appVersion;
    device.pairingCode = null;
    device.status = 'online';
    device.lastSeenAt = new Date();
    await device.save();

    await User.findByIdAndUpdate(device.ownerId, { $addToSet: { devices: device._id } });

    // Issue a long-lived device token (1 year)
    const token = jwt.sign(
      { userId: device.ownerId.toString(), role: 'user' },
      env.jwt.accessSecret,
      { expiresIn: '365d' as jwt.SignOptions['expiresIn'] }
    );

    return { deviceId: device._id.toString(), token };
  }

  async findByOwner(ownerId: string): Promise<IDevice[]> {
    return Device.find({ ownerId }).sort({ lastSeenAt: -1 });
  }

  async findById(id: string): Promise<IDevice | null> {
    return Device.findById(id);
  }

  async setStatus(id: string, status: 'online' | 'offline' | 'error'): Promise<void> {
    await Device.findByIdAndUpdate(id, { status, lastSeenAt: new Date() });
  }

  async updatePermissions(
    id: string,
    permissions: IDevice['permissions']
  ): Promise<void> {
    await Device.findByIdAndUpdate(id, { permissions });
  }

  async delete(id: string, ownerId: string): Promise<boolean> {
    const result = await Device.deleteOne({ _id: id, ownerId });
    if (result.deletedCount > 0) {
      await User.findByIdAndUpdate(ownerId, { $pull: { devices: id } });
      return true;
    }
    return false;
  }
}

export const deviceService = new DeviceService();
