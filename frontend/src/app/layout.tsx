import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Alertas operativas",
  description: "Registro y consulta de alertas operativas por orden",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="es">
      <body>{children}</body>
    </html>
  );
}

