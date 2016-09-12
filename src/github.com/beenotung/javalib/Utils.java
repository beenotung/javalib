package github.com.beenotung.javalib;

import jdk.internal.util.xml.impl.Pair;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.*;

public class Utils {
  public static final InputStream in = System.in;
  public static final Scanner scanner = new Scanner(in);
  public static final PrintStream out = System.out;

  public static void print(Object... os) {
    out.print(toString(os));
  }

  public static void println(Object... os) {
    out.println(toString(os));
  }

  public static void println() {
    out.println();
  }

  public static class ArrayStringBuffer {
    private String buffer;
    boolean first = true;

    public void add(Object o) {
      if (first) {
        first = false;
        buffer = String.valueOf(o);
      } else {
        buffer += "," + String.valueOf(o);
      }
    }

    public String build() {
      return "(" + buffer + ")";
    }
  }

  public static String ObjectToString(Object o) {
    if (o != null && o.getClass().isArray()) {
      Class<?> c = o.getClass().getComponentType();
      ArrayStringBuffer buffer = new ArrayStringBuffer();
      if (c.isPrimitive()) {
        switch (c.getTypeName()) {
          case "int":
            for (int i : ((int[]) o)) {
              buffer.add(i);
            }
            break;
          case "double":
            for (double v : ((double[]) o)) {
              buffer.add(v);
            }
            break;
          case "float":
            for (float v : ((float[]) o)) {
              buffer.add(v);
            }
            break;
          case "byte":
            for (byte b : ((byte[]) o)) {
              buffer.add(b);
            }
            break;
          default:
            throw new Error("unsupported type");
        }
      } else {
        for (Object x : ((Object[]) o)) {
          buffer.add(x);
        }
      }
      return buffer.build();
    } else {
      return String.valueOf(o);
    }
  }

