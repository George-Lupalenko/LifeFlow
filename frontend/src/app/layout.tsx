import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import Header from "@/components/Header/Header";
<<<<<<< HEAD
import { SettingsProvider } from "@/context/settings/SettingsContext";
=======
>>>>>>> e9b8d7dbe94ebc876e598a08c0c1ce8bc9e9f7c9

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
        className={`${geistSans.variable} ${geistMono.variable} antialiased overflow-auto overflow-x-hidden flex flex-col min-h-screen`}
      >
<<<<<<< HEAD
        <SettingsProvider>
          <Header />
          <div className="flex-1 flex flex-col overflow-auto">
            {children}
          </div>
        </SettingsProvider>
=======
        <Header />
        <div className="flex-1 flex flex-col overflow-auto">
          {children}
        </div>
>>>>>>> e9b8d7dbe94ebc876e598a08c0c1ce8bc9e9f7c9
      </body>
    </html>
  );
}
