app.factory('Gists', ['$resource', 'config', function($resource, config) {
  return $resource(config.api + "/gists/:id", {id: '@id'}, {
  	
  });
}]);
