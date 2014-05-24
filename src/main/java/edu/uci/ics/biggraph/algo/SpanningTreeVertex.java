package edu.uci.ics.biggraph.algo;

import edu.uci.ics.biggraph.client.Client;
import edu.uci.ics.biggraph.inputformat.SpanningTreeInputformat;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.HelloCntParentIdWritable;
import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.outputformat.SpanningTreeOutptFormat;
import edu.uci.ics.pregelix.api.graph.Edge;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.job.PregelixJob;
import edu.uci.ics.pregelix.api.util.DefaultMessageCombiner;
import edu.uci.ics.pregelix.example.data.VLongNormalizedKeyComputer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpanningTreeVertex extends Vertex<VLongWritable, IntWritable, FloatWritable, HelloCntParentIdWritable>{

    /** The root vertex id, will be computed/set when read in graph */
//	public static final String ROOT_ID = "SpanningTreeVertex.rootId";
    /** Default root vertex id */
//    public static final long ROOT_ID_DEFAULT = 0;
    /** message to be sent */
    HelloCntParentIdWritable msgToSent = new HelloCntParentIdWritable();
    /** vertex value to be set */
    IntWritable vertexValueToSet = new IntWritable();
    /** deleted edge */
    final float edgeDeleted = -1.0f;
    final FloatWritable edgeDeletedWritable = new FloatWritable(-1.0f);
    /** record the msg sender's id */
    ArrayList<Long> msgSenderIds = new ArrayList<Long>();
    List<Edge<VLongWritable, FloatWritable> > edgeList;

    // check if the vertex is the root vertex
    private boolean isRoot() {
        return (getVertexValue().get() == 0);
//		return (getVertexId().get() == getContext().getConfiguration().getLong(ROOT_ID, ROOT_ID_DEFAULT));
    }
    // get the root id
//	private long getRootId() {
//		return getContext().getConfiguration().getLong(ROOT_ID, ROOT_ID_DEFAULT);
//	}

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
            if (!isEdgeDeleted(edge)) {
                sb.append("\t").append(edge.getDestVertexId().get()).append("\t").append(edge.getEdgeValue().get());
                ++i;
            }
        }
        return Integer.toString(i) + sb.toString();
    }


    @Override
    public void compute(Iterator<HelloCntParentIdWritable> msgIterator) throws Exception {
        /** in the first step,
         * the root id sends the message claiming itself as root by
         * sending value 0L
         */
        if (getSuperstep() == 1) {
//			System.out.println("ID: " + getVertexId().toString());
            if (isRoot()) {
                // set itself vertex value as 0
                vertexValueToSet.set(0);
                setVertexValue(vertexValueToSet);

                // send message that it is the root by setting hello counter as 0
                msgToSent.setHelloCounterParentId(0L, getVertexId().get());
                for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
                    sendMsg(edge.getDestVertexId(), msgToSent);
//            		System.out.print("==== 0 message: ");
//            		System.out.print(" send from root: " + getVertexId().toString());
//            		System.out.print(" to " + edge.getDestVertexId().toString());
//            		System.out.println(" ====");
                }
            } else {
                // set itself vertex value as -1L
                vertexValueToSet.set(Integer.MAX_VALUE);
                setVertexValue(vertexValueToSet);
            }
        } else {
            // get the list of edges
            // if an edge value is set to be edgeDeleted
            // then it is deleted
            edgeList = getEdges();

            /** see if it is the first time to receive the message
             * by checking the vertex value
             * First time: -1L
             * Otherwise >= 0L
             */
            int vertexValue = getVertexValue().get();
            if (vertexValue == Integer.MAX_VALUE) {
                // first time to receive the message
                // init msgSenderIds
                msgSenderIds.clear();

                // Processing the message it got
                // find the lowest hello counter it receives
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

                // send the message to all its neighbors except it's parent
                // NOTE: we did not use the strategy that
                //  only sends the message to the nodes it did not receive message
                // this is because, we need the destination vertex to receive this obsolete
                // message such that they can delete their out going edges to the vertex
                msgToSent.setHelloCounterParentId(minCnt, getVertexId().get());
                for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
                    long destVertexId = edge.getDestVertexId().get();
                    if (destVertexId != minCntVertexId
                            && !isEdgeDeleted(edge)) { // must not be deleted edge
                        // don't sent message to its parent
                        // otherwise, its parent will delete the vertex
                        sendMsg(edge.getDestVertexId(), msgToSent);
//	            		System.out.print("==== 1 message: ");
//	            		System.out.print("Parent id: " + minCntVertexId);
//	            		System.out.print(" send from " + getVertexId().toString());
//	            		System.out.print(" to " + edge.getDestVertexId().toString());
//	            		System.out.println(" ====");
                    }
                }

                // now delete the edge that it receives message
                // EXCEPT the PARENT it chooses
                for (long id : msgSenderIds) {
                    if (id != minCntVertexId) { // EXCEPT the PARENT
                        // begin deleting
                        setEdgeDeleted(edgeList, id);
//	            		System.out.print("==== 1. ");
//	            		System.out.print("vertex: " + getVertexId().toString());
//	            		System.out.print(" remove edge to vertex: " + id);
//	            		System.out.println(" ====");
                    }
                }
            } else {
                // the vertex has already received the hello message
                // just delete any edge if the message was sent through that edge
                while (msgIterator.hasNext()) {
                    HelloCntParentIdWritable msg = msgIterator.next();
                    // begin deleting
                    setEdgeDeleted(edgeList, msg.getParentId());
//            		System.out.print("==== 2. ");
//            		System.out.print("vertex: " + getVertexId().toString());
//            		System.out.print(" remove edge to vertex: " + msg.getParentId());
//            		System.out.println(" ====");
                }
            }
        } // end of more than one time receives the message
        voteToHalt();
    }

    private boolean isEdgeDeleted(Edge<VLongWritable, FloatWritable> edge) {
        return (edge.getEdgeValue().get() == edgeDeleted);
    }

    private void setEdgeDeleted(List<Edge<VLongWritable, FloatWritable> > edgeList, long idToDelete) {
        for (Edge<VLongWritable, FloatWritable> edge : edgeList) {
            if (edge.getDestVertexId().get() == idToDelete) {
                FloatWritable value = new FloatWritable(edgeDeleted);
                edge.setEdgeValue(value);
                printMessage(idToDelete, edge);
                return;
            }
        }
    }

    private void printMessage(long idToDelete, Edge<VLongWritable, FloatWritable> edge) {
        /*
        if (getVertexId().get() != 12 && getVertexId().get() != 16) {
            return;
        }
        System.out.print("==== ");
        System.out.print("vertex: " + getVertexId().toString());
        System.out.print(" removes edge to vertex: " + idToDelete);
        System.out.print(" edge value is: " + edge.getEdgeValue());
        System.out.print(" edge dest id is: " + edge.getDestVertexId().toString());
        System.out.println(" ====");
        */
    }

    public static void main(String[] args) throws Exception {
        PregelixJob job = new PregelixJob(SpanningTreeVertex.class.getSimpleName());
        job.setVertexClass(SpanningTreeVertex.class);
        job.setVertexInputFormatClass(SpanningTreeInputformat.class);
        job.setVertexOutputFormatClass(SpanningTreeOutptFormat.class);
        job.setMessageCombinerClass(DefaultMessageCombiner.class);
        job.setNoramlizedKeyComputerClass(VLongNormalizedKeyComputer.class);
        Client.run(args, job);
    }

}

