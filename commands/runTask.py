import os
import stat
# path to pregelix
## change here for your path
pregelixPath = "/Users/soushimei/Documents/pregelix/hyracks/pregelix/pregelix-dist/target/pregelix-dist-0.2.9-binary-assembly/"
# path to spanning tree results
outputPath = "/tmp/pregelix/results/"
# path to the graph
## change here for your path
graphPath = "/Users/soushimei/Documents/workspace/Pregelix_Social_Graph/WebUI/graphFile/adm/"
## change here for your path
projectJarPath = "/Users/soushimei/Documents/workspace/Pregelix_Social_Graph/target/project-0.2.9-jar-with-dependencies.jar"
# get ip script
getIp = "`bin/getip.sh`"
# port number
portNo = 13199
# source id
sourceId = 0
# number results for task 3
numberResults = 10
# number of vertexes
numVertices = 20
# iteration number
iterations = 10


def runTask(taskName, pregelixPath, projectJarPath, className, graphPath, outputPath, getIp, portNo, sourceId, numberResults, numVertices, iterations):
	script = "bin/pregelix " + projectJarPath + " " 
	script = script + className + " -inputpaths " + graphPath 
	script = script + " -outputpath " + outputPath + " -ip " + getIp 
	script = script + " -port " + str(portNo) + " -iterations " + str(iterations)
	script = script + " -vnum " + str(numVertices)
	script = script + " -source-vertex " + str(sourceId)
	script = script + " -results-num " + str(numberResults)
	fileName = taskName + ".sh"
	filePathName = pregelixPath + fileName
	file = open(filePathName, 'w')
	file.write(script)
	file.close()
	st = os.stat(filePathName)
	os.chmod(filePathName, st.st_mode | 0111)
	os.system(filePathName)