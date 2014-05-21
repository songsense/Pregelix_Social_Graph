import os
import stat
# pageRank Dictionary of each node
pageRankDict = {}
# spanning tree degree Dictionary of each node
stDict = {}
# final result
finalResults = {}
# path to spanning tree results
stOutputPath = "/tmp/pregelix/st/"
# path to pageRank results
pageRankOutputPath = "/tmp/pregelix/pageRank/"
# graph name
graphName = ""
# graph size
graphSize = 0
# spanning tree main class name
stMainClassName = "edu.uci.ics.biggraph.algo.SpanningTreeVertex"
# spanning tree task name
stTaskName = "SpanningTreeVertex"
# pageRank main class name
pageRankClassName = "edu.uci.ics.biggraph.algo.PageRankVertex"
# pagerank task name
pageRankTaskName = "PageRankVertex"
# get ip script
getIp = "`bin/getip.sh`"
# port number
portNo = 13199
# iteration number
iterations = 10
# asterixDB address
asterix_host = "localhost"
asterix_port = 19002

def getGraph(graphPath):
	listFile = os.listdir(graphPath)
	return listFile[0]

def runTask4(taskName, pregelixPath, projectJarPath, className, graphPath, outputPath, getIp, portNo,iterations, graphSize):
	script = "bin/pregelix " + projectJarPath + " " 
	script = script + className + " -inputpaths " + graphPath 
	script = script + " -outputpath " + outputPath + " -ip " + getIp 
	script = script + " -port " + str(portNo) + " -iterations " + str(iterations)
	script = script + " -vnum " + str(graphSize)
	fileName = taskName + ".sh"
	filePathName = pregelixPath + fileName
	file = open(filePathName, 'w')
	file.write(script)
	file.close()
	st = os.stat(filePathName)
	os.chmod(filePathName, st.st_mode | 0111)
	os.system(filePathName)

def analyzeResults(outputPath, resultDict):
	from os.path import isfile, join
	from os import listdir
	files = [ f for f in listdir(outputPath) if isfile(join(outputPath,f)) and f[0] != '.' ]
	
	valueSum = 0.0
	for fileName in files:
		f = open(join(outputPath, fileName), "r")
		lines = f.readlines()		
		f.close()
		for line in lines:
			fields =  line.split("\t")
			resultDict[long(fields[0])] = float(fields[1])
			valueSum = max(valueSum, float(fields[1]))

	# normailize results
	valueSum += 0.01 	# in case it's zero
	for key in resultDict.keys():
		resultDict[key] /= valueSum

def synthetizeRsult(stDict, pageRankDict, finalResults):
	for key in pageRankDict:
		finalResults[key] = stDict[key] + pageRankDict[key]*2

def cleanTask4DB():
	import requests
	from requests import ConnectionError, HTTPError
	cleanDatasetStatment = '''		
		create dataverse Tasks if not exists;
		use dataverse Tasks;
		create type TaskFourType if not exists as open  {
			user_id: int32, // node id (primary key)
			importance: double // the importance of the node
		}
		drop dataset TaskFour if exists;
		create dataset TaskFour(TaskFourType) if not exists primary key user_id;
	'''
	ddl = {'ddl':cleanDatasetStatment}
	ddl_url = "http://" + asterix_host + ":" + str(asterix_port) + "/ddl"

	response = 0
	try:
		response = requests.get(ddl_url, params=ddl)
		print response
	except (ConnectionError, HTTPError):
		print "Encountered connection error; stopping execution with code: " + str(response)
		sys.exit(1)

def saveTask4ToADM(finalResults):
	fileName = '/tmp/pregelix/task4.adm';
	f = open(fileName,'w')
	for key in finalResults.keys():
		line = '''{"user_id":''' + str(key) + ''',"importance":''' + str(finalResults[key]) + "}\n"
		f.write(line)
	f.close()
	return fileName

def saveTask4ToDB(admFileName):
	import requests
	from requests import ConnectionError, HTTPError
	
	loadStatement = '''use dataverse Tasks;load dataset TaskFour using localfs(("path"="localhost://''' + admFileName + '''"),("format"="adm"));'''
	load = {"statements": loadStatement}
	update_url = "http://" + asterix_host + ":" + str(asterix_port) + "/update"
	http_header = {"content-type":"application/json"}

	response = 01
	try:
		response = requests.get(update_url, params=load, headers=http_header)
		print response
	except (ConnectionError, HTTPError):
		print "Encountered connection error; stopping execution with code: " + str(response)
		sys.exit(1)

def task4(pregelixPath, graphPath, projectJarPath):
	graphName = getGraph(graphPath)
	print "get the number of vertexes in the graph..."
	graphSize = open(graphPath+graphName,'rb').read().count('\n')
	print "the vertexes # in the graph: ", graphSize
	
	print "begin running Pregelix Jobs..."
	runTask4(stTaskName, pregelixPath, 
		projectJarPath, stMainClassName, graphPath, stOutputPath, getIp, portNo, iterations, graphSize)
	runTask4(pageRankTaskName, pregelixPath, 
		projectJarPath, pageRankClassName, graphPath, pageRankOutputPath, getIp, portNo, iterations, graphSize)

	print "analyze the results of CDS..."
	analyzeResults(stOutputPath, stDict)
	print "analyze the results of PageRank..."
	analyzeResults(pageRankOutputPath, pageRankDict)
	print "synthetize the results..."
	synthetizeRsult(stDict, pageRankDict, finalResults)
	del stDict, pageRankDict

	print "generate the ADM file for the results..."
	admFileName = saveTask4ToADM(finalResults)
	del finalResults
	
	print "clean the dataset before loading data into AsterixDB..."
	cleanTask4DB()	
	print "save the final results to the AsterixDB..."
	saveTask4ToDB(admFileName)
	print "all done!"
	



