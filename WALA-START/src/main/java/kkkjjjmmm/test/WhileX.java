package kkkjjjmmm.test;

import kkkjjjmmm.slicer.ProbUtil;

public class WhileX {
	public static void main(String[] args) {
		boolean x = SliceL();
		System.out.println(x);
	}
	
	public static boolean SliceL() {
		boolean x, b, c;
		x = ProbUtil.Bernoulli(0.5);
		b = x;
		c = ProbUtil.Bernoulli(0.5);
		while(c) {
			b = !b;
			c = ProbUtil.Bernoulli(0.5);
		}
		
		ProbUtil.Observe(b == false);
		
		x = fake(x);
		return x;
	}
		
	public static boolean fake(boolean b) {
		return b;
	}

}
