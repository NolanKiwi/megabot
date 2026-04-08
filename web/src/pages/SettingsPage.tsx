import { useState } from 'react';
import { useAuthStore } from '../store/useAuthStore';

export default function SettingsPage() {
  const { user } = useAuthStore();
  const [showApiKey, setShowApiKey] = useState(false);

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold text-white mb-6">Settings</h1>

      {/* Profile */}
      <section className="bg-slate-800 border border-slate-700 rounded-xl p-5 mb-4">
        <h2 className="text-lg font-semibold text-white mb-4">Profile</h2>
        <div className="space-y-3">
          <div>
            <label className="text-sm text-slate-400">Display Name</label>
            <p className="text-white">{user?.displayName}</p>
          </div>
          <div>
            <label className="text-sm text-slate-400">Email</label>
            <p className="text-white">{user?.email}</p>
          </div>
          <div>
            <label className="text-sm text-slate-400">Role</label>
            <p className="text-white capitalize">{user?.role}</p>
          </div>
        </div>
      </section>

      {/* Bot Settings */}
      <section className="bg-slate-800 border border-slate-700 rounded-xl p-5 mb-4">
        <h2 className="text-lg font-semibold text-white mb-4">Bot Settings</h2>
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-white">Auto-reply</p>
              <p className="text-sm text-slate-400">Automatically execute response scripts</p>
            </div>
            <input type="checkbox" defaultChecked className="w-5 h-5 accent-blue-600" />
          </div>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-white">Log Messages</p>
              <p className="text-sm text-slate-400">Store incoming/outgoing messages</p>
            </div>
            <input type="checkbox" defaultChecked className="w-5 h-5 accent-blue-600" />
          </div>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-white">SMS Rate Limit</p>
              <p className="text-sm text-slate-400">Max SMS per hour (safety)</p>
            </div>
            <input
              type="number"
              defaultValue={10}
              min={1}
              max={100}
              className="w-20 px-2 py-1 bg-slate-700 border border-slate-600 rounded text-white text-sm text-center"
            />
          </div>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-white">Call Rate Limit</p>
              <p className="text-sm text-slate-400">Max calls per hour (safety)</p>
            </div>
            <input
              type="number"
              defaultValue={5}
              min={1}
              max={50}
              className="w-20 px-2 py-1 bg-slate-700 border border-slate-600 rounded text-white text-sm text-center"
            />
          </div>
        </div>
      </section>

      {/* API Access */}
      <section className="bg-slate-800 border border-slate-700 rounded-xl p-5">
        <h2 className="text-lg font-semibold text-white mb-4">API Access</h2>
        <p className="text-sm text-slate-400 mb-3">
          Use the REST API to integrate _nolanbot with external services.
        </p>
        <div className="bg-slate-900 rounded-lg p-3 font-mono text-sm">
          <div className="flex items-center justify-between">
            <span className="text-slate-400">
              {showApiKey ? 'Bearer eyJhbG...(your token)' : 'Bearer **********************'}
            </span>
            <button
              onClick={() => setShowApiKey(!showApiKey)}
              className="text-xs text-blue-400 hover:text-blue-300"
            >
              {showApiKey ? 'Hide' : 'Show'}
            </button>
          </div>
        </div>
      </section>
    </div>
  );
}
