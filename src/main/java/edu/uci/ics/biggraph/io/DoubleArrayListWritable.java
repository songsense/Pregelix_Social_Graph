package edu.uci.ics.biggraph.io;

import edu.uci.ics.pregelix.api.io.WritableSizable;
import edu.uci.ics.pregelix.api.util.ArrayListWritable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by soushimei on 4/13/14.
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class DoubleArrayListWritable extends ArrayListWritable implements WritableSizable {

    @SuppressWarnings("unchecked")
    public void setClass() {
        Class<DoubleWritable> refClass = DoubleWritable.class;
        setClass(refClass);
    }

    @Override
    public String toString() {
        Iterator itr = this.iterator();
        StringBuffer sb = new StringBuffer();
        while (itr.hasNext()) {
            DoubleWritable value = (DoubleWritable) itr.next();
            sb.append(value.get()).append(" ");
        }
        return sb.toString();
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.clear();
        int numValues = in.readInt(); // read number of values
        // for debug
//        System.out.print("======");
//        System.out.print("readFields: " + Integer.toString(numValues));
        for (int i = 0; i < numValues; i++) {
            DoubleWritable value = new DoubleWritable();
            value.readFields(in); // read a value
            add(value); // store it in values
            // for debug
//            System.out.print(" " + value.toString());
        }
//        System.out.println("======");
    }

    @Override
    public void write(DataOutput out) throws IOException {
        int numValues = size();
        // for debug
//        System.out.print("======");
//        System.out.print("write: " + Integer.toString(numValues));
        out.writeInt(numValues); // write number of values
        for (int i = 0; i < numValues; i++) {
            Double value = ((DoubleWritable) get(i)).get();
            DoubleWritable doubleWritable = new DoubleWritable(value);
//            System.out.print("");
//            System.out.println(value);
            doubleWritable.write(out);
//            System.out.print(" " + doubleWritable.toString());
        }
//        System.out.println("======");
    }

    @Override
    public int sizeInBytes() {
        return 4 + 8 * size();
    }
}
