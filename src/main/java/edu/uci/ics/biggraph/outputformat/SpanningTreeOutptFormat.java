package edu.uci.ics.biggraph.outputformat;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.VertexWriter;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat;
import edu.uci.ics.pregelix.example.io.VLongWritable;
/**
 * Simple VertexOutputFormat that supports {@link edu.uci.ics.biggraph.algo.SpanningTreeVertex}
 */
public class SpanningTreeOutptFormat extends
TextVertexOutputFormat<VLongWritable, IntWritable, FloatWritable> {
	
	public class SpanningTreeWriter extends
    TextVertexWriter<VLongWritable, IntWritable, FloatWritable> {
		public SpanningTreeWriter(RecordWriter<Text, Text> lineRecordWriter) {
		    super(lineRecordWriter);
		}
		@Override
		public void writeVertex(Vertex<VLongWritable, IntWritable, FloatWritable, ?> vertex) throws IOException,
		        InterruptedException {
		    getRecordWriter().write(new Text(vertex.getVertexId().toString()),
		            new Text(vertex.toString()));
		}
	}
	
	@Override
	public VertexWriter<VLongWritable, IntWritable, FloatWritable> createVertexWriter(TaskAttemptContext context)
	    throws IOException, InterruptedException {
		RecordWriter<Text, Text> recordWriter = textOutputFormat.getRecordWriter(context);
		return new SpanningTreeWriter(recordWriter);
	}
}