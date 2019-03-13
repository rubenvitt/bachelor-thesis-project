const
    path = require('path'),
    manifest = require('../manifest'),
    HtmlWebpackPlugin = require('html-webpack-plugin');

const titles = {
    '404': '404',
    '500': '500',
    'calendar': 'Calendar',
    'index': 'Dashboard',
    'new_meeting': 'New Meeting',
    'rooms':'rooms',
    'settings': 'Settings',
    'signin': 'SignIn',
};

module.exports = Object.keys(titles).map(title => {
    return new HtmlWebpackPlugin({
        template: path.join(manifest.paths.src, `${title}.html`),
        path: manifest.paths.build,
        filename: `${title}.html`,
        inject: true,
        minify: {
            collapseWhitespace: true,
            minifyCSS: true,
            minifyJS: true,
            removeComments: true,
            useShortDoctype: true,
        },
    });
});
