import http from 'http';
import app from './app';
import { env } from './config/env';
import { connectDatabase } from './config/database';
import { createSocketServer } from './socket/socketServer';

async function main() {
  await connectDatabase();

  const server = http.createServer(app);
  const io = createSocketServer(server);

  server.listen(env.port, () => {
    console.log(`[Server] MegaBot API running on port ${env.port}`);
    console.log(`[Socket] WebSocket server ready`);
  });
}

main().catch((err) => {
  console.error('[Server] Failed to start:', err);
  process.exit(1);
});
