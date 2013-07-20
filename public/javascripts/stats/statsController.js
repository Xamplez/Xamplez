app.controller("StatsCtrl", ["$scope", "BubbleCloud", function ($scope, BubbleCloud) {

	BubbleCloud.display(_.map( $scope.tags, function (tag) {
		return {
			name: tag.term,
			count: tag.count
		}
	}), "#tagCloud");
}]);