  public static String toString(Object... os) {
    if (os.length == 1)
      return ObjectToString(os[0]);
    else
      return ObjectToString(os);
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

  public static <A> ArrayList<A> list(A[] as) {
    return new ArrayList<A>(Arrays.asList(as));
  }

  public static <A> Stream<A> stream(A[] as) {
    return list(as).stream();
  }

  public static <A> Stream<Pair<Integer, A>> indexedStream(Collection<A> as) {
    final int[] idx = {0};
    return as.stream().sequential().map(a -> pair(idx[0]++, a));
  }

  public static <A> Stream<Pair<Integer, A>> indexedStream(A[] as) {
    return indexedStream(Arrays.asList(as));
  }

  public static boolean instanceOf(Class aClass, Object... os) {
    return stream(os).allMatch(o -> aClass.isInstance(os));
  }

  public static double diff(double a, double b) {
    return Math.abs(a - b);
  }

  public static float diff(float a, float b) {
    return Math.abs(a - b);
  }

  public static int diff(int a, int b) {
    return Math.abs(a - b);
  }

  public static long diff(long a, long b) {
    return Math.abs(a - b);
  }

  public static double sum(double a, double b) {
    return a + b;
  }

  public static float sum(float a, float b) {
    return a + b;
  }

  public static int sum(int a, int b) {
    return a + b;
  }

  public static long sum(long a, long b) {
    return a + b;
  }

  public static double square_error(double a, double b) {
    return Math.pow(a - b, 2);
  }

  public static String repeat(int n, String s) {
    return mkStream(n).map(x -> s).reduce(String::concat).orElseGet(() -> "");
  }

  public static Stream<Integer> mkStream(int offset, int n) {
    Integer[] xs = new Integer[n];
    for (int i = 0; i < xs.length; i++) {
      xs[i] = i + offset;
    }
    return stream(xs);
  }

  public static Stream<Integer> mkStream(int n) {
    return mkStream(0, n);
  }

  public static <A> Optional<Class<A>> optionalClass(A a) {
    return Optional.ofNullable(a).map(x -> (Class<A>) x.getClass());
  }

  /* casted operation , avoid casting in application code */
  public static <A> Class<A> _class(A a) {
    return (Class<A>) a.getClass();
  }

  public static <A> A[] empty(int n, Class<A> aClass) {
    return (A[]) Array.newInstance(aClass, n);
  }

  public static Object[] empty(int n) {
    return (Object[]) Array.newInstance(Object.class, n);
  }

  public static <A> A[] fill(int n, A a) {
    A as[] = empty(n, _class(a));
    for (int i = 0; i < n; i++) {
      as[i] = a;
    }
    return as;
  }

  public static <A> A[] tabulate(int n, Function<Integer, A> f, Class<A> aClass) {
    A as[] = empty(n, aClass);
    for (int i = 0; i < n; i++) {
      as[i] = f.apply(i);
    }
    return as;
  }

  public static <A> Object[] tabulate(int n, Function<Integer, A> f) {
    return tabulate(n, f, (Class<A>) Object.class);
  }

  public static <A> A[] array(Collection<A> list, Class<A> aClass) {
    A[] as = empty(list.size(), aClass);
    as = list.toArray(as);
    return as;
  }

  public static <A> Optional<A> firstNonNull(Iterable<A> as) {
    for (A a : as) {
      if (a != null)
        return Optional.of(a);
    }
    return Optional.empty();
  }

  public static <A> Optional<A> firstNonNull(A[] as) {
    for (A a : as) {
      if (a != null)
        return Optional.of(a);
    }
    return Optional.empty();
  }

  public static char[] toChars(Iterable<Character> cs, int size) {
    char[] res = new char[size];
    int i = 0;
    for (Character c : cs) {
      res[i++] = c;
    }
    return res;
  }

  public static char[] toChars(Collection<Character> cs) {
    return toChars(cs, cs.size());
  }

  public static ArrayList<Character> toChars(char[] cs) {
    FList<Character> res = new FList<>(cs.length);
    for (char a : cs) {
      res.add(a);
    }
    return res;
  }

  public static class Lazy<A> implements Supplier<A> {
    private A a;
    public final Supplier<A> f;
    private boolean done = false;

    public Lazy(Supplier<A> f) {
      this.f = f;
    }

    @Override
    public A get() {
      if (!done)
        a = f.get();
      return a;
    }
  }

  public static <A> ArrayList<A> list(Stream<A> a) {
    return a.collect(Collectors.toCollection(ArrayList::new));
  }

  public static class Pair<A, B> {
    A _1;
    B _2;

    public Pair(A _1, B _2) {
      this._1 = _1;
      this._2 = _2;
    }

    public <C> Pair<A, C> new2(C _2) {
      return new Pair<A, C>(_1, _2);
    }

    public <C> Pair<A, C> map2(Function<B, C> f) {
      return new2(f.apply(_2));
    }

    public <C> Pair<A, C> map2(BiFunction<A, B, C> f) {
      return new2(f.apply(_1, _2));
    }
  }

  /* help cast type */
  public static <A, B> Pair<A, B> pair(A a, B b) {
    return new Pair<A, B>(a, b);
  }

  public static <L extends List<A>, A> void update(L list, Function<A, A> f) {
    for (int i = 0; i < list.size(); i++) {
      list.set(i, f.apply(list.get(i)));
    }
  }

  public static <L extends List<A>, A> void reset(L list, Function<Integer, A> f) {
    for (int i = 0; i < list.size(); i++) {
      list.set(i, f.apply(i));
    }
  }

  public static <L extends List<A>, A> void replace(L list, BiFunction<Integer, A, A> f) {
    for (int i = 0; i < list.size(); i++) {
      list.set(i, f.apply(i, list.get(i)));
    }
  }

  public static <A> Optional<Class<A>> getComponentType(Collection<A> as) {
    return as.stream().filter(a -> a != null).findAny().map(a -> (Class<A>) a.getClass());
  }

  public static <A> Optional<Class<A>> getComponentType(A[] as) {
    return getComponentType(Arrays.asList(as));
  }
}
