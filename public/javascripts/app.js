'use strict';

var app = angular.module('app', ['ngResource'])
  .constant("config", {api: "http://localhost:9000\:9000/api", gistApi: "https://gist.github.com", gitHubApi: "https://api.github.com"})
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: '/views/index',
        controller: 'HomeCtrl'
      })
      .when('/search', {
        templateUrl: '/views/search',
        controller: 'SearchCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });
  }])
  .config(['$locationProvider', function($locationProvider) {
    $locationProvider.html5Mode(true).hashPrefix('!');
  }]);


function displayGist(gist) {
    var $content = angular.element(gist.div);
    var gistId = $content.attr("id");

    $content.find(".gist-file").each(function(index) {
        var fileName = angular.element(this).find(".gist-meta a:nth-child(2)").text();
        var extension = fileName.substring(fileName.lastIndexOf(".")+1);
        if (extension == "md" || extension == "txt" || extension == "markdown") {
            angular.element(this).addClass("remove");
        }
    });

    $content.find(".gist-file.remove").remove();
    angular.element("#"+gistId+"Container .gistWrapper").append($content);
}