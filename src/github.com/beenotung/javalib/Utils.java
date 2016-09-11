package github.com.beenotung.javalib;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
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

  public static <A, B, C> Func1<A, C> compose(Func1<B, C> g, Func1<A, B> f) {
    return a -> g.apply(f.apply(a));
  }

  public static <A> M<A> flat(M<M<A>> mma) {
    return mma.value();
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

  public static <A> A[] fill(int n, A a) {
    A as[] = (A[]) Array.newInstance(a.getClass(), n);
    for (int i = 0; i < n; i++) {
      as[i] = a;
    }
    return as;
  }

  public static <A> A[] tabulate(int n, Function<Integer, A> f, Class<A> aClass) {
    A as[] = (A[]) Array.newInstance(aClass, n);
    for (int i = 0; i < n; i++) {
      as[i] = f.apply(i);
    }
    return as;
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

    public LazyArrayList(A[] list) {
      this.list = new ArrayList(Arrays.asList(list));
    }

    public static <A> LazyArrayList<A> fill(int n, A a) {
      return new LazyArrayList<A>(Utils.fill(n, a));
    }

    public static <A> LazyArrayList<A> tabulate(int n, Function<Integer, A> f, Class<A> aClass) {
      return new LazyArrayList<A>(Utils.tabulate(n, f, aClass));
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
      Object[] as = build().toArray();
      if (as.length > 0 && as[0] != null && Character.class.equals(as[0].getClass()))
        return String.valueOf(toChars((Character[]) as));
      else
        return Arrays.asList(as).toString();
    }
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

    /* TODO support multiple dimension */
    public static <A> FArray<A> fill(int n, A a) {
      return new FArray<A>(Utils.fill(n, a));
    }

    public static <A> FArray<A> tabulate(int n, Function<Integer, A> f, Class<A> aClass) {
      return new FArray<A>(Utils.tabulate(n, f, aClass));
    }

    public static FArray<Character> fromString(String s) {
      return new FArray<Character>(toChars(s.toCharArray()));
    }

    public static FArray<Character> range(char offset, int count) {
      Character[] res = new Character[count];
      for (int i = 0; i < count; i++) {
        res[i] = (char) (i + offset);
      }
      return new FArray<Character>(res);
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

    public FArray<FArray<A>> group(int group_size) {
      if (as.length == 0 || group_size == 0)
        return new FArray(new Object[0]);
      int n_group = (int) Math.round(Math.ceil(1.0 * as.length / group_size));
      FArray[] ass = new FArray[n_group];
      int sizes[] = new int[n_group];
      int size = as.length / n_group;
      for (int i = 0; i < n_group; i++) {
        sizes[i] = size;
      }
      for (int i = 0; i < ass.length - size * n_group; i++) {
        sizes[i]++;
      }
      Class aClass = null;
      for (int i = 0; i < as.length; i++) {
        if (as[i] != null) {
          aClass = as[i].getClass();
          break;
        }
      }
      if (aClass == null)
        aClass = Object.class;
      int offset = 0;
      for (int i = 0; i < n_group; i++) {
        System.out.println("offset:" + offset);
        System.out.println("size:" + sizes[i]);
        ass[i] = new FArray<A>((A[]) Array.newInstance(aClass, sizes[i]));
        System.arraycopy(
          as, offset,
          ass[i].as, 0,
          sizes[i]
        );
        offset += sizes[i];
      }
      return new FArray(ass);
    }

    public FArray<FArray<A>> evenGroup(int n_group) {
      if (as.length == 0 && n_group == 0) {
        return new FArray(new FArray[0]);
      }
      Class aClass = null;
      for (A a : as) {
        if (a != null) {
          aClass = a.getClass();
          break;
        }
      }
      if (aClass == null)
        aClass = Object.class;
      FArray[] ass = new FArray[n_group];
      int sizes[] = new int[n_group];
      int size = as.length / n_group;
      for (int i = 0; i < ass.length; i++) {
        sizes[i] = size;
      }
      for (int i = 0; i < as.length - size * n_group; i++) {
        sizes[i]++;
      }
      for (int i = 0; i < n_group; i++) {
        ass[i] = new FArray((A[]) Array.newInstance(aClass, sizes[i]));
      }
      int ass_i = 0;
      int as_i = 0;
      for (int i = 0; i < as.length; i++) {
        ass[ass_i].as[as_i] = as[i];
        ass_i++;
        if (ass_i == n_group) {
          ass_i = 0;
          as_i++;
        }
      }
      return new FArray(ass);
    }

    @Override
    public String toString() {
      if (as.length > 0 && as[0] != null && Character.class.equals(as[0].getClass()))
        return String.valueOf(toChars((Character[]) as));
      else
        return Arrays.asList(as).toString();
    }
  }
}
