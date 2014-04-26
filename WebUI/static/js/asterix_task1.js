/* -- Renderer used for draw graph 
   Render code is obtained from arborjs
--- */

var intersect_line_line = function(p1, p2, p3, p4)
{
    var denom = ((p4.y - p3.y)*(p2.x - p1.x) - (p4.x - p3.x)*(p2.y - p1.y));
    if (denom === 0) return false // lines are parallel
    var ua = ((p4.x - p3.x)*(p1.y - p3.y) - (p4.y - p3.y)*(p1.x - p3.x)) / denom;
    var ub = ((p2.x - p1.x)*(p1.y - p3.y) - (p2.y - p1.y)*(p1.x - p3.x)) / denom;

    if (ua < 0 || ua > 1 || ub < 0 || ub > 1)  return false
    return arbor.Point(p1.x + ua * (p2.x - p1.x), p1.y + ua * (p2.y - p1.y));
}

var intersect_line_box = function(p1, p2, boxTuple)
{
    var p3 = {x:boxTuple[0], y:boxTuple[1]},
    w = boxTuple[2],
    h = boxTuple[3]

    var tl = {x: p3.x, y: p3.y};
    var tr = {x: p3.x + w, y: p3.y};
    var bl = {x: p3.x, y: p3.y + h};
    var br = {x: p3.x + w, y: p3.y + h};

    return intersect_line_line(p1, p2, tl, tr) ||
        intersect_line_line(p1, p2, tr, br) ||
        intersect_line_line(p1, p2, br, bl) ||
        intersect_line_line(p1, p2, bl, tl) ||
        false
}

var Renderer = function(canvas){
    var canvas = $(canvas).get(0)
    var ctx = canvas.getContext("2d");
    var particleSystem

    var that = {
	init:function(system){
            //
            // the particle system will call the init function once, right before the
            // first frame is to be drawn. it's a good place to set up the canvas and
            // to pass the canvas size to the particle system
            //
            // save a reference to the particle system for use in the .redraw() loop
            particleSystem = system

            // inform the system of the screen dimensions so it can map coords for us.
            // if the canvas is ever resized, screenSize should be called again with
            // the new dimensions
            particleSystem.screenSize(canvas.width, canvas.height) 
            particleSystem.screenPadding(80) // leave an extra 80px of whitespace per side
        
            // set up some event handlers to allow for node-dragging
            that.initMouseHandling()
	},
      
	redraw:function(){
            // 
            // redraw will be called repeatedly during the run whenever the node positions
            // change. the new positions for the nodes can be accessed by looking at the
            // .p attribute of a given node. however the p.x & p.y values are in the coordinates
            // of the particle system rather than the screen. you can either map them to
            // the screen yourself, or use the convenience iterators .eachNode (and .eachEdge)
            // which allow you to step through the actual node objects but also pass an
            // x,y point in the screen's coordinate system
            // 
            ctx.fillStyle = "white"
            ctx.fillRect(0,0, canvas.width, canvas.height)
            // draw the nodes & save their bounds for edge drawing
	    var nodeBoxes = {}

	    particleSystem.eachNode(function(node, pt){
		// node: {mass:#, p:{x,y}, name:"", data:{}}
		// pt:   {x:#, y:#}  node position in screen coords

		// draw a rectangle centered at pt
		var w = 10
		ctx.fillStyle = (node.data.alone) ? "orange" : "black"
		ctx.fillRect(pt.x-w/2, pt.y-w/2, w,w);
		nodeBoxes[node.name] = [pt.x-w/2, pt.y-11, w, 22]
		// zhimin add: add number in each node
		/*-- start --*/
		/*
		var w = ctx.measureText(node.data.label||"").width + 6;
		var label = node.data.label;
	
		ctx.font = "bold 11px Arial"
		ctx.textAlign = "center"
		    
		ctx.fillStyle = "#888888"
		ctx.fillText(label, pt.x, pt.y+4)
		*/
		/*--end--*/
            }) 

            particleSystem.eachEdge(function(edge, pt1, pt2){
		// edge: {source:Node, target:Node, length:#, data:{}}
		// pt1:  {x:#, y:#}  source position in screen coords
		// pt2:  {x:#, y:#}  target position in screen coords

		var tail = intersect_line_box(pt1, pt2, nodeBoxes[edge.source.name]);
		var head = intersect_line_box(tail, pt2, nodeBoxes[edge.target.name]);

		// draw a line from pt1 to pt2
		ctx.save()
		ctx.strokeStyle = "rgba(0,0,0, .333)"
		ctx.lineWidth = 1
		ctx.beginPath()
		ctx.moveTo(pt1.x, pt1.y)
		ctx.lineTo(pt2.x, pt2.y)
		ctx.stroke()
		ctx.restore()
		if(edge.data.directed==true){
		    //zhimin add: add arrow in each edge
		    /*--start--*/
		    ctx.save()
		    // move to the head position of the edge we just drew
		    var wt = 1;
		    var arrowLength = 6 + wt;
		    var arrowWidth = 2 + wt;
		    ctx.fillStyle =  "#cccccc";
		    ctx.translate(head.x, head.y);
		    ctx.rotate(Math.atan2(head.y - tail.y, head.x - tail.x));
		    
		    // delete some of the edge that's already there (so the point isn't hidden)
		    ctx.clearRect(-arrowLength/2,-wt/2, arrowLength/2,wt)

		    // draw the chevron
		    ctx.beginPath();
		    ctx.moveTo(-arrowLength, arrowWidth);
		    ctx.lineTo(0, 0);
		    ctx.lineTo(-arrowLength, -arrowWidth);
		    ctx.lineTo(-arrowLength * 0.8, -0);
		    ctx.closePath();
		    ctx.fill();
		    ctx.restore()
		    /*--end--*/
		}
		
		
            })

              			
	},
	
	initMouseHandling:function(){
            // no-nonsense drag and drop (thanks springy.js)
            var dragged = null;

            // set up a handler object that will initially listen for mousedowns then
            // for moves and mouseups while dragging
            var handler = {
		clicked:function(e){
		    var pos = $(canvas).offset();
		    _mouseP = arbor.Point(e.pageX-pos.left, e.pageY-pos.top)
		    dragged = particleSystem.nearest(_mouseP);

		    if (dragged && dragged.node !== null){
			// while we're dragging, don't let physics move the node
			dragged.node.fixed = true
		    }

		    $(canvas).bind('mousemove', handler.dragged)
		    $(window).bind('mouseup', handler.dropped)

		    return false
		},
		dragged:function(e){
		    var pos = $(canvas).offset();
		    var s = arbor.Point(e.pageX-pos.left, e.pageY-pos.top)

		    if (dragged && dragged.node !== null){
			var p = particleSystem.fromScreen(s)
			dragged.node.p = p
		    }

		    return false
		},

		dropped:function(e){
		    if (dragged===null || dragged.node===undefined) return
		    if (dragged.node !== null) dragged.node.fixed = false
		    dragged.node.tempMass = 1000
		    dragged = null
		    $(canvas).unbind('mousemove', handler.dragged)
		    $(window).unbind('mouseup', handler.dropped)
		    _mouseP = null
		    return false
		}
            }
            
            // start listening
            $(canvas).mousedown(handler.clicked);

	},
	
    }
    return that
}


