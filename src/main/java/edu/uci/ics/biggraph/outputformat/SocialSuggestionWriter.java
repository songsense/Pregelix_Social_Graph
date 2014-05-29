package edu.uci.ics.biggraph.outputformat;

import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.io.VLongArrayListWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.servlet.TaskThreeTypeAccessor;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat.TextVertexWriter;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;

import java.io.IOException;
import java.util.LinkedList;

public class SocialSuggestionWriter extends 
TextVertexWriter<VLongWritable, VLongArrayListWritable, IntWritable>{

    public SocialSuggestionWriter(RecordWriter<Text, Text> arg0) {
        super(arg0);
    }

    @Override
    public void writeVertex(
            Vertex<VLongWritable, VLongArrayListWritable, IntWritable, ?> vertex)
            throws IOException, InterruptedException {
        VLongArrayListWritable val = vertex.getVertexValue();
        String nodeID = vertex.getVertexId().toString();
        String nodeVal = val.toString();

//        getRecordWriter().write(new Text(nodeID), new Text(nodeVal));

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
    }

    public static final String NUM_RESULTS = "SocialSuggestionVertex.results";
    private static int numResults = Vertex.getContext().getConfiguration().getInt(NUM_RESULTS, 10);
}
