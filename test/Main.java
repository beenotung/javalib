import github.com.beenotung.javalib.Functional;

import static github.com.beenotung.javalib.Functional.maybe;
import static github.com.beenotung.javalib.Functional.monad;
import static github.com.beenotung.javalib.Utils.*;

public class Main {
  public static void main(String[] args) {
    println("Test begin");
    println();

    println("monad: ");
    Functional.IMonad ma = monad.unit(1);
    Functional.IMonad mb = ma.map(a -> (int) a + 2);
    mb.ap(b -> println("value: " + b));
    println();

    println("maybe: ");
    Functional.IMonad some = maybe.unit(1);
    some.map(a -> (int) a + 1)
      .ap(x -> println("some: " + x));
    Functional.IMonad none = maybe.unit(null);
    none.map(a -> (int) a + 1)
      .ap(x -> println("none: " + x));
    println();

    println("Test end");
  }
}
