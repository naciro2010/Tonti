const defaultTheme = require('tailwindcss/defaultTheme');
const plugin = require('tailwindcss/plugin');

module.exports = {
  content: ['index.html', './src/**/*.{vue,ts,tsx,js,jsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#FFB300',
        primaryHover: '#E6A100',
        background: '#0B0F1A',
        surface: '#111827',
        success: '#16A34A',
        warning: '#F59E0B',
        danger: '#DC2626',
      },
      fontFamily: {
        sans: ['Inter', ...defaultTheme.fontFamily.sans],
        arabic: ['"Noto Kufi Arabic"', ...defaultTheme.fontFamily.sans],
      },
      screens: {
        xs: '420px',
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
