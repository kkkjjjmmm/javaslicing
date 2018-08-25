package kkkjjjmmm.test;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import kkkjjjmmm.slicer.ProbUtil;


public class Hiv {

	public static void main(String[] args) {
		NormalDistribution guass = new NormalDistribution();
		double muA1 = guass.sample();
		double muA2 = guass.sample();
		UniformRealDistribution uniform = new UniformRealDistribution(0,100);
		double sigmaA1 = uniform.sample();
		double sigmaA2 = uniform.sample();
		
		double [] a1 = new double[2];
		double [] a2 = new double[2];
		
		NormalDistribution normal = new NormalDistribution(muA1,sigmaA1);
		NormalDistribution normal2 = new NormalDistribution(0.1*muA2,sigmaA2);
		for(int i = 0; i<2; i++) {
			a1[i] = normal.sample();
			a2[i] = normal2.sample();
		}
		
		double sigmaY = uniform.sample();
		double [] dataY = new double[] {
				4.2426407, 6.0827625, 3.6055513, 3.6055513, 3.4641016, 5.4772256, 5.2915026, 5.4772256, 
				5.5677644, 5.0990195, 5.3851648
		};
		
		int [] dataPerson = new int[] {1,1,1,1,1,2,2,2,2,2};
		
		double [] dataTime = new double[] {
				0,0.5583333,0.7883333,1.4208333,1.9383333,0,0.23,0.4791667,0.7283333,0.9583333
		};
		
		for(int j=0;j<dataY.length;j++) {
			double yHat = a1[dataPerson[j]-1] + a2[dataPerson[j]-1]*dataTime[j];
			NormalDistribution normal3 = new NormalDistribution(yHat,sigmaY);
			double y = normal3.sample();
			ProbUtil.Observe(y==dataY[j]);
			y = fake(y);
		}
	}
	
	public static double fake(double y) {
		return y;
	}
}
