package kkkjjjmmm.test;

import kkkjjjmmm.slicer.Util;

public class Expriment {
	
	public static void main(String[] args) {
		boolean b = Util.Bernoulli(0.5);
		int x;
		if(b) {
			x = 5;
		}else {
			x = 7;
		}
		
		int z = 2 * x;
		boolean w = z>11;
		Util.Observe(w);
		w = true;
		int y = w?3:4;

		int w1 = 5*y + x;

		System.out.println(w1);
		fake(y);
	}
	
	public static int fake(int b) {
        return b;
    }
}
