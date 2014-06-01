package edu.uci.ics.biggraph.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.uci.ics.pregelix.api.io.WritableSizable;
import edu.uci.ics.pregelix.example.io.VLongWritable;

public class VLongIntWritable implements WritableSizable {
	private VLongWritable vlongWritable = new VLongWritable();
	private IntWritable intWritable = new IntWritable();

    public VLongIntWritable() {
        vlongWritable.set(-1l);
        intWritable.set(-1);
    }

    public void set(long vertexId, int count) {
        vlongWritable.set(vertexId);
        intWritable.set(count);
    }

    public long getVertexId() {
        return vlongWritable.get();
    }

    public int getCount() {
        return intWritable.get();
    }

	@Override
	public void readFields(DataInput arg0) throws IOException {
		vlongWritable.readFields(arg0);
		intWritable.readFields(arg0);
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		vlongWritable.write(arg0);
        intWritable.write(arg0);
	}

	@Override
	public int sizeInBytes() {
		return vlongWritable.sizeInBytes() + intWritable.sizeInBytes();
	}

    @Override
    public String toString() {
        return vlongWritable.toString() + " " + intWritable.toString();
    }

}
