app.controller('MainCtrl', ['$scope', '$location', 'Tags', function($scope, $location, Tags) {
	$scope.data = {
		query: ""
	}

	$scope.search = function() {
		$location.path('/search').search({q: $scope.data.query})
	}

  function clamp (edge0, edge1, x) {
    return (x - edge0)/(edge1 - edge0);
  }

  var MAX_TAGS = 20;

  $scope.tags = Tags.query({}).$then(function (e) {
    var _tags = _.chain(e.data.facets.tags.terms).sortBy(function (tag) { return -tag.count }).take(MAX_TAGS);
    var min = _tags.last().value();
    var max = _tags.first().value();
    return _tags
      .sortBy(function (tag) { return tag.term })
      .map(function (tag, i) {
        var weight = min==max ? 0.5 : clamp(min.count, max.count, tag.count);
        return {
          term: tag.term,
          count: tag.count,
          weight: Math.floor(9*weight)
        };
      })
      .value();
  });

}])

app.factory('Tags', ['$resource', 'apiUrl', function($resource, apiUrl) {
    return $resource(apiUrl + "/tags", {}, {

    });
}]);

