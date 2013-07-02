app.controller("AboutCtrl", ["$scope", "ReposGitHub", function ($scope, ReposGitHub) {

  $scope.contributors = ReposGitHub.contributors();
  
  var fill = d3.scale.category20();

  d3.layout.cloud().size([600, 600])
    .words([
    	{text: "GitHub", size: 100},
    	{text: "Gists", size: 100},
    	{text: "HTML5", size: 75},
    	{text: "CSS3", size: 75},
    	{text: "JavaScript", size: 75},
    	{text: "Play! Framework", size: 50},
    	{text: "AngularJS", size: 50},
    	{text: "Elastic Search", size: 40},
    	{text: "Bootstrap", size: 30},
    	{text: "Select2", size: 30},
    	{text: "D3", size: 30},
    	{text: "Font Awesome", size: 25},
    	{text: "Lodash", size: 25},
    	{text: "Restangular", size: 20},
    	{text: "Angular UI", size: 15},
    	{text: "jQuery", size: 15}
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
      .enter().append("text")
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