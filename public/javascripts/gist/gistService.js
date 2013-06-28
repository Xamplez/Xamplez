app.factory('GistService', ["Colors", function(Colors) {
  function display (gist, userOptions) {
    normalize(gist);
    var options = {};
    angular.extend(options, {prefix: "", suffix: "", classes: "left"}, userOptions);
    // TODO: Add a +/- icon to show/hide a gist inside the span in the .description element
    // TODO: ADD DIRECT LINK TO GIST IN HEADER
    var stars; 
    if(gist.stars>0)
      stars = '<i class="icon-star icon-4 star"></i>' + gist.stars
    else stars = '<i class="icon-star-empty icon-4 star"></i>' + gist.stars

    angular.element(options.selector).append(options.prefix +
      '<div id="'+ getContainerId(gist) +'" class="gistContainer">'+
        '<div class="description '+ options.classes +'">'+
          '<div class="row"><div class="col-12">' +
            '<span class="label gist-stars">'+stars+'</span>'+
            '<span class="gist-link"><a href="/' + gist.id + '">Gist '+ gist.id +'</a></span>' +
            '<span class="author">by <a href="https://github.com/'+ gist.author_login + '"> '+ gist.author_login +'</a></span>' +
            '<div class="langs pull-right">'+
              _.map(gist.langs, function (lang) { return '<span class="label ' + lang.toLowerCase() + '">' + lang + '</span>' }).join() +
            '</div>' +
          '</div></div>' +
          '<hr/>' +
          '<div class="row"><div class="col-12">' +
            gist.taggedDescription +
            '<span data-toggle="collapse" data-target="#'+ getContainerId(gist) + ' .gistWrapper"></span>'+
          '</div></div>' +
        '</div>' +
        '<div class="gistWrapper collapse in"></div>' +
      '</div>'+options.suffix);
    angular.element(options.scriptSelector).append('<div id="'+ getScriptId(gist) +'"><script type="text/javascript" src="'+ gist.url +'.json?callback=displayGist"></script></div>')
  };

  function remove (gist) {
    normalizeId(gist);
    angular.element("#" + getContainerId(gist)).remove(); // TODO: removing also prefix and suffix if possible
    angular.element("#" + getScriptId(gist)).remove();
  };

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
    remove: remove
  }
}]);
