package ca.dal.csci6405.project.mrjob03;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ca.dal.csci6405.project.mrjob03.MyUtils.MASK;

/**
 * Created by serikzhan on 25/03/17.
 */
public class Outcome implements WritableComparable<Outcome> {
    private int key;
    public Outcome() {}
    public Outcome( int real, int predicted ) {
        key = (predicted|(real<<4));
    }
    public int realClassLabel() {
        return key>>4;
    }
    public int predictedClassLabel() {
        return (int)(key&MASK(4));
    }

    @Override
    public String toString() {
        return String.format("realClass = %d, predictedClass = %d",realClassLabel(),predictedClassLabel());
    }

    @Override
    public int compareTo(Outcome o) {
        return key-o.key;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(toString());
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        Pattern num = Pattern.compile("(\\d+)");
        Matcher m = num.matcher(dataInput.readUTF());
        m.find();
        int r = Integer.parseInt(m.group(1));
        m.find();
        int p = Integer.parseInt(m.group(1));
        key = (p|(r<<4));
    }
}
