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

  if ($location.search().q) {
    $scope.searchData.query = $scope.queryFromString($location.search().q);
  }


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
