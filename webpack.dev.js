// webpack.dev.js
const merge = require('webpack-merge');
const common = require('./webpack.config.js');

module.exports = merge(common, {
    mode: 'development',
    devtool: 'inline-source-map',
    devServer: {
        historyApiFallback: true,
        proxy: {
            '/api': 'http://localhost:8081'
        }
    }
});
