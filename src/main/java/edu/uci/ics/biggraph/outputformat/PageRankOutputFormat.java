package edu.uci.ics.biggraph.outputformat;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import edu.uci.ics.biggraph.io.DoubleWritable;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.VertexWriter;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat;
import edu.uci.ics.pregelix.example.io.VLongWritable;

/**
 * Created by soushimei on 5/16/14.
 */

public class PageRankOutputFormat extends
        TextVertexOutputFormat<VLongWritable, DoubleWritable, FloatWritable> {

    public class PageRankWriter extends
            TextVertexWriter<VLongWritable, DoubleWritable, FloatWritable> {
        public PageRankWriter(RecordWriter<Text, Text> lineRecordWriter) {
            super(lineRecordWriter);
        }
        @Override
        public void writeVertex(Vertex<VLongWritable, DoubleWritable, FloatWritable, ?> vertex) throws IOException,
                InterruptedException {
            getRecordWriter().write(new Text(vertex.getVertexId().toString()),
                    new Text(vertex.toString()));
        }
    }

    @Override
    public VertexWriter<VLongWritable, DoubleWritable, FloatWritable> createVertexWriter(TaskAttemptContext context)
            throws IOException, InterruptedException {
        RecordWriter<Text, Text> recordWriter = textOutputFormat.getRecordWriter(context);
        return new PageRankWriter(recordWriter);
    }
}