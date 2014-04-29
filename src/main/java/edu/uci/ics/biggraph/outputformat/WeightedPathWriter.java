package edu.uci.ics.biggraph.outputformat;

import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.io.WeightedPathWritable;
import edu.uci.ics.biggraph.servlet.TaskOneTypeAccessor;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by soushimei on 4/13/14.
 */
public class WeightedPathWriter extends
        TextVertexOutputFormat.TextVertexWriter<VLongWritable, WeightedPathWritable, FloatWritable> {
    public WeightedPathWriter(RecordWriter<Text, Text> lineRecordWriter) {
        super(lineRecordWriter);
    }

    @Override
    public void writeVertex(Vertex<VLongWritable, WeightedPathWritable, FloatWritable, ?> vertex) throws IOException,
            InterruptedException {
        getRecordWriter().write(new Text(vertex.getVertexId().toString()),
                new Text(vertex.getVertexValue().toString() ));

        synchronized (this) {
            int targetId = (int) vertex.getVertexId().get();
            double weight = (int) vertex.getVertexValue().getWeight();
            ArrayList<Double> pathInDouble = vertex.getVertexValue().getPathArrayList();
            LinkedList<Integer> path = new LinkedList<Integer>();

            for (double node : pathInDouble) {
                int n = (int) node;
                path.add(n);
            }

            TaskOneTypeAccessor.getInstance().setVertex(targetId, weight, path);
            TaskOneTypeAccessor.getInstance().storeEntry();
        }
    }
}