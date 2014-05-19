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


/*----- use above functions to draw graph ---*/





/*---- global variable ----*/
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


//var nodeIDStr = "";

function clearColoredNodesAndEdges(){
	coloredNodes = [];
	coloredEdges = [];
	labelNodeIDTable = {};
}


function initializeRender(){
	sys = arbor.ParticleSystem(3500, 50, 0.5);
    sys.parameters({gravity:true});
    sys.renderer = Renderer('#graph');
}


function initializeCommunication(){


    var queryInitial = 'use dataverse Communication; delete $node from dataset Protocol; insert into dataset Protocol({"id":0,"load_graph": 0,"task1_status":0,"task2_status":0,"task3_status":0,"graph_file_path":"","number_of_iterations":-1,"source_id":-1,"target_id":-1,"number_of_results":-1});';
    var xmlhttp2;
    if(window.XMLHttpRequest){
		xmlhttp2 = new XMLHttpRequest();
    }
    else{
		xmlhttp2 = new ActiveXObject("Microsoft.XMLHTTP");
    }
    
    xmlhttp2.open("GET", "http://localhost:19002/update?statements="+queryInitial);
    xmlhttp2.send();
}

function uploadFile(){
	$('form').submit();
}

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

function drawGraph(dom, res){
    //var sys = arbor.ParticleSystem(100, 50, 0.1);
    /*
    sys = arbor.ParticleSystem(3500, 50, 0.5);
    sys.parameters({gravity:true});
    sys.renderer = Renderer(dom);
    */
    
    deleteAllNodesAndEdges();
    var nodeSet = {};
    for(i in res){
	//alert(res[i]);
		var resJson = eval('('+res[i]+')');
		var sourceNode = resJson.source_node.int32.toString();
		var targetNodeArray = resJson.target_node.unorderedlist;
		
		
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
    
    $('#tips').html("");
    

    var A = new AsterixDBConnection().dataverse("OriginalGraph");
    var expression0a = new FLWOGRExpression()
	.ForClause("$node", new AExpression("dataset Graph"))
	.ReturnClause("$node");
    
    var success = function(res){
		drawGraph('#graph', res["results"]);
		var connector = new AsterixDBConnection().dataverse("Communication");
		var expressionGetProtocol = new FLWOGRExpression()
	    .ForClause("$node", new AExpression("dataset Protocol"))
	    .ReturnClause("$node");
		var successGetProtocol = function(tempres){
		    var res=tempres["results"];
		    for(i in res){
			var resJson = eval('('+res[i]+')');
			var task1_status = resJson.task1_status.int32.toString();
			var task2_status = resJson.task2_status.int32.toString();
			var task3_status = resJson.task3_status.int32.toString();
			var number_of_iterations = resJson.number_of_iterations.int32.toString();
			var source_id = resJson.source_id.int32.toString();
			var target_id = resJson.target_id.int32.toString();
			var number_of_results = resJson.number_of_results.int32.toString();
			var load_graph = "2";
			//var graph_file_path = pathGraph.substr(0, pathGraph.length-4)+"txt/"+fileName.substr(0, fileName.length-3)+"txt";
			var graph_file_path = pathGraph+'/'+fileName;
			//alert(graph_file_path);
			var querySetFlag = 'use dataverse Communication; delete $node from dataset Protocol; insert into dataset Protocol({"id":0,"load_graph":'+load_graph+',"task1_status":'+task1_status+',"task2_status":'+task2_status+',"task3_status":'+task3_status+',"graph_file_path":"'+graph_file_path+'", "number_of_iterations":'+number_of_iterations+',"source_id":'+source_id+',"target_id":'+target_id+',"number_of_results":'+number_of_results+'});';
			//alert(querySetFlag);
			
			var xmlhttp2;
			if(window.XMLHttpRequest){
			    xmlhttp2 = new XMLHttpRequest();
			}
			else{
			    xmlhttp2 = new ActiveXObject("Microsoft.XMLHTTP");
			}
			
			xmlhttp2.open("GET", "http://localhost:19002/update?statements="+querySetFlag);
			xmlhttp2.send();
		    }
		}
		connector.query(expressionGetProtocol.val(), successGetProtocol);
    }
    A.query(expression0a.val(),  success);
}



function loadGraphFirstTime(){

	//$('#iframeID1').html("");

	//alert("load");

	clearColoredNodesAndEdges();

	initializeCommunication();

    fileName = $("#filePath").val().replace(/^.*[\\\/]/, '');

    var queryCreate = 'drop dataverse OriginalGraph if exists; create dataverse OriginalGraph; use dataverse OriginalGraph; create type GraphType as open{source_node: int32, label: string, target_node:{{int32}}, weight:{{double}}} \n create dataset Graph(GraphType) primary key source_node;';

    var queryUpdate = 'use dataverse OriginalGraph; load dataset Graph using localfs(("path"="localhost://'+pathGraph+fileName+'"),("format"="adm"));';
    
    //alert(queryCreate);
    //alert(queryUpdate);
    var xmlhttp;
    if(window.XMLHttpRequest){
		xmlhttp = new XMLHttpRequest();
    }
    else{
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
    }
    //xmlhttp = new XDomainRequest();
    //alert(queryUpdate);
    xmlhttp.open("GET","http://localhost:19002/ddl?ddl="+queryCreate);
    xmlhttp.send();
    
    xmlhttp.onreadystatechange=function(){
		var xmlhttp1;
		if(window.XMLHttpRequest){
		    xmlhttp1 = new XMLHttpRequest();
		}
		else{
		    xmlhttp1 = new ActiveXObject("Microsoft.XMLHTTP");
		}
		//alert(queryCreate);
		xmlhttp1.open("GET", "http://localhost:19002/update?statements="+queryUpdate);
		xmlhttp1.send();
		xmlhttp1.onreadystatechange=function(){
		    loadGraph();
		}
    }
}

function drawGraphTask1(target_id){
    var connTask1 = new AsterixDBConnection().dataverse("Tasks");
    var whereClauseStr = '$node.target_node='+target_id;
    //alert(whereClauseStr);
    var expTask1 = new FLWOGRExpression()
	.ForClause("$node", new AExpression("dataset TaskOne"))
	.WhereClause(new AExpression(whereClauseStr))
	.ReturnClause("$node");
    var succTask1 = function(tempres){
        var res = tempres["results"];
        for(i in res){
            var resJson = eval('('+res[i]+')');
            var path = resJson.path.orderedlist;
            //draw graph
            //alert("start:"+path[0].int32.toString());
            sys.getNode(path[0].int32.toString()).data.color="#FF0000";
            sys.getNode(path[path.length-1].int32.toString()).data.color="#FF0000";
	    	coloredNodes.push(sys.getNode(path[0].int32.toString()));
	    	coloredNodes.push(sys.getNode(path[path.length-1].int32.toString()));
            for(var j=1; j<path.length-1; ++j){
                if(j!=1){
                    sys.getNode(path[j].int32.toString()).data.color="#CA7A2C";
		    		coloredNodes.push(sys.getNode(path[j].int32.toString()));
				}
            	var sourceNodeObj = sys.getNode(path[j].int32.toString());
            	var targetNodeObj = sys.getNode(path[j+1].int32.toString());
            	var edgeArray = sys.getEdges(sourceNodeObj, targetNodeObj);
            	sys.pruneEdge(edgeArray[0]);
            	sys.addEdge(path[j].int32.toString(), path[j+1].int32.toString(), {directed:false, color:"#FF0000"});
				edgeArray = sys.getEdges(sourceNodeObj, targetNodeObj);
				coloredEdges.push(edgeArray[0]);
           
            }
        }
    }
    connTask1.query(expTask1.val(), succTask1);
}

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

function runTask1(){
    
    clearNodeEdgeColor();
    var source_label = $('#source_id').val().toString();
    var target_label = $('#target_id').val().toString();
    if(!(source_label in labelNodeIDTable && target_label in labelNodeIDTable)){
    	alert("No such source or target node!");
    	return;
    }
    var source_node = labelNodeIDTable[source_label];
    var target_node = labelNodeIDTable[target_label];
    
    $('#tips').html("");
    
    var connector = new AsterixDBConnection().dataverse("Communication");
    var expressionGetProtocol = new FLWOGRExpression()
	.ForClause("$node", new AExpression("dataset Protocol"))
	.ReturnClause("$node");
    //get and set flag
    var successGetProtocol = function(tempres){
		var res = tempres["results"];
		for(i in res){
		    var resJson = eval('('+res[i]+')');
		    var load_graph = resJson.load_graph.int32.toString();
		    var task2_status = resJson.task2_status.int32.toString();
		    var task3_status = resJson.task3_status.int32.toString();
		    var number_of_iterations = resJson.number_of_iterations.int32.toString();
		    //var graph_file_path = resJson.graph_file_path;
	            var graph_file_path = pathGraph.substr(0, pathGraph.length-4)+"txt/"+fileName.substr(0, fileName.length-3)+"txt";
		    var number_of_results = resJson.number_of_results.int32.toString();
		    
		    var task1_status = resJson.task1_status.int32.toString();
		    var source_id = source_node;
		    var target_id = target_node;
	        if(task1_status=="0" || preSourceNode!=source_id){
				task1_status="1";
				preSourceNode = source_id;
		        var querySetFlag = 'use dataverse Communication; delete $node from dataset Protocol; insert into dataset Protocol({"id":0, "load_graph":'+load_graph+',"task1_status":'+task1_status+',"task2_status":'+task2_status+',"task3_status":'+task3_status+',"graph_file_path":"'+graph_file_path+'", "number_of_iterations":'+number_of_iterations+',"source_id":'+source_id+',"target_id":'+target_id+',"number_of_results":'+number_of_results+'});';
		        var xmlhttp3;
		        if(window.XMLHttpRequest){
			    	xmlhttp3 = new XMLHttpRequest();
		        }
		        else{
			    	xmlhttp3 = new ActiveXObject("Microsoft.XMLHTTP");
		        }
			
		        xmlhttp3.open("GET", "http://localhost:19002/update?statements="+querySetFlag);
		        xmlhttp3.send();
		        xmlhttp3.onreadystatechange=function(){

				    var c = new AsterixDBConnection().dataverse("Communication");
				    var e = new FLWOGRExpression()
				        .ForClause("$node", new AExpression("dataset Protocol"))
				        .ReturnClause("$node");
			    	var s = function(tempres){
			        	var res=tempres["results"];
			        	for(i in res){
					    	var resJson = eval('('+res[i]+')');
					    	//alert(resJson.task1_status.int32);
					    	if(resJson.task1_status.int32!=2){
					        	if(resJson.task1_status.int32==1)
					            	$('#graphDiplayStatus').html('Graph Display: <span style="color:#DB4D6D">Processing!</span>');
					        	setTimeout(function(){ c.query(e.val(), s);}, timeOut);
					    	}
					    	else{
								$('#graphDiplayStatus').html('Graph Display');
					        	drawGraphTask1(target_id);	
					    	}
			        	}
			    	}
			    	c.query(e.val(), s);
		        }
	        }
	        else if(task1_status=="2"){
				drawGraphTask1(target_id);
	        }    
		    
		}
    }
    connector.query(expressionGetProtocol.val(), successGetProtocol);
}



function drawGraphTask2(nodeID){
    //get the commit_id
    var whereClauseStr = "$node.node_id="+nodeID;
    //alert(whereClauseStr);
    var connectorGetCommID = new AsterixDBConnection().dataverse("Tasks");
    var expressionGetCommID = new FLWOGRExpression()
	.ForClause("$node", new AExpression("dataset TaskTwo"))
	.WhereClause(new AExpression(whereClauseStr))
	.ReturnClause("$node");
    var successGetCommID = function(resCommIDTemp){
        var resCommID = resCommIDTemp["results"];
        for(j in resCommID){
            var resJson = eval('('+resCommID[j]+')');
            var commID = resJson.community_id.int32.toString();
            var commIDNum = resJson.community_id.int32;
            var whereClauseStr = "$node.community_id="+commID;
            var connectorGetNode = new AsterixDBConnection().dataverse("Tasks");
            var expressionGetNode = new FLWOGRExpression()
			.ForClause("$node", new AExpression("dataset TaskTwo"))
			.WhereClause(new AExpression(whereClauseStr))
			.ReturnClause("$node");
            var successGetNode = function(resNodeTemp){
                var resNode = resNodeTemp["results"];
                //draw graph
                sys.addNode("-1", {label:"-1", color:"#FFFFFF"});
                sys.addNode("-2", {label:"-2", color:"#FFFFFF"});
                for(m in resNode){
                    var resJson = eval('('+resNode[m]+')');
                    var nodeStr = resJson.node_id.int32.toString();
                    //alert(nodeStr);
                    sys.getNode(nodeStr).data.color=colorArray[commIDNum%numColor];
		    		coloredNodes.push(sys.getNode(nodeStr));
                    // renew graph
                    sys.addEdge("-1", "-2", {directed:false, color:"#FFFFFF"})
                    var edgeArray = sys.getEdges("-1","-2");
                    sys.pruneEdge(edgeArray[0]);
                }
                //delete temp node
                var tempNode = sys.getNode("-1");
                sys.pruneNode(tempNode);
                tempNode = sys.getNode("-2");
                sys.pruneNode(tempNode);
            }
            connectorGetNode.query(expressionGetNode.val(), successGetNode);
        }
    }
    connectorGetCommID.query(expressionGetCommID.val(), successGetCommID);
}

function runTask2(){

    //set flag
    //alert("run task2");
    clearNodeEdgeColor();
    var nodeLabel = $('#task2_node').val().toString();
    if(!(nodeLabel in labelNodeIDTable)){
    	alert("No such node!");
    	return;
    }
    var nodeID = labelNodeIDTable[nodeLabel];
    var numIterStr = $('#task2_num_iteration').val().toString();
    $('#tips').html("");
    
    var connector = new AsterixDBConnection().dataverse("Communication");
    var expressionGetProtocol = new FLWOGRExpression()
	.ForClause("$node", new AExpression("dataset Protocol"))
	.ReturnClause("$node");
    var successGetProtocol = function(resProtocolTemp){
	//alert("OK");
		var resProtocol = resProtocolTemp["results"];
		for(k in resProtocol){
		    //get value of Protocol
		    var resJson = eval('('+resProtocol[k]+')');
		    var load_graph = resJson.load_graph.int32.toString();
		    var task1_status = resJson.task1_status.int32.toString();
		    var task2_status = resJson.task2_status.int32.toString();
		    var task3_status = resJson.task3_status.int32.toString();
		    var number_of_iterations = numIterStr;
		    //var graph_file_path = resJson.graph_file_path;
            var graph_file_path = pathGraph.substr(0, pathGraph.length-4)+"txt/"+fileName.substr(0, fileName.length-3)+"txt";
	    	var source_id = resJson.source_id.int32.toString();
	    	var target_id = resJson.target_id.int32.toString();
	    	var number_of_results = resJson.number_of_results.int32.toString();
            
        	if(task2_status=="0"){
				task2_status="1";
		        var querySetFlag = 'use dataverse Communication; delete $node from dataset Protocol; insert into dataset Protocol({"id":0, "load_graph":'+load_graph+',"task1_status":'+task1_status+',"task2_status":'+task2_status+',"task3_status":'+task3_status+',"graph_file_path":"'+graph_file_path+'", "number_of_iterations":'+number_of_iterations+',"source_id":'+source_id+',"target_id":'+target_id+',"number_of_results":'+number_of_results+'});';
			
		        var xmlhttp3;
		        if(window.XMLHttpRequest){
			    	xmlhttp3 = new XMLHttpRequest();
		        }
	        	else{
		    		xmlhttp3 = new ActiveXObject("Microsoft.XMLHTTP");
	        	}
	        	xmlhttp3.open("GET", "http://localhost:19002/update?statements="+querySetFlag);
	        	xmlhttp3.send();

	        	xmlhttp3.onreadystatechange=function(){
		    	//alert("change");
			    	var c = new AsterixDBConnection().dataverse("Communication");
			    	var e = new FLWOGRExpression()
			        .ForClause("$node", new AExpression("dataset Protocol"))
			        .ReturnClause("$node");
			    	var s = function(resTemp){
			        	var res=resTemp["results"];
			        	for(i in res){
				    		var resJson = eval('('+res[i]+')');
				    		//alert(resJson.task2_status.int32);
				    		if(resJson.task2_status.int32!=2){
				        		$('#graphDiplayStatus').html('Graph Display: <span style="color:#DB4D6D">Processing!</span>');
				        		setTimeout(function(){ c.query(e.val(), s);}, timeOut);
				    		}
				    		else{
				        		$('#graphDiplayStatus').html('Graph Display');
				        		drawGraphTask2(nodeID);
				    		}
			        	}
			    	}
		    		c.query(e.val(), s);			    
	        	}
	    	}
        	else if(task2_status=="2"){
				drawGraphTask2(nodeID);
        	}
		}
    }
    connector.query(expressionGetProtocol.val(), successGetProtocol);
}

function drawGraphTask3(nodeID, inputFriendNum){
    var connTask3 = new AsterixDBConnection().dataverse("Tasks");
    var whereClauseStr = '$node.node_id='+nodeID;
    //alert(whereClauseStr);
    var expTask3 = new FLWOGRExpression()
	.ForClause("$node", new AExpression("dataset TaskThree"))
	.WhereClause(new AExpression(whereClauseStr))
    //.WhereClause(new AExpression("$node.target_node = 3"))
	.ReturnClause("$node");
    var succTask3 = function(tempres){
        var res = tempres["results"];
        sys.addNode("-1", {label:"-1", color:"#FFFFFF"});
        sys.addNode("-2", {label:"-2", color:"#FFFFFF"});
        for(i in res){
            //alert("draw");
            var resJson = eval('('+res[i]+')');
            var suggestedFriends = resJson.suggested_friends.orderedlist;
            var friendNum = suggestedFriends.length;
            if(friendNum==0){
                $('#tips').html('<p style="font-size: 30px; text-align:center">No suggested friends!</p>')
            }
            else{
                //draw graph
                sys.getNode(nodeID).data.color=colorArray[0];
				coloredNodes.push(sys.getNode(nodeID));
                var minFriendNum = (friendNum>inputFriendNum?inputFriendNum:friendNum);
                for(var k=0; k<minFriendNum; ++k){
                    sys.getNode(suggestedFriends[k].int32.toString()).data.color=colorArray[1];
		    		coloredNodes.push(sys.getNode(suggestedFriends[k].int32.toString()));
                }
                sys.addEdge("-1", "-2", {directed:false, color:"#FFFFFF"})
                var edgeArray = sys.getEdges("-1","-2");
                sys.pruneEdge(edgeArray[0]);
            }   
        }
        //delete temp node
        var tempNode = sys.getNode("-1");
        sys.pruneNode(tempNode);
        tempNode = sys.getNode("-2");
        sys.pruneNode(tempNode);
    }
    connTask3.query(expTask3.val(), succTask3);
}

function runTask3(){
    clearNodeEdgeColor();
    var nodeLabel = $('#task3_node').val().toString();
    if(!(nodeLabel in labelNodeIDTable)){
    	alert("No such node!");
    	return;
    }
    var nodeID = labelNodeIDTable[nodeLabel];
    var numFriendsStr = $('#task3_num_friends').val().toString();
    var inputFriendNum = $('#task3_num_friends').val();
    var numIterStr = $('#task3_num_iteration').val().toString();
    $('#tips').html("");
    var connector = new AsterixDBConnection().dataverse("Communication");
    var expressionGetProtocol = new FLWOGRExpression()
	.ForClause("$node", new AExpression("dataset Protocol"))
	.ReturnClause("$node");
    //get and set flag
    var successGetProtocol = function(tempres){
		var res = tempres["results"];
		for(i in res){
		    var resJson = eval('('+res[i]+')');
		    var load_graph = resJson.load_graph.int32.toString();
		    var task1_status = resJson.task1_status.int32.toString();
		    var task2_status = resJson.task2_status.int32.toString();
		    var task3_status = resJson.task3_status.int32.toString();
		    var number_of_iterations = numIterStr;
		    //var graph_file_path = resJson.graph_file_path;
	            var graph_file_path = pathGraph.substr(0, pathGraph.length-4)+"txt/"+fileName.substr(0, fileName.length-3)+"txt";
		    var source_id = resJson.source_id.int32.toString();
		    var target_id = resJson.target_id.int32.toString();
		    var number_of_results = numFriendsStr;
	            
	        if(task3_status=="0" || preIterNum!=number_of_iterations){
	        	preIterNum = number_of_iterations;
				task3_status="1";
		        var querySetFlag = 'use dataverse Communication; delete $node from dataset Protocol; insert into dataset Protocol({"id":0, "load_graph":'+load_graph+',"task1_status":'+task1_status+',"task2_status":'+task2_status+',"task3_status":'+task3_status+',"graph_file_path":"'+graph_file_path+'", "number_of_iterations":'+number_of_iterations+',"source_id":'+source_id+',"target_id":'+target_id+',"number_of_results":'+number_of_results+'});';
			
		        var xmlhttp3;
		    	if(window.XMLHttpRequest){
			    	xmlhttp3 = new XMLHttpRequest();
		        }
		        else{
			    	xmlhttp3 = new ActiveXObject("Microsoft.XMLHTTP");
		        }
			
		        xmlhttp3.open("GET", "http://localhost:19002/update?statements="+querySetFlag);
		        xmlhttp3.send();
		        xmlhttp3.onreadystatechange=function(){
			    	var c = new AsterixDBConnection().dataverse("Communication");
			    	var e = new FLWOGRExpression()
			        .ForClause("$node", new AExpression("dataset Protocol"))
			        .ReturnClause("$node");
			    	var s = function(tempres){
			        	var res=tempres["results"];
			        	for(i in res){
					    	var resJson = eval('('+res[i]+')');
					    	//alert(resJson.task3_status.int32);
				    		if(resJson.task3_status.int32!=2){
				       			$('#graphDiplayStatus').html('Graph Display: <span style="color:#DB4D6D">Processing!</span>');
				        		setTimeout(function(){ c.query(e.val(), s);}, timeOut);
				    		}
				    		else{
				        		$('#graphDiplayStatus').html('Graph Display');
				        		drawGraphTask3(nodeID, inputFriendNum);	
				    		}
			        	}
			    	}
			    	c.query(e.val(), s);
		        }
	        }
	        else if(task3_status=="2"){
				drawGraphTask3(nodeID, inputFriendNum);
	        }
		}
    }
    connector.query(expressionGetProtocol.val(), successGetProtocol);
}


$(document).ready(function(){

    initializeRender();

    $("#filePath").change(uploadFile);

    $("#iframeID1").load(loadGraphFirstTime);

    $("#runTask1").click(runTask1);

    $("#runTask2").click(runTask2);

    $('#runTask3').click(runTask3);

    $('#accordion').accordion();

    
});
