package kkkjjjmmm.test;

import kkkjjjmmm.slicer.ProbUtil;

public class noisyOrModel {

	public static void main(String[] args) {
		 int n1 = 0, n21 = 0, n22 = 0, n31 = 0, n32 = 0, n33 = 0;
         int n0 = ProbUtil.flip(1/2);
         int n4 = ProbUtil.flip(1/2);
         
         if (n0==1){
        	 n1 = ProbUtil.flip(4/5);
        	 n21 = ProbUtil.flip(4/5);
         }else{
        	 n1 = ProbUtil.flip(1/10);
        	 n21 = ProbUtil.flip(1/10);
         }
         
         if (n4==1){
        	 n22 = ProbUtil.flip(4/5);
        	 n33 = ProbUtil.flip(4/5);
         }else{
        	 n22 = ProbUtil.flip(1/10);
        	 n33 = ProbUtil.flip(1/10);
         }
         
         boolean n21bool = n21 == 1;
         boolean n22bool = n22 == 1;
         boolean n2 = n21bool || n22bool;
         
         if (n1==1){
        	 n31 = ProbUtil.flip(4/5);
         }else{
        	 n31 = ProbUtil.flip(1/10);
         }
         
         if (n2){
        	 n32 = ProbUtil.flip(4/5);
         }else{
        	 n32 = ProbUtil.flip(1/10);
         }
         
         boolean n31bool = n31 == 1;
         boolean n32bool = n32 == 1;
         boolean n33bool = n33 == 1;
         boolean n3 = n31bool || n32bool || n33bool; 
         ProbUtil.Observe(n3);
         
         n3=fake(n3); 
	}
	
	public static boolean fake(boolean y) {
		return y;
	}

}
