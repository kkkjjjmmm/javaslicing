package kkkjjjmmm.test;

import org.apache.commons.math3.distribution.BetaDistribution;
import kkkjjjmmm.slicer.ProbUtil;

public class CoinBias {

	public static void main(String[] args) {		
		int [] observedResults = new int[] {1,1,0,1,0}; 
		BetaDistribution beta = new BetaDistribution(2,5);
		double bias = beta.sample();
		int [] tossResults = new int[5];
		for(int i = 0;i<5;i++) {
			tossResults[i] = ProbUtil.flip(bias);
		}
		
		for(int i = 0;i<5;i++) {
			ProbUtil.Observe(tossResults[i] == observedResults[i]);
			tossResults[i] = fake(tossResults[i]);
		}		
	}

	public static int fake(int y) {
		return y;
	}
}
