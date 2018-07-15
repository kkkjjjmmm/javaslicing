package kkkjjjmmm.modified;

import kkkjjjmmm.slicer.Util;

public class Example {

    public static void main(String[] args) {
        boolean l = SliceL();
        System.out.println(l);
    }

    public static boolean SliceL() {
        boolean d, l, i, s, g;
        d = Util.Bernoulli(0.6);
        i = Util.Bernoulli(0.7);
        {
            boolean variable3 = !i && !d;
            if (variable3) {
                g = Util.Bernoulli(0.3);
            } else {
                boolean variable2 = !i && d;
                if (variable2) {
                    g = Util.Bernoulli(0.05);
                } else {
                    boolean variable1 = i && !d;
                    if (variable1) {
                        g = Util.Bernoulli(0.9);
                    } else {
                        g = Util.Bernoulli(0.5);
                    }
                }
            }
        }
        {
            boolean variable4 = g == false;
            // g = Yeah.negate(g);
            Util.Observe(variable4);
            g = false;
        }
        {
            boolean variable5 = !i;
            // d = true;
            if (variable5) {
                s = Util.Bernoulli(0.2);
            } else {
                s = Util.Bernoulli(0.95);
            }
        }
        {
            boolean variable6 = !g;
            if (variable6) {
                l = Util.Bernoulli(0.1);
            } else {
                l = Util.Bernoulli(0.4);
            }
        }
        l = fake(l);
        return l;
    }

    public static boolean fake(boolean b) {
        return b;
    }
}
