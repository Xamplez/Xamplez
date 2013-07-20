app.controller('GistCtrl', ['$scope', '$routeParams', "$compile", 'GistService', 'Gists', function($scope, $routeParams, $compile, GistService, Gists) {

  $scope.gist = Gists.get({id: $routeParams.id});
  $scope.comments = Gists.comments({id: $routeParams.id});

  $scope.isStarred = function(id){
    return GistService.isStarred(id);
  };

}]);
