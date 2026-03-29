'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '../lib/utils';
import { Home, Inbox, ClipboardList, FileText, Settings } from 'lucide-react';

const navigation = [
  { name: '대시보드', href: '/', icon: Home },
  { name: '문의 관리', href: '/inquiries', icon: Inbox },
  { name: '업무 관리', href: '/tasks', icon: ClipboardList },
  { name: '답변 템플릿', href: '/templates', icon: FileText },
  { name: '설정', href: '/settings', icon: Settings },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="w-64 border-r bg-muted/40 p-4">
      <div className="mb-8">
        <h1 className="text-2xl font-bold">🍞 BreadDesk</h1>
        <p className="text-sm text-muted-foreground">AI Service Desk</p>
      </div>
      <nav className="space-y-2">
        {navigation.map((item) => {
          const Icon = item.icon;
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
                isActive
                  ? 'bg-primary text-primary-foreground'
                  : 'hover:bg-muted'
              )}
            >
              <Icon className="h-5 w-5" />
              {item.name}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
