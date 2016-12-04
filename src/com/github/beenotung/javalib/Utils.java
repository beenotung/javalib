package com.github.beenotung.javalib;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Utils {
  public static final InputStream in = System.in;
  public static final Scanner scanner = new Scanner(in);
  public static final PrintStream out = System.out;

  public static class PrintMode {
    public static final byte Scala = 0;
    public static final byte Go = 1;
    public static byte mode = Go;

    static final Object lock = new Object();
    public static Boolean synchronize = true;

    public static void print_go(Object[] os) {
      out.print(Utils.toString(os[0]));
      for (int i = 1; i < os.length; i++) {
        out.print(" ");
        out.print(Utils.toString(os[i]));
      }
    }
  }

  public static void print(Object... os) {
    if (os.length == 0)
      return;
    if (os.length == 1)
      out.print(toString(os[0]));
    else {
      if (PrintMode.mode == PrintMode.Scala) {
        out.print(toString(os));
      } else {
        if (PrintMode.synchronize) {
          synchronized (PrintMode.lock) {
            PrintMode.print_go(os);
          }
        } else {
          PrintMode.print_go(os);
        }
      }
    }
  }

  public static void println(Object... os) {
    if (os.length == 0)
      return;
    if (os.length == 1)
      out.println(toString(os[0]));
    else {
      if (PrintMode.mode == PrintMode.Scala) {
        out.println(toString(os));
      } else {
        if (PrintMode.synchronize) {
          synchronized (PrintMode.lock) {
            PrintMode.print_go(os);
            out.println();
          }
        } else {
          PrintMode.print_go(os);
          out.println();
        }
      }
    }
  }

  public static void println() {
    out.println();
  }

  public static class ArrayStringBuffer {
    private String buffer;
    boolean empty = true;
    boolean single = true;

    public void add(Object o) {
      if (empty) {
        empty = false;
        buffer = String.valueOf(o);
      } else {
        single &= false;
        buffer += "," + String.valueOf(o);
      }
    }

    public String build() {
      return single ? buffer : "(" + buffer + ")";
    }
  }

  public static String objectToString(Object o) {
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
          case "char":
            for (char x : ((char[]) o)) {
              buffer.add(x);
            }
            break;
          default:
            throw new Error("unsupported type");
        }
      } else {
        for (Object x : ((Object[]) o)) {
          buffer.add(objectToString(x));
        }
      }
      return buffer.build();
    } else if (instanceOf(Stream.class, o)) {
      return objectToString(list(((Stream) o)));
    } else {
      return String.valueOf(o);
    }
  }

  public static String toString(Object... os) {
    if (os.length == 1)
      return objectToString(os[0]);
    else
      return objectToString(os);
  }

  /**
   * calendar insensitive
   * inaccurate when when larger than a month
   * */
  public static final long YEAR = 1;
  public static final long MONTH = YEAR * 12;
  public static final long WEEK = MONTH * 4;
  public static final long DAY = WEEK * 7;
  public static final long HOUR = DAY * 24;
  public static final long MINUTE = HOUR * 60;
  public static final long SECOND = MINUTE * 60;
  public static final long MILLISECOND = SECOND * 1000;
  public static final long NANOSECOND = SECOND * 1000 * 1000 * 1000;

  public static final long NANO_IN_MICROSECOND = 1000L;
  public static final long NANO_IN_MILLISECOND = NANO_IN_MICROSECOND * 1000L;
  public static final long NANO_IN_SECOND = NANO_IN_MILLISECOND * 1000L;
  public static final long NANO_IN_MINUTE = NANO_IN_SECOND * 60L;
  public static final long NANO_IN_HOUR = NANO_IN_MINUTE * 60L;
  public static final long NANO_IN_DAY = NANO_IN_HOUR * 24L;
  public static final long NANO_IN_WEEK = NANO_IN_DAY * 7L;
  public static final long NANO_IN_MONTH = NANO_IN_WEEK * 4L;
  public static final long NANO_IN_YEAR = NANO_IN_MONTH * 12L;

  public static String nano_to_string(long time) {
    if (time == 0) {
      return "0 ns.";
    }
    StringBuilder b = new StringBuilder();
    if (time > NANO_IN_YEAR) {
      b.append(time / NANO_IN_YEAR);
      b.append(" year ");
      time %= NANO_IN_YEAR;
    }
    if (time > NANO_IN_MONTH) {
      b.append(time / NANO_IN_MONTH);
      b.append(" month ");
      time %= NANO_IN_MONTH;
    }
    if (time > NANO_IN_WEEK) {
      b.append(time / NANO_IN_WEEK);
      b.append(" week ");
      time %= NANO_IN_WEEK;
    }
    if (time > NANO_IN_DAY) {
      b.append(time / NANO_IN_DAY);
      b.append(" day ");
      time %= NANO_IN_DAY;
    }
    if (time > NANO_IN_HOUR) {
      b.append(time / NANO_IN_HOUR);
      b.append(" hr. ");
      time %= NANO_IN_HOUR;
    }
    if (time > NANO_IN_MINUTE) {
      b.append(time / NANO_IN_MINUTE);
      b.append(" min. ");
      time %= NANO_IN_MINUTE;
    }
    if (time > NANO_IN_SECOND) {
      b.append(time / NANO_IN_SECOND);
      b.append(" sec. ");
      time %= NANO_IN_SECOND;
    }
    if (time > NANO_IN_MILLISECOND) {
      b.append(time / NANO_IN_MILLISECOND);
      b.append(" ms. ");
      time %= NANO_IN_MILLISECOND;
    }
    if (time > 0) {
      b.append(time);
      b.append(" ns.");
    }
    return b.toString();
  }

  public static final Random random = new Random();

  public static int random(int offset, int range) {
    return offset + random.nextInt(range + 1);
  }

  public static byte[] randomBytes(int n) {
    byte[] res = new byte[n];
    random.nextBytes(res);
    return res;
  }

  public static byte randomByte() {
    return randomBytes(1)[0];
  }

  public static char randomVisibleChar() {
    return (char) (random.nextInt(127 - 32) + 32);
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

  public static boolean not(boolean b) {
    return !b;
  }

  public static <A> A id(A a) {
    return a;
  }

  public static <A> ArrayList<A> list(A[] as) {
    return new ArrayList<A>(Arrays.asList(as));
  }

  public static <A> ArrayList<ArrayList<A>> wrap(ArrayList<A> as) {
    ArrayList<ArrayList<A>> res = new ArrayList<>(1);
    res.add(as);
    return res;
  }

  public static <A> AtomicReference<A> atom(A a) {
    return new AtomicReference<A>(a);
  }

  public static Stream<Character> stream(char[] cs) {
    return Stream.of(chars(cs));
  }

  public static <A> Stream<A> stream(A[] as) {
    return Stream.of(as);
  }

  public static Stream<Boolean> stream(boolean[] bs) {
    AtomicInteger idx = new AtomicInteger(0);
    return Stream.iterate(bs[0], b -> bs[idx.incrementAndGet()]);
  }

  public static Stream<Character> stream(String s) {
    return list(s.toCharArray()).stream();
  }

  public static String string(Stream<Character> cs) {
    return String.valueOf(chars(cs));
  }

  public static String string(Character[] cs) {
    return String.valueOf(chars(cs));
  }

  public static String string(Collection<Character> cs) {
    return String.valueOf(chars(cs.stream(), cs.size()));
  }

  public static <A> Stream<Pair<Integer, A>> indexedStream(Collection<A> as) {
    final int[] idx = {0};
    return as.stream().sequential().map(a -> pair(idx[0]++, a));
  }

  public static <A> Stream<Pair<Integer, A>> indexedStream(A[] as) {
    return indexedStream(Arrays.asList(as));
  }

  /**
   * at least one object to compare
   */
  public static boolean instanceOf(Class aClass, Object o, Object... others) {
    ArrayList<Object> os = list(others);
    os.add(o);
    return os.stream().map(x -> x != null).allMatch(x -> x.getClass().equals(aClass));
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
    return mkStream(n).mapToObj(x -> s).reduce(String::concat).orElseGet(() -> "");
  }

  public static LongStream mkStream(long offset, long n) {
    return Stream.iterate(offset, i -> i + 1).limit(n).mapToLong(Utils::id);
  }

  public static IntStream mkStream(int offset, int n) {
    if (n <= 0)
      return Stream.empty().mapToInt(x -> 0);
    else
      return Stream.iterate(offset, i -> i + 1).limit(n).mapToInt(Utils::id);
  }

  public static LongStream mkStream(long n) {
    return mkStream(0L, n);
  }

  public static IntStream mkStream(int n) {
    return mkStream(0, n);
  }

  public static <A> Stream<Collection<A>> cast(Stream<ArrayList<A>> xss) {
    return xss.map(Utils::id);
  }

  public static <A> ArrayList<A> flatten(Stream<Collection<A>> xss) {
    ArrayList<A> res = new ArrayList<A>();
    xss.forEach(res::addAll);
    return res;
  }

  public static char[] flatten(char[][] xss) {
    int n = 0;
    for (char[] xs : xss) {
      n += xs.length;
    }
    char[] res = new char[n];
    int i = 0;
    for (char[] xs : xss) {
      for (int j = 0; j < xs.length; j++) {
        res[i++] = xs[j];
      }
    }
    return res;
  }

  public static <A> A[] flatten(A[][] xss, Class<A> c) {
    int n = 0;
    for (A[] xs : xss) {
      n += xs.length;
    }
    A[] res = (A[]) Array.newInstance(c, n);
    int i = 0;
    for (A[] xs : xss) {
      for (int j = 0; j < xs.length; j++) {
        res[i++] = xs[j];
      }
    }
    return res;
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

  public static <A> A[][] empty(int n1, int n2, Class<A> aClass) {
    return (A[][]) Array.newInstance(aClass, n1, n2);
  }

  /*
  * help cast type;
  * use default inflate factor if necessary
  * */
  public static <A> ArrayList<A> emptyList(int init_size) {
    if (init_size > 0)
      return new ArrayList<A>(init_size);
    else
      return new ArrayList<A>();
  }

  public static <A> A[][] empty(int n, Function<Integer, Integer> f, Class<A> aClass) {
    A[][] res = empty(n, 0, aClass);
    reset(res, i -> (A[]) Array.newInstance(aClass, f.apply(i)));
    return res;
  }

  public static Object[] empty(int n) {
    return (Object[]) Array.newInstance(Object.class, n);
  }

  public static <A> A[] fill(int n, Supplier<A> f, Class<A> aClass) {
    A[] as = empty(n, aClass);
    for (int i = 0; i < n; i++) {
      as[i] = f.get();
    }
    return as;
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

  public static <A> A[] array(Stream<A> stream, Class<A> aClass) {
    return array(list(stream), aClass);
  }

  public static <A> A[] array(A... as) {
    return as;
  }

  public static byte[] bytes(char[] xs) {
    byte[] res = new byte[xs.length];
    for (int i = 0; i < res.length; i++) {
      res[i] = (byte) xs[i];
    }
    return res;
  }

  public static Integer[] reverseByIndex(Integer[] xs) {
    Integer[] res = new Integer[xs.length];
    par_foreach(xs.length, i -> res[xs[i]] = i);
    return res;
  }

  public static int[] reverseByIndex(int[] xs) {
    int[] res = new int[xs.length];
    par_foreach(xs.length, i -> res[xs[i]] = i);
    return res;
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

  public static char[] chars(Stream<Character> cs, int size) {
    char[] res = new char[size];
    final int[] i = {0};
    cs.forEachOrdered(c -> res[i[0]++] = c);
    return res;
  }

  public static char[] chars(Collection<Character> cs) {
    return chars(cs.stream(), cs.size());
  }

  public static Character[] chars(char[] cs) {
    Character[] res = new Character[cs.length];
    for (int i = 0; i < cs.length; i++) {
      res[i] = cs[i];
    }
    return res;
  }

  public static char[] chars(Character[] cs) {
    char[] res = new char[cs.length];
    for (int i = 0; i < cs.length; i++) {
      res[i] = cs[i];
    }
    return res;
  }

  public static char[] chars(Stream<Character> cs) {
    return chars(list(cs));
  }

  public static Character[] chars(String cs) {
    return chars(cs.toCharArray());
  }

  public static Integer[] ints(int[] xs) {
    Integer[] res = new Integer[xs.length];
    for (int i = 0; i < xs.length; i++) {
      res[i] = xs[i];
    }
    return res;
  }

  public static int[] ints(Integer[] xs) {
    int[] res = new int[xs.length];
    for (int i = 0; i < xs.length; i++) {
      res[i] = xs[i];
    }
    return res;
  }

  /**
   * not use casting, it map [-128..127] to [0..255]
   * */
  public static int uint(byte x) {
    return Byte.toUnsignedInt(x);
  }

  /**
   * get from byte array of 256 element, help avoid negative index
   * */
  public static byte get(byte[] bs, byte b) {
    return bs[uint(b)];
  }

  public static int mod(int v, int base) {
    while (v < 0)
      v += base;
    return v % base;
  }

  public static ArrayList<Character> list(char[] cs) {
    return new ArrayList<>(Arrays.asList(chars(cs)));
  }

  public static ArrayList<Double> list(double[] cs) {
    ArrayList<Double> res = new ArrayList<>(cs.length);
    for (double a : cs) {
      res.add(a);
    }
    return res;
  }

  public static ArrayList<Integer> list(int[] cs) {
    ArrayList<Integer> res = new ArrayList<>(cs.length);
    for (int a : cs) {
      res.add(a);
    }
    return res;
  }

  public static ArrayList<Boolean> list(boolean[] bs) {
    ArrayList<Boolean> res = new ArrayList<>(bs.length);
    for (boolean b : bs) {
      res.add(b);
    }
    return res;
  }

  public static ArrayList<Character> list(String s) {
    return list(s.toCharArray());
  }

  public static <A> ArrayList<A> list(Stream<A> a) {
    return a.collect(Collectors.toCollection(ArrayList::new));
  }

  public static ArrayList<Integer> list(IntStream a) {
    ArrayList<Integer> res = new ArrayList<>();
    a.sequential().forEachOrdered(res::add);
    return res;
  }

  public static class Pair<A, B> {
    public final A _1;
    public final B _2;

    public Pair(A _1, B _2) {
      this._1 = _1;
      this._2 = _2;
    }

    @Override
    public String toString() {
      return Utils.toString(array());
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

    public Object[] array() {
      Object[] xs = empty(2);
      xs[0] = _1;
      xs[1] = _2;
      return xs;
    }

    public ArrayList list() {
      return Utils.list(array());
    }

    public <C> Pair3<A, B, C> lift(C _3) {
      return pair3(_1, _2, _3);
    }
  }

  public static class Pair3<T1, T2, T3> extends Pair<T1, T2> {
    public final T3 _3;

    public Pair3(T1 t1, T2 t2, T3 t3) {
      super(t1, t2);
      _3 = t3;
    }

    @Override
    public Object[] array() {
      Object[] xs = empty(3);
      xs[0] = _1;
      xs[1] = _2;
      xs[2] = _3;
      return xs;
    }

    /**
     * @param idx indicate which element to drop, start from 1 , [1,3]
     */
    public <A, B> Pair<A, B> truncate(int idx) {
      if (idx == 1)
        return pair((A) _2, (B) _3);
      else if (idx == 2)
        return pair((A) _1, (B) _3);
      else
        return pair((A) _1, (B) _2);
    }

    public <A, B> Pair<A, B> truncate() {
      return truncate(3);
    }
  }

  public static <T1, T2, T3> Pair3<T1, T2, T3> pair3(T1 _1, T2 _2, T3 _3) {
    return new Pair3<T1, T2, T3>(_1, _2, _3);
  }

  public static class Either<A, B> extends Pair<A, B> {
    public final boolean isLeft;
    public final boolean isRight;

    public Either(A _1, B _2) {
      super(_1, _2);
      isLeft = _1 != null;
      isRight = !isLeft;
    }

    public Either(boolean isLeft, Pair<A, B> pair) {
      super(pair._1, pair._2);
      this.isLeft = isLeft;
      isRight = !isLeft;
    }

    public Either(A _1, B _2, boolean isLeft) {
      super(_1, _2);
      this.isLeft = isLeft;
      this.isRight = !isLeft;
    }

    public A left() {
      if (isLeft)
        return _1;
      else
        throw new ErrorObject(_2);
    }

    public B right() {
      if (isRight)
        return _2;
      else
        throw new ErrorObject(_1);
    }

    public void apply(Consumer<A> leftF, Consumer<B> rightF) {
      if (isLeft)
        leftF.accept(_1);
      else
        rightF.accept(_2);
    }

    public <C, D> Either<C, D> map(Function<A, C> leftF, Function<B, D> rightF) {
      if (isLeft)
        return Utils.left(leftF.apply(_1));
      else
        return Utils.right(rightF.apply(_2));
    }

    public <C, D> Either<C, D> map(Function<Either<A, B>, Either<C, D>> f) {
      return f.apply(this);
    }
  }

  public static <A> Either<A, String> _try(Supplier<A> f) {
    try {
      return left(f.get());
    } catch (Throwable e) {
      return right(e.toString());
    }
  }

  public static <A, B> Either<A, B> left(A value) {
    return new Either<A, B>(value, null, true);
  }

  public static <A, B> Either<A, B> right(B value) {
    return new Either<A, B>(null, value, true);
  }

  /* for type cast */
  public <A> ArrayList<A> list(Pair<A, A> p) {
    return p.list();
  }

  /* help cast type */
  public static <A, B> Pair<A, B> pair(A a, B b) {
    return new Pair<A, B>(a, b);
  }

  public static <A> void update(A[] list, Function<A, A> f) {
    for (int i = 0; i < list.length; i++) {
      list[i] = f.apply(list[i]);
    }
  }

  public static void update(byte[] list, int offset, int count, Function<Byte, Byte> f) {
    for (int i = 0; i < count; i++) {
      list[i + offset] = f.apply(list[i + offset]);
    }
  }

  public static void update(byte[] list, Function<Byte, Byte> f) {
    for (int i = 0; i < list.length; i++) {
      list[i] = f.apply(list[i]);
    }
  }

  public static <A> void par_update(A[] list, Function<A, A> f) {
    par_foreach(list.length, i -> list[i] = f.apply(list[i]));
  }

  public static <L extends List<A>, A> void update(L list, Function<A, A> f) {
    for (int i = 0; i < list.size(); i++) {
      list.set(i, f.apply(list.get(i)));
    }
  }

  public static <A> void reset(A[] list, Function<Integer, A> f) {
    for (int i = 0; i < list.length; i++) {
      list[i] = f.apply(i);
    }
  }

  public static <A> void par_reset(A[] list, Function<Integer, A> f) {
    par_foreach(list.length, i -> list[i] = f.apply(i));
  }

  public static <L extends List<A>, A> void reset(L list, Function<Integer, A> f) {
    for (int i = 0; i < list.size(); i++) {
      list.set(i, f.apply(i));
    }
  }

  public static <A> void replace(A[] list, BiFunction<Integer, A, A> f) {
    for (int i = 0; i < list.length; i++) {
      list[i] = f.apply(i, list[i]);
    }
  }

  public static <A> void par_replace(A[] list, BiFunction<Integer, A, A> f) {
    par_foreach(list.length, i -> list[i] = f.apply(i, list[i]));
  }

  public static <L extends List<A>, A> void replace(L list, BiFunction<Integer, A, A> f) {
    for (int i = 0; i < list.size(); i++) {
      list.set(i, f.apply(i, list.get(i)));
    }
  }

  public static <B, A> B foldl(A[] list, BiFunction<B, A, B> f, B init) {
    for (A b : list) {
      init = f.apply(init, b);
    }
    return init;
  }

  public static <A> A foldl1(A[] list, BiFunction<A, A, A> f) {
    A acc = list[0];
    for (int i = 1; i < list.length; i++) {
      acc = f.apply(acc, list[i]);
    }
    return acc;
  }

  public static <A> Optional<Class<A>> componentType(Collection<A> as) {
    return as.stream().filter(a -> a != null).findAny().map(a -> (Class<A>) a.getClass());
  }

  public static <A> Optional<Class<A>> componentType(A[] as) {
    return componentType(Arrays.asList(as));
  }

  public static <A, B, C> Function<A, Function<B, C>> curry(Function<Pair<A, B>, C> f) {
    return a -> b -> f.apply(new Pair<A, B>(a, b));
  }

  public static <A, B, C> Function<Pair<A, B>, C> uncurry(Function<A, Function<B, C>> f) {
    return ab -> f.apply(ab._1).apply(ab._2);
  }

  public static <A, B, C> Function<B, Function<A, C>> flip(Function<A, Function<B, C>> f) {
    return b -> a -> f.apply(a).apply(b);
  }

  public static <A> Function<?, A> _const(A a) {
    return b -> a;
  }

  public static <A> Function<Function<A, A>, Function<A, A>> until(Function<A, Boolean> f) {
    return g -> h -> {
      A t;
      do {
        t = g.apply(h);
      } while (!f.apply(t));
      return t;
    };
  }

  public static <A, B> B seq(Function<A, B> f) {
    if (Lazy.class.isInstance(f))
      return ((Lazy<B>) f).get();
    else
      throw new Error("f is not instance of Lazy (" + f.getClass().getName() + ")");
  }

  public static Function<String, ArrayList<String>> split(String pattern) {
    return s -> new ArrayList<String>(list(s.split(pattern)));
  }

  public static Function<ArrayList<String>, String> unsplit(String pattern) {
    return s -> s.stream().sequential().reduce("", (acc, c) -> acc + pattern + c).substring(1);
  }

  public static ArrayList<String> lines(String s) {
    return split("\n").apply(s);
  }

  public static ArrayList<String> words(String s) {
    return split(" ").apply(s);
  }

  public static String unlines(ArrayList<String> ss) {
    return unsplit("\n").apply(ss);
  }

  public static String unwords(ArrayList<String> ss) {
    return unsplit(" ").apply(ss);
  }

  public static <A, B, C> Function<List<A>, Function<List<B>, ArrayList<C>>> zipWith(Function<A, Function<B, C>> f) {
    return as -> bs -> {
      final int n = Math.min(as.size(), bs.size());
      ArrayList<C> cs = new ArrayList<C>(n);
      for (int i = 0; i < n; i++) {
        cs.add(f.apply(as.get(i)).apply(bs.get(i)));
      }
      return cs;
    };
  }

  public static <A> Stream<Pair<Integer, A>> zipWithIndex(Stream<A> stream) {
    final int[] i = {0};
    return stream.sequential().map(s -> pair(i[0]++, s));
  }

  public static <A> Stream<Pair<Integer, A>> zipWithIndex(Collection<A> list) {
    return zipWithIndex(list.stream());
  }

  /**
   * [a] -> [b] -> [(a,b)]
   */
  public static <A, B> Function<List<B>, ArrayList<Pair<A, B>>> zip(List<A> as) {
    Function<A, Function<B, Pair<A, B>>> f = a -> b -> new Pair<A, B>(a, b);
    return b -> zipWith(f).apply(as).apply(b);
  }

  public static <A, B> Pair<ArrayList<A>, ArrayList<B>> unzip(Stream<Pair<A, B>> xs, int init_size) {
    ArrayList<A> as = emptyList(init_size);
    ArrayList<B> bs = emptyList(init_size);
    xs.forEachOrdered(x -> {
      as.add(x._1);
      bs.add(x._2);
    });
    return pair(as, bs);
  }

  public static <A, B> Pair<ArrayList<A>, ArrayList<B>> unzip(Collection<Pair<A, B>> xs) {
    return unzip(xs.stream(), xs.size());
  }

  public static <A, B> Pair<ArrayList<A>, ArrayList<B>> unzip(Stream<Pair<A, B>> xs) {
    return unzip(xs, 0);
  }

  /**
   * @param n : bin size, if the bin size is unknown, use @group instead
   * @param f : tell the element to be routed to left or right
   * @return : binned List of Data
   * @remark different from @span, this is not longest match, this go through every element
   */
  public static <A> ArrayList<ArrayList<A>> group(Iterable<A> xs, int n, Function<A, Integer> f) {
    ArrayList<A>[] res = tabulate(n, i -> new ArrayList<A>(n), ArrayList.class);
    xs.forEach(x -> res[f.apply(x)].add(x));
    return list(res);
  }

  public static <K, V> HashMap<K, ArrayList<V>> group(Iterable<V> xs, Function<V, K> f) {
    HashMap<K, ArrayList<V>> res = new HashMap<K, ArrayList<V>>();
    xs.forEach(x -> {
      K k = f.apply(x);
      res.putIfAbsent(k, new ArrayList<V>());
      res.get(k).add(x);
    });
    return res;
  }

  public static <A> A[][] group(A[] as, int group_size, Class<A> aClass) {
    if (group_size < 0)
      throw new Error(new IllegalArgumentException("group_size should be positive"));
    /* total number of element */
    int n = as.length;
    if (n == 0 || group_size == 0) {
      return empty(0, 0, aClass);
    } else if (n == 1) {
      A[][] res = empty(1, 0, aClass);
      res[0] = as;
      return res;
    }
    int n_group = (int) Math.round(Math.ceil(1.0 * n / group_size));
    int size = n / n_group;
    final Integer sizes[] = fill(n_group, size);
    foreach(n - size * n_group, i -> sizes[i]++);
    A[][] ass = empty(n_group, i -> sizes[i], aClass);
    int offset = 0;
    for (int i = 0; i < n_group; i++) {
      System.arraycopy(
        as, offset,
        ass[i], 0,
        sizes[i]
      );
      offset += sizes[i];
    }
    return ass;
  }

  public static <A> A[][] group(Collection<A> as, int group_size, Class<A> aClass) {
    return group(array(as, aClass), group_size, aClass);
  }

  public static <K, V> HashMap<K, ArrayList<V>> groupByKey(Stream<Pair<K, V>> stream) {
    HashMap<K, ArrayList<V>> res = new HashMap<>();
    stream.sequential().forEach(p -> {
      res.putIfAbsent(p._1, new ArrayList<V>());
      res.get(p._1).add(p._2);
    });
    return res;
  }

  /* the pair key should be unique, otherwise some records will be lost during this operation */
  public static <K, V> HashMap<K, V> flatByKey(Stream<Pair<K, V>> stream) {
    HashMap<K, V> res = new HashMap<K, V>();
    stream.forEach(p -> res.put(p._1, p._2));
    return res;
  }

  public static <K, V> Stream<Pair<K, V>> stream(Map<K, V> map) {
    return map.entrySet().stream().map(p -> pair(p.getKey(), p.getValue()));
  }

  public static <A> ArrayList<ArrayList<A>> group(Stream<A> as, int group_size) {
    if (group_size < 0)
      throw new Error(new IllegalArgumentException("group_size should be positive"));
    else if (group_size == 1)
      return wrap(list(as));
    ArrayList<ArrayList<A>> res = new ArrayList<>();
    AtomicReference<ArrayList<A>> group = new AtomicReference<>(new ArrayList<A>(group_size));
    as.forEach(a -> {
      if (group.get().size() == group_size) {
        res.add(group.get());
        group.set(new ArrayList<A>(group_size));
      }
      group.get().add(a);
    });
    res.add(group.get());
    return res;
  }

  public static <A> ArrayList<ArrayList<A>> evenGroup(Stream<A> as, int n_group) {
    if (n_group < 0)
      throw new Error(new IllegalArgumentException("n_group should be positive"));
    if (n_group == 0)
      return new ArrayList<>();
    else if (n_group == 1)
      return wrap(list(as));
    ArrayList<A>[] res = fill(n_group, () -> new ArrayList<A>(), ArrayList.class);
    final int[] i = {0};
    as.forEach(a -> {
      res[i[0]].add(a);
      i[0] = (i[0] + 1) % n_group;
    });
    return list(res);
  }

  public static <A> ArrayList<ArrayList<A>> evenGroup(Collection<A> as, int n_group) {
    return evenGroup(as.stream(), n_group);
  }

  public static <A> ArrayList<ArrayList<A>> randomGroup(Iterable<A> xs, int n_group) {
    ArrayList<A>[] res = fill(n_group, () -> new ArrayList(), ArrayList.class);
    xs.forEach(x -> res[random.nextInt(n_group)].add(x));
    return list(res);
  }

  public static <A> Pair<ArrayList<A>, ArrayList<A>> copy(Stream<A> xs) {
    return unzip(xs.map(x -> pair(x, x)));
  }

  /**
   * copy reference, not deep clone
   * if the stream element are mutable, don't use this function
   * <p>
   * Sample input : [1,2,3]
   * Sample output : [[1,2,3], [1,2,3]]
   */
  public static <A> Stream<ArrayList<A>> copy(Stream<A> stream, int n_copy) {
    ArrayList<A> list = list(stream);
    return mkStream(n_copy)
      .mapToObj(i -> list);
  }

  public static void foreach(int n, Consumer<Integer> f) {
    for (int i = 0; i < n; i++) {
      f.accept(i);
    }
  }

  public static void par_foreach(int n, Consumer<Integer> f) {
    mkStream(n).parallel().forEach(f::accept);
  }

  public static <A, B> ArrayList<Pair<A, B>> combine(Collection<A> as, Collection<B> bs) {
    ArrayList<Pair<A, B>> res = new ArrayList<>(as.size() * bs.size());
    for (A a : as) {
      for (B b : bs) {
        res.add(pair(a, b));
      }
    }
    return res;
  }

  public static <A> ArrayList<Pair<A, A>> self_combine(Collection<A> as) {
    return combine(as, as);
  }

  public static int round_up_to_even(int i) {
    return i + i % 2;
  }
  /*  from Haskell  */

  public static <A> ArrayList<A> reverse(List<A> as) {
    final int n = as.size();
    ArrayList<A> bs = new ArrayList<A>(n);
    for (int i = n - 1; i >= 0; i--) {
      bs.add(as.get(i));
    }
    return bs;
  }

  public static <A> Function<Collection<A>, ArrayList<A>> append(Collection<A> a) {
    return b -> {
      ArrayList<A> c = new ArrayList<A>();
      c.addAll(a);
      c.addAll(b);
      return c;
    };
  }

  public static <A> Function<List<A>, ArrayList<A>> filter(Function<A, Boolean> f) {
    return a -> list(a.stream().filter(f::apply));
  }

  /**
   * @return List of two list (logically list pair of same type)
   */
  public static <A> Function<List<A>, ArrayList<ArrayList<A>>> span(Function<A, Boolean> f) {
    return a -> {
      ArrayList<A> b = new ArrayList<A>(a.size());
      ArrayList<A> c = new ArrayList<A>(a.size());
      final boolean[] met = {false};
      a.stream().forEachOrdered(x -> {
        if (met[0])
          c.add(x);
        else if
          (f.apply(x)) b.add(x);
        else {
          met[0] = true;
          c.add(x);
        }
      });
      return pair(b, c).list();
    };
  }

  public static <A> Function<ArrayList<A>, ArrayList<ArrayList<A>>> _break(Function<A, Boolean> f) {
    return a -> reverse(span(f).apply(a));
  }

  public static Boolean and(ArrayList<Boolean> a) {
    return a.stream().allMatch(Utils::id);
  }

  public static boolean and(boolean[] bs) {
    for (boolean b : bs) {
      if (!b)
        return false;
    }
    return true;
  }

  public static Boolean or(ArrayList<Boolean> a) {
    return a.stream().anyMatch(Utils::id);
  }

  public static boolean or(boolean[] bs) {
    for (boolean b : bs) {
      if (b)
        return true;
    }
    return false;
  }

  public static int first_true(boolean[] bs) {
    for (int i = 0; i < bs.length; i++) {
      if (bs[i])
        return i;
    }
    return -1;
  }

  public static <A> Function<ArrayList<A>, Boolean> any(Function<A, Boolean> f) {
    return a -> a.stream().anyMatch(f::apply);
  }

  public static <A> Function<ArrayList<A>, Boolean> all(Function<A, Boolean> f) {
    return a -> a.stream().allMatch(f::apply);
  }

  public static <A> ArrayList<A> concat(Collection<Collection<A>> ass) {
    return ass.stream()
      .flatMap(Collection::stream)
      .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * ( A -> [B] ) -> [A] -> [B]
   */
  public static <A, B> Function<Collection<A>, ArrayList<B>> concatMap(Function<A, Collection<B>> f) {
    return a -> list(a.stream().map(f).flatMap(Collection::stream));
  }

  /**
   * @not_used?
   */
  public static <A, B> Function<A, B> map(Function<A, B> f) {
    return a -> f.apply(a);
  }

  /*  Monad  */

  public interface Monad<A> {
    /**
     * @protected
     */
    A value();

    default <T> Monad<T> unit(T a) {
      try {
        if (a == null)
          return getClass().getDeclaredConstructor(Object.class).newInstance(a);
        else
          return getClass().getDeclaredConstructor(a.getClass()).newInstance(a);
      } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
        e.printStackTrace();
        throw new Error("Failed to create monad instance of " + getClass().getName() + "! Consider override this method");
      }
    }

    default <B> Monad<B> bind(Function<A, Monad<B>> f) {
      return f.apply(value());
    }

    default <B> Monad<B> map(Function<A, B> f) {
      return bind(f.andThen(this::unit));
    }

    default <B> B ap(Function<A, B> f) {
      return f.apply(this.value());
    }

    default void apply(Consumer<A> f) {
      f.accept(value());
    }
  }

  public interface ConcatableMonad<A> extends Monad<A> {
    ConcatableMonad<A> concat(ConcatableMonad<A> another);
  }

  public static <A> Monad<A> flat(Monad<Monad> mma) {
//    return mma.unit((A) mma.value().value());
    return mma.value();
  }

  /* the supplier will do side effect (block and wait function) */
  public interface IO<A> extends Monad<Supplier<A>> {
    default A _do() {
      return value().get();
    }

    default Promise<A, String> promise() {
      return defer(() -> _try(() -> value().get())).promise;
    }
  }

  public static <A> IO<A> io(Supplier<A> f) {
    return new IO() {
      @Override
      public Object value() {
        return f.get();
      }
    };
  }

  /**
   * do all io stuff, will not skip even if any goes wrong
   */
  public static <A> IO<ArrayList<Either<A, String>>> join(Collection<IO<A>> ios) {
    return io(() ->
      list(ios.stream().sequential()
        .map(io -> io.promise())
        .map(promise -> promise.either()))
    );
  }

  public static class ErrorObject extends Error {
    public final Object value;

    public ErrorObject(Object value) {
      super();
      this.value = value;
    }

    public ErrorObject(String message, Object value) {
      super(message);
      this.value = value;
    }

    public ErrorObject(String message, Throwable cause, Object value) {
      super(message, cause);
      this.value = value;
    }

    public ErrorObject(Throwable cause, Object value) {
      super(cause);
      this.value = value;
    }

    protected ErrorObject(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object value) {
      super(message, cause, enableSuppression, writableStackTrace);
      this.value = value;
    }
  }

  public static class Promise<A, E> implements ConcatableMonad<A> {
    private boolean done = false;
    private boolean fail = false;
    private A result;
    private E error;

    @Override
    public A value() {
      return either().left();
    }

    public <B> Promise<B, E> then(Function<A, B> f) {
      return defer(() -> {
        try {
          return left(f.apply(value()));
        } catch (ErrorObject e) {
          return right((E) e.value);
        }
      }).promise;
    }

    @Override
    public Promise<A, E> concat(ConcatableMonad<A> another) {
      Promise<A, E> _this = this;
      if (instanceOf(Promise.class, another)) {
        return defer(() -> _this.either().map(x -> ((Promise<A, E>) another).either())).promise;
      } else throw new Error("another is not instanceof Promise");
    }

    public Either<A, E> either() {
      while (!done) {
        try {
          this.wait();
        } catch (InterruptedException e) {
          if (!done) {
            e.printStackTrace();
            throw new Error(e);
          }
        }
      }
      if (fail)
        return right(error);
      else
        return left(result);
    }
  }

  public static class Defer<A, E> implements ConcatableMonad<Promise<A, E>> {
    public final Promise<A, E> promise;

    public Defer(Promise<A, E> promise) {
      this.promise = promise;
    }

    @Override
    public Promise<A, E> value() {
      return this.promise;
    }

    public void resolve(A a) {
      promise.result = a;
      promise.done = true;
      promise.notifyAll();
    }

    public void reject(E e) {
      promise.error = e;
      promise.fail = true;
      promise.done = true;
      promise.notifyAll();
    }

    @Override
    public Defer<A, E> concat(ConcatableMonad<Promise<A, E>> another) {
      if (instanceOf(Defer.class, another)) {
        return new Defer(promise.concat(((Defer<A, E>) another).promise));
      } else throw new Error("another is not instanceof Defer");
    }
  }

  public static Thread fork(Runnable f) {
    Thread res = new Thread(f);
    res.start();
    return res;
  }

  public static Thread[] fork(final Runnable f, final int n_repeat) {
    Thread[] res = new Thread[n_repeat];
    for (int i = 0; i < res.length; i++) {
      res[i] = fork(f);
    }
    return res;
  }

  public static Thread[] fork(Runnable[] fs) {
    Thread[] res = new Thread[fs.length];
    for (int i = 0; i < fs.length; i++) {
      res[i] = fork(fs[i]);
    }
    return res;
  }

  public static void wait(Thread[] ts) throws InterruptedException {
    for (Thread t : ts) {
      t.join();
    }
  }

  public static void fork_and_wait(Runnable[] fs) throws InterruptedException {
    wait(fork(fs));
  }

  public static void fork_and_wait(Runnable f, int n_repeat) throws InterruptedException {
    wait(fork(f, n_repeat));
  }

  public static <A, E> Defer<A, E> defer(Supplier<Either<A, E>> f) {
    Defer<A, E> res = new Defer<A, E>(new Promise<A, E>());
    fork(() -> f.get().apply(res::resolve, res::reject));
    return res;
  }

  static final WeakHashMap<Lazy, Object> LazyCache = new WeakHashMap();

  public interface Lazy<A> extends Supplier<A>, IO<Supplier<A>> {
    @Override
    default A get() {
      if (LazyCache.containsKey(this)) {
        return (A) LazyCache.get(this);
      } else {
        A a = value().get().get();
        LazyCache.put(this, a);
        return a;
      }
    }
  }

  public static <A extends IO<a>, a> Lazy<a> lazy(IO<a> io) {
    return () -> io::value;
  }

  public static boolean isPrintable(char a) {
    return a > 31;
  }

  public static boolean isAlphabet(char a) {
    return ('A' <= a && a <= 'Z') || ('a' <= a && a <= 'z');
  }

  public static boolean isDigit(char a) {
    return '0' <= a && a <= '9';
  }

  public static boolean isWhitespace(char a) {
    switch (a) {
      case ' ':
      case '\t':
      case '\r':
      case '\n':
        return true;
      default:
        return false;
    }
  }

  public static boolean isSymbol(char a) {
    switch (a) {
      case '~':
      case '`':
      case '!':
      case '@':
      case '#':
      case '$':
      case '%':
      case '^':
      case '&':
      case '*':
      case '(':
      case ')':
      case '-':
      case '_':
      case '+':
      case '=':
      case '{':
      case '}':
      case '[':
      case ']':
      case ':':
      case ';':
      case '\'':
      case '"':
      case '<':
      case '>':
      case ',':
      case '.':
      case '/':
      case '?':
      case '|':
      case '\\':
        return true;
      default:
        return false;
    }
  }

  public static boolean isVisible(char a) {
    //return isDigit(a) || isAlphabet(a) || isWhitespace(a) || isSymbol(a);
    return 32 <= a && a < 127;
  }

  public static void write_to_file(String filename, String content) throws IOException {
    FileWriter w = new FileWriter(filename);
    w.write(content);
    w.close();
  }

  public static void append_to_file(String filename, String content) throws IOException {
    FileWriter w = new FileWriter(filename, true);
    w.write(content);
    w.close();
  }

  /**
   * C style wrappers
   * low level stuff
   * to avoid creating new array instance, and faster access than using list
   * */
  public static class ObjectArray<A> {
    public A[] data;
    public int offset;
    public int len; /* number of valid data starting from offset, can be smaller than data.length */

    public ObjectArray(int size, Class<A> aClass) {
      data = (A[]) Array.newInstance(aClass, size);
      len = size;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ObjectArray) {
        ObjectArray o = (ObjectArray) obj;

        if (this.len != o.len)
          return false;

        for (int i = 0; i < len; i++) {
          if (data[i + offset] != o.data[i + o.offset]) {
            return false;
          }
        }
        return true;
      }
      return super.equals(obj);
    }
  }

  public static class ByteArray {
    public byte[] data;
    public int offset;
    public int len;

    public ByteArray(int size) {
      data = new byte[size];
      len = size;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ByteArray) {
        ByteArray o = (ByteArray) obj;

        if (this.len != o.len)
          return false;

        for (int i = 0; i < len; i++) {
          if (data[i + offset] != o.data[i + o.offset]) {
            return false;
          }
        }
        return true;
      }
      return super.equals(obj);
    }

    @Override
    public String toString() {
      StringBuilder b = new StringBuilder(len);
      b.append(getClass().getName());
      b.append('[');
      if (0 < len && len <= 10) {
        b.append(data[offset]);
        for (int i = 1; i < len; i++) {
          b.append(", ");
          b.append(data[i + offset]);
        }
      } else if (len > 10) {
        b.append(data[offset]);
        for (int i = 1; i < 9; i++) {
          b.append(", ");
          b.append(data[i + offset]);
        }
        b.append(" ... ");
        b.append(data[offset + len - 1]);
      }
      b.append(']');
      return b.toString();
    }
  }

  public static class CharArray {
    public char[] data;
    public int offset;
    public int len;

    public CharArray(int size) {
      data = new char[size];
      len = size;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof CharArray) {
        Utils.CharArray o = (CharArray) obj;

        if (this.len != o.len)
          return false;

        for (int i = 0; i < len; i++) {
          if (data[i + offset] != o.data[i + o.offset]) {
            return false;
          }
        }
        return true;
      }
      return super.equals(obj);
    }

    @Override
    public String toString() {
      StringBuilder b = new StringBuilder(len);
      b.append(getClass().getName());
      b.append('[');
      if (0 < len && len <= 10) {
        b.append(data[offset]);
        for (int i = 1; i < len; i++) {
          b.append(", ");
          b.append(data[i + offset]);
        }
      } else if (len > 10) {
        b.append(data[offset]);
        for (int i = 1; i < 9; i++) {
          b.append(", ");
          b.append(data[i + offset]);
        }
        b.append(" ... ");
        b.append(data[offset + len - 1]);
      }
      b.append(']');
      return b.toString();
    }
  }

  public static int gcd(int a, int b) {
    for (int i = Math.max(a, b); ; i--) {
      if (a % i == 0 && b % i == 0)
        return i;
    }
  }

  /**
   * similar to ??? in scala
   * */
  public static <A> A $$$() {
    throw new NotImplementedException();
  }

  public static class ThreadLocalStorage<A> {
    public ConcurrentHashMap<Thread, A> data;
    public Supplier<A> defaultSupplier;

    public ThreadLocalStorage(Supplier<A> defaultSupplier) {
      data = new ConcurrentHashMap<Thread, A>();
      this.defaultSupplier = defaultSupplier;
    }

    public ThreadLocalStorage(int initSize, Supplier<A> defaultSupplier) {
      data = new ConcurrentHashMap<Thread, A>(initSize);
      this.defaultSupplier = defaultSupplier;
    }

    public A current() {
      A a = data.get(Thread.currentThread());
      if (a == null) {
        a = defaultSupplier.get();
        data.put(Thread.currentThread(), a);
        return a;
      } else {
        return a;
      }
    }
  }

  /**
   * the Maps in java std api do not have getOrUpdate, mainly for this feature
   *
   * minimal requirement (e.g. no size)
   * user can use any data struct underlay (e.g. use array for speed)
   *
   * one application is to implement pure function's cache
   *   i.e. same argument will be evaluated only once
   *
   * it can be used to implement SparseIntegerMap (in android sdk)
   * */
  public interface IMap<K, V> {
    V get(K k);

    default void set(K k, V v) {
    }
  }

  public interface IMap2<K1, K2, V> {
    V get(K1 k1, K2 k2);

    default void set(K1 k1, K2 k2, V v) {
    }
  }
}
