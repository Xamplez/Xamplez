app.factory('GistsGitHub', ['$resource', 'config', function($resource, config) {
  return $resource(config.gitHubApi + "/gists/:id/:param1", {id: '@id'}, {
  	"comments": { method: "GET", isArray: true, params: { "param1": "comments" } }
  });
}]);

// Big fatty hack directly parsing HTML
app.factory('GistsGitHubHtml', ['$resource', 'config', function($resource, config) {
  return $resource(config.gistApi + "/:id", {id: '@id'}, {
  	"comments": {
			method: "GET",
			transformResponse: function (data, headersGetter) {
				console.log(headersGetter);
				console.log(data);
	  	} 
	  }
  });
}]);