import "./globals.css";
import { AuthProvider } from "@/components/AuthProvider";
import Navbar from "@/components/Navbar";

export const metadata = {
  title: "College Meeting Room Booking",
  description: "Book study rooms and meeting spaces in your college.",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body className="app-body">
        <AuthProvider>
          <Navbar />
          <main className="app-main">{children}</main>
        </AuthProvider>
      </body>
    </html>
  );
}
