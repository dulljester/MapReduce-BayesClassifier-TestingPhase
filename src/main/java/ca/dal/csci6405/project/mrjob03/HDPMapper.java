package ca.dal.csci6405.project.mrjob03;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import sun.util.resources.cldr.nyn.CalendarData_nyn_UG;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HDPMapper extends Mapper<LongWritable,Text,Outcome,IntWritable> {
    private final IntWritable ONE = new IntWritable(1);
    private BayesianClassifier classifier = new BayesianClassifier();
    {
        classifier.readModel(new File("/home/serikzhan/Classes/priors.txt"));
    }
    private long readPokerHand( Scanner scan ) {
        long u = 0;
        int []card = new int[MyUtils.M];
        int i,j,k,m = 0;
        for ( i = 0; i < MyUtils.M; ++i ) {
            // 0 <= suit <= 3 ==> 2 bits
            // 1 <= rank <= 13 ==> 4 bits
            int suit = scan.nextInt()-1, rank = scan.nextInt();
            MyUtils.myAssert(0<=suit&&suit<=3);
            MyUtils.myAssert(1<=rank&&rank<=13);
            card[m++] = suit|(rank<<2);
        }
        //bubble sort the cards in descending order: this way, we "normalize" the hand
        for ( boolean flag = true; flag; )
            for ( flag = false, i = 0; i < MyUtils.M-1; ++i )
                if ( card[i] < card[i+1] ) {
                    k = card[i];
                    card[i] = card[i+1];
                    card[i+1] = k;
                    flag = true ;
                }
        for ( i = 0; i < MyUtils.M; u |= (((long)card[i]) << (i*MyUtils.WIDTH)), ++i ) ;
        long kk = scan.nextLong()+1;
        MyUtils.myAssert(1 <= kk && kk <= 10);
        return u|(kk<<(i*MyUtils.WIDTH));
    }
    public void map( LongWritable key, Text value, Context con ) throws IOException, InterruptedException {
        String txt = value.toString();
        for ( String line : txt.split("\n") ) {
            Scanner scan = new Scanner(line);
            long hand = readPokerHand(scan);
            int predictedClass = classifier.getPrediction(hand);
            int realClass = (int)(hand>>(MyUtils.WIDTH*MyUtils.M));
            con.write(new Outcome(realClass,predictedClass),ONE);
        }
    }
}
