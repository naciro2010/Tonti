const defaultTheme = require('tailwindcss/defaultTheme');

module.exports = {
  content: ['src/**/*.{astro,html,js,jsx,ts,tsx,md,mdx}'],
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
    },
  },
  plugins: [require('@tailwindcss/forms'), require('@tailwindcss/typography')],
};
