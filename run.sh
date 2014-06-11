#PREGELIX_LIB_HOME="/Users/yingyib/workspace/hyracks/pregelix/pregelix-dist/target/pregelix-dist-0.2.12-SNAPSHOT-binary-assembly/"
PROJ_TARGET="`pwd`/target/"
PROJ_CLASSES="`pwd`/target/classes/"
#INPUT_PATH="`pwd`/data/task3/"
INPUT_PATH="/Volumes/TOSHIBA/BigGraphData/Facebook/adm"
#INPUT_PATH="/Users/yingyib/Downloads/test"
#OUTPUT_PATH="/tmp/wsp_result/"
OUTPUT_PATH="/Volumes/TOSHIBA/BigGraphData/Facebook/output"
#OUTPUT_PATH="/tmp/out"

#echo *********Starting the cluster**************
cd ${PREGELIX_LIB_HOME}
#bin/startCluster.sh

echo ********Starting the main program**********
if [ $# != 1 ]; then 
	echo Usage: ./run.sh \[1,2,3,5,rebuild\]
	exit
fi

if [ $1 == 1 ]; then 
	echo running task 1...
	bin/pregelix ${PROJ_TARGET}/project-0.2.9-jar-with-dependencies.jar edu.uci.ics.biggraph.algo.WeightedShortestPathVertex -inputpaths ${INPUT_PATH} -outputpath ${OUTPUT_PATH} -ip `bin/getip.sh` -port 13199 -source-vertex 3
elif [ $1 == 2 ]; then
	echo running task 2...
	bin/pregelix ${PROJ_TARGET}/project-0.2.9-jar-with-dependencies.jar edu.uci.ics.biggraph.algo.CommunityClusterVertex -inputpaths ${INPUT_PATH} -outputpath ${OUTPUT_PATH} -ip `bin/getip.sh` -port 13199 -iterations 10
elif [ $1 == 3 ]; then
	echo running task 3...
	bin/pregelix ${PROJ_TARGET}/project-0.2.9-jar-with-dependencies.jar edu.uci.ics.biggraph.algo.SocialSuggestionVertex -inputpaths ${INPUT_PATH} -outputpath ${OUTPUT_PATH} -ip `bin/getip.sh` -port 13199 -iterations 10 -results-num 5
elif [ $1 == 5 ]; then
	echo running task 5...
	bin/pregelix ${PROJ_TARGET}/project-0.2.9-jar-with-dependencies.jar edu.uci.ics.biggraph.algo.SubGraphVertex -inputpaths ${INPUT_PATH} -outputpath ${OUTPUT_PATH} -ip `bin/getip.sh` -port 13199 -iterations 10 -source-vertex 3
elif [ $1 == rebuild ]; then
	echo rebuilding the database...
	cd ${PROJ_CLASSES}
	java edu.uci.ics.biggraph.servlet.DatabaseInitializer
else 
	echo Undefined task!
fi
#echo ***********Stopping the cluster************
cd ${PREGELIX_LIB_HOME}
#bin/stopCluster.sh

echo ****************All Done!******************
