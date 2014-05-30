package edu.uci.ics.biggraph.algo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.uci.ics.biggraph.client.Client;
import edu.uci.ics.biggraph.inputformat.SubGraphInputFormat;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.HelloCntParentIdWritable;
import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.outputformat.SubGraphOutputFormat;
import edu.uci.ics.pregelix.api.graph.Edge;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.job.PregelixJob;
import edu.uci.ics.pregelix.api.util.DefaultMessageCombiner;
import edu.uci.ics.pregelix.example.data.VLongNormalizedKeyComputer;
import edu.uci.ics.pregelix.example.io.VLongWritable;

/**
 * Task 5: Generate a sub graph of given range from a large graph.
 * Created by liqiangw on 5/18/14.
 */
public class SubGraphVertex extends Vertex<VLongWritable, IntWritable, FloatWritable, HelloCntParentIdWritable> {
    /** message to be sent */
    HelloCntParentIdWritable msgToSent = new HelloCntParentIdWritable();
    /** vertex value to be set */
    IntWritable vertexValueToSet = new IntWritable();
    List<Edge<VLongWritable, FloatWritable>> edgeList;

    /** Maximum iteration */
    public static final String ITERATIONS = "SubGraphVertex.iteration";
    private int maxIteration = -1;
    /** The shortest paths id */
    public static final String SOURCE_ID = "SubGraphVertex.sourceId";
    /** Default shortest paths id */
    public static final long SOURCE_ID_DEFAULT = 1;

