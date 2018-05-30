public class Foo {

  public static void main(String[] args) {
    int x = 20;
    int y = 40;

    while (x > 0) {
      x--;
      y++;
    }

    foo(y);
    foo2(x);
  }

  private static void foo(int x) {
    System.out.println(x);
  }

  private static void foo2(int x) {
    System.out.println(x);
  }

}
