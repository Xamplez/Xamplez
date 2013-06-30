app.factory('GistService', ["$http", "$compile", "Colors", function($http, $compile, Colors) {

  function buildGistHeader(gist, scope, options) {
    var stars;
    /*var updown =
        '<span class="stars-updown">'+
        '<i class="icon-caret-up stars-up" ng-click="isStarred('+gist.id+')"></i>'+
        '<i class="icon-caret-down stars-down" ng-click="isStarred('+gist.id+')"></i>'+
        '</span>';*/
    if(gist.stars>0)
      stars = gist.stars + '<i class="icon-star icon-4 star"></i>'/* + updown*/;
    else stars = gist.stars + '<i class="icon-star-empty icon-4 star"></i>'/* + updown*/;

    var compiled = /*$compile(*/angular.element(options.prefix +
      '<div id="'+ getContainerId(gist) +'" class="gistContainer" data-ng-controller="GistCtrl">'+
        '<div class="description '+ options.classes +'">'+
          '<div class="row"><div class="col-12">' +
            '<span class="gist-link"><a href="/' + gist.id + '">Gist '+ gist.id +'</a></span>' +
            '<span class="author">by <a href="https://github.com/'+ gist.author_login + '"> '+ gist.author_login +'</a></span>' +
            '<div class="pull-right"><span class="label gist-stars">'+stars+'</span></div>'+
            '<div class="langs pull-right">'+
              _.map(gist.langs, function (lang) { return '<span class="label gist-lang ' + lang.toLowerCase() + '">' + lang + '</span>' }).join() +
            '</div>' +
          '</div></div>' +
          '<hr/>' +
          '<div class="row"><div class="col-12">' +
            gist.taggedDescription +
            '<span data-toggle="collapse" data-target="#'+ getContainerId(gist) + ' .gistWrapper"></span>'+
          '</div></div>' +
        '</div>' +
        '<div class="gistWrapper collapse in"></div>' +
      '</div>'+options.suffix)/*)*/;

    return compiled/*(scope)*/;
  }

  function buildGistBody(gist, scope, options) {
    return angular.element(
      '<div id="'+ getScriptId(gist) +'">'+
        '<script type="text/javascript" src="'+ gist.url +'.json?callback=displayGist"></script>'+
      '</div>'
    );
  }

  function display (gist, scope, userOptions) {
    normalize(gist);
    var options = {};
    angular.extend(options, {prefix: "", suffix: "", classes: "left"}, userOptions);

    var header = buildGistHeader(gist, scope, options);
    var body = buildGistBody(gist, scope, options);

    angular.element(options.selector).append(header);
    angular.element(options.scriptSelector).append(body);
  };

  function remove (gist) {
    normalizeId(gist);
    angular.element("#" + getContainerId(gist)).remove(); // TODO: removing also prefix and suffix if possible
    angular.element("#" + getScriptId(gist)).remove();
  };

  function isStarred (id) {
    $http.jsonp("https://api.github.com/gists/"+id+"/star")
         .success(function(data, status) {
           console.log("status", status);
         });
  }

  function normalize (gist) {
    normalizeId(gist);
    normalizeUrl(gist);
    normalizeDescription(gist);
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

  function getScriptId (gist) {
    return "gist" + gist.id + "Script";
  };

  return {
    display: display,
    remove: remove,
    isStarred: isStarred
  }
}]);
