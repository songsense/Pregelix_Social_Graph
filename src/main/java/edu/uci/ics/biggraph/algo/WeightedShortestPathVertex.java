package edu.uci.ics.biggraph.algo;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.io.FloatWritable;

import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.pregelix.api.graph.Edge;
import edu.uci.ics.pregelix.api.graph.MessageCombiner;
import edu.uci.ics.pregelix.api.graph.MsgList;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.job.PregelixJob;
import edu.uci.ics.pregelix.example.data.VLongNormalizedKeyComputer;
import edu.uci.ics.biggraph.client.Client;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.io.DoubleWritable;
import edu.uci.ics.biggraph.inputformat.WeightedShortestPathsInputFormat;
import edu.uci.ics.biggraph.outputformat.SimpleOutputFormat;


public class WeightedShortestPathVertex extends Vertex<VLongWritable, DoubleWritable, FloatWritable, DoubleWritable> {
    /**
     * Test whether combiner is called by summing up the messages.
     */
    public static class SimpleMinCombiner extends MessageCombiner<VLongWritable, DoubleWritable, DoubleWritable> {
        private double min = Double.MAX_VALUE;
        private DoubleWritable agg = new DoubleWritable();
        private MsgList<DoubleWritable> msgList;

        @Override
        public void stepPartial(VLongWritable vertexIndex, DoubleWritable msg) throws HyracksDataException {
            double value = msg.get();
            if (min > value)
                min = value;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void init(MsgList msgList) {
            min = Double.MAX_VALUE;
            this.msgList = msgList;
        }

        @Override
        public DoubleWritable finishPartial() {
            agg.set(min);
            return agg;
        }

        @Override
        public void stepFinal(VLongWritable vertexIndex, DoubleWritable partialAggregate) throws HyracksDataException {
            double value = partialAggregate.get();
            if (min > value)
                min = value;
        }

        @Override
        public MsgList<DoubleWritable> finishFinal() {
            agg.set(min);
            msgList.clear();
            msgList.add(agg);
            return msgList;
        }
    }

    private DoubleWritable outputValue = new DoubleWritable();
    private DoubleWritable tmpVertexValue = new DoubleWritable();
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
    public void compute(Iterator<DoubleWritable> msgIterator) {
        if (getSuperstep() == 1) {
            tmpVertexValue.set(Double.MAX_VALUE);
            setVertexValue(tmpVertexValue);
        }
        double minDist = isSource() ? 0d : Double.MAX_VALUE;
        while (msgIterator.hasNext()) {
            minDist = Math.min(minDist, msgIterator.next().get());
        }
        if (LOG.getLevel() == Level.FINE) {
            LOG.fine("Vertex " + getVertexId() + " got minDist = " + minDist + " vertex value = " + getVertexValue());
        }
        if (minDist < getVertexValue().get()) {
            tmpVertexValue.set(minDist);
            setVertexValue(tmpVertexValue);
            for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
                if (LOG.getLevel() == Level.FINE) {
                    LOG.fine("Vertex " + getVertexId() + " sent to " + edge.getDestVertexId() + " = "
                            + (minDist + edge.getEdgeValue().get()));
                }
                outputValue.set(minDist + edge.getEdgeValue().get());
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
        job.setVertexOutputFormatClass(SimpleOutputFormat.class);
        job.setMessageCombinerClass(WeightedShortestPathVertex.SimpleMinCombiner.class);
        job.setNoramlizedKeyComputerClass(VLongNormalizedKeyComputer.class);
        job.getConfiguration().setLong(SOURCE_ID, 0);
        Client.run(args, job);
    }

}