app.factory('Tags', ['$resource', 'config', function($resource, config) {
  return $resource(config.api + "/tags", {}, {

  });
}]);
