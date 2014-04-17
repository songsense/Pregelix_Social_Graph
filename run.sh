PREGELIX_LIB_HOME="/Users/liqiangw/Documents/workspace/twitter_graph/hyracks/pregelix/pregelix-dist/target/pregelix-dist-0.2.9-binary-assembly/"
PROJ_TARGET="`pwd`/target/"
INPUT_PATH="`pwd`/data/weighted/small/"
OUTPUT_PATH="/tmp/wsp_result/"

#echo *********Starting the cluster**************
cd ${PREGELIX_LIB_HOME}
#bin/startCluster.sh

echo ********Starting the main program**********
bin/pregelix ${PROJ_TARGET}/project-0.2.9-jar-with-dependencies.jar edu.uci.ics.biggraph.algo.WeightedShortestPathVertex -inputpaths ${INPUT_PATH} -outputpath ${OUTPUT_PATH} -ip `bin/getip.sh` -port 3099 -source-vertex 184755890 -vnum 4 -enum 4

#echo ***********Stopping the cluster************
cd ${PREGELIX_LIB_HOME}
#bin/stopCluster.sh

echo ****************All Done!******************
