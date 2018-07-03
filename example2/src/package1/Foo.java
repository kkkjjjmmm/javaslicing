package package1;

import package2.Bar;

public class Foo {
	public static void main(String args[]) {
		Bar b1 = new Bar();
		int n = b1.times2(10);
		boolean flag = false;
		int sum = 0;
		int product = 1;
		
		if (!flag) {
			while(sum < n) {
				sum += 3;
				product = product * 2;
				flag = !flag;
			}
		}
		
		foo(sum);
		foo2(product);
	}
	
	
	private static void foo(int sum) {
		System.out.println(sum);
	}

	private static void foo2(int product) {
		System.out.println(product);
	}
}
