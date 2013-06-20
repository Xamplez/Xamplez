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

		if (query) {
			$location.search("q", query);

			$scope.searchResults = Search.query({q: query}, function() {
				console.log($scope.searchResults);
		    angular.forEach($scope.searchResults.hits.hits, function(value) {
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
		}
	};

	$scope.queryFromString = function (queryString) {
		console.log(queryString);
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