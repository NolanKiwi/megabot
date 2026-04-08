import { Server as HttpServer } from 'http';
import { Server, Socket } from 'socket.io';
import jwt from 'jsonwebtoken';
import { env } from '../config/env';
import { AuthPayload } from '../middleware/auth';
import { deviceService } from '../services/device.service';
import { logService } from '../services/log.service';
import { scriptService } from '../services/script.service';

interface AuthenticatedSocket extends Socket {
  userId?: string;
  deviceId?: string;
  socketType?: 'device' | 'web';
}

export function createSocketServer(httpServer: HttpServer): Server {
  const io = new Server(httpServer, {
    cors: { origin: env.corsOrigin, credentials: true },
  });

  // Auth middleware
  io.use(async (socket: AuthenticatedSocket, next) => {
    const token = socket.handshake.auth.token;
    if (!token) return next(new Error('Authentication required'));

    try {
      const payload = jwt.verify(token, env.jwt.accessSecret) as AuthPayload;
      socket.userId = payload.userId;
      socket.socketType = socket.handshake.auth.type || 'web';
      socket.deviceId = socket.handshake.auth.deviceId;
      next();
    } catch {
      next(new Error('Invalid token'));
    }
  });

  io.on('connection', (socket: AuthenticatedSocket) => {
    const userId = socket.userId!;
    const type = socket.socketType!;

    // Join user room for targeted events
    socket.join(`user:${userId}`);

    if (type === 'device' && socket.deviceId) {
      handleDeviceConnection(io, socket);
    } else {
      handleWebConnection(io, socket);
    }
  });

  return io;
}

function handleDeviceConnection(io: Server, socket: AuthenticatedSocket) {
  const deviceId = socket.deviceId!;
  const userId = socket.userId!;

  socket.join(`device:${deviceId}`);
  deviceService.setStatus(deviceId, 'online');
  io.to(`user:${userId}`).emit('device:statusChange', { deviceId, status: 'online' });

  console.log(`[Socket] Device connected: ${deviceId}`);

  // Heartbeat
  socket.on('device:heartbeat', (payload) => {
    deviceService.setStatus(deviceId, 'online');
  });

  // Permission updates
  socket.on('device:permissions', (payload) => {
    deviceService.updatePermissions(deviceId, payload.permissions);
  });

  // Message received from messenger
  socket.on('message:received', async (payload) => {
    await logService.createMessageLog({
      deviceId,
      direction: 'in',
      packageName: payload.packageName,
      sender: payload.sender,
      room: payload.room,
      content: payload.content,
      isGroupChat: payload.isGroupChat,
    });
    io.to(`user:${userId}`).emit('message:new', payload);
  });

  // Bot replied
  socket.on('message:replied', async (payload) => {
    await logService.createMessageLog({
      deviceId,
      direction: 'out',
      packageName: payload.packageName,
      sender: 'Bot',
      room: payload.room,
      content: payload.content,
      isGroupChat: false,
      scriptId: payload.scriptId,
    });
    io.to(`user:${userId}`).emit('message:new', payload);
  });

  // Log entry from device
  socket.on('log:entry', (payload) => {
    io.to(`user:${userId}`).emit('log:stream', payload);
  });

  // Script compile result
  socket.on('script:compileResult', (payload) => {
    io.to(`user:${userId}`).emit('script:compiled', payload);
  });

  // Disconnect
  socket.on('disconnect', () => {
    deviceService.setStatus(deviceId, 'offline');
    io.to(`user:${userId}`).emit('device:statusChange', { deviceId, status: 'offline' });
    console.log(`[Socket] Device disconnected: ${deviceId}`);
  });
}

function handleWebConnection(io: Server, socket: AuthenticatedSocket) {
  const userId = socket.userId!;
  console.log(`[Socket] Web client connected: ${userId}`);

  // Save script and deploy to device
  socket.on('script:save', async (payload) => {
    const script = await scriptService.findById(payload.scriptId, userId);
    if (!script) return;
    await scriptService.update(payload.scriptId, userId, { code: payload.code });
    if (script.deviceId) {
      io.to(`device:${script.deviceId}`).emit('script:deploy', {
        scriptId: script._id.toString(),
        code: payload.code,
        name: script.name,
      });
    }
  });

  // Compile script on device
  socket.on('script:compile', async (payload) => {
    const script = await scriptService.findById(payload.scriptId, userId);
    if (!script?.deviceId) return;
    io.to(`device:${script.deviceId}`).emit('script:compile', {
      scriptId: script._id.toString(),
      code: script.code,
    });
  });

  // Toggle script
  socket.on('script:toggle', async (payload) => {
    const script = await scriptService.update(payload.scriptId, userId, { enabled: payload.enabled });
    if (!script?.deviceId) return;
    io.to(`device:${script.deviceId}`).emit('script:toggle', {
      scriptId: script._id.toString(),
      enabled: payload.enabled,
    });
  });

  // Phone command
  socket.on('command:phone', (payload) => {
    io.to(`device:${payload.deviceId}`).emit('command:phone', {
      action: payload.action,
      number: payload.number,
    });
  });

  // SMS command
  socket.on('command:sms', (payload) => {
    io.to(`device:${payload.deviceId}`).emit('command:sms', {
      numbers: payload.numbers,
      message: payload.message,
    });
  });

  // Log subscription
  socket.on('log:subscribe', (payload) => {
    socket.join(`device:${payload.deviceId}`);
  });

  socket.on('log:unsubscribe', (payload) => {
    socket.leave(`device:${payload.deviceId}`);
  });

  socket.on('disconnect', () => {
    console.log(`[Socket] Web client disconnected: ${userId}`);
  });
}
