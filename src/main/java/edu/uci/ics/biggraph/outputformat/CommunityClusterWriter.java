package edu.uci.ics.biggraph.outputformat;

import edu.uci.ics.biggraph.io.FloatWritable;
import edu.uci.ics.biggraph.io.VLongIntWritable;
import edu.uci.ics.biggraph.io.VLongWritable;
import edu.uci.ics.biggraph.servlet.TaskTwoTypeAccessor;
import edu.uci.ics.pregelix.api.graph.Vertex;
import edu.uci.ics.pregelix.api.io.text.TextVertexOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;

import java.io.IOException;

/**
 * Created by soushimei on 4/19/14.
 */
public class CommunityClusterWriter extends
        TextVertexOutputFormat.TextVertexWriter<VLongWritable, VLongIntWritable, FloatWritable> {
    public CommunityClusterWriter(RecordWriter<Text, Text> lineRecordWriter) {
        super(lineRecordWriter);
    }

    @Override
    public void writeVertex(Vertex<VLongWritable, VLongIntWritable, FloatWritable, ?> vertex) throws IOException,
            InterruptedException {
        getRecordWriter().write(new Text(vertex.getVertexId().toString()),
                new Text(vertex.getVertexValue().toString()));

        TaskTwoTypeAccessor t = TaskTwoTypeAccessor.getInstance();
        t.setVertex((int) vertex.getVertexId().get(), (int) vertex.getVertexValue().getVertexId());
        t.storeEntry();
    }
}