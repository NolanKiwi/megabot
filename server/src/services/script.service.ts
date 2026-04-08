import { Script, IScript } from '../models/Script';
import { ScriptCreateInput, ScriptUpdateInput } from '@megabot/shared';

export class ScriptService {
  async create(ownerId: string, input: ScriptCreateInput): Promise<IScript> {
    const script = new Script({ ...input, ownerId });
    await script.save();
    return script;
  }

  async findByOwner(ownerId: string): Promise<IScript[]> {
    return Script.find({ ownerId }).sort({ updatedAt: -1 });
  }

  async findById(id: string, ownerId: string): Promise<IScript | null> {
    return Script.findOne({ _id: id, ownerId });
  }

  async update(id: string, ownerId: string, input: ScriptUpdateInput): Promise<IScript | null> {
    const update: Record<string, unknown> = { ...input };
    if (input.code !== undefined) {
      update.version = { $inc: 1 } as unknown;
    }
    return Script.findOneAndUpdate(
      { _id: id, ownerId },
      input.code !== undefined
        ? { ...input, $inc: { version: 1 } }
        : input,
      { new: true }
    );
  }

  async delete(id: string, ownerId: string): Promise<boolean> {
    const result = await Script.deleteOne({ _id: id, ownerId });
    return result.deletedCount > 0;
  }

  async findByDevice(deviceId: string): Promise<IScript[]> {
    return Script.find({ deviceId, enabled: true });
  }
}

export const scriptService = new ScriptService();
