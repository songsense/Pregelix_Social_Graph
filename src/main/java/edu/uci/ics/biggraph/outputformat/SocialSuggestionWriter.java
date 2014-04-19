package edu.uci.ics.biggraph.outputformat;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;

import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.io.VLongArrayListWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat.TextVertexWriter;

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
        getRecordWriter().write(new Text(vertex.getVertexId().toString()), 
                new Text(val.toString()));
    }
}
