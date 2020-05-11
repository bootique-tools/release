const path = require("path");
const webpack = require("webpack");
const sass = require("sass");
const ExtractTextPlugin = require("extract-text-webpack-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const autoprefixer = require("autoprefixer")({
    overrideBrowserslist: ["> 1%", "last 2 versions", "Firefox ESR"],
    remove: false
});

module.exports = function (options = {}) {
  // Settings
  // --env.NODE_ENV root --env.SOURCE_MAP source-map ...
  mode = 'production';
  const NODE_ENV = options.NODE_ENV || "development"; // "production"
  const SOURCE_MAP = options.SOURCE_MAP || "eval-source-map"; // "source-map"
  console.log(`
Build started with following configuration:
===========================================
→ NODE_ENV: ${NODE_ENV}
→ SOURCE_MAP: ${SOURCE_MAP}
`);

  const publicPath = "/";
  const limit = 1024;

  return {
      // mode: NODE_ENV,
      performance: {
        maxEntrypointSize: 400000,
        maxAssetSize: 400000
      },
      entry: {
        app: [
            path.resolve(__dirname, "js", "main.js")
        ]
      },
      output: {
        path: path.resolve(__dirname, "..", "..", "..", "target", "classes", "static"),
        filename: "[name].js?[hash]",
        libraryTarget: 'var',
        library: 'app',
        publicPath
      },
      resolve: {
        extensions: [".js"]
      },
      bail: false,
      devtool: SOURCE_MAP,
      module: {
        rules: [{
            test: /\.s[ac]ss$/,
            use: ExtractTextPlugin.extract({
                fallback: "style-loader",
                use: [{
                    loader: "css-loader"
                }, {
                    loader: "postcss-loader",
                    options: {
                        plugins: function () {
                            return [
                                autoprefixer
                            ];
                        },
                        url: false,
                        minimize: true,
                        sourceMap: true
                    }
                }, {
                    loader: "sass-loader",
                    options: {
                        implementation: sass,
                        sourceMap: true
                    }
                }]
            })
        }, {
            test: /\.css$/,
            use: ExtractTextPlugin.extract({
                fallback: "style-loader",
                use: [{
                    loader: "css-loader"
                }, {
                    loader: "postcss-loader",
                    options: {
                        plugins: function () {
                            return [
                                autoprefixer
                            ];
                        }
                    }
                }]
            })
        }, {
            test: /\.(png|jpg|gif)$/,
            loader: "url-loader",
            options: {
                limit,
                publicPath,
                name: "/images/[name].[ext]?[hash]"
            }
        }
        ]
      },
      plugins: [
          new ExtractTextPlugin("app.css?[hash]"),
          new webpack.ProvidePlugin({
              $: "jquery",
              jQuery: "jquery",
              "window.jQuery": "jquery",
              Popper: ['popper.js', 'default'],
              Util: "exports-loader?Util!bootstrap/js/dist/util"
          }),
          new HtmlWebpackPlugin({
              filename: "../../../target/classes/assets.mustache",
              template: "assets.ejs",
              inject: false
          })
      ]
  };
};