app.controller('AppCtrl', ['$scope', '$location', 'Search', 'GistService', 'Tags', function($scope, $location, Search, GistService, Tags) {

  $scope.autocompleteTags = [];

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
    $scope.autocompleteTags = _.flatten(_.map(tags, function (tag) {
      return [ tag.term, "#"+tag.term ];
    }));
	});

  function initAutocomplete (input, value) {
    $scope.autocomplete = input;
    input.value = value;

    var lastValue = null;
    var lastIndexOfMatch = null;

    var currentSubword = null;
    var currentMatches = [];
    var currentIndexOfMatch = 0;
    
    $(input).on("keydown", function(e) {
      if (e.which === 0) return; // Android have a bug and we can't make it work due to this...
      switch (e.which) {
        case 46: // delete
        case 8: // backspace
          return;
        case 40: // bottom arrow
        e.preventDefault();
        currentIndexOfMatch = (currentIndexOfMatch+1 < currentMatches.length) ? currentIndexOfMatch+1 : 0;
        break;
        case 38: // top arrow
        e.preventDefault();
        currentIndexOfMatch = (currentIndexOfMatch > 0) ? currentIndexOfMatch-1 : currentMatches.length-1;
        break;
      }
    });

    i=0;

    $(input).on("keyup", function (e) {
      if (e.which === 0) return; // Android have a bug and we can't make it work due to this...
      switch (e.which) {
        case 46: // delete
        case 8: // backspace
          return;
      }

      var newValue = input.value.substring(0, input.selectionStart);
      if (newValue === lastValue && currentIndexOfMatch===lastIndexOfMatch) return;
      lastValue = newValue;
      lastIndexOfMatch = currentIndexOfMatch;

      var tags = $scope.autocompleteTags;
      var words = _.filter(newValue.split(/[ ]+/), function (w) { return w.length>0; });
      if (words.length > 0) {
        var lastWord = words[words.length-1];
        var lastSpace = newValue.lastIndexOf(" ");
        if (lastSpace+1!==newValue.length && !_.contains(lastWord, tags)) {
          if (currentSubword !== lastWord) {
            currentSubword = lastWord;
            currentMatches = _.filter(tags, function (tag) {
              return tag.indexOf(lastWord)===0;
            });
            currentIndexOfMatch = 0;
          }
          if (currentMatches.length > 0) {
            var setValue = newValue.substring(0, lastSpace+1) + currentMatches[currentIndexOfMatch];

            var selectionStart = newValue.length;
            var selectionEnd = setValue.length;
            input.value = setValue;
            input.selectionStart = selectionStart;
            input.selectionEnd = selectionEnd;
            setTimeout(function(){
              input.selectionStart = selectionStart;
              input.selectionEnd = selectionEnd;
            }, 0);
          }
        }
      }
    });
  }

  $scope.initAutocomplete = function (){
    // FIXME Here this is hacky to getElementById. Anyway with Angular I can have the node in parameter given from the ng-init function view?
    initAutocomplete(document.getElementById("autocomplete"), $location.search().q||"");
    if ("q" in $location.search())
      $scope.search();
    else {
      $scope.searchResults = [];
    }
  };

  /*
	$scope.$watch("searchData.query", function(newValue, oldValue) {
		$scope.search();
	}, true);
  */

	$scope.search = function () {
    var query = $scope.autocomplete.value;
    $location.search("q", query);
    $scope.searchResults = Search.query({q: query});
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
