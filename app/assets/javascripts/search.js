app.controller('SearchCtrl', ['$scope', '$routeParams', "Search", "GistService", function($scope, $routeParams, Search, GistService) {
  $scope.data = {
    query: $routeParams.q,
    result: {}
  };

  $scope.data.result = Search.query({q: $scope.data.query}, function() {
    angular.forEach($scope.data.result.hits.hits, function(value) {
      var options = {
        selector: "#results ul",
        scriptSelector: "#resultScripts",
        classes: "left",
        prefix: "<li>",
        suffix: "</li>"
      };

      GistService.display(value._source, options);
    })
  });
}])

app.factory('Search', ['$resource', 'config', function($resource, config) {
  return $resource(config.api + "/search", {}, {
    query: {method: 'GET', isArray: false}
  });
}]);
