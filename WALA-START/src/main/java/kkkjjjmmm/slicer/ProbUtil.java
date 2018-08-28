package kkkjjjmmm.slicer;

import java.util.Random;

import org.apache.commons.math3.distribution.BinomialDistribution;

public class ProbUtil {
	
	private static Random random;    
    private static long seed;        

    static {
        seed = System.currentTimeMillis();
        random = new Random(seed);
    }
	
	 public static double Uniform() {//number between 0-1
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

	 public static boolean Bernoulli() {//Bernoulli distribution with p=0.5
	     return Bernoulli(0.5);
	 }
	 
	 
	 public static void Observe(boolean b) {
		 while(!b) {}
	 }
	 
	 public static int flip(double p) {
		 BinomialDistribution bino = new BinomialDistribution(1, p);
		 return bino.sample();
	 }
}
