import Link from "next/link";
import { Home } from "lucide-react"; "next/link";

export default function Header() {
  return (
    <header className="w-full bg-black text-white shadow-lg shadow-white/10 z-10 sticky top-0">
      <nav className="px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-6">
          <Link
            href="/"
            className="flex items-center px-4 py-2 rounded-xl font-bold hover:bg-white hover:text-black transition-all duration-300"
          >
            <Home className="w-6 h-6" />
          </Link>
        </div>

        <div className="flex items-center gap-10">
          <Link
            href="/banking"
            className="px-4 py-2 rounded-xl font-bold hover:bg-white hover:text-black transition-all duration-300"
          >
            Banking
          </Link>

          <Link
            href="/automailer"
            className="px-4 py-2 rounded-xl font-bold hover:bg-white hover:text-black transition-all duration-300"
          >
            Automailer
          </Link>

          <Link
            href="/booker"
            className="px-4 py-2 rounded-xl font-bold hover:bg-white hover:text-black transition-all duration-300"
          >
            Booker
          </Link>
        </div>

        <Link
          href="/profile"
          className="px-4 py-2 rounded-xl font-bold hover:bg-white hover:text-black transition-all duration-300"
        >
          Profile
        </Link>
      </nav>
    </header>
  );
}
