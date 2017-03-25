package ca.dal.csci6405.project.mrjob03;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import java.util.Arrays;

public class HDPDriver extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        if ( args.length < 2 ) {
            System.err.printf("Supplied parameters: %s\n", Arrays.toString(args));
            System.err.printf("usage: %s <inputfile> <outputdir>\n",getClass().getSimpleName());
            System.exit(1);
        }
        Job job = Job.getInstance(getConf(),"NBC: Testing the Classifier");

        job.setJarByClass(HDPDriver.class);
        job.setMapperClass(HDPMapper.class);
        job.setReducerClass(HDPReducer.class);

        job.setInputFormatClass(NLinesInputFormat.class);

        job.setMapOutputKeyClass(Outcome.class);
        job.setMapOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        return job.waitForCompletion(true)?0:1;
    }
    public static void main( String [] args ) throws Exception {
        Configuration conf = new Configuration();
        System.exit(ToolRunner.run(conf,new HDPDriver(),args));
    }
}
