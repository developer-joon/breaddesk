import { create } from 'zustand';

type Theme = 'light' | 'dark' | 'system';

interface ThemeState {
  theme: Theme;
  resolvedTheme: 'light' | 'dark';
  setTheme: (theme: Theme) => void;
  initTheme: () => void;
}

const getSystemTheme = (): 'light' | 'dark' => {
  if (typeof window === 'undefined') return 'light';
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
};

const applyTheme = (theme: 'light' | 'dark') => {
  if (typeof window === 'undefined') return;
  const root = window.document.documentElement;
  root.classList.remove('light', 'dark');
  root.classList.add(theme);
};

export const useThemeStore = create<ThemeState>((set, get) => ({
  theme: 'system',
  resolvedTheme: 'light',

  setTheme: (theme: Theme) => {
    const resolved = theme === 'system' ? getSystemTheme() : theme;
    localStorage.setItem('theme', theme);
    applyTheme(resolved);
    set({ theme, resolvedTheme: resolved });
  },

  initTheme: () => {
    if (typeof window === 'undefined') return;

    const stored = localStorage.getItem('theme') as Theme | null;
    const theme = stored || 'system';
    const resolved = theme === 'system' ? getSystemTheme() : theme;

    applyTheme(resolved);
    set({ theme, resolvedTheme: resolved });

    // Listen for system theme changes
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const handleChange = () => {
      if (get().theme === 'system') {
        const newResolved = getSystemTheme();
        applyTheme(newResolved);
        set({ resolvedTheme: newResolved });
      }
    };
    mediaQuery.addEventListener('change', handleChange);
  },
}));
