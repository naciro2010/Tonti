const defaultTheme = require('tailwindcss/defaultTheme');
const plugin = require('tailwindcss/plugin');

module.exports = {
  content: ['index.html', './src/**/*.{vue,ts,tsx,js,jsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#FFB300',
        primaryHover: '#E6A100',
        primarySoft: '#FFD166',
        background: '#0B0F1A',
        backgroundElevated: '#0F1524',
        surface: '#111827',
        surfaceHover: '#1A2337',
        success: '#22C55E',
        successSoft: '#4ADE80',
        warning: '#F59E0B',
        warningSoft: '#FBBF24',
        danger: '#EF4444',
        dangerSoft: '#F87171',
      },
      fontFamily: {
        sans: ['Inter', ...defaultTheme.fontFamily.sans],
        arabic: ['"Noto Kufi Arabic"', ...defaultTheme.fontFamily.sans],
        display: ['Inter', ...defaultTheme.fontFamily.sans],
      },
      screens: {
        xs: '420px',
      },
      boxShadow: {
        glow: '0 20px 45px -18px rgba(255, 179, 0, 0.45)',
        'glow-lg': '0 30px 60px -20px rgba(255, 179, 0, 0.55)',
        card: '0 14px 45px -25px rgba(0, 0, 0, 0.8)',
      },
      backgroundImage: {
        'hero-radial':
          'radial-gradient(60% 60% at 50% 0%, rgba(255,179,0,0.18) 0%, rgba(255,179,0,0) 70%)',
        'grid-fade':
          'linear-gradient(180deg, rgba(255,255,255,0.04) 0%, rgba(255,255,255,0) 100%)',
      },
      keyframes: {
        'fade-in-up': {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        shimmer: {
          '100%': { transform: 'translateX(100%)' },
        },
        'pulse-ring': {
          '0%': { boxShadow: '0 0 0 0 rgba(255,179,0,0.45)' },
          '70%': { boxShadow: '0 0 0 12px rgba(255,179,0,0)' },
          '100%': { boxShadow: '0 0 0 0 rgba(255,179,0,0)' },
        },
      },
      animation: {
        'fade-in-up': 'fade-in-up 420ms ease-out both',
        'fade-in': 'fade-in 300ms ease-out both',
        shimmer: 'shimmer 1.6s linear infinite',
        'pulse-ring': 'pulse-ring 1.8s cubic-bezier(0.66, 0, 0, 1) infinite',
      },
    },
  },
  plugins: [
    plugin(({ addVariant }) => {
      addVariant('rtl', "&[dir='rtl'] &");
      addVariant('ltr', "&[dir='ltr'] &");
    }),
    plugin(({ addVariant }) => {
      addVariant('supports-backdrop', '@supports (backdrop-filter: blur(4px)) &');
    }),
  ],
};
