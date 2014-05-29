# usage:
# change the following path variables
# (too long to consider them as input of the script)
# 1. pregelixPath: the path to the pregelix platform
# 2. graphPath: the path to the adm graph file
# 3. projectJarPath" the path to the project jar

# The following variables need to be configured according to the graph
# sourceId: source ID for task 1 and sub graph display;
# numberResuls: number of results for task 3;
# numVertices: number of vertices in the graph for task 4 (self configuration);
# iterations: number of iterations to be run for task 2, 3 and sub graph display.

import runTask
import bootstrap
import task4
## change here for your path
pregelixPath = "/Users/liqiangw/Documents/workspace/twitter_graph/hyracks/pregelix/pregelix-dist/target/pregelix-dist-0.2.9-binary-assembly/"
# path to the graph
## change here for your path
graphPath = "/Volumes/TOSHIBA/BigGraphData/10k/adm/"
## change here for your path
projectJarPath = "/Users/liqiangw/Documents/workspace/Pregelix_Social_Graph/target/project-0.2.9-jar-with-dependencies.jar"
# path to spanning tree results
outputPath = "/tmp/pregelix/WeightedShortestPath/"
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
if __name__ == '__main__':
	# establish database
	bootstrap.bootstrap()

	# run task one
	print "run task one..."
	outputPath = "/tmp/pregelix/WeightedShortestPathVertex/"
	sourceId = 1
	runTask.runTask("WeightedShortestPathVertex", pregelixPath, projectJarPath, \
		"edu.uci.ics.biggraph.algo.WeightedShortestPathVertex", graphPath, \
		outputPath, getIp, portNo, sourceId, numberResults, numVertices, iterations)

	# run task two
	print "run task two..."
	outputPath = "/tmp/pregelix/CommunityClusterVertex/"
	iterations = 10
	runTask.runTask("CommunityClusterVertex", pregelixPath, projectJarPath, \
		"edu.uci.ics.biggraph.algo.CommunityClusterVertex", graphPath, \
		outputPath, getIp, portNo, sourceId, numberResults, numVertices, iterations)

	# run task three
	print "run task three..."
	outputPath = "/tmp/pregelix/SocialSuggestionVertex/"
	iterations = 10
	numberResults = 10
	runTask.runTask("SocialSuggestionVertex", pregelixPath, projectJarPath, \
		"edu.uci.ics.biggraph.algo.SocialSuggestionVertex", graphPath, \
		outputPath, getIp, portNo, sourceId, numberResults, numVertices, iterations)

	# run task four
	print "run task four..."
	task4.task4(pregelixPath, graphPath, projectJarPath)

	# run display graph
	print "run display graph task..."
	outputPath = "/tmp/pregelix/SubGraphVertex/"
	iterations = 10
	sourceId = 1
	runTask.runTask("SubGraphVertex", pregelixPath, projectJarPath, \
		"edu.uci.ics.biggraph.algo.SubGraphVertex", graphPath, \
		outputPath, getIp, portNo, sourceId, numberResults, numVertices, iterations)

