// webpack.dev.js
const { merge } = require('webpack-merge');
const common = require('./webpack.config.js');
const HtmlWebpackPlugin = require("html-webpack-plugin");
const path = require("path");

module.exports = merge(common, module.exports = {
    mode: 'development',
    entry: {
        index: './frontend/index.js',
    },
    plugins: [
        new HtmlWebpackPlugin({
            title: 'Development',
            filename: './frontend/index.html'
        }),
    ],
    output: {
        filename: '[name].bundle.js',
        path: path.resolve(__dirname, 'dist'),
        clean: true,
    },
    devServer: {
        historyApiFallback: true
    }
})
