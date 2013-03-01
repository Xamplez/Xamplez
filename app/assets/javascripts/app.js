'use strict';

var app = angular.module('app', ['ngResource', 'ui', 'ui.bootstrap'])
    .constant("apiUrl", "http://localhost:9000\:9000/api")
    .config(['$routeProvider', function($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: '/views/index'
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
    var gistId = gist.div.match(/^<div id="(gist[0-9]+)"/);
    var $content = angular.element(gist.div);

    $content.find(".gist-file").each(function(index) {
        var fileName = angular.element(this).find(".gist-meta a:nth-child(2)").text();
        var extension = fileName.substring(fileName.lastIndexOf(".")+1);
        if (extension == "md" || extension == "txt" || extension == "markdown") {
            angular.element(this).addClass("remove");
        }
    });

    $content.find(".gist-file.remove").remove();

    angular.element("#"+gistId[1])
        .append($content)
        .append('<link rel="stylesheet" media="screen" href="'+gist.stylesheet+'">')
}