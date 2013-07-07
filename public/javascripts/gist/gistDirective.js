app.directive("gist", function () {
	function normalize (gist) {
    normalizeId(gist);
    normalizeUrl(gist);
    normalizeDescription(gist);
    return gist;
  };

  function normalizeId (gist) {
    gist.id = gist.id || gist.url.match(/([a-zA-Z0-9]+)$/)[1];
  };

  function normalizeUrl (gist) {
    gist.url = gist.url || "https://gist.github.com/"+ (gist.author_login && gist.author_login+"/" || "") + gist.id;
  };

  function normalizeDescription (gist) {
    gist.taggedDescription = gist.description.replace(/(#([a-zA-Z0-9_\.]*[a-zA-Z0-9]+))/g , '<a href="/?q=%23$2">$1</a>');
  };

  function getContainerId (gist) {
    return "gist" + gist.id + "Container";
  };

  function handleGist (scope, elem, gist) {
  	scope.gist = normalize(gist);
  	scope.containerId = getContainerId(scope.gist);
		elem.append('<script type="text/javascript" src="'+ scope.gist.url +'.json?callback=displayGist"></script>');
  };

	return {
		restrict: "E",
		replace: true,
		scope: {
			value: "="
		},
		templateUrl: "/templates/gist",
		link: function (scope, elem, attrs) {
			if (scope.value.$then) {
				scope.value.$then(function (request) {
					handleGist (scope, elem, request.data);
				});
			} else {
				handleGist (scope, elem, scope.value._source);
			}
		}
	}
});