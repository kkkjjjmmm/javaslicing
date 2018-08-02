package kkkjjjmmm.test;

import kkkjjjmmm.slicer.Util;
//import kkkjjjmmm.slicer.Yeah;

public class Example {
	
	public static void main(String [] args) {
		boolean l = SliceL();
		System.out.println(l);
	}
	
	public static boolean SliceL() {
		boolean d, l, i, s, g;
		d = Util.Bernoulli(0.6);
		i = Util.Bernoulli(0.7);
		if(!i && !d) {
			g = Util.Bernoulli(0.3);
		}else if(!i && d) {
			g = Util.Bernoulli(0.05);
		}else if(i && !d) {
			g = Util.Bernoulli(0.9);
		}else {
			g = Util.Bernoulli(0.5);
		}
		
		//g = d||g;
		//g = Yeah.negate(g);
		boolean q4 = g==false;
		Util.Observe(q4);
		g=false;

		//d = true;
		if(!i) {
			s = Util.Bernoulli(0.2);
			//s = fake(s);
		}else {
			s = Util.Bernoulli(0.95);
		}

		if(!g) {
			l = Util.Bernoulli(0.1);
			
		}else {
			l = Util.Bernoulli(0.4);
		}
		s = fake(s);
		return s;
	}
	
	public static boolean fake(boolean b) {
		return b;
	}
}
