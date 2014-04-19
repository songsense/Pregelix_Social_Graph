package edu.uci.ics.biggraph.algo;

import java.util.Iterator;

import edu.uci.ics.biggraph.client.Client;
import edu.uci.ics.biggraph.inputformat.WeightedShortestPathsInputFormat;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.outputformat.SimpleOutputFormat;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.job.PregelixJob;
import edu.uci.ics.pregelix.api.util.DefaultMessageCombiner;
import edu.uci.ics.pregelix.example.data.VLongNormalizedKeyComputer;

public class CommunityClusterVertex extends Vertex<VLongWritable, VLongWritable, FloatWritable, VLongWritable>{
	
	@Override
	public void compute(Iterator<VLongWritable> msgIterator) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
    public static void main(String[] args) throws Exception {
        PregelixJob job = new PregelixJob(CommunityClusterVertex.class.getSimpleName());
        job.setVertexClass(CommunityClusterVertex.class);
        job.setVertexInputFormatClass(WeightedShortestPathsInputFormat.class);
        job.setVertexOutputFormatClass(SimpleOutputFormat.class);
        job.setMessageCombinerClass(DefaultMessageCombiner.class);
        job.setNoramlizedKeyComputerClass(VLongNormalizedKeyComputer.class);
        Client.run(args, job);
    }



}
