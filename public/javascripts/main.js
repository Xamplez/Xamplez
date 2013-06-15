app.controller('IndexCtrl', ['$scope', '$location', 'Tags', 'Search', 'GistService', function($scope, $location, Tags, Search, GistService) {
  $scope.data = {
    query: "",
    currentPopular: -1,
    populars: [],
    popularOptions: {
      selector: "#popularGists",
      scriptSelector: "#popularGists",
      classes: ""
    }
  }

  $scope.$on("$viewContentLoaded", function() {
    Search.query({q: "json"}, function(result) {
      $scope.data.populars = _.map(result.hits.hits, function(hit){ return hit._source; });
      $scope.nextPopular();
      $scope.startPopulars();
    });
  });

  $scope.$on('$destroy', function() {
    $scope.stopPopulars();
  });

  $scope.nextPopular = function() {
    var $popularGists = angular.element("#popularGists");
    var nextPopularIndex = ($scope.data.currentPopular + 1) % $scope.data.populars.length;
    var nextPopular = $scope.data.populars[nextPopularIndex];
    var $firstPopular = $popularGists.children().eq(0);

    if ($firstPopular.length && $firstPopular.animate) {
      $firstPopular.animate({opacity: 0}, {
        duration: 500,
        done: function() {
          GistService.remove($scope.data.populars[$scope.data.currentPopular]);
          GistService.display(nextPopular, $scope.data.popularOptions);
          $scope.data.currentPopular = nextPopularIndex;
        }
      });
    }
    else if (!$firstPopular.length) {
      GistService.display(nextPopular, $scope.data.popularOptions);
      $scope.data.currentPopular = nextPopularIndex;
    }
  }

  $scope.startPopulars = function() {
    $scope.popularsInterval = setInterval($scope.nextPopular, 5000);
  }

  $scope.stopPopulars = function() {
    clearInterval($scope.popularsInterval);
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
    var min = _tags.last(1).value();
    var max = _tags.first(1).value();
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

app.factory('Tags', ['$resource', 'config', function($resource, config) {
  return $resource(config.api + "/tags", {}, {

  });
}]);

