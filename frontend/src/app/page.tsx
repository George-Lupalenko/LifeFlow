import Link from "next/link";
import { Home } from "lucide-react";
import { motion } from "framer-motion";

import AnimatedButtons from '../components/Motion/Buttons';

export default function MainPage() {
  const buttons = [
    { href: '/banking', label: 'Banking' },
    { href: '/automailer', label: 'AutoMailer' },
    { href: '/events', label: 'Event Planner' },
    { href: '/booker', label: 'Booker' },
  ];

  return (
    <>
      <main className="w-full flex-1 bg-gradient-to-b from-black via-gray-900 to-black overflow-hidden flex flex-col items-center justify-center  text-white text-center px-6">
        <h1 className="text-5xl md:text-6xl lg:text-7xl font-extrabold mb-16 drop-shadow-lg">
          Hi! How can I help you today?
        </h1>

        <AnimatedButtons buttons={buttons} />

        {/* <div className="fixed top-0 left-1/2 transform -translate-x-1/2 w-[600px] h-[600px] bg-white/20 rounded-full blur-3xl animate-pulse -z-10 pointer-events-none"></div>
<div className="fixed bottom-0 right-1/4 w-[400px] h-[400px] bg-purple-600/20 rounded-full blur-2xl animate-ping -z-10 pointer-events-none"></div> */}


      </main>
    </>
  );
}