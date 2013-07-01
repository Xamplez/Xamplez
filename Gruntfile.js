"use strict";

var path = require("path");

var renamedTasks = {

};

module.exports = function (grunt) {
  var bower = require("bower");
  var _ = grunt.util._;
  var isMatch = grunt.file.isMatch;

  require("matchdep").filterDev("grunt-*").forEach(function (plugin) {
    grunt.loadNpmTasks(plugin);
    if (renamedTasks[plugin]) {
      grunt.renameTask(renamedTasks[plugin].original, renamedTasks[plugin].renamed);
    }
  });

  var configuration = {
    pkg : grunt.file.readJSON("package.json"),
    version: "0.0.1",
    dir: {
      app: {
        root: "app",
        controllers: "controllers",
        models: "models",
        views: "views"
      },
      conf: {
        root: "conf"
      },
      public: {
        root: "public",
        styles: "<%= config.dir.public.root %>/stylesheets",
        scripts: "<%= config.dir.public.root %>/javascripts",
        fonts: "<%= config.dir.public.styles %>/fonts",
        images: "<%= config.dir.public.root %>/images"
      },
      test: {
        root: "test"
      },
      components: {
        root: "components"
      }
    }
  };

  grunt.registerTask("bower", [
    "shell:bowerInstall",
    "parallel:bowerBuild",
    "clean:components"
  ]);

  grunt.registerTask("build", [
    "clean:public",
    "parallel:bowerCopy",
    "less:raw"
  ]);

  grunt.registerTask("default", [
    "build",
    "watch"
  ]);

  grunt.registerTask("deploy", [
    "build",
    "less:dist"
  ]);

  grunt.registerTask("test", [
    "build",
    "karma:test"
  ]);

  grunt.initConfig({
    config: configuration,

    clean: {
      public: [
        "<%= config.dir.public.scripts %>/vendors/**/*",
        "<%= config.dir.public.styles %>/vendors/**/*"
      ],
      components: [
        "<%= config.dir.components.root %>/angular",
        "<%= config.dir.components.root %>/angular-resource"
      ]
    },

    concat: {

    },

    less: {
      options: {
        paths: ["components", "public/stylesheets/less"]
      },
      raw: {
        files: {
          "<%= config.dir.public.styles %>/main.css": "<%= config.dir.public.styles %>/less/main.less"
        }
      },
      dist: {
        options: {
          compress: true
        },
        files: [{
          "<%= config.dir.public.styles %>/main.min.css": "<%= config.dir.public.styles %>/less/main.less"
        }]
      }
    },

/*    uglify: {
      options: {
        banner: ""
      },
      public: {
        options: {
          compress: true
        },
        files: [{
          "<%= config.dir.public.scripts %>/app.min.js": ["<%= config.dir.public.scripts %>/app.js"]
        }]
      }
    },
      */
    copy: {
      // Here start the Bower hell: manual copying of all required resources installed with Bower
      // Prefix them all with "bower"
      // Be sure to add them to the "bowerCopy" task near the top of the file
      bowerFontAwesome: {
        files: [{
          expand: true,
          cwd: "<%= config.dir.components.root %>/font-awesome/font/",
          src: ["*"],
          dest: "<%= config.dir.public.fonts %>/fontawesome/"
        }]
      },
      bowerJQuery: {
        files: [{
          expand: true,
          cwd: "<%= config.dir.components.root %>/jquery/",
          src: ["jquery.js", "jquery.min.js"],
          dest: "<%= config.dir.public.scripts %>/vendors/jquery/"
        }]
      },
      bowerModernizr: {
        files: [{
          expand: true,
          cwd: "<%= config.dir.components.root %>/modernizr/",
          src: ["modernizr.js"],
          dest: "<%= config.dir.public.scripts %>/vendors/modernizr/"
        }]
      },
      bowerLodash: {
        files: [{
          expand: true,
          cwd: "<%= config.dir.components.root %>/lodash/dist/",
          src: ["lodash.js", "lodash.min.js"],
          dest: "<%= config.dir.public.scripts %>/vendors/lodash/"
        }]
      },
      bowerD3: {
        files: [{
          expand: true,
          cwd: "<%= config.dir.components.root %>/d3/",
          src: ["d3.js", "d3.min.js"],
          dest: "<%= config.dir.public.scripts %>/vendors/d3/"
        }]
      },
      bowerD3LayoutCloud: {
        files: [{
          expand: true,
          cwd: "<%= config.dir.components.root %>/d3-layout-cloud/",
          src: ["d3.layout.cloud.js"],
          dest: "<%= config.dir.public.scripts %>/vendors/d3/layout-cloud/"
        }]
      },
      bowerSelect2: {
        files: [{
          expand: true,
          cwd: "<%= config.dir.components.root %>/select2/",
          src: ["*.js"],
          dest: "<%= config.dir.public.scripts %>/vendors/select2/"
        },{
          expand: true,
          cwd: "<%= config.dir.components.root %>/select2/",
          src: ["*.gif", "*.png"],
          dest: "<%= config.dir.public.images %>/vendors/select2/"
        },{
          src: "<%= config.dir.components.root %>/select2/select2.css",
          dest: "<%= config.dir.public.styles %>/less/vendors/select2/select2.less"
        }]
      },
      bowerAngular: {
        files: [{
          expand: true,
          cwd: "<%= config.dir.components.root %>/angular-latest/build/",
          src: ["*.js"],
          dest: "<%= config.dir.public.scripts %>/vendors/angular/"
        }]
      },
      bowerRestangular: {
        files: [{
          expand: true,
          cwd: "<%= config.dir.components.root %>/restangular/dist/",
          src: ["restangular.js", "restangular.min.js"],
          dest: "<%= config.dir.public.scripts %>/vendors/restangular/"
        }]
      },
      bowerAngularHttpAuth: {
        files: [{
          expand: true,
          cwd: "<%= config.dir.components.root %>/angular-http-auth/src/",
          src: ["*.js"],
          dest: "<%= config.dir.public.scripts %>/vendors/angular-http-auth/"
        }]
      },
      bowerAngularUiUtils: {
        files: [{
          expand: true,
          cwd: "<%= config.dir.components.root %>/angular-ui-utils/out/build/",
          src: ["*.js"],
          dest: "<%= config.dir.public.scripts %>/vendors/angular-ui/utils/"
        }]
      },
      bowerAngularUiBootstrap: {
        files: [{
          expand: true,
          cwd: "<%= config.dir.components.root %>/angular-bootstrap/",
          src: ["*.js"],
          dest: "<%= config.dir.public.scripts %>/vendors/angular-ui/bootstrap/"
        }]
      },
      bowerAngularUiSelect2: {
        files: [{
          expand: true,
          cwd: "<%= config.dir.components.root %>/angular-ui-select2/src",
          src: ["*.js"],
          dest: "<%= config.dir.public.scripts %>/vendors/angular-ui/select2/"
        }]
      }
    },

    karma: {
      options: {
        configFile: 'karma.conf.js'
      },
      test: {
        singleRun: true,
        autoWatch: false
      },
      auto: {
        autoWatch: true
      }
    },

    shell: {
      bowerInstall: {
        command: "bower install",
        options: {
          stdout: true
        }
      },
      angularNpm: {
        command: "(cd ./components/angular-latest && exec npm install)",
        options: {
          stdout: true
        }
      },
      angularPackage: {
        command: "(cd ./components/angular-latest && exec grunt clean buildall minall)",
        options: {
          stdout: true
        }
      },
      angularUiUtilsNpm: {
        command: "(cd ./components/angular-ui-utils && exec npm install)",
        options: {
          stdout: true
        }
      },
      angularUiUtilsBuild: {
        command: "(cd ./components/angular-ui-utils && exec grunt build)",
        options: {
          stdout: true
        }
      }
    },

    parallel: {
      options: {
        grunt: true
      },
      bowerBuild: {
        tasks: [
          ["shell:angularNpm", "shell:angularPackage"],
          ["shell:angularUiUtilsNpm", "shell:angularUiUtilsBuild"]
        ]
      },
      bowerCopy: {
        tasks: [
          "copy:bowerFontAwesome",
          "copy:bowerJQuery",
          "copy:bowerModernizr",
          "copy:bowerLodash",
          "copy:bowerD3",
          "copy:bowerD3LayoutCloud",
          "copy:bowerSelect2",
          "copy:bowerAngular",
          "copy:bowerRestangular",
          "copy:bowerAngularHttpAuth",
          "copy:bowerAngularUiUtils",
          "copy:bowerAngularUiBootstrap",
          "copy:bowerAngularUiSelect2"
        ]
      }
    },

    watch: {
      options: {
        livereload: false,
        forever: true
      },
      less: {
        files: ["<%= config.dir.public.styles %>/less/*.less", "<%= config.dir.public.styles %>/less/**/*.less"],
        tasks: ["less:raw"]
      },
      public: {
        options: {
          livereload: true
        },
        files: [
          "<%= config.dir.app.root %>/**/*.scala",
          "<%= config.dir.app.root %>/**/*.html",
          "<%= config.dir.conf.root %>/*",
          "<%= config.dir.public.scripts %>/*.js",
          "<%= config.dir.public.scripts %>/**/*.js",
          "<%= config.dir.public.styles %>/*.css",
          "<%= config.dir.public.styles %>/**/*.css"
        ],
        tasks: []
      }
    }
  });

};