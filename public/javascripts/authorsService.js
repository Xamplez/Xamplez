app.factory('Authors', ['$resource', 'config', function($resource, config) {
  return $resource(config.api + "/authors", {}, {

  });
}]);
