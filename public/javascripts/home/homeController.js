app.controller('HomeCtrl', ['$scope', '$location', '$timeout', '$window', 'Tags', 'Search', 'GistService', function($scope, $location, $timeout, $window, Tags, Search, GistService) {
  
  /*
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
  */

  var $w = angular.element($window);

  if (!("q" in $location.search())) {
    var scrollIndicator = $(".scrollIndicator");
    function doEmptySearch () {
      $scope.$apply(function(){
        var $w = angular.element($window);
        $w.off("scroll wheel mousewheel", firstScrollDown);
        scrollIndicator.off("click", doEmptySearch);
        var input = document.getElementById("autocomplete");
        $scope.searchResults = Search.query({q: input ? input.value : ""/*, size: 10 <- only when we have a pager? */});
      });
    }
    function firstScrollDown (e) {
      var $w = angular.element($window);
      if ($w.scrollTop() >= $(document).height()-$w.height()) {
        doEmptySearch();
      }
    }
    $w.on("scroll wheel mousewheel", firstScrollDown);
    scrollIndicator.on("click", function (e) {
      e.preventDefault();
      doEmptySearch();
    });
  }

  var searchBar = $(".search-bar:first");
  var searchBarContainer = $(".search-bar-container:first");
  var searchBarTopPosition;
  function computeSearchBarTopPosition () {
    searchBarTopPosition = searchBarContainer.offset().top;
  }
  function syncSearchBarFixed () {
    var $w = angular.element($window);
    var top = $w.scrollTop();
    searchBar.toggleClass("fixed", top > searchBarTopPosition);
  }
  $w.on("scroll", function (e) {
    syncSearchBarFixed();
  });
  $w.on("resize", function (e) {
    computeSearchBarTopPosition();
  });

  // Also wait for font load end
  $w.on("load", function () {
    computeSearchBarTopPosition();
    syncSearchBarFixed();
  });

  // Trigger once on ready
  $(function(){
    computeSearchBarTopPosition();
    syncSearchBarFixed();
  });

  // EXPERIMENTS:

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

}]);
