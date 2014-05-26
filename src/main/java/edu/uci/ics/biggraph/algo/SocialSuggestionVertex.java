package edu.uci.ics.biggraph.algo;

import edu.uci.ics.biggraph.client.Client;
import edu.uci.ics.biggraph.inputformat.SocialSuggestionInputFormat;
import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.io.VLongArrayListWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.outputformat.SocialSuggestionOutputFormat;
import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.pregelix.api.graph.Edge;
import edu.uci.ics.pregelix.api.graph.MessageCombiner;
import edu.uci.ics.pregelix.api.graph.MsgList;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.job.PregelixJob;
import edu.uci.ics.pregelix.api.util.DefaultMessageCombiner;
import edu.uci.ics.pregelix.example.data.VLongNormalizedKeyComputer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

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

        @SuppressWarnings({ "rawtypes" })
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
        VLongArrayListWritable msg = new VLongArrayListWritable();
        
        if (maxIteration < 0) {
            maxIteration = getContext().getConfiguration().getInt(ITERATIONS, 10);
        }
        if (numResults < 0) {
            numResults = getContext().getConfiguration().getInt(NUM_RESULTS, 10);
        }
        
        long step = getSuperstep();
        
        if (step == 1) {
            for (Edge<VLongWritable, IntWritable> edge : getEdges()) {
                msg.add(edge.getDestVertexId());
            }
            for (Edge<VLongWritable, IntWritable> edge : getEdges()) {
                // broadcast message
                sendMsg(edge.getDestVertexId(), msg);
            }
        } else if (step >= 2 && step < maxIteration) {
            tmpVertexValue = getVertexValue(); // get values from last iterations

            buildMap(tmpVertexValue);
            // get new vertices from incoming message without duplicates
            VLongArrayListWritable newVertices = new VLongArrayListWritable();

            while (msgIterator.hasNext()) {
                VLongArrayListWritable t = msgIterator.next();
                for (int i = 0; i < t.size(); i++) {
                    long vid = ((VLongWritable) t.get(i)).get();
                    if (!verticesMap.containsKey(vid)) {
                        verticesMap.put(vid, 1l);
                        newVertices.add(t.get(i));
//                        tmpVertexValue.add(t.get(i));
                    } else {
                        long cur = verticesMap.get(vid);
                        verticesMap.put(vid, cur + 1);
                    }
                }
            }

            // prioritize incoming vertices by its appearing frequency.
            ArrayList<VertexFrequency> vf = new ArrayList<VertexFrequency>();
            for (int i = 0; i < newVertices.size(); i++) {
                VLongWritable vl = (VLongWritable) newVertices.get(i);
                long vid = vl.get();
                long freq = verticesMap.get(vid);
                vf.add(new VertexFrequency(vl, freq));
            }
            Collections.sort(vf);
            for (int i = 0; i < vf.size(); i++) {
                tmpVertexValue.add(vf.get(i).vlong);
            }

            curNumResults = tmpVertexValue.size();
            setVertexValue(tmpVertexValue);

            // termination predicate
            if (curNumResults >= numResults) {
//                terminateJob();
                voteToHalt();
                return;
            }

            // send the newly received vertex IDs
            for (Edge<VLongWritable, IntWritable> edge : getEdges()) {
                sendMsg(edge.getDestVertexId(), newVertices);
            }
        } else {
            terminateJob();
        }
        voteToHalt();
    }
    
    /**
     * Build the hash set for efficient avoiding vertex ID duplicates. 
     */
    private void buildMap(VLongArrayListWritable currentVertexValue) {
        verticesMap.clear();
        
        // add vertex ID itself
        verticesMap.put(getVertexId().get(), 1l);
        
        // add IDs of its neighbors
        for (Edge<VLongWritable, IntWritable> edge : getEdges()) {
            verticesMap.put(edge.getDestVertexId().get(), 1l);
        }
        
        // add current results IDs from the vertex value
        if (currentVertexValue == null) {
            return;
        }
        for (int i = 0; i < currentVertexValue.size(); i++) {
            long vid = ((VLongWritable) currentVertexValue.get(i)).get();
            if (!verticesMap.containsKey(vid)) {
                verticesMap.put(vid, 1l);
            }
        }
    }

    private class VertexFrequency implements Comparable<VertexFrequency> {
        VLongWritable vlong;
        long freq = 0;

        public VertexFrequency(VLongWritable vlong, long freq) {
            this.vlong = vlong;
            this.freq = freq;
        }

        @Override
        public int compareTo(VertexFrequency that) {
            assert that != null;
            if (this.freq > that.freq) {
                return -1;
            } else if (this.freq == that.freq) {
                return 0;
            } else {
                return 1;
            }
        }
    }
    
    private VLongArrayListWritable tmpVertexValue = null;
    
    /** Visited vertices: used for efficiently eliminate duplicates. */
    private HashMap<Long, Long> verticesMap = new HashMap<Long, Long>();
    
    /** Maximum iteration */
    public static final String ITERATIONS = "SocialSuggestionVertex.iteration";
    private int maxIteration = -1;
    
    /** Result number: how many suggestions desired */
    public static final String NUM_RESULTS = "SocialSuggestionVertex.results";
    private int numResults = -1;
    private int curNumResults = 0;
    
    /** Class logger */
    private static final Logger LOG = Logger.getLogger(SocialSuggestionVertex.class.getName());

    public static void main(String[] args) throws Exception {
        PregelixJob job = new PregelixJob(SocialSuggestionVertex.class.getSimpleName());
        job.setVertexClass(SocialSuggestionVertex.class);
        job.setVertexInputFormatClass(SocialSuggestionInputFormat.class);
        job.setVertexOutputFormatClass(SocialSuggestionOutputFormat.class); 
//        job.setMessageCombinerClass(WeightedShortestPathVertex.SimpleMinCombiner.class);
        job.setMessageCombinerClass(DefaultMessageCombiner.class);
        job.setNoramlizedKeyComputerClass(VLongNormalizedKeyComputer.class);
        job.setDynamicVertexValueSize(true);
        
        System.out.println("-----Begin to run-----");
        Client.run(args, job);
    }
}
