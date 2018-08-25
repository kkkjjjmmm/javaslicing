package kkkjjjmmm.test;

import kkkjjjmmm.slicer.ProbUtil;

public class grass {

	public static void main(String[] args) {
		int cloudy = ProbUtil.flip(0.5);
		boolean cloudybool = cloudy == 1;
		int rain = 0; 
		int sprinkler = 0;

		if (cloudybool) {
			rain = ProbUtil.flip(0.8);
			sprinkler = ProbUtil.flip(0.1);
		} else {
			rain = ProbUtil.flip(0.2);
			sprinkler = ProbUtil.flip(0.5);
		}

		boolean rainbool = rain == 1;
		boolean sprinklerbool = sprinkler == 1;
		int temp1 = ProbUtil.flip(0.7);
		boolean temp1bool = temp1 == 1;
		boolean wetRoof = temp1bool && rainbool;
		int temp2 = ProbUtil.flip(0.9);
		boolean temp2bool = temp2 == 1;
		int temp3 = ProbUtil.flip(0.9);
		boolean temp3bool = temp3 == 1;
		boolean wetGrass = temp2bool && rainbool || temp3bool && sprinklerbool;

		ProbUtil.Observe(wetGrass);

		rain = fake(rain);
	}
	
	public static int fake(int y) {
		return y;
	}

}
