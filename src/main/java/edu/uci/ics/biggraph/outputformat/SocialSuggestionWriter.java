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

        /*
         * Assemble vertex payload as part of AQL UPDATE command.
         * Find out the fields format:
         * @see https://docs.google.com/document/d/1HvSHJrj2qdY6Zr8wCQRlMVbTfrFzz7qnOhqEed3J4DQ/edit
         */
        System.out.println("[OUTPUT] ID: " + nodeID + ", Val = " + nodeVal);
        String[] items = new String[2];
        StringBuilder vals = new StringBuilder();
        items[0] = "\"node_id\":" + nodeID;
        vals.append("\"suggested_friends\":" + "{{");
        String[] ss = nodeVal.split(" ");
        for (int i = 0; i < ss.length; i++) {
            vals.append(ss[i]);
            if (i != ss.length - 1) {
                vals.append(",");
            }
        } // XXX: should we put "null" afterwards?
        vals.append("}}");
        items[1] = vals.toString();
        System.out.println(items[1]);

        String aql = URLGenerator.update("Tasks", "TaskThree", items);
        aql = URLGenerator.cmdParser(aql);
        String url = URLGenerator.generate("localhost", 19002, RestAPI.UPDATE, aql);
        Commander.sendGet(url); // no payload to get
    }

}
