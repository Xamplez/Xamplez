app.controller('GistCtrl', ['$scope', function($scope) {

}])

app.factory('gist', ['$resource', function($resource) {
  return $resource('https://api.github.com/gists/:id', {}, {
    'get' : {
      method : 'GET',
      isArray : false
    }
  });
}]);

app.service('GistService', function() {
  this.display = function(gist, userOptions) {
    this.normalize(gist);
    var options = {};
    angular.extend(options, {prefix: "", suffix: "", classes: "left"}, userOptions);
    // TODO: Add a +/- icon to show/hide a gist inside the span in the .description element
    angular.element(options.selector).append(options.prefix + '<div id="'+ this.getContainerId(gist) +'" class="gistContainer"><div class="description '+ options.classes +'">'+ gist.taggedDescription +'<span data-toggle="collapse" data-target="#'+ this.getContainerId(gist) +' .gistWrapper"></span></div><div class="gistWrapper collapse in"></div></div>'+options.suffix);
    angular.element(options.scriptSelector).append('<div id="'+ this.getScriptId(gist) +'"><script type="text/javascript" src="'+ gist.url +'.json?callback=displayGist"></script></div>')
  }

  this.remove = function(gist) {
    this.normalizeId(gist);
    angular.element("#" + this.getContainerId(gist)).remove(); // TODO: removing also prefix and suffix if possible
    angular.element("#" + this.getScriptId(gist)).remove();
  }

  this.normalize = function(gist) {
    this.normalizeId(gist);
    this.normalizeUrl(gist);
    this.normalizeDescription(gist);
  }

  this.normalizeId = function(gist) {
    gist.id = gist.id || gist.url.match(/([0-9]+)$/)[1];
  }

  this.normalizeUrl = function(gist) {
    gist.url = gist.url || "https://gist.github.com/"+ (gist.author_login && gist.author_login+"/" || "") + gist.id;
  }

  this.normalizeDescription = function(gist) {
    gist.taggedDescription = gist.description.replace(/(#([a-zA-Z0-9_\.]*[a-zA-Z0-9]+))/g , '<a href="/search?q=$2">$1</a>');
  }

  this.getContainerId = function(gist) {
    return "gist" + gist.id + "Container";
  }

  this.getScriptId = function(gist) {
    return "gist" + gist.id + "Script";
  }
})