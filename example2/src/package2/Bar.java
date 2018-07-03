package package2;

public class Bar {

	  int x = 0;
	  boolean g;

	  public int times2(int y){
		Observe obs= new Observe();
		g = obs.getFlag(false);
	    return 2*y;
	  }
	  
}
