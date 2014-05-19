package edu.uci.ics.biggraph.inputformat;

/**
 * Created by soushimei on 5/16/14.
 */

import edu.uci.ics.biggraph.io.DoubleWritable;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.VertexReader;
import edu.uci.ics.pregelix.api.io.text.TextVertexInputFormat;
import edu.uci.ics.pregelix.api.util.BspUtils;
import org.apache.hadoop.io.*;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PageRankVertexInputFormat extends
        TextVertexInputFormat<VLongWritable, DoubleWritable, FloatWritable, DoubleWritable> {
    @Override
    public VertexReader<VLongWritable, DoubleWritable, FloatWritable, DoubleWritable> createVertexReader(
            InputSplit split, TaskAttemptContext context) throws IOException {
        return new PageRankVertexReader(textInputFormat.createRecordReader(split, context));
    }
}

@SuppressWarnings("rawtypes")
class PageRankVertexReader extends
        TextVertexInputFormat.TextVertexReader<VLongWritable, DoubleWritable, FloatWritable, DoubleWritable> {

    private final static String separator = " ";
    private Vertex vertex;
    private VLongWritable vertexId = new VLongWritable();
    private List<VLongWritable> pool = new ArrayList<VLongWritable>();
    private int used = 0;

    public PageRankVertexReader(RecordReader<LongWritable, Text> lineRecordReader) {
        super(lineRecordReader);
    }

    @Override
    public boolean nextVertex() throws IOException, InterruptedException {
        return getRecordReader().nextKeyValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Vertex<VLongWritable, DoubleWritable, FloatWritable, DoubleWritable> getCurrentVertex() throws IOException,
            InterruptedException {
        if (vertex == null)
            vertex = (Vertex) BspUtils.createVertex(getContext().getConfiguration());

        vertex.getMsgList().clear();
        vertex.getEdges().clear();
        vertex.reset();
        Text line = getRecordReader().getCurrentValue();
        String[] fields = line.toString().split(separator);
        /**
         * for data format, see:
         * https://docs.google.com/a/uci.edu/document/d/18jaKJT3OCVdKgXPB6JMClRKvsl2IWx8vJYfiTgFjxd0/edit
         */

        if (fields.length > 0) {
            /**
             * set the src vertex id
             */
            long src = Long.parseLong(fields[0]);
            vertexId.set(src);
            vertex.setVertexId(vertexId);
            long dest;

            /**
             * get neighbor num
             */
            int neighborNum = Integer.parseInt(fields[1]);

            /**
             * set up edges & weights
             */
            for (int i = 0; i < neighborNum; ++i) {
                // set up edge
                dest = Long.parseLong(fields[2*i+2]);
                VLongWritable destId = allocate();
                destId.set(dest);

                // set up weight
                vertex.addEdge(destId, null);
            }
        }
        // vertex.sortEdges();
        return vertex;
    }

    private VLongWritable allocate() {
        if (used >= pool.size()) {
            VLongWritable value = new VLongWritable();
            pool.add(value);
            used++;
            return value;
        } else {
            VLongWritable value = pool.get(used);
            used++;
            return value;
        }
    }
}