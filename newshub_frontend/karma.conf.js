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
    
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage-sonar/lcov-report'),
      subdir: '.',
      reporters: [
        { type: 'html' },
        { type: 'lcovonly' }, 
        { type: 'text-summary' }
      ]
    },
    
    reporters: ['progress', 'kjhtml', 'sonarqube'],

    sonarqubeReporter: {
      basePath: 'src',
      outputFolder: require('path').join(__dirname, './coverage-sonar/tests-report'),
      filePattern: '**/*.spec.ts',
      encoding: 'utf-8',
      
      outputFile: 'sonar-spec-reporter.xml',
      useBrowserName: false, 
      
      sonarQubeVersion: 'LATEST',
      legacyMode: false,
      testReportName: 'testExecutions' 
    },
    
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: false,
    browsers: ['ChromeHeadless'],
    singleRun: true, 
    restartOnFileChange: false,
    
    browserNoActivityTimeout: 30000,
    browserDisconnectTimeout: 10000,
    pingTimeout: 10000
  });
};