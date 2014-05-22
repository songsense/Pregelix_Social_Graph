import runTask
import bootstrap
import task4
## change here for your path
pregelixPath = "/Users/soushimei/Documents/pregelix/hyracks/pregelix/pregelix-dist/target/pregelix-dist-0.2.9-binary-assembly/"
# path to the graph
## change here for your path
graphPath = "/Users/soushimei/Documents/workspace/Pregelix_Social_Graph/data/graph/"
## change here for your path
projectJarPath = "/Users/soushimei/Documents/workspace/Pregelix_Social_Graph/target/project-0.2.9-jar-with-dependencies.jar"
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