/*----- use above functions to draw graph ---*/


function addResult(dom, res) {
    //alert("add result");
    for (i in res) {
        $(dom).append(res[i] + "<br/>");
    }
}

function drawGraph(dom, res){
    //var sys = arbor.ParticleSystem(100, 50, 0.1);
    var sys = arbor.ParticleSystem(100, 100, 0.5);
    sys.parameters({gravity:true});
    sys.renderer = Renderer(dom);
    for(i in res){
	var resJson = eval('('+res[i]+')');
	var sourceNode = resJson.source_node.int32.toString();
	var targetNodeArray = resJson.target_node.unorderedlist;
	sys.addNode(sourceNode, {label:sourceNode});
	for(var i=0; i<targetNodeArray.length; ++i){
	    var targetNode = targetNodeArray[i].int32.toString();
	    sys.addEdge(sourceNode, targetNode, {directed:false});
	}
    }
}

function loadGraph(){
    var fileName = $("#filePath").val().replace(/^.*[\\\/]/, '');
    var query = 'use dataverse Task1; delete $tuple from dataset Graph; load dataset Graph using localfs(("path"="localhost:///home/zhimin/study/CS295/display/src/main/resources/graph-display-arborjs/graphFiles/'+fileName+'"),("format"="adm"));';
    //alert(query);
    var xmlhttp;
    if(window.XMLHttpRequest){
	xmlhttp = new XMLHttpRequest();
    }
    else{
	xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
    }

    xmlhttp.onreadystatechange=function(){
	//alert("OK");
	//if(xmlhttp.readyState==4 && xmlhttp.status==200){
	    //alert("start");
	    var A = new AsterixDBConnection().dataverse("Task1");
	    var expression0a = new FLWOGRExpression()
		.ForClause("$node", new AExpression("dataset Graph"))
		.ReturnClause({
		    "source_node":"$node.source_node",
		    "target_node":"$node.target_node"});
	    
	    var success = function(res){
		//alert(res["results"]);
		//addResult('#graph', res["results"]);
		drawGraph('#graph', res["results"]);
	    };
	    A.query(expression0a.val(),  success);
	//}
    }

    xmlhttp.open("GET", "http://localhost:19002/update?statements="+query, true);
    xmlhttp.send();
    
    
}


$(document).ready(function(){

    $("#filePath").change(loadGraph);

    var A = new AsterixDBConnection().dataverse("Task1");
    
    
    var expression0a = new FLWOGRExpression()
    .ForClause("$node", new AExpression("dataset Graph"))
    .ReturnClause({
	"source_node":"$node.source_node",
	"target_node":"$node.target_node"});
    
    var success = function(res){
	//addResult('#graphDisplayBlock', res["results"]);
	//alert(res["results"]);
	drawGraph('#graph', res["results"]);
    };
    A.query(expression0a.val(),  success);
    
});
