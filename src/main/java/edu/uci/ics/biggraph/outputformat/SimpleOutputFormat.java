package edu.uci.ics.biggraph.outputformat;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import edu.uci.ics.biggraph.io.DoubleWritable;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.pregelix.api.io.VertexWriter;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat;
import edu.uci.ics.pregelix.example.io.VLongWritable;
/**
 * Simple VertexOutputFormat that supports {@link SimplePageRankVertex}
 */
public class SimpleOutputFormat extends
TextVertexOutputFormat<VLongWritable, DoubleWritable, FloatWritable> {
	@Override
	public VertexWriter<VLongWritable, DoubleWritable, FloatWritable> createVertexWriter(TaskAttemptContext context)
	    throws IOException, InterruptedException {
		RecordWriter<Text, Text> recordWriter = textOutputFormat.getRecordWriter(context);
		return new SimpleWriter(recordWriter);
	}
}




