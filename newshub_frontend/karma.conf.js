module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('karma-sonarqube-reporter'), // 👈 Наш новий плагін
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    
    client: {
      jasmine: {},
      clearContext: false 
    },
    
    jasmineHtmlReporter: {
      suppressAll: true 
    },
    
    // 1. Покриття коду відправляємо в окрему ізольовану папку
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage-sonar/lcov-report'),
      subdir: '.',
      reporters: [
        { type: 'html' },
        { type: 'lcovonly' }, // генерує lcov.info
        { type: 'text-summary' }
      ]
    },
    
    // Активуємо обидва репортери
    reporters: ['progress', 'kjhtml', 'sonarqube'],

    // 2. Звіт про кількість тестів відправляємо в ІНШУ ізольовану папку
    sonarqubeReporter: {
      basePath: 'src',
      outputFolder: require('path').join(__dirname, './coverage-sonar/tests-report'), // 👈 окрема підпапка
      filePattern: '**/*.spec.ts',
      encoding: 'utf-8',
      outputFile: 'sonar-spec-reporter.xml',
      legacyMode: false
    },
    
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: false,
    browsers: ['ChromeHeadless'],
    singleRun: true, // 👈 КРИТИЧНО для CI: Karma чекає завершення обох плагінів
    restartOnFileChange: false,
    
    // Даємо асинхронним плагінам час записати файли на диск
    browserNoActivityTimeout: 30000,
    browserDisconnectTimeout: 10000,
    pingTimeout: 10000
  });
};