import os
import sys

neighborDict = {} #dictionary containing neighbors of each node
weightDict = {} #dictionary containing weights of edges
featureDict = {} #dictionary containing features of each node
featureDictTotal = {} #dictionay containing all listed features of each node
totalFeatureDict = {} #ditionary containing features of all nodes

# the path of data files
currPath = "../twitter"

# list all files
fileArray = os.listdir(currPath)


######## get totalFeature #############
for fileGraphName in fileArray:
    if fileGraphName.endswith('.featnames'): # if the file is the '*.featnames' file which lists all possible features of current node
        nodeNum = fileGraphName[0:len(fileGraphName)-10]; #get current node
        fileGraphName = os.path.join(currPath, fileGraphName); 
        fileGraph = open(fileGraphName, 'r');
        line = fileGraph.readline();
        featureArray = []
        while line:
            line = line.rstrip();
            lineArray = line.split(' ');
            # add each feature into dictionary
            if(not totalFeatureDict.has_key(lineArray[1])):
               length = len(totalFeatureDict);
               totalFeatureDict[lineArray[1]] = length;
            featureArray.append(lineArray[1]);
            line = fileGraph.readline();
        featureDictTotal[nodeNum]=featureArray;

######## get features ###############
for fileGraphName in fileArray:
    if fileGraphName.endswith('.egofeat'): # if the file is the '*.egofeat' file which lists the actual features of each node
        nodeNum = fileGraphName[0:len(fileGraphName)-8]; #get current node
        fileGraphName = os.path.join(currPath, fileGraphName);
        fileGraph = open(fileGraphName, 'r');
        line = fileGraph.readline();
        features = []
        while line:
            line = line.rstrip();
            lineArray = line.split(' ');
            for i in range(0, len(lineArray)):
                if(lineArray[i]=='1'): #'1' indicates that the node has the feature to which '1' corresponds
                    features.append(totalFeatureDict[featureDictTotal[nodeNum][i]]);
            line = fileGraph.readline();
        featureDict[nodeNum] = features;
        
######### get neighbors and weights #############
for fileGraphName in fileArray:
    if fileGraphName.endswith('.feat'): # if the file is the '*.feat' file which lists all the neighbors of each node and their features
        nodeNum = fileGraphName[0:len(fileGraphName)-5]; #get current node
        fileGraphName = os.path.join(currPath, fileGraphName)
        fileGraph = open(fileGraphName, 'r');
        line = fileGraph.readline();
        neighbor = []; # array to contain neighbors
        weights = []; #array to contain weights
        ## get node features ##
        fileNodeFeature = open(os.path.join(currPath, nodeNum+'.egofeat'), 'r');
        lineEgoFeature = fileNodeFeature.readline();
        lineEgoFeature = lineEgoFeature.rstrip();
        lineEgoFeatureArray = lineEgoFeature.split(' ');
        while line:
            line = line.rstrip();
            lineArray = line.split(' ');
            neighbor.append(lineArray[0]);
            weight = 0;
            for i in range(0, len(lineEgoFeatureArray)):
                if(lineArray[i+1]=='1' and lineEgoFeatureArray[i]=='1'):# if both a neighbor and current node have a same feature, weight increases by 1
                    weight+=1;
            weights.append(weight);
            line = fileGraph.readline();
        neighborDict[nodeNum] = neighbor;
        weightDict[nodeNum] = weights;

######### write to profile ################

### write feature and index num ####
fileName = 'featureIndex.txt'
fileOut = open(fileName, 'w');
for tag in totalFeatureDict.keys():
    fileOut.writelines(tag+' '+str(totalFeatureDict[tag])+'\n')
fileOut.close()

### write neightbors and weights ####
fileName = 'graph.txt'
fileOut = open(fileName, 'w');
for nodeNum in neighborDict.keys():
    line = nodeNum+' '+str(len(neighborDict[nodeNum]));
    for i in range(0, len(neighborDict[nodeNum])):
        line = line+' '+neighborDict[nodeNum][i];
        line = line+' '+str(weightDict[nodeNum][i]);
    line = line + ' ' + str(len(featureDict[nodeNum]));
    for feature in featureDict[nodeNum]:
        line = line + ' ' + str(feature);
    line = line+'\n';
    fileOut.writelines(line);
fileOut.close()
    



    

        
        


        
            
        
