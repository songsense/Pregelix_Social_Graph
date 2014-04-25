package edu.uci.ics.biggraph.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.uci.ics.pregelix.api.io.WritableSizable;

public class HelloCntParentIdWritable implements WritableSizable {
	private VLongWritable HelloCounter = new VLongWritable();
	private VLongWritable parentId = new VLongWritable();

    public HelloCntParentIdWritable() {
    	HelloCounter.set(-1L);
        parentId.set(-1L);
    }

    public void setHelloCounterParentId(long vv, long vi) {
    	HelloCounter.set(vv);
        parentId.set(vv);
    }

    public long getHelloCounter() {
        return HelloCounter.get();
    }

    public long getParentId() {
        return parentId.get();
    }

	@Override
	public void readFields(DataInput arg0) throws IOException {
		HelloCounter.readFields(arg0);
		parentId.readFields(arg0);
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		HelloCounter.write(arg0);
		parentId.write(arg0);
	}

	@Override
	public int sizeInBytes() {
		return HelloCounter.sizeInBytes() + parentId.sizeInBytes();
	}

    @Override
    public String toString() {
        return HelloCounter.toString() + " parent id: " + parentId.toString();
    }

}
