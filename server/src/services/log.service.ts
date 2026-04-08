import { MessageLog, IMessageLog } from '../models/MessageLog';

export class LogService {
  async createMessageLog(data: {
    deviceId: string;
    direction: 'in' | 'out';
    packageName: string;
    sender: string;
    room: string;
    content: string;
    isGroupChat: boolean;
    scriptId?: string;
  }): Promise<IMessageLog> {
    const log = new MessageLog(data);
    await log.save();
    return log;
  }

  async getMessageLogs(
    deviceId: string,
    options: { page?: number; limit?: number; room?: string; direction?: 'in' | 'out' } = {}
  ): Promise<{ logs: IMessageLog[]; total: number }> {
    const { page = 1, limit = 50, room, direction } = options;
    const query: Record<string, unknown> = { deviceId };
    if (room) query.room = room;
    if (direction) query.direction = direction;

    const [logs, total] = await Promise.all([
      MessageLog.find(query)
        .sort({ timestamp: -1 })
        .skip((page - 1) * limit)
        .limit(limit),
      MessageLog.countDocuments(query),
    ]);

    return { logs, total };
  }
}

export const logService = new LogService();