    /**
     * Check out if the current vertex is the starting (root) one.
     */
    private boolean isSource() {
        return (getVertexId().get() == getContext().getConfiguration().getLong(SOURCE_ID, SOURCE_ID_DEFAULT));
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
//            System.out.println("[compute] iteration = " + getSuperstep()
//                    + ", vertex id = " + getVertexId().get());

            // In step 1, only root vertex sends messages.
            if (isSource()) {
                vertexValueToSet.set(0);
                setVertexValue(vertexValueToSet);

                // send its current value (0l) to all its neighbors
                msgToSent.setHelloCounterParentId(0L, getVertexId().get());
                for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
                    sendMsg(edge.getDestVertexId(), msgToSent);
                    // set the value of the outgoing edge #iteration
                    if (step == maxIteration) {
                        edge.setEdgeValue(new FloatWritable(0.0f));
                    } else {
                        edge.setEdgeValue(new FloatWritable((float) step));
                    }
                }
            } else {
                // set itself vertex value INT_MAX
                vertexValueToSet.set(Integer.MAX_VALUE);
                setVertexValue(vertexValueToSet);
                // set all values of its outgoing edges 0.0f
                for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
                    edge.setEdgeValue(new FloatWritable(0.0f));
                }
            }
        } else if (step >= 2 && step < maxIteration) { // general steps
            edgeList = getEdges();

            // In order to avoid any duplicates, we should only let vertex values
            // be changed only once.
            int vertexValue = getVertexValue().get();
            if (vertexValue == Integer.MAX_VALUE) {
                int minCnt = Integer.MAX_VALUE;
                long minCntVertexId = -1L;

                // Record Ids of vertices sending message to the current
                // vertex in the last iteration.
                HashSet<Long> senders = new HashSet<Long>();

                // We only accept message from the vertex with the lowest value
                // in order to avoid duplicates
                while (msgIterator.hasNext()) {
                    HelloCntParentIdWritable msg = msgIterator.next();

                    if (!senders.contains(msg.getParentId())) {
                        senders.add(msg.getParentId());
                    }

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

//                System.out.println("[compute] iteration = " + getSuperstep()
//                        + ", vertex id = " + getVertexId().get()
//                        + ", current val = " + getVertexValue().get()
//                        + ", val got from id " + minCntVertexId);
//                System.out.print("\t[" + getVertexId().get() + "] receive from: ");
//                Iterator<Long> it = senders.iterator();
//                while (it.hasNext()) {
//                    System.out.print(it.next() + " ");
//                }
//                System.out.println(". set size = " + senders.size());

                msgToSent.setHelloCounterParentId(minCnt, getVertexId().get());
//                System.out.println("\t[" + getVertexId().get() + "]:"
//                            + " minCount = " + minCnt);
                for (Edge<VLongWritable, FloatWritable> edge : edgeList) {
                    long destVertexId = edge.getDestVertexId().get();

                    // send the message to all its neighbors except it's parent
                    if (destVertexId != minCntVertexId) {
                        sendMsg(edge.getDestVertexId(), msgToSent);
//                        System.out.println("\t[" + getVertexId().get() + "]:"
//                            + " sending to " + destVertexId);
                    }

                    // Set the value of the outgoing edge #iteration-1 as
                    // a response so that both side of one edge have the same
                    // edge value if those two vertices are incorporated as
                    // candidates of this sub-graph.
                    if (senders.contains(destVertexId)) {
                        edge.setEdgeValue(new FloatWritable((float) step - 1));
                    } else {
                        edge.setEdgeValue(new FloatWritable((float) step));
                    }
                }
            }
        } else if (step == maxIteration) {
            int vertexValue = getVertexValue().get();

            if (vertexValue == Integer.MAX_VALUE) {
//                System.out.println("[compute] iteration = " + getSuperstep()
//                        + ", vertex id = " + getVertexId().get()
//                        + ", current val = " + getVertexValue().get());
                int minCnt = Integer.MAX_VALUE;

                while (msgIterator.hasNext()) {
                    HelloCntParentIdWritable msg = msgIterator.next();

                    // update the minimum hello counter
                    if (minCnt > (int) msg.getHelloCounter()) {
                        minCnt = (int) msg.getHelloCounter();
                    }
                }

                // Only let marginal vertices send message
                // 0 so to notify that the current vertex is
                // actually useless
//                System.out.println("\t[" + getVertexId().get() + "]:"
//                        + " about to send msg");
                if (minCnt != Integer.MAX_VALUE) {
                    // value should not below 0
                    msgToSent.setHelloCounterParentId(0, getVertexId().get());
                    for (Edge<VLongWritable, FloatWritable> e : getEdges()) {
                        sendMsg(e.getDestVertexId(), msgToSent);
//                        System.out.println("\t[" + getVertexId().get() + "]:"
//                                + " about to send msg to " + e);
                    }
//                    System.out.println("\t[" + getVertexId().get() + "]:"
//                            + " sending msg complete!");
                }
            }
        } else if (step == maxIteration + 1) {
            int vertexValue = getVertexValue().get();
            if (vertexValue != Integer.MAX_VALUE) {
//                System.out.println("[compute] iteration = " + getSuperstep()
//                        + ", vertex id = " + getVertexId().get()
//                        + ", current val = " + getVertexValue().get());

                HashMap<Long, Edge<VLongWritable, FloatWritable>> edgeHashMap
                        = new HashMap<Long, Edge<VLongWritable, FloatWritable>>();
                for (Edge<VLongWritable, FloatWritable> e : getEdges()) {
                    edgeHashMap.put(e.getDestVertexId().get(), e);
                }

                // If receives the message from the vertices that do
                // not belong to the sub-graph, change the edges value
                // back to 0.0f
//                System.out.println("\t[" + getVertexId().get() + "]:"
//                        + " begin to receive msg...");
                while (msgIterator.hasNext()) {
                    HelloCntParentIdWritable msg = msgIterator.next();
                    if (msg.getHelloCounter() == 0) {
                        Edge<VLongWritable, FloatWritable> e
                                = edgeHashMap.get(msg.getParentId());
                        e.setEdgeValue(new FloatWritable(0.0f));
//                        System.out.println("\t[" + getVertexId().get() + "]:"
//                                + " find marginal vertex = " + e.getDestVertexId());
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
