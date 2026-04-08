import mongoose, { Schema, Document } from 'mongoose';

export interface IMessageLog extends Document {
  deviceId: mongoose.Types.ObjectId;
  direction: 'in' | 'out';
  packageName: string;
  sender: string;
  room: string;
  content: string;
  isGroupChat: boolean;
  scriptId: mongoose.Types.ObjectId | null;
  timestamp: Date;
}

const messageLogSchema = new Schema<IMessageLog>({
  deviceId: { type: Schema.Types.ObjectId, ref: 'Device', required: true, index: true },
  direction: { type: String, enum: ['in', 'out'], required: true },
  packageName: { type: String, required: true },
  sender: { type: String, default: '' },
  room: { type: String, required: true },
  content: { type: String, required: true },
  isGroupChat: { type: Boolean, default: false },
  scriptId: { type: Schema.Types.ObjectId, ref: 'Script', default: null },
  timestamp: { type: Date, default: Date.now, index: true },
});

messageLogSchema.set('toJSON', {
  transform: (_doc, ret) => {
    ret.id = ret._id.toString();
    delete ret._id;
    delete ret.__v;
    return ret;
  },
});

export const MessageLog = mongoose.model<IMessageLog>('MessageLog', messageLogSchema);
