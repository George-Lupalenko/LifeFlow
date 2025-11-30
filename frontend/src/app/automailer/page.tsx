'use client';
import axios from 'axios';
import { use, useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useSettings } from '@/context/settings/SettingsContext';

export default function AutoMailerPage() {
    const [recipient, setRecipient] = useState('');
    const [subject, setSubject] = useState('');
    const [emailBody, setEmailBody] = useState('');
    const [notification, setNotification] = useState<{ message: string; type: 'success' | 'error' } | null>(null);
    const [responseBody, setResponseBody] = useState<any>(null);
    const [isResponseOpen, setIsResponseOpen] = useState(false);
    const [isSending, setIsSending] = useState(false);
    const { settings } = useSettings();

    const [recentRecipients, setRecentRecipients] = useState<string[]>([]);

    useEffect(() => {
        const stored = localStorage.getItem("recentRecipients");
        if (stored) {
            setRecentRecipients(JSON.parse(stored));
        }
    }, []);

    const saveRecentRecipients = (list: string[]) => {
        localStorage.setItem('recentRecipients', JSON.stringify(list));
    };

    const handleSendEmail = async () => {
        if (!subject) return;
        setIsSending(true);

        try {
            const promtText = `send email for ${subject} with the following email body: ${emailBody} ${settings.name ? 'and' + settings.name + 'using as my name' : ''} ${settings.email ? 'and' + settings.email + 'using as my email if needed' : ''}`
            console.log(promtText)
            const response = await axios.post(process.env.NEXT_PUBLIC_AUTO_MAILER || '', {
                prompt: promtText,
            });

            if (response.status === 201 || response.status === 200) {
                console.log(response)
                setResponseBody(response.data.body);
                setIsResponseOpen(true);
                setRecentRecipients(prev => {
                    const updated = [...prev, subject];
                    if (updated.length > 5) updated.shift();
                    saveRecentRecipients(updated);
                    return updated;
                });
                setRecipient('');
                setSubject('');
                setEmailBody('');
                setNotification({ message: 'Email sent successfully!', type: 'success' });
            } else {
                console.log(response)
                setNotification({ message: 'Failed to send email.', type: 'error' });
            }
        } catch (error) {
            console.error(error);
            setNotification({ message: 'Failed to send email.', type: 'error' });
        } finally {
            setIsSending(false);
            setTimeout(() => setNotification(null), 10000);
        }
    };


    return (
        <div className="max-w-2xl min-w-[600px] mx-auto mt-10 p-6 bg-white/5 rounded-2xl shadow-2xl border border-white/10 backdrop-blur flex flex-col items-center">
            <h1 className="text-4xl font-bold mb-10">AutoMailer</h1>

            <div className="mb-6 w-full max-w-lg z-20">
                <label className="block mb-2 font-semibold">Recipient</label>
                <select
                    className="w-full px-4 py-2 rounded-lg bg-gray-900 border border-gray-700 focus:outline-none focus:ring-2 focus:ring-purple-500"
                    value={recipient}
                    onChange={(e) => {
                        setSubject(e.target.value)
                        setRecipient(e.target.value)
                    }}
                >
                    <option value="">Select recipient</option>
                    {recentRecipients.map((email) => (
                        <option key={email} value={email}>{email}</option>
                    ))}
                    <option value="custom">Enter new</option>
                </select>

                {recipient === 'custom' && (
                    <input
                        type="email"
                        placeholder="Enter email"
                        className="mt-2 w-full px-4 py-2 rounded-lg bg-gray-900 border border-gray-700 focus:outline-none focus:ring-2 focus:ring-purple-500"
                        onChange={(e) => setSubject(e.target.value)}
                    />
                )}
            </div>

            <div className="mb-6 w-full max-w-lg">
                <label className="block mb-2 font-semibold">Email Body</label>
                <textarea
                    className="w-full h-48 px-4 py-3 rounded-lg bg-gray-900 border border-gray-700 focus:outline-none focus:ring-2 focus:ring-purple-500 resize-none"
                    placeholder="Describe your email here..."
                    value={emailBody}
                    onChange={(e) => setEmailBody(e.target.value)}
                />
            </div>
            {responseBody && (
                <div className="w-full max-w-lg mt-8">
                    <div
                        onClick={() => setIsResponseOpen(!isResponseOpen)}
                        className="flex justify-between items-center cursor-pointer bg-gray-900 rounded-lg p-4 hover:bg-gray-700 transition-colors"
                    >
                        <span className="font-semibold text-white">Email Response</span>
                        <motion.div
                            animate={{ rotate: isResponseOpen ? 90 : 0 }}
                            className="w-4 h-4 text-white border-l-4 border-t-4 border-white transform"
                        />
                    </div>

                    <AnimatePresence>
                        {isResponseOpen && (
                            <motion.div
                                initial={{ opacity: 0, height: 0 }}
                                animate={{ opacity: 1, height: 'auto' }}
                                exit={{ opacity: 0, height: 0 }}
                                className="overflow-hidden bg-gray-900 rounded-b-lg p-4 text-gray-200 text-sm whitespace-pre-wrap"
                            >
                                {responseBody}
                            </motion.div>
                        )}
                    </AnimatePresence>
                </div>
            )}

            <button
                onClick={handleSendEmail}
                disabled={isSending}
                className={`px-6 py-3 mt-10 rounded-full text-white font-bold transition-colors duration-300 ${isSending ? 'bg-gray-500 cursor-not-allowed' : 'bg-purple-500 hover:bg-purple-400'}`}
            >
                {isSending ? 'Sending…' : 'Send Email'}
            </button>

            <AnimatePresence>
                {notification && (
                    <motion.div
                        initial={{ opacity: 0, x: 100 }}
                        animate={{ opacity: 1, x: 0 }}
                        exit={{ opacity: 0, x: 100 }}
                        className={`fixed bottom-6 right-6 max-w-xs px-6 py-4 rounded-lg shadow-lg text-white font-bold ${notification.type === 'success' ? 'bg-green-500' : 'bg-red-500'}`}
                    >
                        <div className="flex justify-between items-center">
                            <span>{notification.message}</span>
                            <button onClick={() => setNotification(null)} className="ml-4 font-bold">✕</button>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
}