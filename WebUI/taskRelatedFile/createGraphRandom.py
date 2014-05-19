# create a graph randomly


import os
import sys
import random
from sets import Set

nodeNum = 50;
maxNeighborNum = 15;

fileName = "graph_"+str(nodeNum)+"_"+str(maxNeighborNum)+".txt"
fileOut = open(fileName, 'w');

for i in range(0, nodeNum):
    neighborNum = random.randint(1, maxNeighborNum);
    neighborArray = [];
    for j in range(0, neighborNum): 
        nodeID = random.randint(0, nodeNum-1);
        if(nodeID!=j):
            neighborArray.append(nodeID);
    neighborSet = Set(neighborArray);
    line = str(i)+" "+str(len(neighborSet))+" ";
    for neighbor in neighborSet:
        line = line+str(neighbor)+" 0 ";
    line = line+"0"+"\n";
    fileOut.writelines(line);
fileOut.close();
    
