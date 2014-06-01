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
		var fontColor = "white";
		if(node.data.font_color!="")
			fontColor = node.data.font_color;
		var fontSize;
		if(node.data.font_size==undefined)
			fontSize = 12;
		else
			fontSize = parseInt(node.data.font_size);
		//alert(fontSize);
		gfx.text(node.data.label, pt.x, pt.y+7, {color:fontColor, align:"center", font:"Arial", size: fontSize})
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
		if(edge.data.dashFlag == true)
			ctx.setLineDash([5]);
		if (edge.data.text != undefined) {
			gfx.text(edge.data.text + " hops", (pt1.x+pt2.x)/2, (pt1.y+pt2.y)/2, {color:edge.data.color, align:"center", font:"Arial", size: 12})
			//ctx.fillText(edge.data.text, (pt1.x+pt2.x)/2, (pt1.y+pt2.y)/2);
		}
		ctx.moveTo(tail.x, tail.y)
		ctx.lineTo(head.x, head.y)
		//ctx.dashedLine(head.x, head.y,tail.x, tail.y,[30,10]);
		ctx.stroke()
		ctx.restore()
		
		
		
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
				},
		      	enter:function(e){
		            var pos = $(canvas).offset();
		            _mouseP = arbor.Point(e.pageX-pos.left, e.pageY-pos.top)
		            nearest = sys.nearest(_mouseP);

		            if (!nearest.node || nearest.distance > 50) 
		            // if (!nearest.node)
		            	return false;
		            var label = nearest.node.data.label;
		            if (isNaN(parseInt(label))) {
		            	// label string
		            	nearest.node.data.copyLabel = nearest.node.data.label;
			            nearest.node.data.label = nearest.node.name;
			            nearest.node.data.font_color = "#FFFF00";			       
		            } else {
		            	// id string
		            	nearest.node.data.label = nearest.node.data.copyLabel;
		            	nearest.node.data.font_color = "#FFFFFF";
		            }
		            
				},
				// leave:function(e){
		  //           var pos = $(canvas).offset();
		  //           _mouseP = arbor.Point(e.pageX-pos.left, e.pageY-pos.top)
		  //           nearest = sys.nearest(_mouseP);

		  //           if (!nearest.node || nearest.distance > 120) 
		  //           // if (!nearest.node)
		  //           	return false;
		  //           if (nearest.node.data.copyLabel == undefined ||
		  //           	!nearest.node.data.copyLabel ||
		  //           	isNaN(parseInt(nearest.node.data.label)))
		  //           	return false;
		  //           nearest.node.data.label = nearest.node.data.copyLabel;
				// }
			}          
	        // start listening
	        $(canvas).mousedown(handler.clicked);
	        $(canvas).mousedown(handler.enter);
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

var nodeIDLabelTable = {};

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


var backgroundColor = "#B19693";
var outsideNodesSetSet = {};
var outsideEdgesSet = {};

var maxNumVIP = 5;
var maxNumOutsideVIP = 5;

// max num of community member
var maxCommunityMembers = 15;

//var maxDegreeArray = [4, 4, 4, 3, 3, 2, 2];

var maxDegree = 10;

var maxNodeNum = 20;

var maxLevel = 3;

var maxPathLength = 7;

// max len of the label
var maxLenLabel = 7;

var logInNodeColor = "#990099";


