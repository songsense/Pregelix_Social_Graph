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

        getRecordWriter().write(new Text(nodeID), new Text(nodeVal));

        String[] friends = nodeVal.split(" ");
        LinkedList<Integer> list = new LinkedList<Integer>();
        for (String f : friends) {
            list.add(Integer.parseInt(f));
        }
        TaskThreeTypeAccessor p = TaskThreeTypeAccessor.getInstance();
        p.setVertex(Integer.parseInt(nodeID), list);
        p.storeEntry();
    }

}
