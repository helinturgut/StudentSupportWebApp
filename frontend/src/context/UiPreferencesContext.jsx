import { createContext, useContext, useEffect, useState } from 'react';

const UiPreferencesContext = createContext(null);

const THEMES = ['system', 'light', 'dark'];
const TEXT_SIZES = ['small', 'medium', 'large'];

function readStored(key, allowed, fallback) {
  const value = localStorage.getItem(key);
  return allowed.includes(value) ? value : fallback;
}

export function UiPreferencesProvider({ children }) {
  const [theme, setTheme] = useState(() => readStored('theme', THEMES, 'system'));
  const [textSize, setTextSize] = useState(() => readStored('textSize', TEXT_SIZES, 'medium'));

  useEffect(() => {
    if (theme === 'system') {
      document.documentElement.removeAttribute('data-theme');
    } else {
      document.documentElement.setAttribute('data-theme', theme);
    }
    localStorage.setItem('theme', theme);
  }, [theme]);

  useEffect(() => {
    document.documentElement.setAttribute('data-text-size', textSize);
    localStorage.setItem('textSize', textSize);
  }, [textSize]);

  return (
    <UiPreferencesContext.Provider value={{ theme, setTheme, textSize, setTextSize }}>
      {children}
    </UiPreferencesContext.Provider>
  );
}

export function useUiPreferences() {
  const ctx = useContext(UiPreferencesContext);
  if (!ctx) throw new Error('useUiPreferences must be used within UiPreferencesProvider');
  return ctx;
}
