app.controller('SearchCtrl', ['$scope', '$routeParams', "Search", function($scope, $routeParams, Search) {
    $scope.data = {
        query: $routeParams.q,
        result: {}
    };

    $scope.data.result = Search.query({q: $scope.data.query}, function() {
        angular.forEach($scope.data.result.hits.hits, function(value) {
            var gist = value._source;
            gist.id = gist.id || gist.url.match(/([0-9]+)$/)[1];
            gist.url = gist.url || "https://gist.github.com/"+ (gist.author_login && gist.author_login+"/" || "") + gist.id;
            gist.taggedDescription = gist.description.replace(/(#([a-zA-Z0-9_\.]*[a-zA-Z0-9]+))/g , '<a href="/search?q=$2">$1</a>');
            // TODO: Add a +/- icon to show/hide a gist inside the span in the .description element
            angular.element("#results ul").append('<li><div class="description">'+ gist.taggedDescription +'<span data-toggle="collapse" data-target="#gist'+ gist.id +'Container"></span></div><div id="gist'+ gist.id +'Container" class="gist collapse in"></div></li>');
            angular.element("#resultScripts").append('<script type="text/javascript" src="'+ gist.url +'.json?callback=displayGist"></script>')
        })
    });
}])

app.factory('Search', ['$resource', 'config', function($resource, config) {
    return $resource(config.api + "/search", {}, {
        query: {method: 'GET', isArray: false}
    });
}]);
