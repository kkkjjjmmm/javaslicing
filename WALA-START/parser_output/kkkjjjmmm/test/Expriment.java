package kkkjjjmmm.test;

import kkkjjjmmm.slicer.Util;

public class Expriment {

    public static void main(String[] args) {
        boolean b = Util.Bernoulli(0.5);
        int x;
        {
            boolean variable1 = b;
            if (variable1) {
                x = 5;
            } else {
                x = 7;
            }
        }
        int z = 2 * x;
        boolean variable2 = z > 11;
        Util.Observe(variable2);
        int y = x;
        fake(y);
    }

    public static int fake(int b) {
        return b;
    }
}
