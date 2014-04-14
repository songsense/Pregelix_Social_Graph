package edu.uci.ics.biggraph.io;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

import edu.uci.ics.pregelix.api.util.ArrayListWritable;

/**
 * Created by soushimei on 4/13/14.
 */
public class VLongArrayListWritable extends ArrayListWritable {

    public void setClass() {
        Class<VLongWritable> refClass = VLongWritable.class;
        setClass(refClass);
    }

    @Override
    public String toString() {
        Iterator itr = this.iterator();
        StringBuffer sb = new StringBuffer();
        while (itr.hasNext()) {
            VLongWritable value = (VLongWritable) itr.next();
            sb.append(value.get()).append(" ");
        }
        return sb.toString();
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.clear();
        int numValues = in.readInt(); // read number of values
        for (int i = 0; i < numValues; i++) {
            VLongWritable value = new VLongWritable();
            value.readFields(in); // read a value
            add(value); // store it in values
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void write(DataOutput out) throws IOException {
        int numValues = size();
        out.writeInt(numValues); // write number of values
        for (int i = 0; i < numValues; i++) {
            Long value = ((VLongWritable) get(i)).get();
            VLongWritable vLongWritable = new VLongWritable(value);
            vLongWritable.write(out);
        }
    }

}
