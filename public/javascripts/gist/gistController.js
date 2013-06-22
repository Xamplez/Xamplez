app.controller('GistCtrl', ['$scope', '$routeParams', 'GistService', 'Gists', function($scope, $routeParams, GistService, Gists) {

	Gists.get({id: $routeParams.id}, function (gist) {
		GistService.display(gist._source, {
			selector: "#gist",
      scriptSelector: "#gistScript"
		});
	});

}]);
