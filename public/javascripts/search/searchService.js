app.factory('Search', ['$resource', 'config', function($resource, config) {
  return $resource(config.api + "/search", {}, {
    query: {method: 'GET', isArray: false}
  });
}]);
