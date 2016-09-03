import github.com.beenotung.javalib.Functional;

import static github.com.beenotung.javalib.Functional.*;
import static github.com.beenotung.javalib.Utils.*;

public class Main {
  public static void main(String[] args) throws Throwable {
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
    Functional.IList<Object> list1 = list().prepend(1).prepend(2).prepend(3).prepend(4);
    println("length_1: " + list1.size());
    IList list2 = list1.map(a -> (int) a * 10);
    println("length_2: " + list2.size());
    println("1: " + list1);
    println("2: " + list2);
    println("r: " + list2.reverse());
    println("empty: " + list());
    int n = 10000;
    Integer[] arr = new Integer[n];
    for (int i = 0; i < n; i++) {
      arr[i] = i;
    }
    IList<Integer> l10 = fromArray(arr);
    println("from array: " + l10);
    println("created: " + createList(new IFunc<Long, Object>() {
      @Override
      public Object apply(Long i) {
        return i;
      }
    }, 10));
    println("even: " + l10.filter(new IFunc<Integer, Boolean>() {
      @Override
      public Boolean apply(Integer integer) {
        return integer % 2 == 0;
      }
    }));
    println("div: " + l10.filter(new IFunc<Integer, Boolean>() {
      @Override
      public Boolean apply(Integer integer) {
        return integer > 0 && integer < 10;
      }
    }).reduce(new IFunc<Pair<Integer, Integer>, Integer>() {
      @Override
      public Integer apply(Pair<Integer, Integer> p) {
//        println(p.a()+" : "+p.b());
        return p.a() + p.b();
      }
    }));
    println("small: " + l10.take(5L));

    println("Test end");
  }
}
