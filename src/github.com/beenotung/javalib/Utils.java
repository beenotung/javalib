package github.com.beenotung.javalib;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
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

    /*
    * functional Array
    *
    * non-resizable
    * use native array directly, should be faster than LazyArrayList?
    * */
    public static class FArray<A> {
      public final A[] as;
      public final int length;

      FArray(A[] value) {
        this.as = value;
        this.length = value.length;
      }

      public static FArray<Character> fromString(String s) {
        return new FArray<Character>(toChars(s.toCharArray()));
      }

      public <B> FArray<B> map(Function<A, B> f, Class<B> bClass) {
        B bs[] = (B[]) Array.newInstance(bClass, as.length);
        for (int i = 0; i < bs.length; i++) {
          bs[i] = f.apply(as[i]);
        }
        return new FArray<B>((B[]) bs);
      }

      public FArray<A> filter(Function<A, Boolean> f) {
        ArrayList<A> buffer = new ArrayList<A>(as.length);
        for (A a : as) {
          if (f.apply(a))
            buffer.add(a);
        }
        A[] res;
        if (buffer.size() == 0)
          res = (A[]) new Object[0];
        else
          res = (A[]) Array.newInstance(buffer.get(0).getClass(), buffer.size());
        res = buffer.toArray(res);
        return new FArray<A>(res);
      }

      public static FArray<Character> range(char offset, int count) {
        Character[] res = new Character[count];
        for (int i = 0; i < count; i++) {
          res[i] = (char) (i + offset);
        }
        return new FArray<Character>(res);
      }

      public void forEach(Function<A, Void> f) {
        for (A a : as) {
          f.apply(a);
        }
      }

      public <B> B foldl(BiFunction<B, A, B> f, B acc) {
        for (A a : as) {
          acc = f.apply(acc, a);
        }
        return acc;
      }

      public A head() {
        return as[0];
      }

      public FArray<A> tail() {
        Object[] res = new Object[as.length - 1];
        for (int i = 1; i < as.length; i++) {
          res[i] = as[i];
        }
        return new FArray<A>((A[]) res);
      }

      public FArray<FArray<A>> group(int group_size, Class<A> classObject) {
            /* total number of element */
        final int n = as.length;
        group_size = Math.min(group_size, as.length);
        FArray<A>[] res = (FArray<A>[]) new FArray[(int) Math.round(Math.ceil(1.0 * n / group_size))];
        A[] bs = (A[]) Array.newInstance(classObject, group_size);
        int i_res = 0;
        int i_b = 0;
        for (int i = 0; i < as.length; i++) {
          bs[i_b++] = as[i];
          if (i_b == group_size) {
            res[i_res++] = new FArray<A>(bs);
            group_size = Math.min(group_size, n - group_size * i_res);
            bs = (A[]) Array.newInstance(classObject, group_size);
            i_b = 0;
          }
        }
        return new FArray(res);
      }
    }

    public static char[] toChars(Character[] as) {
      char[] cs = new char[as.length];
      for (int i = 0; i < as.length; i++) {
        cs[i] = as[i];
      }
      return cs;
    }

    public static Character[] toChars(char[] as) {
      Character[] cs = new Character[as.length];
      for (int i = 0; i < as.length; i++) {
        cs[i] = Character.valueOf(as[i]);
      }
      return cs;
    }

    public static String toString(FArray<Character> as) {
      return String.valueOf(toChars(as.as));
    }
  }
}
