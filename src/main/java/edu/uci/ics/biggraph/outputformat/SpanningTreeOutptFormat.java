package edu.uci.ics.biggraph.outputformat;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.VertexWriter;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.io.FloatWritable;
/**
 * Simple VertexOutputFormat that supports {@link SimplePageRankVertex}
 */
public class SpanningTreeOutptFormat extends
TextVertexOutputFormat<VLongWritable, VLongWritable, FloatWritable> {
	
	public class SpanningTreeWriter extends
    TextVertexWriter<VLongWritable, VLongWritable, FloatWritable> {
		public SpanningTreeWriter(RecordWriter<Text, Text> lineRecordWriter) {
		    super(lineRecordWriter);
		}
		@Override
		public void writeVertex(Vertex<VLongWritable, VLongWritable, FloatWritable, ?> vertex) throws IOException,
		        InterruptedException {
		    getRecordWriter().write(new Text(vertex.getVertexId().toString()),
		            new Text(vertex.getVertexValue().toString()));
		}
	}
	
	@Override
	public VertexWriter<VLongWritable, VLongWritable, FloatWritable> createVertexWriter(TaskAttemptContext context)
	    throws IOException, InterruptedException {
		RecordWriter<Text, Text> recordWriter = textOutputFormat.getRecordWriter(context);
		return new SpanningTreeWriter(recordWriter);
	}
}