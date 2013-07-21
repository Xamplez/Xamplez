'use strict';

var app = angular.module('app', ['ngResource', 'ui.select2'])
  .constant("config", {api: "/api", gistApi: "https://gist.github.com", gitHubApi: "https://api.github.com"})
  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: '/views/index',
        controller: 'HomeCtrl'
      })
      .when('/stats', {
        templateUrl: '/views/stats',
        controller: 'StatsCtrl'
      })
      .when('/about', {
        templateUrl: '/views/about',
        controller: 'AboutCtrl'
      })
      .when('/:id', {
        templateUrl: '/views/gist',
        controller: 'GistCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });
  }])
  .config(['$locationProvider', function ($locationProvider) {
    $locationProvider.html5Mode(true).hashPrefix('!');
  }])
  .run(['$rootScope', '$log', '$location', function ($rootScope, $log, $location) {
    $rootScope.$log = $log;
    $rootScope.$location = $location;
  }]);


function displayGist(gist) {
  var $content = angular.element(gist.div);
  var gistId = $content.attr("id");
  // var owner = gist.owner;
  // var files = _(gist.files).filter(function(file){
  //   var extension = file.substring(file.lastIndexOf(".")+1);
  //   if (extension == "md" || extension == "txt" || extension == "markdown") return false;
  //   else return true;
  // }).map(function(file){
  //   return file.substring(file.lastIndexOf(".")+1);
  // }).value();

  $content.find(".gist-file").each(function(index) {
      var fileName = angular.element(this).find(".gist-meta a:nth-child(2)").text();
      var extension = fileName.substring(fileName.lastIndexOf(".")+1);
      if (
        fileName == "_README.md" || fileName == "__LICENSE.txt" /*||
        extension == "md" || extension == "txt" || extension == "markdown"*/) {
          angular.element(this).addClass("remove");
      }
  });

  $content.find(".gist-file.remove").remove();
  angular.element("#"+gistId+"Container .gistWrapper").empty().append($content);
}

function displayGistFull(gist) {
  var $content = angular.element(gist.div);
  var gistId = $content.attr("id");

  $content.find(".gist-file").each(function(index) {
      var fileName = angular.element(this).find(".gist-meta a:nth-child(2)").text();
      if (fileName == "_README.md") {
        angular.element(this).addClass("remove");
      }
  });

  $content.find(".gist-file.remove").remove();

  angular.element("#"+gistId+"Container .gistWrapper").empty().append($content);
}