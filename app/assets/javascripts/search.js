app.controller('SearchCtrl', ['$scope', '$routeParams', "Search", function($scope, $routeParams, Search) {
    $scope.query = $routeParams.q;
    $scope.results = Search.query({q: $scope.query}).$then(function(e) {
        console.log("then");
        console.log(e);
    });
}])

app.factory('Search', ['$resource', 'apiUrl', function($resource, apiUrl) {
    return $resource(apiUrl + "/search", {}, {

    });
}]);
