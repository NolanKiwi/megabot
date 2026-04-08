import { Router, Request, Response, NextFunction } from 'express';
import { authenticate } from '../middleware/auth';
import { deviceService } from '../services/device.service';

const router = Router();

// Public endpoint — pairing code acts as the credential
router.post('/pair', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { pairingCode, androidId, name, model, osVersion, appVersion } = req.body;
    if (!pairingCode || !androidId) {
      res.status(400).json({ error: 'pairingCode and androidId are required' });
      return;
    }
    const result = await deviceService.pairDevice(
      pairingCode,
      androidId,
      name || 'Android Device',
      model || 'Unknown',
      osVersion || 'Android',
      appVersion || '1.0.0'
    );
    if (!result) {
      res.status(400).json({ error: 'Invalid or expired pairing code' });
      return;
    }
    res.json(result);
  } catch (err) { next(err); }
});

router.use(authenticate);

router.get('/', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const devices = await deviceService.findByOwner(req.user!.userId);
    res.json({ devices: devices.map(d => d.toJSON()) });
  } catch (err) { next(err); }
});

router.post('/pairing-code', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const code = await deviceService.generatePairingCode(req.user!.userId);
    res.json({ pairingCode: code });
  } catch (err) { next(err); }
});

router.delete('/:id', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const deleted = await deviceService.delete(req.params.id, req.user!.userId);
    if (!deleted) { res.status(404).json({ error: 'Device not found' }); return; }
    res.json({ success: true });
  } catch (err) { next(err); }
});

export default router;
