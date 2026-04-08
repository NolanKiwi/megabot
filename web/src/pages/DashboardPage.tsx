import { useEffect } from 'react';
import { useDeviceStore } from '../store/useDeviceStore';
import { useScriptStore } from '../store/useScriptStore';

export default function DashboardPage() {
  const { devices, fetchDevices } = useDeviceStore();
  const { scripts, fetchScripts } = useScriptStore();

  useEffect(() => {
    fetchDevices();
    fetchScripts();
  }, [fetchDevices, fetchScripts]);

  const onlineDevices = devices.filter((d) => d.status === 'online').length;
  const activeScripts = scripts.filter((s) => s.enabled).length;

  const stats = [
    { label: 'Devices Online', value: `${onlineDevices} / ${devices.length}`, color: 'bg-green-500' },
    { label: 'Active Scripts', value: `${activeScripts}`, color: 'bg-blue-500' },
    { label: 'Total Scripts', value: `${scripts.length}`, color: 'bg-purple-500' },
    { label: 'Platform', value: 'Running', color: 'bg-emerald-500' },
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold text-white mb-6">Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {stats.map((stat) => (
          <div key={stat.label} className="bg-slate-800 border border-slate-700 rounded-xl p-5">
            <div className="flex items-center gap-3 mb-2">
              <div className={`w-2 h-2 rounded-full ${stat.color}`} />
              <span className="text-sm text-slate-400">{stat.label}</span>
            </div>
            <p className="text-2xl font-bold text-white">{stat.value}</p>
          </div>
        ))}
      </div>

      {/* Recent devices */}
      <div className="bg-slate-800 border border-slate-700 rounded-xl p-5">
        <h2 className="text-lg font-semibold text-white mb-4">Connected Devices</h2>
        {devices.length === 0 ? (
          <p className="text-slate-400 text-sm">No devices registered yet. Go to Devices to pair one.</p>
        ) : (
          <div className="space-y-3">
            {devices.map((device) => (
              <div
                key={device.id}
                className="flex items-center justify-between p-3 bg-slate-700/50 rounded-lg"
              >
                <div>
                  <p className="text-white font-medium">{device.name}</p>
                  <p className="text-sm text-slate-400">{device.model} - Android {device.osVersion}</p>
                </div>
                <span
                  className={`px-2 py-1 text-xs rounded-full ${
                    device.status === 'online'
                      ? 'bg-green-500/20 text-green-400'
                      : 'bg-slate-600 text-slate-400'
                  }`}
                >
                  {device.status}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
