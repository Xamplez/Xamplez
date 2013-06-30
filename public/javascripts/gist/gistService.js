app.factory('GistService', ["$http", "$compile", "Colors", function($http, $compile, Colors) {

  function isStarred (id) {
    $http.jsonp("https://api.github.com/gists/"+id+"/star")
         .success(function(data, status) {
           console.log("status", status);
         });
  }

  return {
    isStarred: isStarred
  }
}]);
