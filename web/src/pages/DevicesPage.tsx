import { useEffect, useState } from 'react';
import { useDeviceStore } from '../store/useDeviceStore';
import { generatePairingCode } from '../api/devices';

export default function DevicesPage() {
  const { devices, loading, fetchDevices, deleteDevice } = useDeviceStore();
  const [pairingCode, setPairingCode] = useState<string | null>(null);
  const [codeLoading, setCodeLoading] = useState(false);
  const [codeError, setCodeError] = useState<string | null>(null);

  useEffect(() => {
    fetchDevices();
  }, [fetchDevices]);

  const handleGenerateCode = async () => {
    setCodeLoading(true);
    setCodeError(null);
    try {
      const code = await generatePairingCode();
      setPairingCode(code);
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : '알 수 없는 오류';
      setCodeError(`페어링 코드 발급 실패: ${msg}`);
    } finally {
      setCodeLoading(false);
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-white">Devices</h1>
        <button
          onClick={handleGenerateCode}
          disabled={codeLoading}
          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-60 text-white text-sm rounded-lg"
        >
          {codeLoading ? '발급 중...' : '+ Pair New Device'}
        </button>
      </div>

      {codeError && (
        <div className="mb-4 p-3 bg-red-500/20 border border-red-500/40 rounded-lg text-red-400 text-sm">
          {codeError}
        </div>
      )}

      {pairingCode && (
        <div className="mb-6 bg-blue-500/10 border border-blue-500/30 rounded-xl p-5 text-center">
          <p className="text-sm text-blue-300 mb-2">Enter this code in the MegaBot Android app:</p>
          <p className="text-4xl font-mono font-bold text-white tracking-widest">{pairingCode}</p>
          <p className="text-xs text-slate-400 mt-2">Code expires in 10 minutes</p>
        </div>
      )}

      {loading ? (
        <p className="text-slate-400">Loading devices...</p>
      ) : devices.length === 0 ? (
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-8 text-center">
          <p className="text-slate-400">No devices paired yet.</p>
          <p className="text-sm text-slate-500 mt-1">
            Install MegaBot on your Android device and use a pairing code to connect.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {devices.map((device) => (
            <div
              key={device.id}
              className="bg-slate-800 border border-slate-700 rounded-xl p-5"
            >
              <div className="flex items-start justify-between mb-3">
                <div>
                  <h3 className="text-white font-semibold">{device.name}</h3>
                  <p className="text-sm text-slate-400">{device.model}</p>
                </div>
                <span
                  className={`px-2 py-1 text-xs rounded-full ${
                    device.status === 'online'
                      ? 'bg-green-500/20 text-green-400'
                      : device.status === 'error'
                      ? 'bg-red-500/20 text-red-400'
                      : 'bg-slate-600 text-slate-400'
                  }`}
                >
                  {device.status}
                </span>
              </div>

              <div className="space-y-1 text-sm text-slate-400 mb-4">
                <p>Android {device.osVersion}</p>
                <p>App v{device.appVersion}</p>
                {device.permissions && (
                  <div className="flex flex-wrap gap-1 mt-2">
                    {device.permissions.notificationAccess && (
                      <span className="px-2 py-0.5 bg-green-500/10 text-green-400 text-xs rounded">Notifications</span>
                    )}
                    {device.permissions.sendSms && (
                      <span className="px-2 py-0.5 bg-green-500/10 text-green-400 text-xs rounded">SMS</span>
                    )}
                    {device.permissions.callPhone && (
                      <span className="px-2 py-0.5 bg-green-500/10 text-green-400 text-xs rounded">Phone</span>
                    )}
                  </div>
                )}
              </div>

              <button
                onClick={() => deleteDevice(device.id)}
                className="text-xs text-red-400 hover:text-red-300"
              >
                Remove Device
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
