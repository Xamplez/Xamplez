app.controller('HomeCtrl', ['$scope', '$location', '$timeout', 'Tags', 'Search', 'GistService', function($scope, $location, $timeout, Tags, Search, GistService) {
  

  $scope.sentences = [
    {
      question: "What kind of developer are you right now?",
      createTitle: "I'm an artist!",
      createDescription: "What are you waiting for? Click on the following button, read the Gist if it's your first time, and start crafting your next masterpiece!",
      searchTitle: "I'm an adventurer!",
      searchDescription: "That's the spirit! Just start typing on the following input and let the journey begin inside a world of awesome code snippets."
    },
    {
      question: "What is your I/O style today?",
      createTitle: "Input rock!",
      createDescription: "What are you waiting for? Click on the following button, read the Gist if it's your first time, and start crafting your next masterpiece!",
      searchTitle: "Output all the way!",
      searchDescription: "That's the spirit! Just start typing on the following input and let the journey begin inside a world of awesome code snippets."
    },
    {
      question: "What is your feeling?",
      createTitle: "I'm feeling loudy!",
      createDescription: "What are you waiting for? Click on the following button, read the Gist if it's your first time, and start crafting your next masterpiece!",
      searchTitle: "I'm feeling lucky!",
      searchDescription: "That's the spirit! Just start typing on the following input and let the journey begin inside a world of awesome code snippets."
    },
    {
      question: "If I say 'Play JSON formater', you first tought is about...",
      createTitle: "... writer",
      createDescription: "What are you waiting for? Click on the following button, read the Gist if it's your first time, and start crafting your next masterpiece!",
      searchTitle: "... reader",
      searchDescription: "That's the spirit! Just start typing on the following input and let the journey begin inside a world of awesome code snippets."
    }
  ];


  $scope.sentence = $scope.sentences[_.random(0, $scope.sentences.length - 1)];

  // function indexOfSentence () {
  //   return _.indexOf($scope.sentences, $scope.sentence);
  // };

  // function nextSentence () {
  //   var index = indexOfSentence();
  //   $scope.sentence = $scope.sentences[index < 0 || index + 1 >= $scope.sentences.length ? 0 : index +1];
  // };

  // function rollSentence () {
  //   $timeout(function () {
  //     nextSentence();
  //     rollSentence();
  //   }, 5000);
  // };

  // rollSentence();

  $scope.data = {
    currentPopular: -1,
    populars: [],
    popularOptions: {
      selector: "#popularGists",
      scriptSelector: "#popularGists",
      classes: ""
    }
  };

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

}]);
