import { Router, Request, Response, NextFunction } from 'express';
import { authenticate } from '../middleware/auth';
import { logService } from '../services/log.service';
import { deviceService } from '../services/device.service';

const router = Router();
router.use(authenticate);

router.get('/:deviceId/messages', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const device = await deviceService.findById(req.params.deviceId);
    if (!device || device.ownerId.toString() !== req.user!.userId) {
      res.status(404).json({ error: 'Device not found' });
      return;
    }

    const { page, limit, room, direction } = req.query;
    const result = await logService.getMessageLogs(req.params.deviceId, {
      page: page ? parseInt(page as string, 10) : undefined,
      limit: limit ? parseInt(limit as string, 10) : undefined,
      room: room as string | undefined,
      direction: direction as 'in' | 'out' | undefined,
    });

    res.json({
      logs: result.logs.map(l => l.toJSON()),
      total: result.total,
      page: page ? parseInt(page as string, 10) : 1,
    });
  } catch (err) { next(err); }
});

export default router;
