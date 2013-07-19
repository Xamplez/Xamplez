app.controller("AboutCtrl", ["$scope", "ReposGitHub", function ($scope, ReposGitHub) {

  $scope.contributors = ReposGitHub.contributors();
  
  var fill = d3.scale.category20();

  d3.layout.cloud().size([600, 600])
    .words([
    	{text: "GitHub", size: 100, url: "http://www.github.com"},
    	{text: "Gists", size: 100, url: "http://gist.github.com"},
    	{text: "HTML5", size: 75},
    	{text: "CSS3", size: 75},
    	{text: "JavaScript", size: 75},
    	{text: "Play! Framework", size: 50, url: "http://playframework.org"},
    	{text: "AngularJS", size: 50, url: "http://www.angularjs.org/"},
    	{text: "Elastic Search", size: 40, url: "http://www.elasticsearch.org/"},
    	{text: "Bootstrap", size: 30, url: "http://twitter.github.io/bootstrap/"},
    	{text: "Select2", size: 30, url: "http://ivaynberg.github.io/select2/"},
    	{text: "D3", size: 30, url: "http://d3js.org/"},
    	{text: "Font Awesome", size: 25, url: "http://fortawesome.github.io/Font-Awesome/"},
    	{text: "Lodash", size: 25, url: "http://lodash.com/"},
    	{text: "Restangular", size: 20},
    	{text: "Angular UI", size: 15},
    	{text: "jQuery", size: 15, url: "http://jquery.com/"}
    ])
    .padding(5)
    .rotate(function() { return (~~(Math.random() * 3) - 1) * 45; })
    .font("Impact")
    .fontSize(function(d) { return d.size; })
    .on("end", draw)
    .start();

  function draw(words) {
    d3.select("#techCloud").append("svg")
      .attr("width", 600)
      .attr("height", 600)
      .append("g")
      .attr("transform", "translate(300,300)")
      .selectAll("text")
      .data(words)
      .enter()
      .append("a")
      .attr("xlink:href", function(d) { return d.url;})
      .append("text")
      .style("font-size", function(d) { return d.size + "px"; })
      .style("font-family", "Impact")
      .style("fill", function(d, i) { return fill(i); })
      .attr("text-anchor", "middle")
      .attr("transform", function(d) {
        return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
      })
      .text(function(d) { return d.text; });
  }


}]);