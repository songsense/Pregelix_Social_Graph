package edu.uci.ics.biggraph.algo;

import edu.uci.ics.biggraph.client.Client;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.HelloCntParentIdWritable;
import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.outputformat.SubGraphOutputFormat;
import edu.uci.ics.biggraph.inputformat.SubGraphInputFormat;
import edu.uci.ics.pregelix.api.graph.Edge;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.job.PregelixJob;
import edu.uci.ics.pregelix.api.util.DefaultMessageCombiner;
import edu.uci.ics.pregelix.example.data.VLongNormalizedKeyComputer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by liqiangw on 5/18/14.
 */
public class SubGraphVertex extends Vertex<VLongWritable, IntWritable, FloatWritable, HelloCntParentIdWritable> {
    /** message to be sent */
    HelloCntParentIdWritable msgToSent = new HelloCntParentIdWritable();
    /** vertex value to be set */
    IntWritable vertexValueToSet = new IntWritable();
    /** record the msg sender's id */
    ArrayList<Long> msgSenderIds = new ArrayList<Long>();
    List<Edge<VLongWritable, FloatWritable> > edgeList;

    /** Maximum iteration */
    public static final String ITERATIONS = "SocialSuggestionVertex.iteration";
    private int maxIteration = -1;

    /**
     * Check out if the current vertex is the starting (root) one.
     */
    private boolean isRoot() {
        return (getVertexValue().get() == 0);
    }

    @Override
    public void compute(Iterator<HelloCntParentIdWritable> msgIterator) throws Exception {
        if (maxIteration < 0) {
            maxIteration = getContext().getConfiguration().getInt(ITERATIONS, 10);
        }

        // First step: for root node, set its value to 0l, for others,
        // set it to INT_MAX
        long step = getSuperstep();
        if (step == 1) {
            System.out.println("[compute] iteration = " + getSuperstep()
                    + ", vertex id = " + getVertexId().get());

            if (isRoot()) {
                vertexValueToSet.set(0);
                setVertexValue(vertexValueToSet);

                // send its current value (0l) to all its neighbors
                msgToSent.setHelloCounterParentId(0L, getVertexId().get());
                for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
                    sendMsg(edge.getDestVertexId(), msgToSent);
                }
            } else {
                // set itself vertex value as -1L
                vertexValueToSet.set(Integer.MAX_VALUE);
                setVertexValue(vertexValueToSet);
            }
        } else if (step >= 2 && step <= maxIteration) { // general steps
            edgeList = getEdges();

            // In order to avoid any duplicates, we should only let vertex values
            // be changed only once.
            int vertexValue = getVertexValue().get();
            if (vertexValue == Integer.MAX_VALUE) {
                msgSenderIds.clear();

                // We only accept message from the vertex with the lowest value
                // in order to avoid duplicates
                int minCnt = Integer.MAX_VALUE;
                long minCntVertexId = -1L;
                while (msgIterator.hasNext()) {
                    HelloCntParentIdWritable msg = msgIterator.next();
                    // save the sender's id of message
                    msgSenderIds.add(msg.getParentId());

                    // update the minimum hello counter
                    if (minCnt > (int) msg.getHelloCounter()) {
                        minCnt = (int) msg.getHelloCounter();
                        minCntVertexId = msg.getParentId();
                    }
                }

                // set the hello counter by increment 1
                ++minCnt;
                vertexValueToSet.set(minCnt);
                setVertexValue(vertexValueToSet);

                System.out.println("[compute] iteration = " + getSuperstep()
                        + ", vertex id = " + getVertexId().get()
                        + ", current val = " + getVertexValue().get()
                        + ", val got from id " + minCntVertexId);

                // send the message to all its neighbors except it's parent
                msgToSent.setHelloCounterParentId(minCnt, getVertexId().get());
                System.out.println("\t[" + getVertexId().get() + "]:"
                            + " minCount = " + minCnt);
                for (Edge<VLongWritable, FloatWritable> edge : edgeList) {
                    long destVertexId = edge.getDestVertexId().get();
                    if (destVertexId != minCntVertexId) {
                        sendMsg(edge.getDestVertexId(), msgToSent);
                        System.out.println("\t[" + getVertexId().get() + "]:"
                            + " sending to " + destVertexId);
                    }
                }
            }
        }
        voteToHalt();
    }

    public static void main(String[] args) throws Exception {
        PregelixJob job = new PregelixJob(SubGraphVertex.class.getSimpleName());
        job.setVertexClass(SubGraphVertex.class);
        job.setVertexInputFormatClass(SubGraphInputFormat.class);
        job.setVertexOutputFormatClass(SubGraphOutputFormat.class);
        job.setMessageCombinerClass(DefaultMessageCombiner.class);
        job.setNoramlizedKeyComputerClass(VLongNormalizedKeyComputer.class);
        Client.run(args, job);
    }
}
