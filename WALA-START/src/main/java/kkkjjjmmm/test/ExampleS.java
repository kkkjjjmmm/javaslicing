package kkkjjjmmm.test;

import kkkjjjmmm.slicer.ProbUtil;

public class ExampleS {
	public static void main(String [] args) {
		boolean l = SliceL();
		System.out.println(l);
	}
	
	public static boolean SliceL() {
		boolean d, l, i, s, g;
		d = ProbUtil.Bernoulli(0.6);
		i = ProbUtil.Bernoulli(0.7);
		if(!i && !d) {
			g = ProbUtil.Bernoulli(0.3);
		}else if(!i && d) {
			g = ProbUtil.Bernoulli(0.05);
		}else if(i && !d) {
			g = ProbUtil.Bernoulli(0.9);
		}else {
			g = ProbUtil.Bernoulli(0.5);
		}
		
		ProbUtil.Observe(g == false);

		if(!i) {
			s = ProbUtil.Bernoulli(0.2);
		}else {
			s = ProbUtil.Bernoulli(0.95);
		}
		
		if(!g) {
			l = ProbUtil.Bernoulli(0.1);			
		}else {
			l = ProbUtil.Bernoulli(0.4);
		}
		s = fake(s);
		return s;
	}
	
	public static boolean fake(boolean b) {
		return b;
	}
}
