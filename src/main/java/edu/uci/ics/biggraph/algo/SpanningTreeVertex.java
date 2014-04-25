package edu.uci.ics.biggraph.algo;

import java.util.ArrayList;
import java.util.Iterator;

import edu.uci.ics.biggraph.client.*;
import edu.uci.ics.biggraph.inputformat.SpanningTreeInputformat;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.io.HelloCntParentIdWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.outputformat.SpanningTreeOutptFormat;
import edu.uci.ics.pregelix.api.graph.Edge;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.job.PregelixJob;
import edu.uci.ics.pregelix.api.util.DefaultMessageCombiner;
import edu.uci.ics.pregelix.example.data.VLongNormalizedKeyComputer;

public class SpanningTreeVertex extends Vertex<VLongWritable, IntWritable, FloatWritable, HelloCntParentIdWritable>{

	/** The root vertex id, will be computed/set when read in graph */
	public static final String ROOT_ID = "SpanningTreeVertex.rootId";
	/** Default root vertex id */
    public static final long ROOT_ID_DEFAULT = 1;
    /** message to be sent */
    HelloCntParentIdWritable msg2Sent = new HelloCntParentIdWritable();
    /** vertex value to be set */
    IntWritable vertexValue2Set = new IntWritable();
    /** sender ID Writable for deletion */
	VLongWritable senderIdWritable = new VLongWritable();
	/** default edge value Writable for deletion */
	FloatWritable edgeValueByDefault = new FloatWritable(1.0f);
	/** record the msg sender's id */
	ArrayList<Long> msgSenderIds = new ArrayList<Long>(); 
	
	// check if the vertex is the root vertex
	private boolean isRoot() {
		return (getVertexId().get() == getContext().getConfiguration().getLong(ROOT_ID, ROOT_ID_DEFAULT));
	}
	// get the root id
	private long getRootId() {
		return getContext().getConfiguration().getLong(ROOT_ID, ROOT_ID_DEFAULT);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getNumEdges());
		for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
			sb.append(" ").append(edge.getDestVertexId().get()).append(" ").append(edge.getEdgeValue().get());
		}
		return sb.toString();
	}
	
	
	@Override
	public void compute(Iterator<HelloCntParentIdWritable> msgIterator) throws Exception {
		/** in the first step, 
		 * the root id sends the message claiming itself as root by
		 * sending value 0L
		 */
		if (getSuperstep() == 1) {
			if (isRoot()) {
				// set itself vertex value as 0L
				vertexValue2Set.set(0);
				setVertexValue(vertexValue2Set);
				
				// send message that it is the root by setting hello counter as 0
				msg2Sent.setHelloCounterParentId(0L, getRootId());
	            for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
	                sendMsg(edge.getDestVertexId(), msg2Sent);
	            }
			} else {
				// set itself vertex value as -1L
				vertexValue2Set.set(-1);
				setVertexValue(vertexValue2Set);
			}
		} else {
			/** see if it is the first time to receive the message 
			 * by checking the vertex value
			 * First time: -1L
			 * Otherwise >= 0L
			 */
			int vertexValue = getVertexValue().get();
			if (vertexValue == -1) {
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
					if (minCnt > msg.getHelloCounter()) {
						minCnt = (int) msg.getHelloCounter();
						minCntVertexId = msg.getParentId();
					}
				}
				
				// set the hello counter by increment 1
				++minCnt;
				vertexValue2Set.set(minCnt);
				setVertexValue(vertexValue2Set);		
				
				// send the message to all its neighbors except it's parent
				// NOTE: we did not use the strategy that
				//  only send the message to the nodes it did not receive message
				// this is because, we need the destination vertex to receive this obsolete
				// message such that they can delete their out going edges to the vertex
				msg2Sent.setHelloCounterParentId(minCnt, getVertexId().get());
	            for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
	            	long destVertexId = edge.getDestVertexId().get();
	            	if (destVertexId != minCntVertexId) {
	            		// don't sent message to its parent
	            		// otherwise, its parent will delete the vertex
	            		sendMsg(edge.getDestVertexId(), msg2Sent); 
	            	}
	            }
	            
	            // now delete the edge that it receives message
	            // EXCEPT the PARENT it chooses
	            for (long id : msgSenderIds) {
	            	if (id != minCntVertexId) { // EXCEPT the PARENT
	            		// begin deleting
	            		senderIdWritable.set(id);
	            		Edge<VLongWritable, FloatWritable> edge = 
								new Edge<VLongWritable, FloatWritable>(senderIdWritable, edgeValueByDefault);
	            		removeEdge(edge);
	            		System.out.print("==== 1. ");
	            		System.out.print("vertex: " + getVertexId().toString());
	            		System.out.print(" remove edge to vertex: " + senderIdWritable.toString());
	            		System.out.println(" ====");
	            	}
	            }
			} else {
				// the vertex has already received the hello message
				// just delete any edge if the message was sent through that edge
				while (msgIterator.hasNext()) {
					HelloCntParentIdWritable msg = msgIterator.next();
					// begin deleting
					senderIdWritable.set(msg.getParentId());					 
					Edge<VLongWritable, FloatWritable> edge = 
							new Edge<VLongWritable, FloatWritable>(senderIdWritable, edgeValueByDefault);
					removeEdge(edge);
            		System.out.print("==== 2. ");
            		System.out.print("vertex: " + getVertexId().toString());
            		System.out.print(" remove edge to vertex: " + senderIdWritable.toString());
            		System.out.println(" ====");
				}				
			}
		} // end of more than one time receives the message
		voteToHalt();
	}
	
    public static void main(String[] args) throws Exception {
        PregelixJob job = new PregelixJob(SpanningTreeVertex.class.getSimpleName());
        job.setVertexClass(SpanningTreeVertex.class);
        job.setVertexInputFormatClass(SpanningTreeInputformat.class);
        job.setVertexOutputFormatClass(SpanningTreeOutptFormat.class);
        job.setMessageCombinerClass(DefaultMessageCombiner.class);
        job.setNoramlizedKeyComputerClass(VLongNormalizedKeyComputer.class);
        job.getConfiguration().setLong(ROOT_ID, 0L);
        job.setDynamicVertexValueSize(true);
        Client.run(args, job);
    }

}

