app.factory('ReposGitHub', ['$resource', 'config', function($resource, config) {
  return $resource(config.gitHubApi + "/repos/:owner/:repo/:param1/:param2", {owner: '@owner', repo: '@repo'}, {
  	"contributors": { method: "GET", isArray: true, params: { owner: 'Xamplez', repo: 'xamplez', "param1": "stats", "param2": "contributors" }  }
  });
}]);
