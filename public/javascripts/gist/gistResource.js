app.factory('Gists', ['$resource', 'config', function($resource, config) {
  return $resource(config.api + "/gists/:id/:param1", {id: '@id'}, {
  	comments: {
  		method: "GET",
  		isArray: false,
  		params: {param1: "comments"},
  		transformResponse: function (data, headersGetter) {
				return {html: data};
	  	}
  	}
  });
}]);
