var fs = require('fs');
var inlineJSON = require('./utils.js').inlineJSON;
var shell = require('gulp-shell');

module.exports = function(gulp, config) {

  var paths = config.paths;

  require('./build.js')(gulp, config);

  /**
   * dev task
   * Watch js and html files and launch jetty, without automatic reloading
   */
  gulp.task('dev', ['widgets', 'generator', 'dev:html', 'dev:watch'], shell.task('mvn jetty:run -Djetty.reload=manual'));

  /**
   * Watch task.
   */
  gulp.task('dev:watch', function () {
    gulp.watch(paths.css, ['generator:css']);
    gulp.watch(paths.generator, ['generator:js']);
    gulp.watch(paths.widgets, ['dev:widgets']);
    gulp.watch(paths.templates, ['dev:html']);
  });

  /**
   * html task, just updating the templates used by jetty
   */
  gulp.task('dev:html', function () {
    return gulp.src(paths.templates).pipe(gulp.dest('target/classes/templates/'));
  });

  /**
   * Task to inline json and build a widgets repository for development.
   */
  gulp.task('dev:widgets', function () {
    // only copy widgets if the repository exist to let
    // the application create and build them the first time.
    if (fs.existsSync('target/widgets-repository')) {
      gulp.src(paths.widgets)
        .pipe(inlineJSON())
        .pipe(gulp.dest('target/widgets-repository'));
    }
  });

};
