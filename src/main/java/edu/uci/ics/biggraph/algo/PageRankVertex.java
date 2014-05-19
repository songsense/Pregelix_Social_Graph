package edu.uci.ics.biggraph.algo;

import edu.uci.ics.biggraph.client.Client;
import edu.uci.ics.biggraph.inputformat.PageRankVertexInputFormat;
import edu.uci.ics.biggraph.io.DoubleWritable;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.outputformat.PageRankOutputFormat;
import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.pregelix.api.graph.MessageCombiner;
import edu.uci.ics.pregelix.api.graph.MsgList;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.job.PregelixJob;
import edu.uci.ics.pregelix.example.data.VLongNormalizedKeyComputer;

import java.util.Iterator;

/**
 * Created by soushimei on 5/16/14.
 */
public class PageRankVertex extends Vertex<VLongWritable, DoubleWritable, FloatWritable, DoubleWritable> {
    public static final String ITERATIONS = "PAGERANK_ITERATION";
    public static final String NUM_VERTICES = "NUM_VERTICES";
    private DoubleWritable outputValue = new DoubleWritable();
    private DoubleWritable tmpVertexValue = new DoubleWritable();
    private int maxIteration = -1;
    private final long numVertices = getContext().getConfiguration().getLong(NUM_VERTICES, 20);
    /**
     * Test whether combiner is called by summing up the messages.
     */

    public static class SumCombiner extends MessageCombiner<VLongWritable, DoubleWritable, DoubleWritable> {
        private double sum = 0.0;
        private DoubleWritable agg = new DoubleWritable();
        private MsgList<DoubleWritable> msgList;

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void init(MsgList msgList) {
            sum = 0.0;
            this.msgList = msgList;
        }

        @Override
        public void stepPartial(VLongWritable vertexIndex, DoubleWritable msg) throws HyracksDataException {
            sum += msg.get();
        }

        @Override
        public DoubleWritable finishPartial() {
            agg.set(sum);
            return agg;
        }

        @Override
        public void stepFinal(VLongWritable vertexIndex, DoubleWritable partialAggregate) throws HyracksDataException {
            sum += partialAggregate.get();
        }

        @Override
        public MsgList<DoubleWritable> finishFinal() {
            agg.set(sum);
            msgList.clear();
            msgList.add(agg);
            return msgList;
        }
    }
    /**
     * The key method that users need to implement to process
     * incoming messages in each superstep.
     * 1. In a superstep, this method can be called multiple times in a continuous manner for a single
     * vertex, each of which is to process a batch of messages. (Note that
     * this only happens for the case when the messages for a single vertex
     * exceed one frame.)
     * 2. In each superstep, before any invocation of this method for a vertex,
     * open() is called; after all the invocations of this method for the vertex,
     * close is called.
     * 3. In each partition, the vertex Java object is reused
     * for all the vertice to be processed in the same partition. (The model
     * is the same as the key-value objects in hadoop map tasks.)
     *
     * @param msgIterator an iterator of incoming messages
     */
    @Override
    public void compute(Iterator<DoubleWritable> msgIterator) throws Exception {
        if (maxIteration < 0) {
            maxIteration = getContext().getConfiguration().getInt(ITERATIONS, 10);
        }
        if (getSuperstep() == 1) {
            tmpVertexValue.set(1.0 / numVertices);
            PrintStatus(tmpVertexValue);
            setVertexValue(tmpVertexValue);
        }
        if (getSuperstep() >= 2 && getSuperstep() <= maxIteration) {
            double sum = 0.;
            while (msgIterator.hasNext()) {
                sum += msgIterator.next().get();
            }
            tmpVertexValue.set((0.15 / numVertices) + 0.85 * sum);
            setVertexValue(tmpVertexValue);
        }

        if (getSuperstep() >= 1 && getSuperstep() < maxIteration) {
            long edges = getNumOutEdges();
            outputValue.set(getVertexValue().get() / edges);
            sendMsgToAllEdges(outputValue);
        } else {
            voteToHalt();
        }
    }

    @Override
    public String toString() {
        return getVertexValue().toString();
    }

    private void PrintStatus(DoubleWritable tmpVertexValue) {
        System.out.print("===== ");
        System.out.print(" Vertex: " + getVertexId().toString());
        System.out.print(" with tmpVertexValue: " + tmpVertexValue.toString());
        System.out.print(" total vertexes number: " + getNumVertices());
        System.out.println(" =====");
    }

    public static void main(String[] args) throws Exception {
        PregelixJob job = new PregelixJob(PageRankVertex.class.getSimpleName());
        job.setVertexClass(PageRankVertex.class);
        job.setVertexInputFormatClass(PageRankVertexInputFormat.class);
        job.setVertexOutputFormatClass(PageRankOutputFormat.class);
        job.setMessageCombinerClass(PageRankVertex.SumCombiner.class);
        job.setNoramlizedKeyComputerClass(VLongNormalizedKeyComputer.class);
        Client.run(args, job);
    }
}
