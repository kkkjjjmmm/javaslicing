package kkkjjjmmm.test;

import kkkjjjmmm.slicer.ProbUtil;

public class burglarAlarm {

	public static void main(String[] args) {
		int earthquake = ProbUtil.flip(0.0001);
		int burglary = ProbUtil.flip(0.001);
		boolean earthquakebool = earthquake == 1;
		boolean burglarybool = burglary == 1;
		boolean alarm = earthquakebool||burglarybool;
		
		int phoneWorking = 0;
		if (earthquakebool) {
			phoneWorking = ProbUtil.flip(0.7);
		}else {
			phoneWorking = ProbUtil.flip(0.99);
		}
		
		int maryWakes = write(0);
	    if (alarm){
	        if (earthquakebool){
	            maryWakes = ProbUtil.flip(0.8);
	        }else{
	            maryWakes = ProbUtil.flip(0.6);
	        }
	    }else{
	        maryWakes = ProbUtil.flip(0.2);
	    }
	    
	    boolean phoneWorkingbool = phoneWorking == 1;
	    boolean maryWakesbool = maryWakes == 1;
	    boolean called = maryWakesbool && phoneWorkingbool;
	    ProbUtil.Observe(called);

	    burglary = fake(burglary);
	}
	
	public static int fake(int y) {
		return y;
	}
	
	public static int write(int y) {
		return y;
	}
	
	public static int writeChain(int currentValue, int previousValue) {
		return currentValue;
	}

}
