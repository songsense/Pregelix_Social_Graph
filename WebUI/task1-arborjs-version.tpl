<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <script type="text/javascript" src="static/js/jquery.min.js"></script>
  <script type="text/javascript" src="static/js/asterix-sdk-stable.js"></script>
  <script type="text/javascript" src="static/js/asterix_task1.js"></script>
  <script type="text/javascript" src="static/js/arbor.js"></script>
  <script type="text/javascript" src="static/js/arbor-graphics.js"></script>
  <script type="text/javascript" src="static/js/arbor-tween.js"></script>
  <style>
    #main{
   // border-style:solid;
   // border-width:1px;
    width: 100%;
    height: 100%;
    margin-left:2%;
    }
    
    #header{
    //border-style:solid;
    //border-width:1px;
    width: 95%;
    height: 20%;
    margin-top: 2%;
    font-size: 30px;
    font-weight: bold;
    line-height: 36px;
    text-align: center;
    background-color: #DCDCDC;
    border-radius:10px;
    }

   

    #controlBlock{
    //border-style:solid;
    //border-width:1px;
    width: 29%;
    height: 80%;
    margin-top: 2%;
    margin-left: 2%;
    }
    
    #graphDisplayBlock{
    border-style:solid;
    border-radius: 10px;
    border-color: #55acee;
    //border-width:1px;
    width: 64%;
    height: 70%;
    margin-top: 2%;
    
    }

    #submitGraph{
    border-style:solid;
    border-width:1px;
    width: 25%;
    }
    
    .taskBlock{
    border-radius: 5px;
    border-style: solid;
    border-color: #C0C0C0;
    padding: 10px;
    margin-bottom: 10px;
    }
    /*
    .button{
    width: 90px;
    height: 40px;
    font-size: 15px;
    }*/
    
    .controlBlockTitle{
    font-size:18px; 
    font-weight:bold; 
    }

    .controlBlockContent{
    margin-top:-15px; 
    font-weight:bold;
    color:#000033;
    }

    .task1Text{
    width: 60px;
    }
  </style>
  
  <title>Large Graph Analysis</title>
</head>
<body>
  <div id="main" style="float:left">
    <div id="header" style="float:left">
      <p>Graph Analysis and Visualization Based on Pregelix and AsterixDB</p>
    </div>
    <div id="graphDisplayBlock" style="float:left">
      <p id="graphDiplayStatus" style="color:#55acee; font-size: 20px; margin-left:10px; margin-bottom:5px">Graph Display</p>
      <canvas id="graph" width="800" height="640"></canvas>
      <div id="tips">
      </div>
    </div>
    <div id="controlBlock" style="float:left">
      <div id="loadGraph" class="taskBlock">
	<p class="controlBlockTitle">Load Graph:</p>
	<p style="margin-bottom:3px">Choose your graph file:</p>
	<input id="filePath" type="file" name="filePath"/>
      </div>
      <div id="task1" class="taskBlock">
	<p class="controlBlockTitle">Task 1: Connection Strength</p>
	<p class="controlBlockContent">Shortest Distance Over Weighted Paths</p>
	<span style="margin-left:5px">Source Name:</span>
	<input id="source_id" class="task1Text" name="source_id" type="text"/>
	</br>
	<span style="margin-left:5px">Target Name:</span>
	<input id="target_id" class="task1Text" name="target_id" type="text"/>
	</br>
	<button class="button" id="runTask1" type="submit">Run Task1</button>
	<button class="reloadButton" type="submit">Reload Graph</button>
      </div>
      <div id="task2" class="taskBlock">
	<p class="controlBlockTitle">Task 2: Group Recommendation</p>
	<p class="controlBlockContent">Different Communities in Graph</p>
	<span style="margin-left:5px">Name:</span>
	<input id="task2_node" class="task1Text" name="task2_node" type="text"/>
	</br>
	<span style="margin-left:5px">Number Of Iteration:</span>
	<input id="task2_num_iteration" class="task1Text" name="task2_num_iteration" type="text"/>
	</br>
	<button class="button" id="runTask2" type="submit">Run Task2</button>
	<button class="reloadButton" type="submit">Reload Graph</button>
      </div>
      <div id="task3" class="taskBlock">
	<p class="controlBlockTitle">Task 3: Friend Recommendation</p>
	<p class="controlBlockContent">Socialization Suggestion</p>
	<span style="margin-left:5px">Name:</span>
	<input id="task3_node" class="task1Text" name="task3_node" type="text"/>
	</br>
	<span style="margin-left:5px">Number Of Recommended Friends:</span>
	<input id="task3_num_friends" class="task1Text" name="task3_num_friends" type="text"/>
	</br>
	<span style="margin-left:5px">Number Of Iteration:</span>
	<input id="task3_num_iteration" class="task1Text" name="task3_num_iteration" type="text"/>
	</br>
	<button class="button" id="runTask3" type="submit">Run Task3</button>
	<button class="reloadButton" type="submit">Reload Graph</button>
      </div>
    </div>
  </div>
</body>
</html>
