import mongoose, { Schema, Document } from 'mongoose';

export interface IScript extends Document {
  ownerId: mongoose.Types.ObjectId;
  name: string;
  code: string;
  enabled: boolean;
  compiledAt: Date | null;
  compileError: string | null;
  targetPackages: string[];
  deviceId: mongoose.Types.ObjectId | null;
  version: number;
  createdAt: Date;
  updatedAt: Date;
}

const scriptSchema = new Schema<IScript>(
  {
    ownerId: { type: Schema.Types.ObjectId, ref: 'User', required: true },
    name: { type: String, required: true },
    code: { type: String, default: '' },
    enabled: { type: Boolean, default: false },
    compiledAt: { type: Date, default: null },
    compileError: { type: String, default: null },
    targetPackages: [{ type: String }],
    deviceId: { type: Schema.Types.ObjectId, ref: 'Device', default: null },
    version: { type: Number, default: 1 },
  },
  { timestamps: true }
);

scriptSchema.set('toJSON', {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  transform: (_doc, ret: any) => {
    ret.id = ret._id.toString();
    delete ret._id;
    delete ret.__v;
    return ret;
  },
});

export const Script = mongoose.model<IScript>('Script', scriptSchema);
