app.controller('SearchCtrl', ['$scope', '$routeParams', "Search", function($scope, $routeParams, Search) {
    $scope.data = {
        query: $routeParams.q,
        result: {}
    }
    $scope.data.result = Search.query({q: $scope.data.query}, function() {
        angular.forEach($scope.data.result.hits.hits, function(value) {
            var gistId = value._source.url.match(/([0-9]+)$/)[1];
            angular.element("#results ul").append('<li id="gist'+ gistId +'"></li>');
            angular.element("#resultScripts").append('<script type="text/javascript" src="'+value._source.url+'.json?callback=displayGist"></script>')
        })
    });
}])

app.factory('Search', ['$resource', 'apiUrl', function($resource, apiUrl) {
    return $resource(apiUrl + "/search", {}, {
        query: {method: 'GET', isArray: false}
    });
}]);
