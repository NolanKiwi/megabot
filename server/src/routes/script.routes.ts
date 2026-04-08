import { Router, Request, Response, NextFunction } from 'express';
import { authenticate } from '../middleware/auth';
import { validate } from '../middleware/validation';
import { scriptCreateSchema, scriptUpdateSchema } from '@megabot/shared';
import { scriptService } from '../services/script.service';

const router = Router();
router.use(authenticate);

router.get('/', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const scripts = await scriptService.findByOwner(req.user!.userId);
    res.json({ scripts: scripts.map(s => s.toJSON()) });
  } catch (err) { next(err); }
});

router.get('/:id', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const script = await scriptService.findById(req.params.id, req.user!.userId);
    if (!script) { res.status(404).json({ error: 'Script not found' }); return; }
    res.json({ script: script.toJSON() });
  } catch (err) { next(err); }
});

router.post('/', validate(scriptCreateSchema), async (req: Request, res: Response, next: NextFunction) => {
  try {
    const script = await scriptService.create(req.user!.userId, req.body);
    res.status(201).json({ script: script.toJSON() });
  } catch (err) { next(err); }
});

router.patch('/:id', validate(scriptUpdateSchema), async (req: Request, res: Response, next: NextFunction) => {
  try {
    const script = await scriptService.update(req.params.id, req.user!.userId, req.body);
    if (!script) { res.status(404).json({ error: 'Script not found' }); return; }
    res.json({ script: script.toJSON() });
  } catch (err) { next(err); }
});

router.delete('/:id', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const deleted = await scriptService.delete(req.params.id, req.user!.userId);
    if (!deleted) { res.status(404).json({ error: 'Script not found' }); return; }
    res.json({ success: true });
  } catch (err) { next(err); }
});

export default router;
