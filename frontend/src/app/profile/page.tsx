'use client';

import { useEffect, useState } from "react";
import { useSettings, UserSettings } from "@/context/settings/SettingsContext";

export default function ProfilePage() {
  const [activeTab, setActiveTab] = useState("account");

  const { settings, setSettings } = useSettings();

  const [localSettings, setLocalSettings] = useState<UserSettings>(settings);
  const [saved, setSaved] = useState(false);

  const tabs = [
    { id: "emails", label: "Automailer" },
    { id: "banking", label: "Banking analysis" },
    { id: "booking", label: "Booker" },
  ];

  useEffect(() => {
    setActiveTab("emails");
    setLocalSettings(settings);
  }, [settings]);

   const handleFieldChange = (field: keyof UserSettings, value: any) => {
    setLocalSettings((prev) => ({ ...prev, [field]: value }));
  };

  const handleSave = () => {
    setSettings(localSettings);
    setSaved(true);
    setTimeout(() => setSaved(false), 1000);
  };

  return (
    <main className="flex-1 bg-black text-white py-12 px-6 flex flex-col items-center">
      <h1 className="text-4xl font-bold text-center mb-10">Profile Settings</h1>

      <div className="w-full max-w-xl bg-gray-900 border border-white/20 rounded-lg shadow-lg overflow-hidden">
        <div className="flex bg-gray-800">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`
                flex-1 px-6 py-2 text-center transition-all duration-200
                ${
                  activeTab === tab.id
                    ? "bg-gray-900 text-white font-semibold border-b-2 border-purple-500"
                    : "text-gray-400 hover:bg-gray-700"
                }
              `}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="p-6">
          {activeTab === "emails" && (
            <div className="space-y-4">
              <div>
                <label className="block font-semibold mb-1">Name</label>
                <input
                  value={localSettings.name}
                  onChange={(e) => handleFieldChange("name", e.target.value)}
                  className="w-full bg-gray-800 border border-gray-700 px-4 py-2 rounded-lg outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>

              <div>
                <label className="block font-semibold mb-1">Email</label>
                <input
                  value={localSettings.email}
                  onChange={(e) => handleFieldChange("email", e.target.value)}
                  className="w-full bg-gray-800 border border-gray-700 px-4 py-2 rounded-lg outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>
            </div>
          )}

          {activeTab === "banking" && (
            <div className="space-y-4">
              <div>
                <label className="block font-semibold mb-1">Bank Email</label>
                <input
                  value={localSettings.bankEmail}
                  onChange={(e) => handleFieldChange("bankEmail", e.target.value)}
                  className="w-full bg-gray-800 border border-gray-700 px-4 py-2 rounded-lg outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>

              <div>
                <label className="block font-semibold mb-1">Bank Email Password</label>
                <input
                  type="password"
                  value={localSettings.bankEmailPassword}
                  onChange={(e) => handleFieldChange("bankEmailPassword", e.target.value)}
                  className="w-full bg-gray-800 border border-gray-700 px-4 py-2 rounded-lg outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>

              <div>
                <label className="block font-semibold mb-1">Bank Email App Password</label>
                <input
                  value={localSettings.bankEmailAppPassword}
                  onChange={(e) => handleFieldChange("bankEmailAppPassword", e.target.value)}
                  className="w-full bg-gray-800 border border-gray-700 px-4 py-2 rounded-lg outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>
            </div>
          )}

          {activeTab === "booking" && (
            <div className="space-y-4">
              {/* To be added... */}
            </div>
          )}
        </div>
      </div>

      <div className="flex justify-center mt-8">
        <button
          onClick={handleSave}
          className="px-8 py-3 bg-purple-600 rounded-full text-white font-bold shadow-lg shadow-purple-600/30 transition-transform duration-150 transform active:scale-95 hover:bg-purple-500"
        >
          {saved ? "Saved!" : "Save Settings"}
        </button>
      </div>
    </main>
  );
}
