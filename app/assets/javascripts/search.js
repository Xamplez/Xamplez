app.controller('SearchCtrl', ['$scope', '$routeParams', "Search", function($scope, $routeParams, Search) {
    $scope.data = {
        query: $routeParams.q,
        result: {}
    }
    $scope.data.result = Search.query({q: $scope.data.query}, function() {
        angular.forEach($scope.data.result.hits.hits, function(value) {
            var gistId = value._source.id || value._source.url.match(/([0-9]+)$/)[1];
            var gistUrl = value._source.url || "https://gist.github.com/"+gistId;
            var gistDescription = value._source.description;
            angular.element("#results ul").append('<li><div class="description" data-toggle="collapse" data-target="#gist'+ gistId +'">'+ gistDescription +'</div><div id="gist'+ gistId +'" class="gist collapse in"></div></li>');
            angular.element("#resultScripts").append('<script type="text/javascript" src="'+ gistUrl +'.json?callback=displayGist"></script>')
        })
    });
}])

app.factory('Search', ['$resource', 'apiUrl', function($resource, apiUrl) {
    return $resource(apiUrl + "/search", {}, {
        query: {method: 'GET', isArray: false}
    });
}]);
