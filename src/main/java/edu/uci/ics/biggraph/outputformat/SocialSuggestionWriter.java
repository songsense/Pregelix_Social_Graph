package edu.uci.ics.biggraph.outputformat;

import java.io.IOException;

import edu.uci.ics.biggraph.servlet.URLGenerator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;

import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.io.VLongArrayListWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat.TextVertexWriter;

import edu.uci.ics.biggraph.servlet.*;

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

        // Assemble vertex payload as part of AQL UPDATE command.
        String[] items = new String[2];
        items[0] = "\"node_id\":" + nodeID;
        items[1] = "\"suggested_friends\":" + nodeVal; // XXX: NOT SURE!

        String aql = URLGenerator.update("Tasks", "TaskThreeType", items);
        aql = URLGenerator.cmdParser(aql);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, aql);
        Commander.sendGet(url); // no payload to get
    }

}
