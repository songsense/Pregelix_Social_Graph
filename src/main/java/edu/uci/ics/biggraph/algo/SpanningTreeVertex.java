package edu.uci.ics.biggraph.algo;

import java.util.HashSet;
import java.util.Iterator;

import edu.uci.ics.biggraph.client.Client;
import edu.uci.ics.biggraph.inputformat.SpanningTreeInputformat;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.TwoVLongWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.outputformat.SpanningTreeOutptFormat;
import edu.uci.ics.pregelix.api.graph.Edge;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.job.PregelixJob;
import edu.uci.ics.pregelix.api.util.DefaultMessageCombiner;
import edu.uci.ics.pregelix.example.data.VLongNormalizedKeyComputer;

public class SpanningTreeVertex extends Vertex<VLongWritable, VLongWritable, FloatWritable, TwoVLongWritable>{

	/** The root vertex id, will be computed/set when read in graph */
	public static final String ROOT_ID = "SpanningTreeVertex.rootId";
	/** Default root vertex id */
    public static final long ROOT_ID_DEFAULT = 1;
    /** message to be sent */
    TwoVLongWritable msg2Sent = new TwoVLongWritable();
    /** vertex value to be set */
    VLongWritable vertexValue2Set = new VLongWritable();
    /** sender ID Writable for deletion */
	VLongWritable senderIdWritable = new VLongWritable();
	/** default edge value Writable for deletion */
	FloatWritable edgeValueByDefault = new FloatWritable(1.0f);
	/** record the msg sender's id */
	HashSet<Long> msgSenderIds = new HashSet<Long>(); 
	
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
	public void compute(Iterator<TwoVLongWritable> msgIterator) throws Exception {
		/** in the first step, 
		 * the root id sends the message claiming itself as root by
		 * sending value 0L
		 */
		if (getSuperstep() == 1 && isRoot()) {
			// set itself vertex value as 0L
			vertexValue2Set.set(0L);
			setVertexValue(vertexValue2Set);
			
			// send message that it is the root by setting hello counter as 0
			msg2Sent.setHelloCounterParentId(0L, getRootId());
            for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
                sendMsg(edge.getDestVertexId(), msg2Sent);
            }
		} else {
			/** see if it is the first time to receive the message 
			 * by checking the vertex value
			 * First time: -1L
			 * Otherwise >= 0L
			 */
			long vertexValue = getVertexValue().get();
			if (vertexValue == -1L) {
				// first time to receive the message
				// init msgSenderIds
				msgSenderIds.clear();
				
				// Processing the message it got
				// find the lowest hello counter it receives
				long minCnt = Long.MAX_VALUE;
				long minCntVertexId = -1L;
				while (msgIterator.hasNext()) {
					TwoVLongWritable msg = msgIterator.next();
					// save the sender's id of message
					msgSenderIds.add(msg.getParentId());
					
					// update the minimum hello counter
					if (minCnt > msg.getHelloCounter()) {
						minCnt = msg.getHelloCounter();
						minCntVertexId = msg.getParentId();
					}
				}
				
				// set the hello counter by increment 1
				++minCnt;
				vertexValue2Set.set(minCnt);
				setVertexValue(vertexValue2Set);		
				
				// send the message to all its neighbors except those who sent it messages
				msg2Sent.setHelloCounterParentId(minCnt, getVertexId().get());
	            for (Edge<VLongWritable, FloatWritable> edge : getEdges()) {
	            	long destVertexId = edge.getDestVertexId().get();
	            	if (msgSenderIds.contains(destVertexId) == false) {
	            		// only send the message to the nodes it did not receive message
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
	            	}
	            }
			} else {
				// just delete any edge if the message was sent through that edge
				while (msgIterator.hasNext()) {
					TwoVLongWritable msg = msgIterator.next();
					// begin deleting
					senderIdWritable.set(msg.getParentId());					 
					Edge<VLongWritable, FloatWritable> edge = 
							new Edge<VLongWritable, FloatWritable>(senderIdWritable, edgeValueByDefault);
					removeEdge(edge);
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
        Client.run(args, job);
    }

}

