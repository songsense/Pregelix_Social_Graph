import os
import sys

currPath = "./"
fileName = "graph_50_15.txt"
fileOutName = "graph_50_15.adm"

######### open file ##########
fullFileName = os.path.join(currPath, fileName)
fileGraph = open(fullFileName, 'r');
fileContent = fileGraph.readlines();
fileGraph.close();

fullFileName = os.path.join(currPath, fileOutName);
fileOut = open(fullFileName, 'w');

######## deal with each tuple ###########
tupleNum = len(fileContent);
for i in range(0, tupleNum):
    line = fileContent[i];
    line = line.rstrip();
    lineArray = line.split(' ');
    lineOut = '{';
    lineOut = lineOut+'"source_node":'+lineArray[0]+',';
    lineTargetNode = "";
    lineWeight="";
    numTargetNode = int(lineArray[1]);
    for i in range(0, numTargetNode):
        lineTargetNode=lineTargetNode+lineArray[2*i+2]+',';
        lineWeight=lineWeight+lineArray[2*i+3]+',';
    lineOut=lineOut+'"target_node":{{'+lineTargetNode[0:len(lineTargetNode)-1]+'}}';
    lineOut = lineOut+',"weight":{{'+lineWeight[0:len(lineWeight)-1]+'}}';
    lineOut = lineOut+'}\n';
    fileOut.writelines(lineOut);
fileOut.close();
    
