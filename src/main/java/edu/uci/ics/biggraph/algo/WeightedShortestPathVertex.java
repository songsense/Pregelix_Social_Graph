package edu.uci.ics.biggraph.algo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uci.ics.biggraph.io.WeightedPathWritable;
import org.apache.hadoop.io.FloatWritable;


import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.pregelix.api.graph.Edge;
import edu.uci.ics.pregelix.api.graph.MessageCombiner;
import edu.uci.ics.pregelix.api.graph.MsgList;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.job.PregelixJob;
import edu.uci.ics.pregelix.api.util.DefaultMessageCombiner;


import edu.uci.ics.pregelix.example.data.VLongNormalizedKeyComputer;
import edu.uci.ics.biggraph.client.Client;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.inputformat.WeightedShortestPathsInputFormat;
import edu.uci.ics.biggraph.outputformat.WeightedOutputFormat;


public class WeightedShortestPathVertex extends Vertex<VLongWritable, WeightedPathWritable, FloatWritable, WeightedPathWritable> {
    /**
     * Test whether combiner is called by summing up the messages.
     */
    public static class SimpleMinCombiner extends MessageCombiner<VLongWritable, WeightedPathWritable, WeightedPathWritable> {
        private double min = Double.MAX_VALUE;
        private ArrayList<Double> path = new ArrayList<Double>();
        private WeightedPathWritable agg = new WeightedPathWritable();
        private MsgList<WeightedPathWritable> msgList;

        private int metaSlot = 8;
        private int accumulatedSize = metaSlot;

        @Override
        public void stepPartial(VLongWritable vertexIndex, WeightedPathWritable msg) throws HyracksDataException {
            double value = msg.getWeight();
            if (min > value) {
                min = value;
                path = msg.getPathArrayList();
            }
            accumulatedSize += msg.sizeInBytes();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void init(MsgList msgList) {
            min = Double.MAX_VALUE;
            path = new ArrayList<Double>();
            agg = new WeightedPathWritable();
            this.msgList = msgList;
            this.accumulatedSize = metaSlot;
        }

        @Override
        public WeightedPathWritable finishPartial() {
            agg.setWeight(min);
            agg.setPathAlone(path);
            return agg;
        }

        @Override
        public void stepFinal(VLongWritable vertexIndex, WeightedPathWritable partialAggregate) throws HyracksDataException {
            double value = partialAggregate.getWeight();
            if (min > value) {
                min = value;
                path = partialAggregate.getPathArrayList();
            }
            accumulatedSize += partialAggregate.sizeInBytes();
        }

        @Override
        public MsgList<WeightedPathWritable> finishFinal() {
            agg = new WeightedPathWritable();
            agg.setWeight(min);
//            agg.setPathAlone("ac");
            msgList.clear();
            msgList.add(agg);
            return msgList;
        }

        @Override
        public int estimateAccumulatedStateByteSizePartial(VLongWritable vertexIndex, WeightedPathWritable msg) throws HyracksDataException {
            return accumulatedSize + msg.sizeInBytes();
        }

        @Override
        public int estimateAccumulatedStateByteSizeFinal(VLongWritable vertexIndex, WeightedPathWritable partialAggregate)
                throws HyracksDataException {
            int size = accumulatedSize;
            size += (partialAggregate.sizeInBytes());
            return size;
        }
    }

    private WeightedPathWritable outputValue = new WeightedPathWritable();
    private WeightedPathWritable tmpVertexValue = new WeightedPathWritable();
    /** Class logger */
    private static final Logger LOG = Logger.getLogger(WeightedShortestPathVertex.class.getName());
    /** The shortest paths id */
    public static final String SOURCE_ID = "WeightedShortestPathVertex.sourceId";
    /** Default shortest paths id */
    public static final long SOURCE_ID_DEFAULT = 1;

    /**
     * Is this vertex the source id?
     * 
     * @return True if the source id
     */
    private boolean isSource() {
        return (getVertexId().get() == getContext().getConfiguration().getLong(SOURCE_ID, SOURCE_ID_DEFAULT));
    }

    @Override
    public void compute(Iterator<WeightedPathWritable> msgIterator) {
        if (getSuperstep() == 1) {
            tmpVertexValue.setWeight(Double.MAX_VALUE);
            // tmpVertexValue.setPathAlone(new Text("s: "));
            setVertexValue(tmpVertexValue);
        }
        double minDist = isSource() ? 0d : Double.MAX_VALUE;
        ArrayList<Double> minPath = new ArrayList<Double>();
        if (isSource() == true) {
            double id = (double) getVertexId().get();
            minPath.add(id);
        } else {
            minPath = getVertexValue().getPathArrayList();
        }

        WeightedPathWritable msg;
        while (msgIterator.hasNext()) {
            msg = msgIterator.next();
            if (minDist > msg.getWeight()) {
                minDist = msg.getWeight();
                minPath = msg.getPathArrayList();
            }
        }
        if (LOG.getLevel() == Level.FINE) {
            LOG.fine("Vertex " + getVertexId() + " got minDist = " + minDist + " vertex value = " + getVertexValue());
        }

        if (minDist < getVertexValue().getWeight()) {
            tmpVertexValue.setWeight(minDist);
            // uncomment here to redisplay the exception
            // this prevents storing the path info into the vertex value
//            tmpVertexValue.setPath(minPath, getVertexId().get());
            setVertexValue(tmpVertexValue);

            // uncomment here
            // this prevents storing the path info into the message
            // outputValue.setPath(minPath, getVertexId());
            for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
                if (LOG.getLevel() == Level.FINE) {
                    LOG.fine("Vertex " + getVertexId() + " sent to " + edge.getDestVertexId() + " = "
                            + (minDist + edge.getEdgeValue().get()));
                }
                outputValue.setWeight(minDist + (double) edge.getEdgeValue().get());
                sendMsg(edge.getDestVertexId(), outputValue);
            }
        }
        voteToHalt();
    }

    @Override
    public String toString() {
        return getVertexId() + " " + getVertexValue();
    }

    public static void main(String[] args) throws Exception {
        PregelixJob job = new PregelixJob(WeightedShortestPathVertex.class.getSimpleName());
        job.setVertexClass(WeightedShortestPathVertex.class);
        job.setVertexInputFormatClass(WeightedShortestPathsInputFormat.class);
        job.setVertexOutputFormatClass(WeightedOutputFormat.class);
//        job.setMessageCombinerClass(WeightedShortestPathVertex.SimpleMinCombiner.class);
        job.setMessageCombinerClass(DefaultMessageCombiner.class);
        job.setNoramlizedKeyComputerClass(VLongNormalizedKeyComputer.class);
        job.getConfiguration().setLong(SOURCE_ID, 0);
        Client.run(args, job);
    }

}