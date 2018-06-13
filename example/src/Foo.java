public class Foo {

  public static void main(String[] args) {
    Bar b1 = new Bar();
    b1.x = b1.times2(20);

    Bar b2 = new Bar();
    b2.x = 40;

    while (b1.x > 10) {
      b1.x--;
      b2.x++;
    }

    foo(b1.x);
    foo2(b2);
  }

  private static void foo(int x) {
    System.out.println(x);
  }

  private static void foo2(Bar x) {
    System.out.println(x.x);
  }

}
