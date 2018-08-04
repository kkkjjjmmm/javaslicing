package kkkjjjmmm.slicer;

import java.util.Random;

public class ProbUtil {
	
	private static Random random;    
    private static long seed;        
    
    static {
        seed = System.currentTimeMillis();
        random = new Random(seed);
    }
	
	 public static double Uniform() {
	        return random.nextDouble();
	    }
	
	 /* return true with probability p
	  * return false with probability 1-p*/
	 public static boolean Bernoulli(double p) {
	        if (!(p >= 0.0 && p <= 1.0)) {
	            throw new IllegalArgumentException("Probability p must be between 0.0 and 1.0: " + p);
	        }
	        return Uniform() < p;
	    }

	    public static boolean Bernoulli() {
	        return Bernoulli(0.5);
	    }
	
	public static void Observe(boolean b) {
		while(!b) {}
	}
}
