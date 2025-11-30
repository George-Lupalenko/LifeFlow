"use client";
import { useSettings } from "@/context/settings/SettingsContext";
import Link from "next/link";
import { useState } from "react";
import axios from "axios";

export default function BankingPage() {
    const { settings } = useSettings();
    const [loading, setLoading] = useState(false);
    const [months, setMonths] = useState(3);

    const [result, setResult] = useState<any | null>(null);  
    const [modal, setModal] = useState<{ type: "success" | "error"; message: string } | null>(null);

    const hasBankingData =
        settings?.bankEmail &&
        settings?.bankPassword &&
        settings?.bankEmailAppPassword;

    const handleAnalyze = async () => {
        setLoading(true);

        try {
            const response = await axios.post(
                process.env.NEXT_PUBLIC_BANK_ANALYZER || "",
                {
                    username: settings.bankEmail,
                    password: settings.bankEmailAppPassword,
                    lastCount: months,
                    pdfPassword: settings.bankPassword,
                }
            );

            setResult(response.data);

            setModal({
                type: "success",
                message: "Banking statistics analyzed successfully!",
            });
        } catch (err) {
            setModal({
                type: "error",
                message: "Failed to analyze your statistics.",
            });
        }

        setLoading(false);
    };

    const handleMonthsChange = (value: string) => {
        const num = Number(value);
        if (isNaN(num)) return;
        if (num < 1) return setMonths(1);
        if (num > 24) return setMonths(24);
        setMonths(num);
    };

    return (
        <div className="max-w-2xl mx-auto mt-10 p-6 bg-white/5 rounded-2xl shadow-xl border border-white/10 backdrop-blur">
            <h1 className="text-3xl font-bold text-center text-white mb-6">
                Banking Analyze
            </h1>

            {!hasBankingData && (
                <div className="p-4 mb-6 bg-red-500/20 border border-red-500/40 text-white rounded-xl">
                    <p className="mb-3">
                        To use banking analytics, please fill in your banking details in your profile.
                    </p>

                    <Link
                        href="/profile"
                        className="inline-block px-5 py-2 bg-red-600 hover:bg-red-500 rounded-full transition-all"
                    >
                        Fill in details
                    </Link>
                </div>
            )}

            <div className="mb-6">
                <label className="block font-semibold mb-1">Months of analysis</label>
                <input
                    type="number"
                    min="1"
                    max="24"
                    value={months}
                    onChange={(e) => handleMonthsChange(e.target.value)}
                    className="w-full bg-gray-800 border border-gray-700 px-4 py-2 rounded-lg 
                    outline-none focus:ring-2 focus:ring-purple-500"
                />
            </div>

            <button
                disabled={!hasBankingData}
                onClick={handleAnalyze}
                className={`w-full py-3 rounded-full font-semibold transition-all duration-300 shadow-lg
                ${
                    hasBankingData
                        ? "bg-purple-600 hover:bg-purple-500 shadow-purple-600/30"
                        : "bg-gray-600 cursor-not-allowed opacity-50"
                }
                ${loading ? "scale-95" : "scale-100"}
            `}
            >
                {loading ? "Processing..." : "Analyze Statistics"}
            </button>

            {loading && (
                <div className="fixed inset-0 flex items-center justify-center bg-black/60 backdrop-blur-sm z-50">
                    <div className="bg-white/10 border border-white/30 p-8 rounded-2xl shadow-xl flex flex-col items-center gap-4">
                        <div className="w-12 h-12 border-4 border-white/30 border-t-white rounded-full animate-spin"></div>

                        <p className="text-white text-lg font-medium">
                            Analyzing statistics…
                        </p>
                    </div>
                </div>
            )}

            {modal && (
                <div className="fixed inset-0 flex items-center justify-center bg-black/60 backdrop-blur-sm z-50">
                    <div className="bg-gray-900 border border-white/20 p-8 rounded-2xl shadow-xl text-center w-80">
                        <h2
                            className={`text-2xl font-bold mb-3 ${
                                modal.type === "success" ? "text-green-400" : "text-red-400"
                            }`}
                        >
                            {modal.type === "success" ? "Success" : "Error"}
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

            {result && (
                <div className="mt-10 p-6 bg-gray-900 border border-white/10 rounded-xl text-white">
                    <h2 className="text-xl font-bold mb-4">Analysis Results:</h2>

                    <pre className="whitespace-pre-wrap bg-black/40 p-4 rounded-lg border border-white/10 text-gray-200">
                        {JSON.stringify(result, null, 2)}
                    </pre>
                </div>
            )}
        </div>
    );
}
