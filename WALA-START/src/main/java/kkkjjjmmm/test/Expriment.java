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
		Util.Observe(z>11);
		int y = x;
		
		fake(y);
	}
	
	public static int fake(int b) {
        return b;
    }
}
