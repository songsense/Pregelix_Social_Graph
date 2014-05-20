package edu.uci.ics.biggraph.outputformat;

import edu.uci.ics.biggraph.algo.SubGraphVertex;
import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.IntWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.servlet.TaskFiveTypeAccessor;
import edu.uci.ics.pregelix.api.graph.Edge;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.VertexWriter;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by liqiangw on 5/18/14.
 */
public class SubGraphOutputFormat extends
        TextVertexOutputFormat<VLongWritable, IntWritable, FloatWritable> {

    @Override
    public VertexWriter<VLongWritable, IntWritable, FloatWritable>
        createVertexWriter(TaskAttemptContext context) throws IOException, InterruptedException {
        RecordWriter<Text, Text> recordWriter = textOutputFormat.getRecordWriter(context);

        return new SubGraphWriter(recordWriter);
    }
}

class SubGraphWriter extends
        TextVertexOutputFormat.TextVertexWriter<VLongWritable, IntWritable, FloatWritable> {
    public SubGraphWriter(RecordWriter<Text, Text> lineRecordWriter) {
        super(lineRecordWriter);
    }

    @Override
    public void writeVertex(Vertex<VLongWritable, IntWritable, FloatWritable, ?> vertex)
            throws IOException, InterruptedException {
        if (vertex.getVertexValue().get() != Integer.MAX_VALUE) {
            getRecordWriter().write(new Text(vertex.getVertexId().toString()),
                    new Text(buildValueLine(vertex)));

            int sourceNode = (int) vertex.getVertexId().get();
            String id = SOURCE_ID + "_" + sourceNode;
            String label = ""; // We cannot provide label in back-end
            LinkedList<Integer> targetNodes = new LinkedList<Integer>();
            for (Edge<VLongWritable, FloatWritable> edge : vertex.getEdges()) {
                if (edge.getEdgeValue().get() > 0.0f) {
                    targetNodes.add((int) edge.getDestVertexId().get());
                }
            }

            TaskFiveTypeAccessor p = TaskFiveTypeAccessor.getInstance();
            p.setVertex(id, SOURCE_ID, sourceNode, label, targetNodes);
            p.storeEntry();
        } else {
            getRecordWriter().write(new Text(vertex.getVertexId().toString()),
                    new Text("Not included!"));
        }
    }

    private String buildValueLine(Vertex<VLongWritable, IntWritable, FloatWritable, ?> vertex) {
        StringBuilder sb = new StringBuilder();
        int size = 0;
        for (Edge<VLongWritable, FloatWritable> edge : vertex.getEdges()) {
            if (edge.getEdgeValue().get() > 0.0f) {
                size++;
                sb.append(" ").append(edge.getDestVertexId().get()).append(" ")
                        .append(edge.getEdgeValue().get());
            }
        }
        return Integer.toString(size) + sb.toString();
    }

    public static final int SOURCE_ID = (int) Vertex.getContext().getConfiguration().
            getLong(SubGraphVertex.SOURCE_ID, SubGraphVertex.SOURCE_ID_DEFAULT);
}



