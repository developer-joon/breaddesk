const path = require('path');

module.exports = {
  plugins: {
    [path.resolve(__dirname, '../../node_modules/tailwindcss')]: {},
    [path.resolve(__dirname, '../../node_modules/autoprefixer')]: {},
  },
}
