const defaultTheme = require('tailwindcss/defaultTheme');

module.exports = {
  content: ['index.html', 'src/**/*.{vue,ts,tsx,js,jsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: '#FFB300',
        secondary: '#111827',
        background: '#0B0F1A',
      },
      fontFamily: {
        sans: ['Inter', ...defaultTheme.fontFamily.sans],
        arabic: ['"Noto Kufi Arabic"', ...defaultTheme.fontFamily.sans],
      },
      boxShadow: {
        glow: '0 10px 50px rgba(255, 179, 0, 0.15)',
      },
    },
  },
  plugins: [],
};
