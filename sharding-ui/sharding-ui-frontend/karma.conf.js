const webpackConfig = require('./build/webpack.unit.config')

module.exports = function(config) {
  config.set({
    frameworks: ['mocha'],

    files: ['test/**/*.spec.js'],

    preprocessors: {
      '**/*.spec.js': ['webpack', 'sourcemap']
    },

    webpack: webpackConfig,

    reporters: ['spec', 'coverage'],

    coverageReporter: {
      dir: './coverage',
      reporters: [{ type: 'lcov', subdir: '.' }, { type: 'text-summary' }]
    },

    browsers: ['Chrome']
  })
}
