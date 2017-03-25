package ca.dal.csci6405.project.mrjob03;

import java.io.*;
import java.util.*;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

public class HDPReducer extends Reducer<Outcome,IntWritable,Outcome,IntWritable> {
    public void reduce( Outcome key, Iterable<IntWritable> values, Context context )
            throws IOException, InterruptedException {
        int cnt = 0;
        for ( Iterator<IntWritable> it = values.iterator(); it.hasNext(); cnt += Integer.parseInt(it.next().toString()) );
        context.write(key,new IntWritable(cnt));
    }
}

