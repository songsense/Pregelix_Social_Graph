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
    var gfx = arbor.Graphics(canvas);
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
		
		var w = 55;
		/*
		//ctx.fillStyle = (node.data.alone) ? "orange" : "black"
		ctx.fillStyle = node.data.color;
		ctx.fillRect(pt.x-w/2, pt.y-w/2, w,w);
		*/
		gfx.oval(pt.x-w/2, pt.y-w/2, w, w, {fill:node.data.color, alpha:1});
		gfx.text(node.data.label, pt.x, pt.y+7, {color:"white", align:"center", font:"Arial", size:12})
		nodeBoxes[node.name] = [pt.x-w/2, pt.y-11, w, 22]
		// zhimin add: add number in each node
		/*-- start --*/
		/*
		  var w = ctx.measureText(node.data.label||"").width + 6;
		  var label = node.data.label;
		  ctx.font = "bold 11px Arial"
		  ctx.textAlign = "center"
		  
		  ctx.fillStyle = "#FFFFFF"
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
		ctx.strokeStyle = edge.data.color;
		ctx.lineWidth = 2
		ctx.beginPath()
		ctx.moveTo(tail.x, tail.y)
		ctx.lineTo(head.x, head.y)
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

//var pathGraph = "/Users/liqiangw/Documents/workspace/Pregelix_Social_Graph/WebUI/graphFile/adm/";

var pathGraph = "/home/zhimin/study/CS295/display/src/main/resources/graph-display-arborjs/graphFiles/";

var sys;

var fileName;


var colorArray=["#CA7A2C", "#E03C8A", "#42AA5D", "#DB4D6D", "#64363C","#D0104C","#E2943B","#77428D","#E87A90","#787878"];

var numColor = colorArray.length;

var timeOut = 1300;

var labelNodeIDTable = {};

var preSourceNode = "0";

var preIterNum = "0";

var coloredNodes = [];

var coloredEdges = [];

var defaultNodeColor = "#4F726C";

var defaultEdgeColor = "#535953";

var allNodes = [];

var allEdges = [];

var logInStatus = false;

var logInUserId;

var nodeSet = {};

function clearGraphVariables(){
	coloredNodes = [];
	coloredEdges = [];
	labelNodeIDTable = {};
	nodeSet = {};
}


function initializeRender(){
	sys = arbor.ParticleSystem(3500, 50, 0.5);
    sys.parameters({gravity:true});
    sys.renderer = Renderer('#graph');
}

function initializeVariables(){
	logInUserId = "";

}

function checkAndLoadGraph(){
	var status = $('#iframeID1').contents().find('#returnResult').html();
	//alert(status);
	if(parseInt(status)!=1){
		//alert('User ID or Password is not correct!');
		logInStatus = false;
	}
	else{
		var username = $('#iframeID1').contents().find('#returnLabel').html();
		//alert(username);
		$('#welcomeText').html('Welcome '+username+'!');
		$('#logIn').hide();
		$('#logOut').show();
		logInStatus = true;
		loadGraph();
	}
}

function logOut(){

	$('#logOut').hide();
	$('#logIn').show();
	logInStatus = false;
	deleteAllNodesAndEdges();

}

/*
delete all nodes and edges in the graph
*/
function deleteAllNodesAndEdges(){
	//alert("delete");
	for(var i=0; i<allEdges.length; ++i){
		var nodeArray = allEdges[i].split("||");
		var sourceNode = nodeArray[0];
		var targetNode = nodeArray[1];
		var edgeArray = sys.getEdges(sourceNode, targetNode);
		sys.pruneEdge(edgeArray[0]);
	}
	for(var i=0; i<allNodes.length; ++i){
		var node = sys.getNode(allNodes[i]);
		sys.pruneNode(node);
	}
	//alert(allEdges.length);
	//alert(allNodes.length);
	allEdges = [];
	allNodes = [];
}

/*
change the color of nodes and edges to default color
*/
function clearNodeEdgeColor(){

    for(var i=0; i<coloredNodes.length; ++i){
		coloredNodes[i].data.color= defaultNodeColor;
    }
    coloredNodes = [];
    
    for (var i=0; i<coloredEdges.length; ++i){
		var sourceNode = coloredEdges[i].source;
		var targetNode = coloredEdges[i].target;
		sys.pruneEdge(coloredEdges[i]);
		sys.addEdge(sourceNode, targetNode, {directed:false, color:defaultEdgeColor});


    }
    coloredEdges = [];

    //flush
    for (var i=0; i<10; ++i){
	    sys.addNode("-1", {label:"-1", color:"#FFFFFF"});
	    sys.addNode("-2", {label:"-2", color:"#FFFFFF"});
	    sys.addEdge("-1", "-2", {directed:false, color:"#FFFFFF"})
	    var edgeArray = sys.getEdges("-1","-2");
	    sys.pruneEdge(edgeArray[0]);   
	    var tempNode = sys.getNode("-1");
	    sys.pruneNode(tempNode);
	    tempNode = sys.getNode("-2");
	    sys.pruneNode(tempNode);
	}
}


