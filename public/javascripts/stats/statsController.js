app.controller("StatsCtrl", ["$scope", "BubbleCloud", function ($scope, BubbleCloud) {

	$scope.showTags = function() {
		angular.element("#bubbleCloud").empty();
		BubbleCloud.display(_.map( $scope.tags, function (tag) {
			return {
				name: tag.term,
				count: tag.count
			}
		}), "#bubbleCloud");
	}

	$scope.showAuthors = function() {
		angular.element("#bubbleCloud").empty();
		BubbleCloud.display(_.map( $scope.authors, function (author) {
			return {
				name: author.term,
				count: author.count
			}
		}), "#bubbleCloud");
	}

	$scope.showTags();
}]);