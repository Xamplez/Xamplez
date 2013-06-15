app.controller('AppCtrl', ['$scope', '$location', function($scope, $location) {

	$scope.searchForm = {};

	$scope.search = function (query) {
		$location.path('/search').search({q: query})
	};

}]);