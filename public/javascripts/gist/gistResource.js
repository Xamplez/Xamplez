app.factory('Gist', ['$resource', function($resource) {
  return $resource('https://api.github.com/gists/:id', {}, {
    'get' : {
      method : 'GET',
      isArray : false
    }
  });
}]);