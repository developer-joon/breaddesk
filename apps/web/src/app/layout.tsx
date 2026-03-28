import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'BreadDesk',
  description: 'AI Service Desk + Task Management',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
