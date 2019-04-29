// webpack v4
const path = require('path');
const webpack = require("webpack");
// const nodeExternals = require('webpack-node-externals');
const HtmlReplaceWebpackPlugin = require("html-replace-webpack-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const CleanWebpackPlugin = require('clean-webpack-plugin');
var CopyWebpackPlugin = require("copy-webpack-plugin");

const outputPath = "public/graph/";

module.exports = (env, argv) => {

    const devMode = argv.mode !== 'production';

    const config = {
        entry: {
            webvowl: "./graph/webvowl/js/entry.js",
            "webvowl.app": "./graph/app/js/entry.js"
        },
        output: {
            path: path.resolve(__dirname, outputPath),
            publicPath: "",
            filename: 'js/[name].js',
            chunkFilename: "js/[chunkhash].js",
            libraryTarget: "assign",
            library: "[name]"
        },
        devtool: devMode ? "eval" : "source-map",

        module: {
            rules: [

                {
                    test: /\.html$/,
                    loader: "html-loader",
                },
                {
                    test: /\.css$/,
                    use: [
                        devMode ? 'style-loader' : MiniCssExtractPlugin.loader,
                        'css-loader',
                        'postcss-loader'
                    ]
                }
            ]
        },
        plugins: [
            new CleanWebpackPlugin(),
            new MiniCssExtractPlugin({
                filename:  'css/[name].css',
            }),
            new HtmlWebpackPlugin({
                inject: false,
                hash: true,
                template: './graph/index.html',
                filename: 'index.html'
            }),
            new HtmlReplaceWebpackPlugin({
                pattern: "<%= version %>",
                replacement: require("./package.json").version
            }),
            new CopyWebpackPlugin([
                { context: "graph/app", from: "data/**/*"},
                { from: "node_modules/d3/d3.min.js",to: "js"}
            ]),
            new webpack.ProvidePlugin({
                d3: "d3",
            })
        ],
        externals: {
            d3: "d3",
            Global: "Global",
            Namespaces: "Namespaces"
        },
        devServer: {
            contentBase: path.join(__dirname, outputPath),
            compress: true,
            hot: true,
            historyApiFallback: true,
            inline: true,
            watchContentBase: true,
            open: true,
            port: 8000
        }
    };

    if (devMode) {
        config.plugins.push(
            new webpack.HotModuleReplacementPlugin()
        );
    }


    return config;
};