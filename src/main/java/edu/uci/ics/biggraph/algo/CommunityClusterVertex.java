package edu.uci.ics.biggraph.algo;

import java.util.HashMap;
import java.util.Iterator;

import edu.uci.ics.biggraph.client.Client;
import edu.uci.ics.biggraph.inputformat.CommunityClusterInputFormat;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.VLongIntWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.outputformat.CommunityClusterOutputFormat;
import edu.uci.ics.pregelix.api.graph.Edge;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.job.PregelixJob;
import edu.uci.ics.pregelix.api.util.DefaultMessageCombiner;
import edu.uci.ics.pregelix.example.data.VLongNormalizedKeyComputer;

public class CommunityClusterVertex extends Vertex<VLongWritable, VLongIntWritable, FloatWritable, VLongWritable>{
	HashMap<Long, Integer> vertexFreq = new HashMap<Long, Integer>();
    VLongWritable msg2Sent = new VLongWritable();
    VLongIntWritable vertexValue2Set = new VLongIntWritable();
	
	@Override
	public void compute(Iterator<VLongWritable> msgIterator) throws Exception {
		// initialize vertex id with maximum count
		long maxVertexId = getVertexValue().getVertexId();
		int maxCount = getVertexValue().getCount();

        // get original vertex id with maximum count
        long originVertexId = maxVertexId;

        // clear map
        vertexFreq.clear();
        // add itself to the map
        vertexFreq.put(maxVertexId, maxCount);

        // collect message to find the maximum count of vertex id
		while (msgIterator.hasNext()) {
			long msg_VertexId = msgIterator.next().get();
            int count;
			// update the vertex id frequency
			if (vertexFreq.containsKey(msg_VertexId)) {
				count = vertexFreq.get(msg_VertexId);
				count += 1;
				vertexFreq.put(msg_VertexId, count);
			} else {
				vertexFreq.put(msg_VertexId, 1);
                count = 1;
			}

            // update the maximum count
            if (maxCount < count) {
                maxCount = count;
                maxVertexId = msg_VertexId;
            } else if (maxCount == count && maxVertexId > msg_VertexId) {
                // choose the minimum vertex id if possible
                maxCount = count;
                maxVertexId = msg_VertexId;
            }
		}

        // update the vertex value
        vertexValue2Set.set(maxVertexId, maxCount);
        setVertexValue(vertexValue2Set);

        // send message to others if there is changes in vertex value
        if (originVertexId != maxVertexId) {
            for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
                msg2Sent.set(maxVertexId);
                sendMsg(edge.getDestVertexId(), msg2Sent);
            }
        }

        voteToHalt();

	}
	
    public static void main(String[] args) throws Exception {
        PregelixJob job = new PregelixJob(CommunityClusterVertex.class.getSimpleName());
        job.setVertexClass(CommunityClusterVertex.class);
        job.setVertexInputFormatClass(CommunityClusterInputFormat.class);
        job.setVertexOutputFormatClass(CommunityClusterOutputFormat.class);
        job.setMessageCombinerClass(DefaultMessageCombiner.class);
        job.setNoramlizedKeyComputerClass(VLongNormalizedKeyComputer.class);
        Client.run(args, job);
    }



}
