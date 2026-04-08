import mongoose, { Schema, Document } from 'mongoose';

export interface IDevice extends Document {
  ownerId: mongoose.Types.ObjectId;
  name: string;
  androidId: string;
  model: string;
  osVersion: string;
  appVersion: string;
  status: 'online' | 'offline' | 'error';
  lastSeenAt: Date;
  pairingCode: string | null;
  permissions: {
    notificationAccess: boolean;
    callPhone: boolean;
    sendSms: boolean;
    readSms: boolean;
    readPhoneState: boolean;
  };
  createdAt: Date;
  updatedAt: Date;
}

const deviceSchema = new Schema<IDevice>(
  {
    ownerId: { type: Schema.Types.ObjectId, ref: 'User', required: true },
    name: { type: String, required: true },
    androidId: { type: String, required: true },
    model: { type: String, required: true },
    osVersion: { type: String, required: true },
    appVersion: { type: String, required: true },
    status: { type: String, enum: ['online', 'offline', 'error'], default: 'offline' },
    lastSeenAt: { type: Date, default: Date.now },
    pairingCode: { type: String, default: null },
    permissions: {
      notificationAccess: { type: Boolean, default: false },
      callPhone: { type: Boolean, default: false },
      sendSms: { type: Boolean, default: false },
      readSms: { type: Boolean, default: false },
      readPhoneState: { type: Boolean, default: false },
    },
  },
  { timestamps: true }
);

deviceSchema.set('toJSON', {
  transform: (_doc, ret) => {
    ret.id = ret._id.toString();
    delete ret._id;
    delete ret.__v;
    return ret;
  },
});

export const Device = mongoose.model<IDevice>('Device', deviceSchema);
