<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <link href="static/js/jquery-ui/css/smoothness/jquery-ui-1.10.4.custom.css" rel="stylesheet">
  <script type="text/javascript" src="static/js/jquery-ui/js/jquery-1.10.2.js"></script>
  <script type="text/javascript" src="static/js/jquery-ui/js/jquery-ui-1.10.4.custom.js"></script>
  <script type="text/javascript" src="static/js/asterix-sdk-stable.js"></script>
  <script type="text/javascript" src="static/js/graphDisplayOffline.js"></script>
  <script type="text/javascript" src="static/js/arbor.js"></script>
  <script type="text/javascript" src="static/js/arbor-graphics.js"></script>
  <script type="text/javascript" src="static/js/arbor-tween.js"></script>
  <style>
    #main{
   // border-style:solid;
   // border-width:1px;
    width: 100%;
    height: 100%;
    //margin-left:2%;
    }
    
    #header{
    //border-style:solid;
    //border-width:1px;
    width: 101.5%;
    height: 80px;
    //margin-top: 2%;
    font-size: 30px;
    font-weight: bold;
    line-height: 36px;
    background-color: #EFEFEF;
    //border-radius:10px;
    margin-top: -10px;
    margin-left:-10px;
    margin-right: -10px;
    }

    #headerContent{
      margin-bottom: 10px;
      margin-top: 20px;
      margin-left: 7%;
      color: #17265a; 
      text-align: left;
      width: 100%;
    }
   

    #controlBlock{
    //border-style:solid;
    //border-width:1px;
    width: 29%;
    height: 80%;
    margin-top: 1.7%;
    margin-left: 2%;
    }
    
    #graphDisplayBlock{
    border-style:solid;
    border-radius: 10px;
    border-color: #55acee;
    //border-width:1px;
    width: 64%;
    height: 70%;
    margin-top: 1.7%;
    margin-left: 2%;
    box-shadow: 0px 0px 20px 3px #d3d3d3;
    
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
    
    #main{
    font-family: klavika-web, 'Helvetica Neue', Helvetica, Arial, Geneva, sans-serif;
    }
    
    
    .controlBlockTitle{
    font-size:18px; 
    font-weight:bold; 
    }

    .controlBlockContent{
    //margin-top:-15px; 
    font-weight:bold;
    color:#000033;
    }

    .task1Text{
    width: 60px;
    }

    #indexMenu{
      width: 98.4%;
      float: left;
      margin-top: 0px;
      margin-bottom: 5px;
      margin-left:-10px;
      margin-right: -10px;
      background-color: #CDCDCD;


    }

    .indexItem, #leftIndex{
      float:left;
      list-style-type:none;
      font-size: 18px;
      padding-top: 7px;
      padding-bottom: 7px;
      color: #000000;
      margin-right: 65px;
      font-weight: bold;

    }

    #leftIndex{
      margin-left: 15px;
    }

    #headerLine{
      float:left;
    }
    #logInOut{
      float:right;
    }

    .nodeText{
      margin-top: 2px;
      margin-bottom: 2px;
      font-size: 15px;
      height: 20px;
      color: #969696;
    }
    
    table{
      margin-top: 15px;
      margin-right: 34px;
    }

    #welcomeText{
      margin-top: 2px;
      margin-bottom: 2px;
      font-size: 15px;
      height: 20px;
      color: #969696;
      float:left;
    }

    #logOut{
      margin-top: 40px;
      margin-right: 150px;
    }

    #logOutButton{
      color: #7B7B7B;
      margin-top: 8px;
      margin-left: 10px;
      float: left;
    }

  </style>
  
  <title>Large Graph Analysis</title>
</head>
<body>
  <div id="main" style="float:left">
    <div id="header" style="float:left">
      <div id="headerLine">
        <p id="headerContent">Social Graph Analysis and Visualization</p>
      </div>
      <div id="logInOut" >
          
          <table id="logIn">
            <tr>
              <td><p class="nodeText">User ID</p></td>
              <td><p class="nodeText">Password</p><td>
            </tr>
            <tr>
              <form action="/logIn" method="POST" target="iframeLogIn">
              <td>
                  <input type="text" name="user_id" id="user_id" style="width: 150px"></input>
              </td>
              <td>
                <input type="password" name="password" id="password" style="width: 150px"></input>
              </td>
              </form>
              <td>
                <input type="submit" id="logInButton" value="Log In" style="color:#7B7B7B"></input>
              </td>
            </tr>
          </table>
          <div id="logOut" style="float:left">
            <p id="welcomeText"> Welcome</p>
            <input type="submit" id="logOutButton" value="Log Out" ></input>
          </div>
          
      </div>

    </div>
  

    <ul id="indexMenu">
      <li class="indexItems" id="leftIndex">
        <a href="#" style="text-decoration:none">Introduction</a>
      </li>
      <li class="indexItem">
        <a href="#" style="text-decoration:none">Pregelix</a>
      </li>
      <li class="indexItem">
        <a href="#" style="text-decoration:none">AsterixDB</a>
      </li>
      <li class="indexItem">
        <a href="#" style="text-decoration:none">Contact</a>
      </li>
    </ul>
    <div id="graphDisplayBlock" style="float:left">
      <p id="graphDiplayStatus" style="color:#55acee; font-size: 20px; margin-left:10px; margin-bottom:5px">Graph Display</p>
      <canvas id="graph" width="800" height="640"></canvas>
      <div id="tips">
      </div>
    </div>
    <div id="controlBlock" style="float:left">

      <div id = "accordion">
        <h3 class="controlBlockTitle">Connection Tracing</h3>
        <div>
          <p class="controlBlockContent">Show Your Connection!</p>
          <span style="margin-left:5px">Target Node ID:</span>
          <input id="target_id" class="task1Text" name="target_id" type="text"/>
          </br>
          <button class="button" id="runTask1" type="submit">Run</button>
        </div>
        <h3 class="controlBlockTitle">Community Display</h3>
        <div>
          <p class="controlBlockContent">Show Your Community!</p>
          <span style="margin-left:5px">Name:</span>
          <input id="task2_node" class="task1Text" name="task2_node" type="text"/>
          </br>
          <span style="margin-left:5px">Number Of Iteration:</span>
          <input id="task2_num_iteration" class="task1Text" name="task2_num_iteration" type="text"/>
          </br>
          <button class="button" id="runTask2" type="submit">Run</button>
        </div>
        <h3 class="controlBlockTitle">Friend Recommendation</h3>
        <div><p class="controlBlockContent">Show Your Potential Friends!</p>
          <span style="margin-left:5px">Name:</span>
          <input id="task3_node" class="task1Text" name="task3_node" type="text"/>
          </br>
          <span style="margin-left:5px">Number Of Friends:</span>
          <input id="task3_num_friends" class="task1Text" name="task3_num_friends" type="text"/>
          </br>
          <span style="margin-left:5px">Number Of Iteration:</span>
          <input id="task3_num_iteration" class="task1Text" name="task3_num_iteration" type="text"/>
          </br>
          <button class="button" id="runTask3" type="submit">Run</button>
        </div>
      </div>
    </div>
  </div>
  <iframe name="iframeLogIn" id="iframeID1" src="" width="0" height="0" frameborder="0" />
</body>
</html>