function clearGraphVariables(){
	coloredNodes = [];
	coloredEdges = [];
	labelNodeIDTable = {};
	nodeIDLabelTable = {};

	nodeSet = {};

	outsideNodesSet = {};
	outsideEdgesSet = {};

	allEdges = [];
	allNodes = [];

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
*		Label Abbreviation
*/
function labelAbbr(label) {
	if (label.length <= maxLenLabel) {
		return label;
	}
	var name = label.split(" ");
	var firstName = name[0];
	var lastName = name[1];

	label = firstName + " " + lastName[0] + ".";
	if (label.length <= maxLenLabel) {
		return label;
	}

	label = firstName[0] + ". " + lastName;
	if (label.length <= maxLenLabel) {
		return label;
	}

	label = firstName[0]+lastName[0];
	return label;
}

/*
*	clear the outside nodes
*/
function clearOutsideNodesSet() {
	
	for(var edge in outsideEdgesSet){
		var nodeArray = edge.split("||");
		var sourceNode = nodeArray[0];
		var targetNode = nodeArray[1];
		var edgeArray = sys.getEdges(sourceNode, targetNode);
		sys.pruneEdge(edgeArray[0]);
	}

	outsideEdgesSet = {};

	for (var nodeStr in outsideNodesSet) {
		var node = sys.getNode(nodeStr);
		sys.pruneNode(node);
	}
	outsideNodesSet = {};
}

/*
*	add the outside nodes beside to a inside node
*/
function addOutsideNode(outsideNode, insideNode, distance2Dest) {
	var connection = new AsterixDBConnection().dataverse("Tasks");
	var expression = new FLWOGRExpression()
	.ForClause("$node", new AExpression("dataset AccountInfo"))
	.WhereClause(new AExpression("$node.user_id=" + outsideNode))
	.ReturnClause("$node.label");

	var successGetLabel = function(tempres) {
		var res = tempres["results"];
		for (i in res) {
			var resJson = eval('(' + res[i] + ')');
			var label = resJson.toString();
			label = labelAbbr(label);
			sys.addNode(outsideNode, {label:label, color:backgroundColor,mass:1, alpha:0});						
			outsideNodesSet[outsideNode] = true;

			sys.addEdge(insideNode, outsideNode, {text:distance2Dest.toString(), color:backgroundColor, dashFlag: true});			
			outsideEdgesSet[insideNode+"||"+outsideNode] = true;
		}
	}
	connection.query(expression.val(), successGetLabel);
	
}


/*
delete all nodes and edges in the graph
*/
function deleteAllNodesAndEdges(){
	clearOutsideNodesSet();
	//alert("delete");

	for(var i=0; i<allEdges.length; ++i){
		var nodeArray = allEdges[i].split("||");
		var sourceNode = nodeArray[0];
		var targetNode = nodeArray[1];
		var edgeArray = sys.getEdges(sourceNode, targetNode);
		sys.pruneEdge(edgeArray[0]);
	}
	for(var i=0; i<allNodes.length; ++i){
		//alert(allNodes[i]);
		var node = sys.getNode(allNodes[i]);
		sys.pruneNode(node);
	}


	allEdges = [];
	allNodes = [];

	
}


/*
change the color of nodes and edges to default color
*/
function clearNodeEdgeColor(){
	// to Zhimin:
	// in case I forgot to tell you 
	// to clear outside nodes as well clear node edge color
	clearOutsideNodesSet();
    
    
    for (var i=0; i<coloredEdges.length; ++i){
		var sourceNode = coloredEdges[i].source;
		var targetNode = coloredEdges[i].target;
		sys.pruneEdge(coloredEdges[i]);
		sys.addEdge(sourceNode, targetNode, {color:defaultEdgeColor});
    }
    coloredEdges = [];

    for(var i=0; i<coloredNodes.length; ++i){
		coloredNodes[i].data.color= defaultNodeColor;
    }
    coloredNodes = [];

    //flush
    for (var i=0; i<10; ++i){
	    sys.addNode("-1", {label:"-1", color:"#FFFFFF"});
	    sys.addNode("-2", {label:"-2", color:"#FFFFFF"});
	    sys.addEdge("-1", "-2", {color:"#FFFFFF"})
	    var edgeArray = sys.getEdges("-1","-2");
	    sys.pruneEdge(edgeArray[0]);   
	    var tempNode = sys.getNode("-1");
	    sys.pruneNode(tempNode);
	    tempNode = sys.getNode("-2");
	    sys.pruneNode(tempNode);
	}

}



function addNodeInGraph(inputNode, inputLabel, inputColor){
	//sys.addNode(inputNode, {label:inputLabel, color:inputColor});
	sys.addNode(inputNode, {label:inputLabel, color:inputColor});
	allNodes.push(inputNode);
	nodeSet[inputNode] = true;
	
}

function addBiDirectEdgeInGraph(sourceNode, targetNode, inputColor){
	sys.addEdge(sourceNode, targetNode, {color:inputColor});
	allEdges.push(sourceNode+"||"+targetNode);
	sys.addEdge(targetNode, sourceNode, {color:inputColor});
	allEdges.push(targetNode+"||"+sourceNode);
}

function drawGraphBFS(dom, res){
	// alert("drawGraph");
	deleteAllNodesAndEdges();
	var nodeNeighbors = {};
	var nodeWeights = {};
	var nodeLabel = {};
	// alert(res.length);
	for(i in res){
		var resJson = eval('('+res[i]+')');
		//alert(res);
		var sourceNode = resJson.user_id.int32.toString();
		var targetNodeArray = resJson.target_nodes.orderedlist;
		nodeNeighbors[sourceNode] = targetNodeArray;
		nodeWeights[sourceNode] = resJson.importance.orderedlist[0];
		nodeLabel[sourceNode] = labelAbbr(resJson.label.orderedlist[0]);
	}
	var levelArray = [];
	var level = [logInUserId];
	levelArray.push(level);
	addNodeInGraph(logInUserId, nodeLabel[logInUserId], logInNodeColor);
	var visited = {};
	visited[logInUserId] = true;
	var nodeNum = 0;
	var edgeSet = {};
	for(var k=0; nodeNum<maxNodeNum; ++k){
		var neighborDisplayArray = [];
		for(var j=0; j<levelArray[k].length; ++j){
			var currNode = levelArray[k][j];
			//alert(currNode);
			var neighborsWeights = [];
			for(var n=0; n<nodeNeighbors[currNode].length; ++n){
				neighborsWeights.push({node:nodeNeighbors[currNode][n].int32.toString(),weight:nodeWeights[nodeNeighbors[currNode][n].int32.toString()]});
			}
			neighborsWeights.sort(function(a, b){if(a.weight>b.weight) return -1; if(a.weight<b.weight) return 1; return 0});
			var nodeCount = 0;
			for(var n=0; n<Math.min(nodeNeighbors[currNode].length, maxDegree); ++n){
				var neighbor = neighborsWeights[n].node;
				//alert(neighborsWeights[n].weight);
				if(visited[neighbor]==true){
					if(!(edgeSet[currNode+"||"+neighbor]==true)){
						addBiDirectEdgeInGraph(currNode, neighbor, defaultEdgeColor);
						edgeSet[currNode+"||"+neighbor]=true;
						edgeSet[neighbor+"||"+currNode]=true;
					}
				}
				else{
						addNodeInGraph(neighbor, nodeLabel[neighbor], defaultNodeColor);
						visited[neighbor]=true;
						neighborDisplayArray.push(neighbor);
						
						addBiDirectEdgeInGraph(currNode, neighbor, defaultEdgeColor);
						edgeSet[currNode+"||"+neighbor]=true;
						edgeSet[neighbor+"||"+currNode]=true;
						//alert(nodeNum);
						++nodeNum;
						if(nodeNum>maxNodeNum){
							//alert("break");
							return;
						}
				}
			}
		}
		if(neighborDisplayArray.length == 0){
			//alert(k);
			break;
		}
		levelArray.push(neighborDisplayArray);
	}
}

function loadGraph(){

	clearGraphVariables();
	
	//get nodes, and draw graph
    $('#tips').html("");
	var A = new AsterixDBConnection().dataverse("Tasks");
	var whereClauseStr = '$node.login_user_id='+logInUserId;
	var joinGraphTaskFourClauseStr = '$node.source_node=$weight.user_id';
	var joinGraphAccountClauseStr = '$account.user_id=$node.source_node';

	var expression1 = new FLWOGRExpression()
	.ForClause("$weight", new AExpression("dataset TaskFour"))
	// .ForClause("$account", new AExpression("dataset AccountInfo"))
	.WhereClause(new AExpression(joinGraphTaskFourClauseStr))
	.ReturnClause(
		// "label" : "$account.label",
		"$weight.importance"
	);

	var expression2 = new FLWOGRExpression()
	.ForClause("$account", new AExpression("dataset AccountInfo"))
	.WhereClause(new AExpression(joinGraphAccountClauseStr))
	.ReturnClause(
		"$account.label"
	);

	var expression = new FLWOGRExpression()
	.ForClause('$node', new AExpression("dataset DisplayGraph"))
	.WhereClause(new AExpression(whereClauseStr))
	.ReturnClause({
		"user_id": "$node.source_node",
		"target_nodes": "$node.target_nodes",
		"importance":expression1,
		"label":expression2
	});

	
	
	var success = function(res){
		drawGraphBFS('#graph', res["results"]);
		//alert(allNodes.length);
		//alert(allEdges.length);
	}
	A.query(expression.val(),  success);
		
}

function addOutSideNodeForTaskOne(nodeID, nodeColor){
	var connAccount = new AsterixDBConnection().dataverse("Tasks");
    var whereClauseStr = '$node.user_id='+nodeID;
    //alert(whereClauseStr);
    var exp = new FLWOGRExpression()
	.ForClause("$node", new AExpression("dataset AccountInfo"))
	.WhereClause().and(new AExpression(whereClauseStr))
	.ReturnClause("$node");
	var succAccount = function(tempres){
		var res = tempres["results"];
		for(i in res){
			var resJson = eval('('+res[i]+')');
			targetLabel = resJson.label;

			sys.addNode(nodeID, {label:labelAbbr(targetLabel), color: nodeColor});            			
			outsideNodesSet[nodeID] = true;
			//allNodes.push(path[path.length-1].int32.toString());
		}
	}
	connAccount.query(exp.val(),succAccount);       
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
	// alert('draw task one');

	var succTask1 = function(tempres){
		var res = tempres["results"];
        for(i in res){
            var resJson = eval('('+res[i]+')');
            var path = resJson.path.orderedlist;
            var targetLabel = "";
            // for(var j=0; j<path.length; ++j)
            	 // alert(path[j].int32.toString());
            sys.getNode(path[0].int32.toString()).data.color=logInNodeColor;
            if(!(path[path.length-1].int32.toString() in nodeSet)){
			    addOutSideNodeForTaskOne(path[path.length-1].int32.toString(), "#FF0000");       	
            }
            else{
            	sys.getNode(path[path.length-1].int32.toString()).data.color="#FF0000";
            	coloredNodes.push(sys.getNode(path[path.length-1].int32.toString()));
            }
	    	coloredNodes.push(sys.getNode(path[0].int32.toString()));
	    	var prevNode = -1;
            for(var j=2; j<path.length-1; ++j){

				if(!(path[j].int32.toString() in nodeSet)){
		    			//add two nodes and add two edges
		    			if(j>=maxPathLength){
							distance2Dest = path.length - j;
		    				sys.addEdge(path[j-1].int32.toString(), path[path.length-1].int32.toString(), {text:distance2Dest.toString(),color:"#000000", dashFlag: true})
		    				outsideEdgesSet[path[j-1].int32.toString()+"||"+path[path.length-1].int32.toString()] = true;
		    				//allEdges.push(path[j].int32.toString()+"||"+path[path.length-1].int32.toString());
		    				prevNode = -1;
		    				break;
		    			}
		    			else{
            				addOutSideNodeForTaskOne(path[j].int32.toString(), "#CA7A2C");
            				sys.addEdge(path[j-1].int32.toString(), path[j].int32.toString(), {color:"#FF0000", dashFlag: false})
		    				outsideEdgesSet[path[j-1].int32.toString()+"||"+path[j].int32.toString()] = true;
		    				prevNode = j;
		    			}
		    	}
		    	else{
		    		sys.getNode(path[j].int32.toString()).data.color="#CA7A2C";
		    		coloredNodes.push(sys.getNode(path[j].int32.toString()));
		    		var sourceNodeObj = sys.getNode(path[j-1].int32.toString());
    				var targetNodeObj = sys.getNode(path[j].int32.toString());
            		var edgeArray = sys.getEdges(sourceNodeObj, targetNodeObj);
            		sys.pruneEdge(edgeArray[0]);
            		sys.addEdge(path[j-1].int32.toString(), path[j].int32.toString(), { color:"#FF0000"});
					edgeArray = sys.getEdges(sourceNodeObj, targetNodeObj);
					coloredEdges.push(edgeArray[0]);
					prevNode = j;
		    	}
           
            }
            if(prevNode != -1){
            	sys.addEdge(path[prevNode].int32.toString(), path[path.length-1].int32.toString(), { color:"#FF0000", dashFlag: false})
		    	outsideEdgesSet[path[prevNode].int32.toString()+"||"+path[path.length-1].int32.toString()] = true;
            }


        }
	}
	connTask1.query(expTask1.val(), succTask1);
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
	clearOutsideNodesSet();
	var targetNode = $('#target_id').val().toString();
	var sourceNode = logInUserId;
	drawTaskOne(sourceNode, targetNode);


}




/*
draw task 2
*/

function doDrawTaskTwo(resNode) {
	sys.addNode("tempNode1", {label:"tempNode1", color:"#FFFFFF"});
	sys.addNode("tempNode2", {label:"tempNode2", color:"#FFFFFF"});	
	var nodesInCommunity = [];
	var cnt = 0;
	for (m in resNode) {
		if (cnt > maxCommunityMembers) {
			break;
		}
		++cnt;
		var resJson = eval('(' + resNode[m] + ')');
		var nodeStr = resJson.user_id.int32.toString();
		if (nodeStr in nodeSet) {
			// color node
			var nodeObj = sys.getNode(nodeStr);
			if (nodeStr == logInUserId)
				nodeObj.data.color = logInNodeColor;
			else
				nodeObj.data.color = colorArray[1];

			coloredNodes.push(nodeObj);

			nodesInCommunity.push(nodeStr);

			sys.addEdge("tempNode1", "tempNode2", {color:"#FFFFFF"});
			var edgeArray = sys.getEdges("tempNode1", "tempNode2");
			sys.pruneEdge(edgeArray[0]);
		} else {
			//alert("not in nodeset:"+nodeStr);			
			findIntermediateNodeNDraw(nodeStr);
		}
	}	
	sys.pruneNode(sys.getNode("tempNode1"));
	sys.pruneNode(sys.getNode("tempNode2"));
		
	for (var i = 0; i < nodesInCommunity.length; ++i) {
		for (var j = 0; j < nodesInCommunity.length; ++j) {
        	var sourceNodeObj = sys.getNode(nodesInCommunity[i]);
        	var targetNodeObj = sys.getNode(nodesInCommunity[j]);
			var edgeArray = sys.getEdges(sourceNodeObj, targetNodeObj);
			if (edgeArray.length == 0)
				continue;
			sys.pruneEdge(edgeArray[0]);
			sys.addEdge(nodesInCommunity[i], nodesInCommunity[j], { color:colorArray[1]})
			edgeArray = sys.getEdges(sourceNodeObj, targetNodeObj);
			coloredEdges.push(edgeArray[0]);
		}
	}
	
}

function queryDrawTaskTwo() {
	nodeID = logInUserId;
	// get the community id
	var whereClauseStr = "$node.user_id="+nodeID;
	var connectionGetCommID = new AsterixDBConnection().dataverse("Tasks");
	var expressionGetCommID = new FLWOGRExpression()
	.ForClause("$node", new AExpression("dataset TaskTwo"))
	.WhereClause(new AExpression(whereClauseStr))
	.ReturnClause("$node");

	var successGetCommID = function(resCommIDTemp) {
		var resCommID = resCommIDTemp["results"];		
		for (j in resCommID) {
			var resJson = eval('('+resCommID[j]+')');
			var commID = resJson.community_id.int32.toString();			
			var whereClauseStr = "$node.community_id="+commID;
			var connectionGetNode = new AsterixDBConnection().dataverse("Tasks");
			var expressionGetNode = new FLWOGRExpression()
			.ForClause("$node", new AExpression("dataset TaskTwo"))
			.WhereClause(new AExpression(whereClauseStr))
			.ReturnClause("$node");

			var successGetNode = function(resNodeTemp) {
				var resNode = resNodeTemp["results"];				
				doDrawTaskTwo(resNode);
			}

			connectionGetNode.query(expressionGetNode.val(), successGetNode);
		}
	}

	connectionGetCommID.query(expressionGetCommID.val(), successGetCommID);
}


/*
run task 2
*/
function runTask2() {
	if (logInStatus == false) {
		alert("Please login first!");
		return;
	}
	clearNodeEdgeColor();

	clearOutsideNodesSet();
	queryDrawTaskTwo();

}

/*
		Find the last known intermediate node and draw the outside node
*/
function findIntermediateNodeNDraw(outsideNode) {
	var connectionGetPath = new AsterixDBConnection().dataverse("Tasks");
	var exprGetPath = new FLWOGRExpression()
	.ForClause("$n", new AExpression("dataset TaskOne"))
	.WhereClause().and(
		new AExpression("$n.login_user_id="+logInUserId),
		new AExpression("$n.target_user_id="+outsideNode))
	.ReturnClause("$n.path");

	var succGetPath = function(tempres) {
		var res = tempres["results"];
		for (var i in res) {
			var resJson = eval('(' + res[i] + ')');
			var path = resJson.orderedlist;
			var lastInNodeSet = logInUserId;
			var step = 0;
			for (var i = 2; i < path.length; ++i) {
				var nodeStr = path[i].int32.toString();				
				if (!(nodeStr in nodeSet)) {
					distance2Dest = path.length-i;
					break;
				}				
				lastInNodeSet = nodeStr;
			}
			addOutsideNode(outsideNode, lastInNodeSet, distance2Dest);
		}
	}

	connectionGetPath.query(exprGetPath.val(), succGetPath);
}

/*
draw task 3
*/
function doDrawTaskThree(resNode) {
	var nodeID = logInUserId;
	var inputFriendNum = $('#task3_num_friends').val();

	sys.addNode("tempNode1", {label:"tempNode1",color:"#FFFFFF"});
	sys.addNode("tempNode2", {label:"tempNode2",color:"#FFFFFF"});

	for (i in resNode) {
		var resJson = eval('(' + resNode[i] + ')');
		var suggestedFriends = resJson.suggested_friends.orderedlist;
		var friendNum = suggestedFriends.length;
		if (friendNum == 0) {
			$('#tips').html('<p style="font-size: 30px; text-align:center">No suggested friends!</p>');
			continue;
		} else {
            sys.getNode(nodeID).data.color=logInNodeColor;
			coloredNodes.push(sys.getNode(nodeID));
            var minFriendNum = (friendNum>inputFriendNum?inputFriendNum:friendNum);
            for(var k=0; k<minFriendNum; ++k){
            	var suggestedFriend = suggestedFriends[k].int32.toString();
            	if (suggestedFriend in nodeSet) {
             	   sys.getNode(suggestedFriend).data.color=colorArray[1];
             	   coloredNodes.push(sys.getNode(suggestedFriend));
	    		} else {	    	
	    			findIntermediateNodeNDraw(suggestedFriend);		    			
	    		}
            }
            sys.addEdge("tempNode1", "tempNode2", { color:"#FFFFFF"})
            var edgeArray = sys.getEdges("tempNode1","tempNode2");
            sys.pruneEdge(edgeArray[0]);
		}
	}

	sys.pruneNode(sys.getNode("tempNode1"));
	sys.pruneNode(sys.getNode("tempNode2"));
}

function queryDrawTaskThree() {
	var nodeID = logInUserId;

	var connGetRecomFriends = new AsterixDBConnection().dataverse("Tasks");
	var whereClauseStr = "$node.user_id=" + nodeID;
	var expressionRecomFriends = new FLWOGRExpression()
	.ForClause("$node", new AExpression("dataset TaskThree"))
	.WhereClause(new AExpression(whereClauseStr))
	.ReturnClause("$node");

	succGetRecomFriends = function(tempres) {
		var resNode = tempres["results"];
		doDrawTaskThree(resNode);
	}

	connGetRecomFriends.query(expressionRecomFriends.val(), succGetRecomFriends);
}

/*
run task 3
*/
function runTask3() {
	if (logInStatus == false) {
		alert("Please login first!");
		return;
	}
	clearNodeEdgeColor();
	clearOutsideNodesSet();
	queryDrawTaskThree();	
}

/*
*	query and draw the task 4
*/
function queryDrawTaskFour() {
	// get max number VIP list
	var connectionGetMaxNumVIPList = new AsterixDBConnection().dataverse("Tasks");
	var expGetMaxNumVIPList = new FLWOGRExpression()
	.ForClause("$n", new AExpression("dataset TaskFour"))
	.OrderbyClause(new AExpression("$n.importance"), "desc")
	.LimitClause(new AExpression(maxNumVIP.toString()))
	.ReturnClause("$n.user_id");

	var succGetMaxNumVIPList = function(tempres) {
		var numOutsideVIP = 0;
		var resNode = tempres["results"];
		for (i in resNode) {
			var resJson = eval('(' + resNode[i] + ')');
			var user_id = resJson.int32.toString();
			if (user_id == logInUserId) {
				continue;
			}
			if (user_id in nodeSet) {
				// color node
				var nodeObj = sys.getNode(user_id);
				nodeObj.data.color = colorArray[1];
				coloredNodes.push(nodeObj);				
			} else if (numOutsideVIP < maxNumOutsideVIP) {
				++numOutsideVIP;
				findIntermediateNodeNDraw(user_id);
			}
		}
		var nodeObj = sys.getNode(logInUserId);
		nodeObj.data.color = logInNodeColor;
		coloredNodes.push(nodeObj);	
	}

	connectionGetMaxNumVIPList.query(expGetMaxNumVIPList.val(), succGetMaxNumVIPList);
}
/*
run task 4
*/
function runTask4() {
	if (logInStatus == false) {
		alert("Please login first!");
		return;
	}
	clearOutsideNodesSet();
	clearNodeEdgeColor();	
	queryDrawTaskFour();
}


$(document).ready(function(){

    initializeRender();

    initializeVariables();


    $("#logOut").hide();

    $('#accordion').accordion();

    $('#logInButton').click(function(){
    	logInUserId = $('#user_id').val();
    	$('form').submit();
    });

    $('#logOutButton').click(logOut);

    $('#iframeID1').load(checkAndLoadGraph);

    $("#runTask1").click(runTask1);


    $("#runTask4").click(runTask4);

    $("#runTask2").click(runTask2);

    $("#runTask3").click(runTask3);

    $("#runTask4").click(runTask4);


});