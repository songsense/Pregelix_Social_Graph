<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <script type="text/javascript" src="static/js/jquery.min.js"></script>
  <script type="text/javascript" src="static/js/asterix-sdk-stable.js"></script>
  <script type="text/javascript" src="static/js/asterix_task1.js"></script>
  <script type="text/javascript" src="static/js/arbor.js"></script>
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
  </style>
  
  <title>Large Graph Analysis</title>
</head>
<body>
  <div id="main" style="float:left">
    <div id="header" style="float:left">
      <p>Twitter Graph Analysis Based On Pregelix</p>
    </div>
    <div id="graphDisplayBlock" style="float:left">
      <p style="color:#55acee; font-size: 20px; margin-left:10px; margin-bottom:5px">Tweeter Graph</p>
      <canvas id="graph" width="800" height="500"></canvas>
    </div>
    <div id="controlBlock" style="float:left">
      <div id="task1" class="taskBlock">
	<p style="font-size:18px; font-weight:bold">Task 1:</p>
	<p style="margin-top:-15px; font-weight:bold">Shortest Distance Over Frequent Communication Paths</p>
	<button class="button" id="runTask1" type="submit">Run Task1</button>
      </div>
      <div id="task2" class="taskBlock">
	<p style="font-size:18px; font-weight:bold">Task 2:</p>
	<p style="margin-top:-15px; font-weight:bold">Interests with Large Communities</p>
	<button class="button" id="runTask2" type="submit">Run Task2</button>
      </div>
      <div id="task3" class="taskBlock">
	<p style="font-size:18px; font-weight:bold">Task 3:</p>
	<p style="margin-top:-15px; font-weight:bold">Socialization Suggestion</p>
	<button class="button" id="runTask3" type="submit">Run Task3</button>
      </div>
    </div>
  </div>
</body>
</html>
