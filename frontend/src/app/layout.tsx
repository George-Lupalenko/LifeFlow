import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import Header from "@/components/Header/Header";
import { SettingsProvider } from "@/context/settings/SettingsContext";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "LifeFlow",
  description: "AI-assistent for your life",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased bg-black/5 overflow-auto overflow-x-hidden flex flex-col min-h-screen`}
      >
        <SettingsProvider>
          <Header />
          <div className="flex-1 flex flex-col overflow-auto">
            {children}
          </div>
        </SettingsProvider>
      </body>
    </html>
  );
}
