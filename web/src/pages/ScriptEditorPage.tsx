import { useEffect, useState } from 'react';
import Editor from '@monaco-editor/react';
import { useScriptStore } from '../store/useScriptStore';
import { getSocket } from '../socket/socketClient';

const DEFAULT_CODE = `function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {
    if (msg == "ping") {
        replier.reply("pong");
    }

    // Phone call example:
    // Phone.call("01012345678");

    // SMS example:
    // Sms.send("01012345678", "Hello!");

    // HTTP example:
    // var res = Http.requestSync("https://api.example.com/data");
    // replier.reply(res.body);
}
`;

export default function ScriptEditorPage() {
  const { scripts, selectedScript, fetchScripts, selectScript, createScript, updateScript, deleteScript } =
    useScriptStore();
  const [code, setCode] = useState(DEFAULT_CODE);
  const [scriptName, setScriptName] = useState('');
  const [compileStatus, setCompileStatus] = useState<string | null>(null);

  useEffect(() => {
    fetchScripts();
  }, [fetchScripts]);

  useEffect(() => {
    if (selectedScript) {
      setCode(selectedScript.code);
      setScriptName(selectedScript.name);
    }
  }, [selectedScript]);

  useEffect(() => {
    const socket = getSocket();
    const handler = (result: { scriptId: string; success: boolean; error?: string }) => {
      if (selectedScript && result.scriptId === selectedScript.id) {
        setCompileStatus(result.success ? 'Compiled successfully' : `Error: ${result.error}`);
      }
    };
    socket.on('script:compiled', handler);
    return () => { socket.off('script:compiled', handler); };
  }, [selectedScript]);

  const handleSave = async () => {
    if (selectedScript) {
      await updateScript(selectedScript.id, { code, name: scriptName });
      getSocket().emit('script:save', { scriptId: selectedScript.id, code });
      setCompileStatus('Saved & deployed');
    }
  };

  const handleCreate = async () => {
    const name = prompt('Script name:');
    if (!name) return;
    const script = await createScript(name, DEFAULT_CODE, ['com.kakao.talk']);
    selectScript(script);
  };

  const handleCompile = () => {
    if (!selectedScript) return;
    setCompileStatus('Compiling...');
    getSocket().emit('script:compile', { scriptId: selectedScript.id });
  };

  const handleToggle = async () => {
    if (!selectedScript) return;
    await updateScript(selectedScript.id, { enabled: !selectedScript.enabled });
    getSocket().emit('script:toggle', {
      scriptId: selectedScript.id,
      enabled: !selectedScript.enabled,
    });
  };

  return (
    <div className="flex h-[calc(100vh-3rem)] gap-4">
      {/* Script list sidebar */}
      <div className="w-56 bg-slate-800 border border-slate-700 rounded-xl p-3 flex flex-col">
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-semibold text-white">Scripts</h3>
          <button
            onClick={handleCreate}
            className="text-xs px-2 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded"
          >
            + New
          </button>
        </div>
        <div className="flex-1 overflow-auto space-y-1">
          {scripts.map((s) => (
            <button
              key={s.id}
              onClick={() => selectScript(s)}
              className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors ${
                selectedScript?.id === s.id
                  ? 'bg-blue-600 text-white'
                  : 'text-slate-300 hover:bg-slate-700'
              }`}
            >
              <div className="flex items-center gap-2">
                <span className={`w-2 h-2 rounded-full ${s.enabled ? 'bg-green-400' : 'bg-slate-500'}`} />
                {s.name}
              </div>
            </button>
          ))}
        </div>
      </div>

      {/* Editor area */}
      <div className="flex-1 flex flex-col bg-slate-800 border border-slate-700 rounded-xl overflow-hidden">
        {/* Toolbar */}
        <div className="flex items-center gap-2 p-3 border-b border-slate-700">
          {selectedScript ? (
            <>
              <input
                value={scriptName}
                onChange={(e) => setScriptName(e.target.value)}
                className="px-2 py-1 bg-slate-700 border border-slate-600 rounded text-white text-sm flex-1 max-w-xs"
              />
              <button
                onClick={handleSave}
                className="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded"
              >
                Save & Deploy
              </button>
              <button
                onClick={handleCompile}
                className="px-3 py-1 bg-purple-600 hover:bg-purple-700 text-white text-sm rounded"
              >
                Compile
              </button>
              <button
                onClick={handleToggle}
                className={`px-3 py-1 text-sm rounded ${
                  selectedScript.enabled
                    ? 'bg-red-600 hover:bg-red-700 text-white'
                    : 'bg-green-600 hover:bg-green-700 text-white'
                }`}
              >
                {selectedScript.enabled ? 'Disable' : 'Enable'}
              </button>
              <button
                onClick={() => { deleteScript(selectedScript.id); selectScript(null); }}
                className="px-3 py-1 bg-slate-600 hover:bg-red-600 text-white text-sm rounded"
              >
                Delete
              </button>
            </>
          ) : (
            <span className="text-slate-400 text-sm">Select a script or create a new one</span>
          )}
        </div>

        {/* Status bar */}
        {compileStatus && (
          <div
            className={`px-3 py-1 text-xs ${
              compileStatus.startsWith('Error') ? 'bg-red-500/20 text-red-400' : 'bg-green-500/20 text-green-400'
            }`}
          >
            {compileStatus}
          </div>
        )}

        {/* Monaco Editor */}
        <div className="flex-1">
          <Editor
            height="100%"
            defaultLanguage="javascript"
            theme="vs-dark"
            value={code}
            onChange={(val) => setCode(val || '')}
            options={{
              minimap: { enabled: false },
              fontSize: 14,
              wordWrap: 'on',
              scrollBeyondLastLine: false,
              automaticLayout: true,
            }}
          />
        </div>
      </div>
    </div>
  );
}
