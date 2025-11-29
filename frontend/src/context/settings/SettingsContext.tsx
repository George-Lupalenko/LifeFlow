'use client';

import { createContext, useContext, useState, ReactNode, useEffect } from "react";

export interface UserSettings {
  name: string;
  email: string;
  bankEmail: string;
  bankEmailPassword: string;
  bankEmailAppPassword: string;
}

interface SettingsContextType {
  settings: UserSettings;
  setSettings: (s: UserSettings) => void;
  updateField: (field: keyof UserSettings, value: any) => void;
}

const SettingsContext = createContext<SettingsContextType | null>(null);

export const SettingsProvider = ({ children }: { children: ReactNode }) => {
  const [settings, setSettings] = useState<UserSettings>({
    name: "",
    email: "",
    bankEmail: "",
    bankEmailPassword: "",
    bankEmailAppPassword: "",
  });

  useEffect(() => {
    const saved = localStorage.getItem("profileSettings");
    if (saved) setSettings(JSON.parse(saved));
  }, []);

  useEffect(() => {
    localStorage.setItem("profileSettings", JSON.stringify(settings));
  }, [settings]);

  const updateField = (field: keyof UserSettings, value: any) => {
    setSettings(prev => ({ ...prev, [field]: value }));
  };

  return (
    <SettingsContext.Provider value={{ settings, setSettings, updateField }}>
      {children}
    </SettingsContext.Provider>
  );
};

export const useSettings = () => {
  const ctx = useContext(SettingsContext);
  if (!ctx) throw new Error("useSettings must be used inside SettingsProvider");
  return ctx;
};
