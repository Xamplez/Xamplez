app.controller('GistCtrl', ['$scope', '$routeParams', "$compile", 'GistService', 'Gists', function($scope, $routeParams, $compile, GistService, Gists) {

	Gists.get({id: $routeParams.id}, function (gist) {
		GistService.display(gist._source, $scope, {
			selector: "#gist",
      scriptSelector: "#gistScript"
		});
	});

  $scope.isStarred = function(id){
    return GistService.isStarred(id);
  }

}]);