/*
draw the graph
*/
function drawGraph(dom, res){
    
    deleteAllNodesAndEdges();
    for(i in res){
		//alert(res[i]);
		var resJson = eval('('+res[i]+')');
		var sourceNode = resJson.source_node.int32.toString();
		var targetNodeArray = resJson.target_nodes.orderedlist;


		//alert("sourceNode:"+sourceNode);
		var label=resJson.label;
		labelNodeIDTable[label]=sourceNode;
		//alert(label);
		sys.addNode(sourceNode, {label:label, color:defaultNodeColor});
		allNodes.push(sourceNode);
		nodeSet[sourceNode] = true;

		for(var i=0; i<targetNodeArray.length; ++i){
		    var targetNode = targetNodeArray[i].int32.toString();
		    if(!nodeSet[targetNode]==true){
		    	sys.addEdge(sourceNode, targetNode, {directed:false, color:defaultEdgeColor});
		    	allEdges.push(sourceNode+"||"+targetNode);
		    }
		}
    }
}

function loadGraph(){

	clearGraphVariables();
	
	//get nodes, and draw graph
    $('#tips').html("");
	var A = new AsterixDBConnection().dataverse("Graph");
	var whereClauseStr = '$node.login_user_id='+logInUserId;
	var expression0a = new FLWOGRExpression()
	.ForClause("$node", new AExpression("dataset DisplayGraph"))
	.WhereClause(new AExpression(whereClauseStr))
	.ReturnClause("$node");
	
	var success = function(res){
		drawGraph('#graph', res["results"]);
	}
	A.query(expression0a.val(),  success);
		
}

/*
draw Task1
*/
function drawTaskOne(sourceNode, targetNode){
	var connTask1 = new AsterixDBConnection().dataverse("Tasks");
    var whereClauseSourceNodeStr = '$node.login_user_id='+sourceNode;
    var whereClauseTargetNodeStr = '$node.target_user_id='+targetNode;
    //alert(whereClauseStr);
    var expTask1 = new FLWOGRExpression()
	.ForClause("$node", new AExpression("dataset TaskOne"))
	.WhereClause().and(new AExpression(whereClauseSourceNodeStr), new AExpression(whereClauseTargetNodeStr))
	.ReturnClause("$node");
	var succTask1 = function(tempres){
		var res = tempres["results"];
        for(i in res){
            var resJson = eval('('+res[i]+')');
            var path = resJson.path.orderedlist;
            //draw graph
            // for(var j=0; j<path.length; ++j)
            	// alert(path[j].int32.toString());
            sys.getNode(path[0].int32.toString()).data.color="#FF0000";
            sys.getNode(path[path.length-1].int32.toString()).data.color="#FF0000";
	    	coloredNodes.push(sys.getNode(path[0].int32.toString()));
	    	coloredNodes.push(sys.getNode(path[path.length-1].int32.toString()));
            for(var j=1; j<path.length-1; ++j){
                if(j!=1){
                	if(path[j].int32.toString() in nodeSet){
                    	sys.getNode(path[j].int32.toString()).data.color="#CA7A2C";
		    			coloredNodes.push(sys.getNode(path[j].int32.toString()));
		    			//draw edges in the graph
		    			var sourceNodeObj = sys.getNode(path[j].int32.toString());
            			var targetNodeObj = sys.getNode(path[j+1].int32.toString());
		            	var edgeArray = sys.getEdges(sourceNodeObj, targetNodeObj);
		            	sys.pruneEdge(edgeArray[0]);
		            	sys.addEdge(path[j].int32.toString(), path[j+1].int32.toString(), {directed:false, color:"#FF0000"});
						edgeArray = sys.getEdges(sourceNodeObj, targetNodeObj);
						coloredEdges.push(edgeArray[0]);
		    		}
		    		else{
		    			//add two nodes and add two edges
		    		}
				}
           
            }
        }
	}
	connTask1.query(expTask1.val(), succTask1);
}

function drawTaskOneOutDisplayGraph(sourceNode, targetNode){

}

/*
run task 1
*/
function runTask1(){
	if(logInStatus == false){
		alert("Please login first!");
		return;
	}
	clearNodeEdgeColor();
	var targetNode = $('#target_id').val().toString();
	var sourceNode = logInUserId;
	/*if(targetNode in nodeSet){
		drawTaskOneInDisplayGraph(sourceNode, targetNode);
	}
	else{
		drawTaskOneOutDisplayGraph(sourceNode, targetNode);
	}*/
}


$(document).ready(function(){

    initializeRender();

    initializeVariables();

    //$("#filePath").change(uploadFile);

    //$("#iframeID1").load(loadGraphFirstTime);

    

    //$("#runTask2").click(runTask2);

    //$('#runTask3').click(runTask3);

    $("#logOut").hide();

    $('#accordion').accordion();

    $('#logInButton').click(function(){
    	logInUserId = $('#user_id').val();
    	$('form').submit();
    });

    $('#logOutButton').click(logOut);

    $('#iframeID1').load(checkAndLoadGraph);

    $("#runTask1").click(runTask1);


});