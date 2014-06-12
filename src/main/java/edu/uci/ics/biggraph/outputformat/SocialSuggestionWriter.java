package edu.uci.ics.biggraph.outputformat;

import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.io.VLongArrayListWritable;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat.TextVertexWriter;
import edu.uci.ics.pregelix.example.io.VLongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;

import java.io.IOException;

public class SocialSuggestionWriter extends TextVertexWriter<VLongWritable, VLongArrayListWritable, IntWritable> {

    public static final String NUM_RESULTS = "SocialSuggestionVertex.results";
    private int numResults = -1; //Vertex.getContext().getConfiguration().getInt(NUM_RESULTS, 10);

    public SocialSuggestionWriter(RecordWriter<Text, Text> arg0) {
        super(arg0);
    }

    @Override
    public void writeVertex(Vertex<VLongWritable, VLongArrayListWritable, IntWritable, ?> vertex) throws IOException,
            InterruptedException {
        VLongArrayListWritable val = vertex.getVertexValue();
        String nodeID = vertex.getVertexId().toString();
        String nodeVal = val.toString();

        getRecordWriter().write(new Text(nodeID), new Text(nodeVal));
        /*      no more database access
        if (numResults < 0) {
            numResults = vertex.getContext().getConfiguration().getInt(NUM_RESULTS, 10);
        }

        VLongArrayListWritable val = vertex.getVertexValue();
        String nodeID = vertex.getVertexId().toString();
        String nodeVal = val.toString();

        System.err.println("numResults = " + numResults);

        String[] friends = nodeVal.split(" ");
        LinkedList<Integer> list = new LinkedList<Integer>();
        int num = 0;

        for (String f : friends) {
            if (f != null && !f.isEmpty()) {
                list.add(Integer.parseInt(f));
                if (++num >= numResults) {
                    break;
                }
            }
        }

        TaskThreeTypeAccessor p = TaskThreeTypeAccessor.getInstance();
        p.setVertex(Integer.parseInt(nodeID), list);
        p.storeEntry();
        */
    }
}
