'use client';

import { useState } from 'react';
import axios from 'axios';

export default function BookerPage() {
  const [prompt, setPrompt] = useState('');
  const [loading, setLoading] = useState(false);
  const [responseText, setResponseText] = useState<string | null>(null);
  const [modal, setModal] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const handleGenerate = async () => {
    if (!prompt) return;

    setLoading(true);
    setResponseText(null);

    try {
      const res = await axios.post(process.env.NEXT_PUBLIC_BOOKER || '', {
        query: prompt,
      });

      setResponseText(res.data.airecomendation || JSON.stringify(res.data));
      setModal({ type: 'success', message: 'Plan generated successfully!' });
    } catch (err) {
      setModal({ type: 'error', message: 'Failed to generate plan.' });
    }

    setLoading(false);
  };

  return (
    <div className="max-w-2xl min-w-[400px] mx-auto mt-10 p-6 bg-white/5 rounded-2xl shadow-xl border border-white/10 backdrop-blur">
      <h1 className="text-3xl font-bold text-center text-white mb-6">Booker</h1>

      <div className="mb-6">
        <label className="block font-semibold mb-1">Enter your travel prompt</label>
        <textarea
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          className="w-full h-32 bg-gray-800 border border-gray-700 px-4 py-2 rounded-lg outline-none focus:ring-2 focus:ring-purple-500 resize-none"
          placeholder="e.g. I want to go to Paris for 4 days next week"
        />
      </div>

      <button
        onClick={handleGenerate}
        disabled={loading}
        className={`w-full py-3 rounded-full font-semibold transition-all duration-300 shadow-lg
          ${loading ? 'bg-gray-600 cursor-not-allowed opacity-50' : 'bg-purple-600 hover:bg-purple-500 shadow-purple-600/30'}
          ${loading ? 'scale-95' : 'scale-100'}`}
      >
        {loading ? 'Generating...' : 'Generate Plan'}
      </button>

      {loading && (
        <div className="fixed inset-0 flex items-center justify-center bg-black/60 backdrop-blur-sm z-50">
          <div className="bg-white/10 border border-white/30 p-8 rounded-2xl shadow-xl flex flex-col items-center gap-4">
            <div className="w-12 h-12 border-4 border-white/30 border-t-white rounded-full animate-spin"></div>
            <p className="text-white text-lg font-medium">Generating your travel plan…</p>
          </div>
        </div>
      )}

      {modal && (
        <div className="fixed inset-0 flex items-center justify-center bg-black/60 backdrop-blur-sm z-50">
          <div className="bg-gray-900 border border-white/20 p-8 rounded-2xl shadow-xl text-center w-80">
            <h2
              className={`text-2xl font-bold mb-3 ${
                modal.type === 'success' ? 'text-green-400' : 'text-red-400'
              }`}
            >
              {modal.type === 'success' ? 'Success' : 'Error'}
            </h2>

            <p className="text-white mb-6">{modal.message}</p>

            <button
              onClick={() => setModal(null)}
              className="px-6 py-2 bg-purple-600 hover:bg-purple-500 rounded-full text-white font-medium"
            >
              Close
            </button>
          </div>
        </div>
      )}

      {responseText && (
        <div className="mt-10 p-6 bg-gray-900 border border-white/10 rounded-xl text-white">
          <h2 className="text-xl font-bold mb-4">Generated Travel Plan:</h2>
          <pre className="whitespace-pre-wrap bg-black/40 p-4 rounded-lg border border-white/10 text-gray-200">
            {responseText}
          </pre>
        </div>
      )}
    </div>
  );
}
