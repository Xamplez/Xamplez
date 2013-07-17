app.controller('AppCtrl', ['$scope', '$location', 'Search', 'GistService', 'Tags', function($scope, $location, Search, GistService, Tags) {

  $scope.autocomplete = {
    value: "", // FIXME: we probably don't need that at all
    tags: []
  };

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
    $scope.autocomplete.tags = _.flatten(_.map(tags, function (tag) {
      return [ tag.term, "#"+tag.term ];
    }));
	});

  function initAutocomplete (input) {
    input.value = $scope.autocomplete.value;

    var lastValue = null;
    var lastIndexOfMatch = null;

    var currentSubword = null;
    var currentMatches = [];
    var currentIndexOfMatch = 0;
    
    $(input).on("keydown", function(e) {
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

    $(input).on("keyup", function (e) {
      switch (e.which) {
        case 46: // delete
        case 8: // backspace
          return;
      }

      var newValue = input.value.substring(0, input.selectionStart);
      if (newValue === lastValue && currentIndexOfMatch===lastIndexOfMatch) return;
      lastValue = newValue;
      lastIndexOfMatch = currentIndexOfMatch;

      var tags = $scope.autocomplete.tags;
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

            //$scope.autocomplete.value = setValue;
            var selectionStart = newValue.length;
            var selectionEnd = setValue.length;
            $scope.autocomplete.value = input.value = setValue;
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
    initAutocomplete(document.getElementById("newautocomplete"));
    $scope.search();
  };

  /*
	$scope.$watch("searchData.query", function(newValue, oldValue) {
		$scope.search();
	}, true);
  */

	$scope.search = function () {
		var query = $scope.autocomplete.value;//$scope.queryToString($scope.searchData.query);

		if (query) {
			$location.search("q", query);
			$scope.searchResults = Search.query({q: query});
		}
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
