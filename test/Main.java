import github.com.beenotung.javalib.Functional;

import static github.com.beenotung.javalib.Functional.*;
import static github.com.beenotung.javalib.Utils.*;

public class Main {
  public static void main(String[] args) {
    println("Test begin");
    println();

    println("monad: ");
    Functional.IMonad ma = unit(1);
    Functional.IMonad mb = ma.map(a -> (int) a + 2);
    mb.ap(b -> println("value: " + b));
    println();

    println("maybe: ");
    Functional.IMonad some = maybe(1);
    some.map(a -> (int) a + 1)
      .ap(x -> println("some: " + x));
    Functional.IMonad none = maybe(null);
    none.map(a -> (int) a + 1)
      .ap(x -> println("none: " + x));
    println();

    println("list: ");
    Functional.IList<Object> list1 = list().append(1).append(2).append(3).append(4);
    println("length_1: " + list1.size());
    IList list2 = list1.map(a -> (int) a * 10);
    println("length_2: " + list2.size());
    println("1: " + list1);
    println("2: " + list2);
    println("r: " + list2.reverse());
    println("empty: " + list());

    println("Test end");
  }
}
