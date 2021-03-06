package edu.uci.ics.biggraph.inputformat;

import java.io.IOException;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import edu.uci.ics.biggraph.io.HelloCntParentIdWritable;
import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.pregelix.api.io.VertexReader;
import edu.uci.ics.pregelix.api.io.text.TextVertexInputFormat;
import edu.uci.ics.pregelix.example.io.VLongWritable;

/**
 * Created by liqiangw on 5/18/14.
 */
public class SubGraphInputFormat extends
    TextVertexInputFormat<VLongWritable, IntWritable, FloatWritable, HelloCntParentIdWritable> {

    @Override
    public VertexReader<VLongWritable, IntWritable, FloatWritable, HelloCntParentIdWritable>
        createVertexReader(InputSplit split, TaskAttemptContext context) throws IOException {

        // Here we just reuse the related class in SpanningTreeInputFormat.java
        return new SpanningTreeGraphReader(textInputFormat.createRecordReader(split, context));
    }
}
