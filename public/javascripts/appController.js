app.controller('AppCtrl', ['$scope', '$location', 'Search', 'GistService', 'Tags', function($scope, $location, Search, GistService, Tags) {

	$scope.searchData = {
		query: [],
		tags: []
	};
	$scope.searchResults = [];
	$scope.searchOptions = {
		tags: []
	};

	Tags.query({}).$then(function (e) {
		$scope.tags = e.data.facets.tags.terms;
		var tags = _($scope.tags)
			.sortBy(function (tag) { return -tag.count })
			.value();
		$scope.searchData.tags = tags.slice(0, 6);
		$scope.searchOptions.tags = _.map( tags, function (tag) {
			return "#" + tag.term;
		});
	});

	$scope.$watch("searchData.query", function(newValue, oldValue) {
		$scope.search();
	}, true);

	$scope.search = function () {
		var query = $scope.queryToString($scope.searchData.query);

		if (query != null) {
			$location.search("q", query);
			$scope.searchResults = Search.query({q: query});
		}
	};

	$scope.queryFromString = function (queryString) {
		return _.map( queryString.split(" "), function (tag) {
			return {
				id: tag,
				text: tag
			};
		});
	};

	$scope.queryToString = function (query) {
		return _.map( query, function (tag) {
			return tag.id;
		}).join(" ");
	};

}]);
