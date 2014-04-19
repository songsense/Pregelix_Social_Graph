package edu.uci.ics.biggraph.outputformat;

import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.VLongIntWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.pregelix.api.io.VertexWriter;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;

/**
 * Created by soushimei on 4/19/14.
 */
public class CommunityClusterOutputFormat extends
        TextVertexOutputFormat<VLongWritable, VLongIntWritable, FloatWritable> {
    @Override
    public VertexWriter<VLongWritable, VLongIntWritable, FloatWritable> createVertexWriter(TaskAttemptContext context)
            throws IOException, InterruptedException {
        RecordWriter<Text, Text> recordWriter = textOutputFormat.getRecordWriter(context);
        return new CommunityClusterWriter(recordWriter);
    }
}
