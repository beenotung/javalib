package github.com.beenotung.javalib;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

public class Utils {
  public static final Scanner in = new Scanner(System.in);
  public static final PrintStream out = System.out;

  public static void print(Object msg) {
    System.out.print(msg);
  }

  public static void println() {
    System.out.println();
  }

  public static void println(Object msg) {
    System.out.println(msg);
  }

  public static <A> A or(A a1, A a2) {
    return a1 == null
      ? a2
      : a1;
  }

  public static <A> A and(A a1, A a2) {
    return a1 == null || a2 == null
      ? null
      : a1;
  }

  public static boolean instanceOf(Class aClass, Object a, Object b) {
    return aClass.isInstance(a) && aClass.isInstance(b);
  }

  public static <A extends Number> A sum(A a, A b) {
    if (instanceOf(Integer.class, a, b))
      return (A) Integer.valueOf(a.intValue() + b.intValue());
    else if (instanceOf(Float.class, a, b))
      return (A) Float.valueOf(a.floatValue() + b.floatValue());
      /* TODO detect more case */
    else return (A) Double.valueOf(a.doubleValue() + b.doubleValue());
  }

  public static class Functional {
    public static <A, B, C> Func1<A, C> compose(Func1<B, C> g, Func1<A, B> f) {
      return a -> g.apply(f.apply(a));
    }

    public static <A> M<A> flat(M<M<A>> mma) {
      return mma.value();
    }

    /* similar to to Function and BiFunction in jdk8, but in case need to run in jdk7 */
    public interface Func1<A, B> {
      B apply(A a);

      default <C> Func1<A, C> map(Func1<B, C> f) {
        return compose(f, this);
      }
    }

    public interface Func2<A1, A2, B> {
      B apply(A1 a1, A2 a2);
    }

    public interface Func3<A1, A2, A3, B> {
      B apply(A1 a1, A2 a2, A3 a3);
    }

    public interface Tuple2<A, B> {
      A _1();

      B _2();
    }

    public interface Tuple3<A, B, C> {
      A _1();

      B _2();

      C _3();
    }

    public interface Producer<A> {
      A apply();
    }

    public interface Consumer<A> {
      void apply(A a);
    }

    public interface ConcatMonad<A> {
      ConcatMonad<A> concat(ConcatMonad<A> ma);
    }

    public interface M<A> {

      /**
       * @protected
       */
      A value();

      /*
      * @private
      * */
      void setValue(A a);

      default <B> M<B> unit(B b) {
        try {
          M ma = getClass().newInstance();
          ma.setValue(b);
          return ma;
        } catch (InstantiationException | IllegalAccessException e) {
          e.printStackTrace();
          throw new Error(e);
        }
      }

      default <B> M<B> bind(Func1<A, M<B>> f) {
        return f.apply(value());
      }

      default <B> M<B> map(Func1<A, B> f) {
        return bind(a -> unit(f.apply(a)));
      }

      default <B> B ap(Func1<A, B> f) {
        return f.apply(value());
      }
    }

    public static class Monad<A> implements M<A> {
      private A value;

      private Monad() {
      }

      /**
       * @final
       */
      public Monad(A value) {
        this.value = value;
      }


      @Override
      public A value() {
        return value;
      }

      @Override
      public void setValue(A a) {
        this.value = a;
      }
    }

    public static class Maybe<A> extends Monad<A> {
      /**
       * @final
       */
      public Maybe(A value) {
        super(value);
      }

      @Override
      public <B> M<B> bind(Func1<A, M<B>> f) {
        return value() == null
          ? (M<B>) this
          : super.bind(f);
      }

      Maybe<A> or(Maybe<A> ma) {
        return value() == null
          ? ma
          : this;
      }

      Maybe<A> and(Maybe<A> ma) {
        return value() == null
          ? this
          : ma.value() == null ? ma : this;
      }
    }

    public static class LazyArrayList<A> /*implements M<Func1<Object, A>>*/ {
      /**
       * @write_only
       */
      public ArrayList list;
      private Func1<Object, A> f = a -> (A) a;

      public LazyArrayList() {
        this.list = new ArrayList();
      }

      public LazyArrayList(ArrayList list) {
        this.list = list;
      }

      public <B> LazyArrayList<B> map(Func1<A, B> f) {
        LazyArrayList<B> res = new LazyArrayList<B>(list);
        res.f = this.f.map(f);
        return res;
      }

      public <B> B foldl(Func2<B, A, B> f, B init) {
        AtomicReference<B> acc = new AtomicReference<B>(init);
        stream().forEachOrdered(c -> acc.set(f.apply(acc.get(), c)));
        return acc.get();
      }

      public Stream<A> stream() {
        return this.list.stream().map(new Function<A, A>() {
          @Override
          public A apply(A a) {
            return f.apply(a);
          }
        });
      }

      public ArrayList<A> build() {
        ArrayList<A> res = new ArrayList<A>(list.size());
        stream().forEachOrdered(a -> res.add(a));
        return res;
      }

      /* compact the chained map */
      public synchronized void update() {
        list = build();
        f = a -> (A) a;
      }

      @Override
      public String toString() {
        return build().toString();
      }
    }

    /* slow, unsafe */
    public static <A extends Number> A sum(LazyArrayList<A> m) {
      return m.stream().reduce(Utils::sum).get();
    }

    public static int sumInt(LazyArrayList<Integer> a) {
      return a.stream().reduce((acc, c) -> acc + c).orElse(0);
    }

    public static long sumLong(LazyArrayList<Long> a) {
      return a.stream().reduce((acc, c) -> acc + c).orElse(0l);
    }

    public static double sumDouble(LazyArrayList<Double> a) {
      return a.stream().reduce((acc, c) -> acc + c).orElse(0d);
    }

    public static float sumFloat(LazyArrayList<Float> a) {
      return a.stream().reduce((acc, c) -> acc + c).orElse(0f);
    }
  }
}
