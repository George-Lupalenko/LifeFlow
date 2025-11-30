// components/AnimatedButtons.tsx
'use client';

import Link from 'next/link';
import { motion } from 'framer-motion';

const buttonVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: (i: number) => ({
        opacity: 1,
        y: 0,
        transition: { delay: i * 0.15, duration: 0.5 },
    }),
};

interface Button {
    href: string;
    label: string;
}

interface Props {
    buttons: Button[];
}

export default function AnimatedButtons({ buttons }: Props) {
    return (
        <div className="flex flex-wrap gap-6 justify-center">
            {buttons.map((btn, i) => (
                <motion.div
                    key={btn.href}
                    custom={i}
                    initial="hidden"
                    animate="visible"
                    variants={buttonVariants}
                >
                    <motion.div
                        whileHover={{ scale: 1.1, y: -2 }}
                        whileTap={{ scale: 0.95 }}
                        className="inline-block"
                    >
                        <Link
                            href={btn.href}
                            className="px-6 py-3 rounded-full bg-white text-black font-bold hover:bg-gray-200 transition-colors duration-300"
                        >
                            {btn.label}
                        </Link>

                    </motion.div>
                </motion.div>
            ))}
        </div>
    );
}
