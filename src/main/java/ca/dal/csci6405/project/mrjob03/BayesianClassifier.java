package ca.dal.csci6405.project.mrjob03;

import org.apache.commons.collections.map.HashedMap;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by serikzhan on 25/03/17.
 */
public class BayesianClassifier {
    private static Map<String,Object> config = new HashMap<>();
    static {
        config.put(JsonGenerator.PRETTY_PRINTING,true);
    }
    private final static int M = 10, HAND = 5;
    private Map<Integer,Map<Integer,Map<Integer,Integer>>> priors = new HashMap<>();
    private Map<Integer,Map<Integer,Map<Integer,Double>>> priorsNormalized = new HashMap<>();
    {
        for ( int i = 1; i <= M; priors.put(i,new HashMap<>()), priorsNormalized.put(i,new HashMap<>()), ++i );
    }
    private Map<Integer,Map<Integer,Integer>> sums = new HashMap<>();

    private int []card = new int[M+1];
    private int total;
    public void readModel( File f ) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            for ( String s; (s = br.readLine()) != null; ) {
                if ( s.charAt(0) == '-' ) {
                    Pattern p = Pattern.compile("\"1\"\\s*:\\s*(\\d+)"); // regex for stuff like -7	[{"1":1027},{},{},{},{}]
                    Matcher m = p.matcher(s);
                    m.find();
                    card[-(new Scanner(s).nextInt())] += Integer.parseInt(m.group(1));
                }
                else {
                    Pattern num = Pattern.compile("(\\d+)\\s+");
                    Matcher m = num.matcher(s);
                    m.find();
                    int whichClass = new Scanner(m.group(1)).nextInt();
                    s = s.substring(m.end());
                    Map<Integer,Map<Integer,Integer>> counts = priors.get(whichClass);

                    JsonReaderFactory factory = Json.createReaderFactory(config);
                    JsonReader reader = factory.createReader(new StringReader(s));
                    JsonArray array = reader.readArray();
                    for ( int i = 0; i < HAND; ++i ) {
                        JsonObject object = array.getJsonObject(i);
                        counts.put(i,new TreeMap<>());
                        if ( object.size() == 0 ) continue ;
                        for ( Map.Entry<String,JsonValue> entry: object.entrySet() )
                            counts.get(i).put(Integer.parseInt(entry.getKey()),Integer.parseInt(entry.getValue().toString()));
                    }
                }
            }
            for ( int i = 1; i <= M; total += card[i++] );
            for ( int i = 1; i <= M; ++i ) {
                sums.put(i,new HashMap<>());
                Map<Integer,Map<Integer,Double>> countsNormalized = priorsNormalized.get(i);
                double a = 0;
                for ( int j = 0; j < HAND; ++j ) {
                    int s = 0;
                    for (Map.Entry<Integer, Integer> entry : priors.get(i).get(j).entrySet())
                        s += entry.getValue();
                    sums.get(i).put(j,s);
                    a += s;
                }
                for ( int j = 0; j < HAND; ++j )
                    for (Map.Entry<Integer, Integer> entry : priors.get(i).get(j).entrySet())
                        countsNormalized.get(j).put(entry.getKey(),entry.getValue()/a);
            }
        }
        catch ( IOException io ) {
            throw new RuntimeException("Could not read the model file: "+io.getMessage());
        }
    }

    public int getPrediction( long hand ) {
        int []a = new int[HAND];
        for ( int i = 0; i < HAND; ++i )
            a[i] = (int)((hand>>(MyUtils.WIDTH*i))&MyUtils.MASK(MyUtils.WIDTH));
        for ( int i = 0; i < HAND-1; ++i )
            MyUtils.myAssert(a[i] > a[i+1]);
        double []mass = new double[M+1];
        int idx = -1;
        double bestMass = 0;
        for ( int i = 1; i <= M; ++i ) {
            //Map<Integer,Map<Integer,Integer>> counts = priors.get(i);
            Map<Integer,Map<Integer,Double>> countsNormalized = priorsNormalized.get(i);
            for ( int j = 0; j < HAND; ++j ) {
                //MyUtils.myAssert(counts.containsKey(j));
                MyUtils.myAssert(countsNormalized.containsKey(j));
                /*
                double up = (!counts.get(j).containsKey(a[j])?1:counts.get(j).get(a[j])), down = (52-j);
                for ( Map.Entry<Integer,Integer> entry: counts.get(j).entrySet() )
                    if ( j == 0 || entry.getKey() < a[j-1] )
                        down += entry.getValue();
                mass[i] += Math.log(up)-Math.log(down);
                */
                double up = (!countsNormalized.get(j).containsKey(a[j])?1:countsNormalized.get(j).get(a[j])), down = (52-j);
                for ( Map.Entry<Integer,Double> entry: countsNormalized.get(j).entrySet() )
                    if ( j == 0 || entry.getKey() < a[j-1] )
                        down += entry.getValue();
                mass[i] += Math.log(up)-Math.log(down);
            }
            mass[i] += Math.log(card[i]);
            if ( idx == -1 || mass[i] >= bestMass )
                bestMass = mass[idx = i];
        }
        return idx;
    }

}

