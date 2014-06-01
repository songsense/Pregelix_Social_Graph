<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <link href="static/js/jquery-ui/css/smoothness/jquery-ui-1.10.4.custom.css" rel="stylesheet">
  <script type="text/javascript" src="static/js/jquery-ui/js/jquery-1.10.2.js"></script>
  <script type="text/javascript" src="static/js/jquery-ui/js/jquery-ui-1.10.4.custom.js"></script>
  <script>
    $(document).ready(function(){
      $(function() {
        $( "#tabs" ).tabs();
      });
    });
  </script>
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

    a:link    {color:blue;}
    a:visited {color:blue;}

    #leftIndex{
      margin-left: 15px;
    }

    #headerLine{
      float:left;
    }

    .controlBlockTitle{
      font-size:18px; 
      font-weight:bold;
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

    #tabs{
      margin-top: 125px;
    }

  </style>
  
  <title>Large Graph Analysis</title>
</head>
<body>
  <div id="main" style="float:left">
    <div id="header" style="float:left">
      <div id="headerLine">
        <a href="/" style="text-decoration:none"><p id="headerContent">Social Graph Analysis and Visualization</p></a>
      </div>
    </div>
  

    <ul id="indexMenu">
      <li class="indexItems" id="leftIndex">
        <a href="/intro" style="text-decoration:none" target="_blank">Introduction</a>
      </li>
      <li class="indexItem">
        <a href="http://pregelix.ics.uci.edu/" style="text-decoration:none" target="_blank">Pregelix</a>
      </li>
      <li class="indexItem">
        <a href="http://asterixdb.ics.uci.edu/" style="text-decoration:none" target="_blank">AsterixDB</a>
      </li>
      <li class="indexItem">
        <a href="/contact" style="text-decoration:none" target="_blank">Contact</a>
      </li>
    </ul>

    <div id="tabs">
      <ul>
        <li><a href="#tabs-1" class="controlBlockTitle" style="color: #17265a">Introduction</a></li>
        <li><a href="#tabs-2" class="controlBlockTitle" style="color: #17265a">Connection Tracing</a></li>
        <li><a href="#tabs-3" class="controlBlockTitle" style="color: #17265a">Community Display</a></li>
        <li><a href="#tabs-4" class="controlBlockTitle" style="color: #17265a">Friend Recommendation</a></li>
        <li><a href="#tabs-5" class="controlBlockTitle" style="color: #17265a">VIP Recommendation</a></li>
      </ul>
      <div id="tabs-1">
        <p> This is a general introduction for this project</p>
      </div>
      <div id="tabs-2">
        <p> This is a introduction for connection tracing</p>
      </div>
      <div id="tabs-3">
        <p> This is a introduction for community display</P>
      </div>
      <div id="tabs-4">
        <p> This is a introduction for friend recommendation</p>
      </div>
      <div id="tabs-5">
        <p> This is a introduction for vip recommendation</P>
      </div>
    </div>
  </div>
</body>
</html>
