app.controller('MainCtrl', ['$scope', '$location', function($scope, $location) {
	$scope.data = {
		query: ""
	}

	$scope.search = function() {
		$location.path('/search').search({q: $scope.data.query})
	}
}])