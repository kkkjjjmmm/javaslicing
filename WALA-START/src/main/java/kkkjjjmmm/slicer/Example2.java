package kkkjjjmmm.slicer;

public class Example2 {
	public static void main(String[] args) {
		boolean x = SliceL();
		System.out.println(x);
	}
	public static boolean SliceL() {
		boolean x, b, c;
		x = Util.Bernoulli(0.5);
		b = x;
		c = Util.Bernoulli(0.5);
		if(c) {
			c = false;
			while(!c) {
				c = true;
			}
		}
		while(c) {
			b = !b;
			c = Util.Bernoulli(0.5);
		}
		b = fake(b);
		Util.Observe(b);
		return x;
	}
	
	
	public static boolean fake(boolean b) {
		return b;
	}

}
