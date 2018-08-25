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
	 
//	 public static double Uniform(double a, double b) {//uniform number between a-b
//		 return Uniform()*(b-a) + a;
//	 }
	
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
	 
	 
	 
//	 public static double Guassian(double mean, double variance) {//guassian distribution
//		 return Math.sqrt(variance)*random.nextGaussian()+mean;
//	 }
//	 
//	 public static double Guassian() {//mean = 0, variance = 1 guassian distribution
//		 return random.nextGaussian();
//	 }
//	 
//	 public static int UniformInt(int a, int b) {
//		 return random.nextInt(b-a)+a;
//	 }
//	 
//	 public static double Beta(double alpha, double beta) {
//	        double a = alpha + beta;
//	        double b = Math.sqrt((a - 2) / (2 * alpha * beta - a));
//	        if (Math.min(alpha, beta) <= 1) {
//	            b = Math.max(1 / alpha, 1 / beta);
//	        }
//	        double c = alpha + 1 / b;
//	        double W = 0;
//	        boolean reject = true;
//	        while (reject) {
//	            double U1 = Math.random();
//	            double U2 = Math.random();
//	            double V = b * Math.log(U1 / (1 - U1));
//	            W = alpha * Math.exp(V);
//	            reject = (a * Math.log(a / (beta + W)) + c * V - Math.log(4)) < Math.log(U1 * U1 * U2);
//	        }
//	        return (W / (beta + W));
//	 }
	 
	 public static void Observe(boolean b) {
		 while(!b) {}
	 }
	 
	 public static int flip(double p) {
		 BinomialDistribution bino = new BinomialDistribution(1, p);
		 return bino.sample();
	 }
}
