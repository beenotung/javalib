import github.com.beenotung.javalib.Functional;
import github.com.beenotung.javalib.Utils.*;

import static github.com.beenotung.javalib.Functional.*;
import static github.com.beenotung.javalib.Utils.*;

public class Main {
  public static void main(String[] args) throws Throwable {
//    println("testing LazyArrayList");
//    Utils.LazyArrayList<Integer> as = Utils.LazyArrayList.tabulate(10, i -> i, Integer.class);
//    as.list.add(10);
//    as.list.add(11);
//    Utils.LazyArrayList<Integer> bs = as.map(a -> a * 10);
//    println("as: " + as);
//    println("bs: " + bs);
//    println();
    println("testing FList");
    FList<?, Character> cs = FList.fromString("this is a long text");
//    println("testing FArray");
//    FArray<Character> cs = FArray.fromString("this is a long text").filter(c -> c.charValue() != 'a');
    println("size: " + cs.length());
    println("upper: " + cs.map(Character::toUpperCase).toString());
    println("no e:" + cs.filter(c -> !c.equals('e')));
//    println("upper: " + Utils.toString(cs.map(c -> {
//      if (c >= 'a' && c <= 'z')
//        return Character.valueOf((char) (c + ('A' - 'a')));
//      else
//        return Character.valueOf(c);
//    }, Character.class)));
    println("full: " + cs);
    println("groups: " + cs.group(4));
    println("even-groups: " + cs.evenGroup(4));
    println();
    println("test stream utils");
    println("avg:" + average(mkIntStream(1000).map(i -> i + 1)));
    println();
    println("testing","println");
  }

  public static void main_old(String[] args) throws Throwable {
    println("Test begin");
    println();

    println("io: ");
    {
      print("input name:");
      String name = in.nextLine();
      println("name=" + name);
      print("input two number");
      int a = in.nextInt();
      int b = in.nextInt();
      println(a);
      println(b);
    }
    println();

    println("monad: ");
    Functional.IMonad ma = unit(1);
    Functional.IMonad mb = ma.map(a -> (int) a + 2);
    mb.ap(b -> println("preValues: " + b));
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
    Functional.IList list2 = list1.map(a -> (int) a * 10);
    println("length_2: " + list2.size());
    println("1: " + list1);
    println("2: " + list2);
    println("r: " + list2.reverse());
    println("empty: " + list());
    int n = 20;
    Integer[] arr = new Integer[n];
    for (int i = 0; i < n; i++) {
      arr[i] = i;
    }
    Functional.IList<Integer> l10 = fromArray(arr);
    println("from array: " + l10);
    println("created: " + createList(new IFunc<Long, Object>() {
      @Override
      public Object apply(Long i) {
        return i;
      }
    }, 32767));
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
        return p.a() + p.b();
      }
    }));
    println("take 5: " + l10.take(5L));
    println("take 10, drop 5: " + l10.take(10L).drop(5L));

    println("Test end");
  }
}
