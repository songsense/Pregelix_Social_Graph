package edu.uci.ics.biggraph.algo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uci.ics.biggraph.io.DoubleArrayListWritable;
import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.io.VLongArrayListWritable;
import edu.uci.ics.biggraph.io.WeightedPathWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.pregelix.api.graph.Edge;
import edu.uci.ics.pregelix.api.graph.MessageCombiner;
import edu.uci.ics.pregelix.api.graph.MsgList;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.WritableSizable;
import edu.uci.ics.pregelix.api.job.PregelixJob;
import edu.uci.ics.pregelix.api.util.DefaultMessageCombiner;

import edu.uci.ics.pregelix.example.data.VLongNormalizedKeyComputer;
import edu.uci.ics.biggraph.client.Client;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.inputformat.SocialSuggestionInputFormat;
import edu.uci.ics.biggraph.inputformat.WeightedShortestPathsInputFormat;
import edu.uci.ics.biggraph.outputformat.WeightedOutputFormat;

public class SocialSuggestionVertex extends Vertex<VLongWritable, VLongArrayListWritable, IntWritable, VLongArrayListWritable> {
    /**
     * In this task, every vertex (corresponded as one person) iteratively 
     * to get information from neighbor vertices about their neighbors
     * and eventually find the k of suggested vertices.
     */
    
    /**
     * The message combiner.
     */
    public static class SocialSuggestionCombiner 
        extends MessageCombiner<VLongWritable, VLongArrayListWritable, VLongArrayListWritable> {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void init(MsgList providedMsgList) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void stepPartial(VLongWritable vertexIndex,
                VLongArrayListWritable msg) throws HyracksDataException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void stepFinal(VLongWritable vertexIndex,
                VLongArrayListWritable partialAggregate)
                throws HyracksDataException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public VLongArrayListWritable finishPartial() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public MsgList<VLongArrayListWritable> finishFinal() {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void compute(Iterator<VLongArrayListWritable> msgIterator)
            throws Exception {
        ArrayList<VLongWritable> nb;
        VLongArrayListWritable msg;
        
        if (maxIteration < 0) {
            maxIteration = getContext().getConfiguration().getInt(ITERATIONS, 10);
        }
        if (numResults < 0) {
            numResults = getContext().getConfiguration().getInt(NUM_RESULTS, 10);
        }
        
        if (getSuperstep() == 1) {
            nb = new ArrayList<VLongWritable>();
            for (Edge<VLongWritable, IntWritable> edge : getEdges()) {
                nb.add(edge.getDestVertexId());
            }
            
            msg = new VLongArrayListWritable();
            msg.addAllElements(nb);
            for (Edge<VLongWritable, IntWritable> edge : getEdges()) {
                sendMsg(edge.getDestVertexId(), msg);
            }
        } else if (getSuperstep() >= 2 && getSuperstep() < maxIteration) {
            
        } else {
            voteToHalt();
        }
    }
    
    private VLongArrayListWritable outputValue = null;
    
    private VLongArrayListWritable tmpVertexValue = null;
    
    /** Maximum iteration */
    public static final String ITERATIONS = "SocialSuggestionVertex.iteration";
    private int maxIteration = -1;
    
    /** Result number: how many suggestions desired */
    public static final String NUM_RESULTS = "SocialSuggestionVertex.results";
    private int numResults = -1;
    
    /** Class logger */
    private static final Logger LOG = Logger.getLogger(SocialSuggestionVertex.class.getName());

    public static void main(String[] args) throws Exception {
        PregelixJob job = new PregelixJob(SocialSuggestionVertex.class.getSimpleName());
        job.setVertexClass(SocialSuggestionVertex.class);
        // TODO: create own input format
        job.setVertexInputFormatClass(SocialSuggestionInputFormat.class);
        job.setVertexOutputFormatClass(WeightedOutputFormat.class); // can still use
//        job.setMessageCombinerClass(WeightedShortestPathVertex.SimpleMinCombiner.class);
        job.setMessageCombinerClass(DefaultMessageCombiner.class);
        job.setNoramlizedKeyComputerClass(VLongNormalizedKeyComputer.class);
        job.setDynamicVertexValueSize(true);
        
        Client.run(args, job);
    }
}
