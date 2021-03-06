<!DOCTYPE html>

<%
String domain=request.getParameter("domain");
String facetLimit=request.getParameter("facetLimit");
String ingoingStr=request.getParameter("ingoing");
String dateStartStr=request.getParameter("dateStart");
String dateEndStr=request.getParameter("dateEnd");

String checkedStr="";
boolean ingoing=false;

//Just default if none is set
long selectedDateMin= 1104537600000l;
long selectedDateMax=System.currentTimeMillis();

if (dateStartStr != null){
  selectedDateMin = Long.parseLong(dateStartStr); 
}
if (dateEndStr != null){
  selectedDateMax = Long.parseLong(dateEndStr); 
}

if (facetLimit == null){
  facetLimit="10";
}
if (ingoingStr  != null){
  ingoing="true".equalsIgnoreCase(ingoingStr);
}

String directionStr;
if (ingoing){
 checkedStr="checked";
}
%>

<html>
<meta charset="utf-8">
<link rel="stylesheet" href="css/iThing-min.css" type="text/css" />
<script src="js/d3.v2.min.js"></script>
<script src="js/jquery-3.2.1.min.js"></script>
<script src="js/jquery-ui.min.js"></script>        
<script src="js/jQDateRangeSlider-min.js"></script>

<style>

.link {
  stroke: #aaa;
}


.box {
margin-left: 40px;
margin-right: 40px;
}

.node text {
stroke:#333;
cursor:pointer;
}

.node circle{
stroke:#fff;
stroke-width:3px;
fill:#555;
}

 /* The switch - the box around the slider */
.switch {
  position: relative;
  display: inline-block;
  width: 60px;
  height: 34px;
}

/* Hide default HTML checkbox */
.switch input {display:none;}

/* The slider */
.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #2196F3;
  -webkit-transition: .4s;
  transition: .4s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 26px;
  width: 26px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  -webkit-transition: .4s;
  transition: .4s;
}

input:checked + .slider {
  background-color: #2196F3;
}

input:focus + .slider {
  box-shadow: 0 0 1px #2196F3;
}

input:checked + .slider:before {
  -webkit-transform: translateX(26px);
  -ms-transform: translateX(26px);
  transform: translateX(26px);
}

/* Rounded sliders */
.slider.round {
  border-radius: 34px;
}

.slider.round:before {
  border-radius: 50%;
}

</style>
<head>
    <title>Link graph for <%=domain%></title>          
</head>
<body>

 <div align="center" style="font-size:32px">
 Domain:<span style="color:#2196F3"><%=domain%></span><br>
<label for="fader">Links</label>
<input type="range" min="1" max="25" value="<%=facetLimit %>" id="fader" step="1" oninput="outputUpdate(value)" onchange="reload();">
<span style="color:#2196F3">
<output for="fader" id="volume"><%=facetLimit%></output> &nbsp;&nbsp;&nbsp;&nbsp;

 <span style="color:<%if(!ingoing){out.println("#2196F3");} else{ out.println("#ccc");}%>">Outgoing</span> 
   <!-- Rectangular switch -->
<label class="switch">
  <input type="checkbox" id="ingoingCheckbox" onchange="reload();" <%=checkedStr%> %>>
  <div class="slider"  <%=checkedStr%>></div>
</label> <span style="color:<%if(ingoing){out.println("#2196F3");} else{ out.println("#ccc");}%>">Ingoing</span>
</span>
</div>
 <div id="rangeslider" class="box"></div>
      

<script>



$("#rangeslider").dateRangeSlider({          
    defaultValues: {min: new Date(<%=selectedDateMin%>), max: new Date(<%=selectedDateMax%>)},
    bounds:{
    	min: new Date(1995, 0, 1),
      max: new Date()
   }});


  
$("#rangeslider").bind("userValuesChanged", function(e, data){	
	  var serviceParameters = getServiceParameters();     
	  var url='/solrwayback/waybacklinkgraph.jsp'+serviceParameters;    
	  location.href=url;  
});



function outputUpdate(vol) {
	  document.querySelector('#volume').value = vol;
	}

function reload(){
	   var serviceParameters = getServiceParameters();	   
	   var url='/solrwayback/waybacklinkgraph.jsp'+serviceParameters;	   
	   location.href=url;
}

//action to take on mouse click
function click() {
    d3.select(this).select("text").transition()
        .duration(750)
        .attr("x", 22)
        .style("fill", "steelblue")
        .style("stroke", "lightsteelblue")
        .style("stroke-width", ".5px")
        .style("font", "40px sans-serif");
    d3.select(this).select("circle").transition()
        .duration(750)
        .attr("r", 16)
        .style("fill", "lightsteelblue");
}
//action to take on mouse double click
function dblclick() {
  var newDomain= d3.select(this).select("text").text();
  var min=  $("#rangeslider").dateRangeSlider("min").getTime();
  var max=  $("#rangeslider").dateRangeSlider("max").getTime();
  location.href='/solrwayback/waybacklinkgraph.jsp?domain='+newDomain+'&facetLimit=<%=facetLimit%>&ingoing=<%=ingoing%>&dateStart='+min+'&dateEnd='+max;
}

var width =  1920,
    height = 1000

    var svg = d3.select("body").append("svg")   
    .attr("width", width)
    .attr("height", height)
    .call(d3.behavior.zoom().on("zoom", function () {
    svg.attr("transform", "translate(" + d3.event.translate + ")" + " scale(" + d3.event.scale + ")")
  }))
  .append("g")

svg.append("defs").selectAll("marker")
.data(["suit", "licensing", "resolved"])
.enter().append("marker")
.attr("id", function(d) { return d; })
.attr("viewBox", "0 -5 10 10")
.attr("refX", 25)
.attr("refY", 0)
.attr("markerWidth", 6)
.attr("markerHeight", 6)
.attr("orient", "auto")
.append("path")
.attr("d", "M0,-5L10,0L0,5 L10,0 L0, -5")
.style("stroke", "#4679BD")
.style("opacity", "0.6");


var force = d3.layout.force()
    .gravity(.05)
    .distance(100)
    .charge(-100)
    .size([width, height]);

var serviceUrl='services/waybacklinkgraph'+getServiceParameters();

d3.json(serviceUrl, function(json) {
  force
      .nodes(json.nodes)
      .links(json.links)
      .start();

  var link = svg.selectAll(".link")
      .data(json.links)
    .enter().append("line")
      .attr("class", "link")
      .style("marker-end",  "url(#suit)") // Modified line 
    .style("stroke-width", function(d) { return Math.sqrt(d.weight); });

  var node = svg.selectAll(".node")
      .data(json.nodes)
      .enter().append("g")
      .attr("class", "node") 
      .on("click", click)
      .on("dblclick", dblclick)
      .call(force.drag);

  node.append("circle")
      .attr("r",function(d){return d.size})
      .style("fill", function(d){return d.color});

  node.append("text")
      .attr("dx", 12)
      .attr("dy", ".35em")
      .text(function(d) { return d.name });

  force.on("tick", function() {
    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });

    node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
  });
});

function getServiceParameters(){
    var ingoingCheckbox = document.getElementById('ingoingCheckbox');
    var facets =  document.getElementById('fader').value;
    var checked=ingoingCheckbox.checked;      
    var min=  $("#rangeslider").dateRangeSlider("min").getTime();
    var max=  $("#rangeslider").dateRangeSlider("max").getTime();
    
    var serviceParameters='?domain=<%=domain%>&facetLimit='+facets+'&ingoing='+checked+'&dateStart='+ min+'&dateEnd='+ max;
    return serviceParameters;
}


</script>

</body>
</html>