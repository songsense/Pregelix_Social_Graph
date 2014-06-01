package edu.uci.ics.biggraph.outputformat;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;

import edu.uci.ics.biggraph.io.DoubleWritable;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat.TextVertexWriter;
import edu.uci.ics.pregelix.example.io.VLongWritable;

/**
 * Simple VertexWriter that supports output format
 */
public class SimpleWriter extends
        TextVertexWriter<VLongWritable, DoubleWritable, FloatWritable> {
    public SimpleWriter(RecordWriter<Text, Text> lineRecordWriter) {
        super(lineRecordWriter);
    }

    @Override
    public void writeVertex(Vertex<VLongWritable, DoubleWritable, FloatWritable, ?> vertex) throws IOException,
            InterruptedException {
        getRecordWriter().write(new Text(vertex.getVertexId().toString()),
                new Text(vertex.getVertexValue().toString()));
    }
}
