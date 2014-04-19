package edu.uci.ics.biggraph.outputformat;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.io.VLongArrayListWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.pregelix.api.io.VertexWriter;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat;

public class SocialSuggestionOutputFormat extends
TextVertexOutputFormat<VLongWritable, VLongArrayListWritable, IntWritable> {

    @Override
    public VertexWriter<VLongWritable, VLongArrayListWritable, IntWritable> createVertexWriter(
            TaskAttemptContext context) throws IOException,
            InterruptedException {
        RecordWriter<Text, Text> recordWriter = textOutputFormat.getRecordWriter(context);
        return new SocialSuggestionWriter(recordWriter);
    }
}
