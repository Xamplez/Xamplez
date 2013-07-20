app.directive("gist", function () {
	function normalize (gist) {
    normalizeId(gist);
    normalizeUrl(gist);
    normalizeDescription(gist);
    normalizeLinks(gist);
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

  var urlPattern = /(http|ftp|https):\/\/[\w-]+(\.[\w-]+)+([\w.,@?^=%&amp;:\/~+#-]*[\w@?^=%&amp;\/~+#-])?/;

  function normalizeLinks (gist) {
    gist.taggedDescription = gist.taggedDescription.replace(urlPattern , '<a href="$1">$1</a>');
  };

  function getContainerId (gist) {
    return "gist" + gist.id + "Container";
  };

  function handleGist (scope, elem, gist) {
  	scope.gist = normalize(gist);
  	scope.containerId = getContainerId(scope.gist);
		elem.append('<script type="text/javascript" src="'+ scope.gist.url +'.json?callback=displayGist"></script>');
  };

  function handleGistFull (scope, elem, gist) {
    scope.gist = normalize(gist);
    scope.containerId = getContainerId(scope.gist);
    elem.addClass("gist-full").append('<script type="text/javascript" src="'+ scope.gist.url +'.json?callback=displayGistFull"></script>');
  };

	return {
		restrict: "E",
		replace: true,
		scope: {
			value: "="
		},
		templateUrl: "/templates/gist",
		link: function (scope, elem, attrs) {
      elem.find('.description').bind('click', function() {
        var arrow = elem.parent().find('.first > i');

        if(arrow.hasClass('icon-double-angle-up')) arrow.removeClass('icon-double-angle-up').addClass('icon-double-angle-down');
        else if(arrow.hasClass('icon-double-angle-down')) arrow.removeClass('icon-double-angle-down').addClass('icon-double-angle-up');

        elem.find('.gistWrapper').toggle();

      });

			if (scope.value.$then) {
				scope.value.$then(function (request) {
					handleGistFull (scope, elem, request.data);
				});
			} else {
				handleGist (scope, elem, scope.value._source);
			}
		}
	}
});
