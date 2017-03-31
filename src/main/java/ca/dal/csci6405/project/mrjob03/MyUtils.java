package ca.dal.csci6405.project.mrjob03;

public class MyUtils {
    public static int M = 5, WIDTH = 6;
    public static long BIT( int k ) { return 1L<<k; }
    public static long MASK( int k ) { return BIT(k)-1L; }
    public static int getPos( long u ) {
        for ( int i = 0; i < M; ++i )
            if ( ((u>>(i*WIDTH)) & MASK(WIDTH)) != 0 )
                return i;
        return -1;
    }

    public static void myAssert(boolean b) {
        if ( !b ) {
            int trap = 1/0;
        }
    }
}
