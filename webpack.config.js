const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    entry: {
        main: './frontend/index.js'
    },
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: ['babel-loader']
            },
            {
                test: /\.less$/,
                loader: ['style-loader', 'css-loader', 'less-loader']
            }
        ]
    },
    resolve: {
        extensions: ['*', '.js', '.jsx']
    },
    output: {
        filename: '[name].[contenthash].js',
        path: path.resolve(__dirname, 'src', 'main', 'resources', 'static'),
        publicPath: '/'
    },
    plugins: [new HtmlWebpackPlugin({
        title: 'Distribusjon av brevet',
        template: path.resolve(__dirname, 'frontend', 'index.html'),
        meta: {
            'charset': 'utf-8',
            // 'X-UA-Compatible': 'content="IE=edge"'
        }
    })],
    devtool: "eval",
    devServer: {
        contentBase: false,
        historyApiFallback: true,
        proxy: {
            '/api': 'http://localhost:8081'
        }
    }
};
