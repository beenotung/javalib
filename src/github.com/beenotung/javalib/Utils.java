package github.com.beenotung.javalib;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.*;

public class Utils {
  public static final Scanner in = new Scanner(System.in);
  public static final PrintStream out = System.out;

  public static void print(Object... msgs) {
    System.out.print(toString(msgs));
  }

  public static String toString(Object... os) {
    if (os.length == 1) {
      if (os[0] != null && os[0].getClass().isArray())
        return toString(os[0]);
      else
        return Objects.toString(os[0]);
    } else {
      String res = "(";
      for (int i = 0; i < os.length; i++) {
        if (i > 0)
          res += ",";
        res += toString(os[i]);
      }
      res += ")";
      return res;
    }
  }

  public static String repeat(int n, String s) {
    if (n == 0) return "";
    return mkIntStream(n).map(i -> s).reduce(String::concat).get();
  }

//  public static void print(Object msg) {
//    System.out.print(msg);
//  }

  public static void println() {
    System.out.println();
  }

  public static void println(Object... msgs) {
    print(msgs);
    println();
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

  public static double mse(double a, double b) {
    return Math.pow(a - b, 2);
  }

  public static <A extends Number> Optional<Double> average(Stream<A> stream) {
    return toOptional(
      stream
        .mapToDouble(a -> a.doubleValue())
        .average()
    );
  }

  public static <A, B, C> Func1<A, C> compose(Func1<B, C> g, Func1<A, B> f) {
    return a -> g.apply(f.apply(a));
  }

  public static <A> M<A> flat(M<M<A>> mma) {
    return mma.value();
  }

  /* slow, unsafe */
//  public static <A extends Number> A sum(LazyArrayList<A> m) {
//    return m.stream().reduce(Utils::sum).get();
//  }

//  public static int sumInt(LazyArrayList<Integer> a) {
//    return a.stream().reduce((acc, c) -> acc + c).orElse(0);
//  }

//  public static long sumLong(LazyArrayList<Long> a) {
//    return a.stream().reduce((acc, c) -> acc + c).orElse(0l);
//  }

//  public static double sumDouble(LazyArrayList<Double> a) {
//    return a.stream().reduce((acc, c) -> acc + c).orElse(0d);
//  }

//  public static float sumFloat(LazyArrayList<Float> a) {
//    return a.stream().reduce((acc, c) -> acc + c).orElse(0f);
//  }

  public static <A> A[] fill(int n, A a) {
    A as[] = (A[]) Array.newInstance(a.getClass(), n);
    for (int i = 0; i < n; i++) {
      as[i] = a;
    }
    return as;
  }

  /**
   * @deprecated slow
   */
  public static <A> A[] tabulate(int n, Function<Integer, A> f) {
    Object[] os = tabulate(n, f, (Class<A>) Object.class);
    A a = firstNonNull((A[]) os);
    return a == null ? (A[]) os : castArray(os, (Class<A>) a.getClass());
  }

  public static <A> A[] tabulate(int n, Function<Integer, A> f, Class<A> aClass) {
    A as[] = (A[]) Array.newInstance(aClass, n);
    for (int i = 0; i < n; i++) {
      as[i] = f.apply(i);
    }
    return as;
  }

  public static <A> A[] empty(int n, Class<A> aClass) {
    return (A[]) Array.newInstance(aClass, n);
  }

  public static <A> A[][] empty(int n, Func1<Integer, Integer> f, Class<A> aClass) {
    int avg_length = mkIntStream(n)
      .map(i -> f.apply(i))
      .reduce(Utils::sum)
      .get() / n;
    A[][] ass = (A[][]) Array.newInstance(aClass, n, avg_length);
    foreach(n, i -> ass[i] = (A[]) Array.newInstance(aClass, f.apply(i)));
    return ass;
  }

  public static char[] toChars(ArrayList<Character> as) {
    char[] cs = new char[as.size()];
    for (int i = 0; i < cs.length; i++) {
      cs[i] = as.get(i).charValue();
    }
    return cs;
  }

  public static ArrayList<Character> toChars(char[] cs) {
    ArrayList<Character> res = new ArrayList<>(cs.length);
    for (char a : cs) {
      res.add(a);
    }
    return res;
  }

//  public static String toString(FArray<Character> as) {
//    return String.valueOf(toChars(as.as));
//  }

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

  public static class Tuple2<T1, T2> {
    public final T1 _1;
    public final T2 _2;

    public Tuple2(T1 t1, T2 t2) {
      _1 = t1;
      _2 = t2;
    }

    @Override
    public String toString() {
      return Utils.toString(_1, _2);
    }

    public <T3> Tuple2<T1, T3> newVal(Func1<T2, T3> f) {
      return new Tuple2<T1, T3>(_1, f.apply(_2));
    }
  }

  public interface Producer<A> {
    A apply();
  }

  public interface Consumer<A> {
    void apply(A a);
  }

  public interface Consumer2<A, B> {
    void apply(A a, B b);
  }

  public interface ConcatMonad<A> {
    ConcatMonad<A> concat(ConcatMonad<A> ma);
  }

  public interface M<A> {

    /**
     * @protected
     */
    A value();

    Class<A> getComponentType();

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

    default void apply(Consumer<A> f) {
      f.apply(value());
    }
  }

  public static class Monad<A> implements M<A> {
    private A value;
    protected Class<A> componentType;

    /**
     * @internal
     */
    protected Monad() {
    }

    /**
     * @final
     * @deprecated slow
     */
    public Monad(A value) {
      this(value, value == null ? (Class<A>) Object.class : (Class<A>) value.getClass());
    }

    /**
     * @final
     */
    public Monad(A value, Class<A> componentType) {
      this.value = value;
      this.componentType = componentType;
    }

    @Override
    /**@protected*/
    public A value() {
      return value;
    }

    @Override
    public Class<A> getComponentType() {
      return null;
    }

    @Override
    /**@private*/
    public void setValue(A a) {
      this.value = a;
    }
  }

  public static class Maybe<A> extends Monad<A> {
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

  public static class IO<A> extends Monad<Producer<A>> {
    final Promise<A> promise;

    public IO(Promise<A> promise) {
      this.promise = promise;
    }

    void _do() {
      try {
        promise.resolve(value().apply());
      } catch (Throwable e) {
        promise.reject(e);
      }
    }

    public <B> IO<B> chain(Func1<A, B> f) {
      return new IO<B>(promise.then(f));
    }
  }

  public static <A, B> B foldl(Stream<A> stream, Func2<B, A, B> f, B init) {
    final B[] acc = (B[]) Array.newInstance(init == null ? Object.class : init.getClass(), 1);
    stream.sequential().forEachOrdered(c -> {
      acc[0] = f.apply(acc[0], c);
    });
    return acc[0];
  }

  public static <A, B> B fold(Stream<A> stream, Func2<B, A, B> f, B init) {
    AtomicReference<B> acc = new AtomicReference(init);
    stream.forEach(c -> {
      acc.updateAndGet(ac -> f.apply(ac, c));
    });
    return acc.get();
  }

  public static <A> A[] toArray(Stream<A> stream, Class<A> aClass) {
    return stream.toArray(i -> (A[]) Array.newInstance(aClass, i));
  }

  public interface RichStream<A> extends Stream<A> {
    Class<A> getComponentType();

    default <B> B foldl(Func2<B, A, B> f, B init) {
      return Utils.foldl(this, f, init);
    }

    default <B> B fold(Func2<B, A, B> f, B init) {
      return Utils.fold(this, f, init);
    }

    default A[] toTypedArray() {
      return castArray(toArray(), getComponentType());
    }

    /**
     * @throws ClassCastException if this stream is not ? extends Number
     */
    default Optional<Double> average() {
      return Utils.average(this.as());
    }

    /* cast the type */
    default <B> Stream<B> as() {
      //return this.map(x -> (B) x);
      return (Stream<B>) this;
    }
  }

  public static <A> RichStream<A> richStream(Stream<A> stream, Class<A> aClass) {
    return new RichStream<A>() {
      @Override
      public Class<A> getComponentType() {
        return aClass;
      }

      @Override
      public Stream<A> filter(Predicate<? super A> predicate) {
        return stream.filter(predicate);
      }

      @Override
      public <R> Stream<R> map(Function<? super A, ? extends R> mapper) {
        return stream.map(mapper);
      }

      @Override
      public IntStream mapToInt(ToIntFunction<? super A> mapper) {
        return stream.mapToInt(mapper);
      }

      @Override
      public LongStream mapToLong(ToLongFunction<? super A> mapper) {
        return stream.mapToLong(mapper);
      }

      @Override
      public DoubleStream mapToDouble(ToDoubleFunction<? super A> mapper) {
        return stream.mapToDouble(mapper);
      }

      @Override
      public <R> Stream<R> flatMap(Function<? super A, ? extends Stream<? extends R>> mapper) {
        return stream.flatMap(mapper);
      }

      @Override
      public IntStream flatMapToInt(Function<? super A, ? extends IntStream> mapper) {
        return stream.flatMapToInt(mapper);
      }

      @Override
      public LongStream flatMapToLong(Function<? super A, ? extends LongStream> mapper) {
        return stream.flatMapToLong(mapper);
      }

      @Override
      public DoubleStream flatMapToDouble(Function<? super A, ? extends DoubleStream> mapper) {
        return stream.flatMapToDouble(mapper);
      }

      @Override
      public Stream<A> distinct() {
        return stream.distinct();
      }

      @Override
      public Stream<A> sorted() {
        return stream.sorted();
      }

      @Override
      public Stream<A> sorted(Comparator<? super A> comparator) {
        return stream.sorted(comparator);
      }

      @Override
      public Stream<A> peek(java.util.function.Consumer<? super A> action) {
        return stream.peek(action);
      }

      @Override
      public Stream<A> limit(long maxSize) {
        return stream.limit(maxSize);
      }

      @Override
      public Stream<A> skip(long n) {
        return stream.skip(n);
      }

      @Override
      public void forEach(java.util.function.Consumer<? super A> action) {
        stream.forEach(action);
      }

      @Override
      public void forEachOrdered(java.util.function.Consumer<? super A> action) {
        stream.forEachOrdered(action);
      }

      @Override
      public Object[] toArray() {
        return stream.toArray();
      }

      @Override
      public <A1> A1[] toArray(IntFunction<A1[]> generator) {
        return stream.toArray(generator);
      }

      @Override
      public A reduce(A identity, BinaryOperator<A> accumulator) {
        return stream.reduce(identity, accumulator);
      }

      @Override
      public Optional<A> reduce(BinaryOperator<A> accumulator) {
        return stream.reduce(accumulator);
      }

      @Override
      public <U> U reduce(U identity, BiFunction<U, ? super A, U> accumulator, BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
      }

      @Override
      public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super A> accumulator, BiConsumer<R, R> combiner) {
        return stream.collect(supplier, accumulator, combiner);
      }

      @Override
      public <R, A1> R collect(Collector<? super A, A1, R> collector) {
        return stream.collect(collector);
      }

      @Override
      public Optional<A> min(Comparator<? super A> comparator) {
        return stream.min(comparator);
      }

      @Override
      public Optional<A> max(Comparator<? super A> comparator) {
        return stream.max(comparator);
      }

      @Override
      public long count() {
        return stream.count();
      }

      @Override
      public boolean anyMatch(Predicate<? super A> predicate) {
        return stream.anyMatch(predicate);
      }

      @Override
      public boolean allMatch(Predicate<? super A> predicate) {
        return stream.allMatch(predicate);
      }

      @Override
      public boolean noneMatch(Predicate<? super A> predicate) {
        return stream.noneMatch(predicate);
      }

      @Override
      public Optional<A> findFirst() {
        return stream.findFirst();
      }

      @Override
      public Optional<A> findAny() {
        return stream.findAny();
      }

      @Override
      public Iterator<A> iterator() {
        return stream.iterator();
      }

      @Override
      public Spliterator<A> spliterator() {
        return stream.spliterator();
      }

      @Override
      public boolean isParallel() {
        return stream.isParallel();
      }

      @Override
      public Stream<A> sequential() {
        return stream.sequential();
      }

      @Override
      public Stream<A> parallel() {
        return stream.parallel();
      }

      @Override
      public Stream<A> unordered() {
        return stream.unordered();
      }

      @Override
      public Stream<A> onClose(Runnable closeHandler) {
        return stream.onClose(closeHandler);
      }

      @Override
      public void close() {
        stream.close();
      }
    };
  }

  public interface RichList<A> {
    Class<A> getComponentType();

    List<A> list();

    default <B> RichList<B> as() {
      return (RichList<B>) this;
    }

    default int length() {
      return list().size();
    }

    default Optional<A> findAny(Func1<A, Boolean> f) {
      return stream().filter(a -> f.apply(a)).findAny();
    }

    default Optional<A> anyNotNull() {
      return findAny(a -> a != null);
    }

    /**
     * @proteched
     */
    void setUnderneath(A[] as, Class<A> aClass);

    /**
     * @protected
     */
    void setUnderneath(Collection<A> collection, Class<A> aClass);

    default Stream<A> stream() {
      return list().stream();
    }

    default A[] toTypedArray() {
      return Utils.toArray(stream(), getComponentType());
    }

    default RichStream<A> richStream() {
      return Utils.richStream(stream(), getComponentType());
    }

    default <A> RichList<A> newInstance(RichList<A> as, Class<A> aClass) {
      return newInstance(as.list(), aClass);
    }

    default <A> RichList<A> newInstance(Collection<A> as, Class<A> aClass) {
      try {
        RichList<A> res = getClass().newInstance();
        res.setUnderneath(as, aClass);
        return res;
      } catch (InstantiationException | IllegalAccessException e) {
        throw new Error("the subclass should have empty constructor");
      }
    }

    default <A> RichList<A> newInstance(A[] as, Class<A> aClass) {
      try {
        RichList<A> res = getClass().newInstance();
        res.setUnderneath(as, aClass);
        return res;
      } catch (InstantiationException | IllegalAccessException e) {
        throw new Error("the subclass of RichList (" + getClass().getName() + ") should have empty constructor");
      }
    }

    default RichList<RichList<A>> group(int group_size) {
      /* total number of element */
      int n = length();
      Class<A> aClass = getComponentType();
      if (n == 0 || group_size == 0) {
        return newInstance((RichList[]) Array.newInstance(getClass(), 0), (Class<RichList<A>>) getClass());
      }
      int n_group = (int) Math.round(Math.ceil(1.0 * n / group_size));
      int size = n / n_group;
      final Integer sizes[] = fill(n_group, size);
      foreach(n - size * n_group, i -> sizes[i]++);
      A[][] ass = Utils.empty(n_group, i -> sizes[i], aClass);
      int offset = 0;
      A[] as = toTypedArray();
      for (int i = 0; i < n_group; i++) {
        System.arraycopy(
          as, offset,
          ass[i], 0,
          sizes[i]
        );
        offset += sizes[i];
      }
      RichList<A>[] res = Utils.empty(n_group, getClass());
      foreach(n_group, i -> res[i] = newInstance((A[]) ass[i], aClass));
      return newInstance(res, (Class<RichList<A>>) getClass());
    }

    default RichList<RichList<A>> evenGroup(int n_group) {
      /* total number of element */
      int n = length();
      if (n == 0 || n_group == 0)
        return newInstance((RichList[]) Array.newInstance(getClass(), 0), (Class<RichList<A>>) getClass());
      Class<A> aClass = getComponentType();
      int size = n / n_group;
      final Integer[] sizes = Utils.fill(n_group, size);
      foreach(n - size * n_group, i -> sizes[i]++);
      A[][] ass = Utils.empty(n_group, i -> sizes[i], aClass);
      int ass_i = 0;
      int as_i = 0;
      A[] as = toTypedArray();
      for (int i = 0; i < n; i++) {
        ass[ass_i][as_i] = as[i];
        if (++ass_i == n_group) {
          ass_i = 0;
          as_i++;
        }
      }
      RichList<A>[] res = Utils.empty(n_group, getClass());
      foreach(n_group, i -> res[i] = newInstance(ass[i], aClass));
      return newInstance(res, (Class<RichList<A>>) getClass());
    }
  }

  public static Stream<Integer> mkIntStream(int n) {
    return mkIntStream(0, n);
  }

  public static <A> Stream<A> mkStream(int n, Func1<Integer, A> f) {
    return mkStream(0, n, f);
  }

  public static <A> Stream<A> mkStream(int offset, int n, Func1<Integer, A> f) {
    return mkIntStream(offset, n)
      .map(i -> f.apply(offset + i));
  }

  public static <A> Stream<Tuple2<Integer, A>> mkIndexedStream(int n, Func1<Integer, A> f) {
    return mkIndexedStream(0, n, f);
  }

  public static <A> Stream<Tuple2<Integer, A>> mkIndexedStream(int offset, int n, Func1<Integer, A> f) {
    return mkIntStream(offset, n)
      .map(i -> new Tuple2<Integer, A>(i, f.apply(i)));
  }

  public static Stream<Integer> mkIntStream(int offset, int count) {
    return new ArrayList<>(Arrays.asList(tabulate(count, i -> i + offset))).stream();
  }

  public static void foreach(int n, Consumer<Integer> f) {
    for (int i = 0; i < n; i++) {
      f.apply(i);
    }
  }

  @Deprecated
  public interface CommonList<A> {
    A get(int i);

    void set(int i, A a);

    int size();

    /**
     * @deprecated
     */
    A[] toArray();

    Stream<A> stream();

    /**
     * @private
     */
    CommonList<A> newInstance();

    default <A> CommonList<A> newInstance(A[] as) {
      return newInstance(Arrays.asList(as));
    }

    /**
     * @deprecated
     */
    default <A> CommonList<A> newInstance(Collection<A> as) {
      return newInstance(as, Utils.getComponentType(as));
    }

    <A> CommonList<A> newInstance(Collection<A> as, Class<A> aClass);

    default <A> CommonList<A> newInstance(CommonList<A> as) {
      return newInstance(as, (Class<A>) getComponentType());
    }

    <A> CommonList<A> newInstance(CommonList<A> as, Class<A> aClass);

    default <A> CommonList<A> fill(int n, A a) {
      return newInstance(Utils.fill(n, a));
    }

    default <A> CommonList<A> tabulate(int n, Function<Integer, A> f) {
      return newInstance(Utils.tabulate(n, f));
    }

    default CommonList<A> filter(Func1<A, Boolean> f) {
      A[] as = mkIntStream(size())
        .map(this::get)
        .filter(a -> f.apply(a))
        .toArray(Utils.genArrayFunc(getComponentType()));
      return newInstance(as);
    }

    /**
     * @deprecated slow
     */
    default <B> CommonList<B> map(Func1<A, B> f) {
      CommonList<B> os = map(f, (Class<B>) Object.class);
      B b = os.firstNonNull();
      return b == null ? os : newInstance(os, (Class<B>) b.getClass());
    }

    <B> CommonList<B> map(Func1<A, B> f, Class<B> bClass);

    default A reduce(Func2<A, A, A> f) {
      return Arrays.asList(toArray()).stream().reduce(new BinaryOperator<A>() {
        @Override
        public A apply(A a, A a2) {
          return f.apply(a, a2);
        }
      }).get();
    }

    default <B> B foldl(Func2<B, A, B> f, B init) {
      return Utils.foldl(stream(), f, init);
    }

    default Class<A> getComponentType() {
      A a = firstNonNull();
      return a == null ? (Class<A>) Object.class : (Class<A>) a.getClass();
    }

    default CommonList<A> filterNonNull() {
      return filter(a -> a != null);
    }

    A firstNonNull();

    default CommonList<CommonList<A>> group(int group_size) {
      /* total number of element */
      int n = size();
      if (n == 0)
        return (CommonList<CommonList<A>>) newInstance(newInstance((A[]) new Object[0]));
      int n_group = (int) Math.round(Math.ceil(1.0 * n / group_size));
//      CommonList<A>[] ass = (CommonList[]) Array.newInstance(getClass(), n_group);
      Class aClass = getComponentType();
      A[][] ass = (A[][]) Array.newInstance(aClass, n_group);
      int size = n / n_group;
      Integer sizes[] = Utils.fill(n_group, size);
      for (int i = 0; i < n - size * n_group; i++) {
        sizes[i]++;
      }
      A[] as = toArray();
      int offset = 0;
      for (int i = 0; i < n_group; i++) {
        ass[i] = (A[]) Array.newInstance(aClass, sizes[i]);
        System.arraycopy(
          as, offset,
          ass[i], 0,
          sizes[i]
        );
        offset += sizes[i];
      }
      CommonList<A>[] res = (CommonList<A>[]) Array.newInstance(getClass(), n_group);
      for (int i = 0; i < n_group; i++) {
        res[i] = newInstance(ass[i]);
      }
      return (CommonList<CommonList<A>>) newInstance((A[]) res);
    }

    default CommonList<CommonList<A>> evenGroup(int n_group) {
      int n = size();
      if (n == 0 && n_group == 0)
        return (CommonList<CommonList<A>>) newInstance(newInstance((A[]) new Object[0]));
      Class aClass = getComponentType();
      A[][] ass = (A[][]) Array.newInstance(aClass, n_group);
      int size = n / n_group;
      Integer[] sizes = Utils.fill(n_group, size);
      for (int i = 0; i < n - size * n_group; i++) {
        sizes[i]++;
      }
      for (int i = 0; i < n_group; i++) {
        ass[i] = (A[]) Array.newInstance(aClass, sizes[i]);
      }
      int ass_i = 0;
      int as_i = 0;
      for (int i = 0; i < n; i++) {
        ass[ass_i][as_i] = get(i);
        if (++ass_i == n_group) {
          ass_i = 0;
          as_i++;
        }
      }
      CommonList<A>[] res = (CommonList<A>[]) Array.newInstance(getClass(), n_group);
      for (int i = 0; i < n_group; i++) {
        res[i] = newInstance(ass[i]);
      }
      return (CommonList<CommonList<A>>) newInstance((A[]) res);
    }
  }

//  public static class LazyArrayList<A> /*implements M<Func1<Object, A>>*/ {
//    /**
//     * @write_only
//     */
//    public ArrayList list;
//    private Func1<Object, A> mapper = a -> (A) a;
//
//    public LazyArrayList() {
//      this.list = new ArrayList();
//    }
//
//    public LazyArrayList(ArrayList list) {
//      this.list = list;
//    }
//
//    public LazyArrayList(A[] list) {
//      this.list = new ArrayList(Arrays.asList(list));
//    }
//
//    public static <A> LazyArrayList<A> fill(int n, A a) {
//      return new LazyArrayList<A>(Utils.fill(n, a));
//    }
//
//    public static <A> LazyArrayList<A> tabulate(int n, Function<Integer, A> mapper, Class<A> aClass) {
//      return new LazyArrayList<A>(Utils.tabulate(n, mapper));
//    }
//
//    public <B> LazyArrayList<B> map(Func1<A, B> mapper) {
//      LazyArrayList<B> res = new LazyArrayList<B>(list);
//      res.mapper = this.mapper.map(mapper);
//      return res;
//    }
//
//    public <B> B foldl(Func2<B, A, B> mapper, B init) {
//      AtomicReference<B> acc = new AtomicReference<B>(init);
//      stream().forEachOrdered(c -> acc.set(mapper.apply(acc.get(), c)));
//      return acc.get();
//    }
//
//    public Stream<A> stream() {
//      return this.list.stream().map(new Function<A, A>() {
//        @Override
//        public A apply(A a) {
//          return mapper.apply(a);
//        }
//      });
//    }
//
//    public ArrayList<A> build() {
//      ArrayList<A> res = new ArrayList<A>(list.size());
//      stream().forEachOrdered(a -> res.add(a));
//      return res;
//    }
//
//    /* compact the chained map */
//    public synchronized void update() {
//      list = build();
//      mapper = a -> (A) a;
//    }
//
//    @Override
//    public String toString() {
//      Object[] as = build().toArray();
//      if (as.length > 0 && as[0] != null && Character.class.equals(as[0].getClass()))
//        return String.valueOf(toChars());
//      else
//        return Arrays.asList(as).toString();
//    }
//  }

  public static <A> IntFunction<A[]> genArrayFunc(Class<A> aClass) {
    return new IntFunction<A[]>() {
      @Override
      public A[] apply(int value) {
        return (A[]) Array.newInstance(aClass, value);
      }
    };
  }

  /**
   * @deprecated slow
   */
  public static <A> Class<A> getComponentType(Collection<A> as) {
    final Class<A>[] res = new Class[1];
    try {
      return (Class<A>) as.stream().filter(a -> a != null).map(a -> a.getClass()).findFirst().get();
    } catch (Exception e) {
      return (Class<A>) Object.class;
    }
  }

  /**
   * @deprecated slow
   */
  public static <A> A[] castArray(Object[] os) {
    A a = firstNonNull((A[]) os);
    return a == null ? (A[]) os : castArray(os, (Class<A>) a.getClass());
  }

  public static <A> A[] castArray(Object[] os, Class<A> aClass) {
    A as[] = (A[]) Array.newInstance(aClass, os.length);
    System.arraycopy(
      os, 0,
      as, 0,
      os.length
    );
    return as;
  }

  /**
   * @deprecated slow
   */
  public static <A, B> B[] map(A as[], Func1<A, B> f) {
    final int n = as.length;
    Object os[] = (Object[]) Array.newInstance(Object.class, n);
    map(as, f, (B[]) os);
    B b = firstNonNull((B[]) os);
    return b == null ? (B[]) os : castArray(os, (Class<B>) b.getClass());
  }

  public static <A> A firstNonNull(A as[]) {
    for (A a : as) {
      if (a != null)
        return a;
    }
    return null;
  }

  public static <A, B> B[] map(A as[], Func1<A, B> f, Class<B> bClass) {
    B bs[] = (B[]) Array.newInstance(bClass, as.length);
    map(as, f, bs);
    return bs;
  }

  public static <A, B> void map(A as[], Func1<A, B> f, B bs[]) {
    for (int i = 0; i < as.length; i++) {
      bs[i] = f.apply(as[i]);
    }
  }

  /**
   * @deprecated slow
   */
  public static <A> A[] filter(A as[], Func1<A, Boolean> f) {
    ArrayList<A> buffer = new ArrayList<A>(as.length);
    for (A a : as) {
      if (f.apply(a))
        buffer.add(a);
    }
    Class<A> aClass = getComponentType(buffer);
    A res[] = (A[]) Array.newInstance(aClass, buffer.size());
    res = buffer.toArray(res);
    return res;
  }

  public static <A> A[] filter(A as[], Func1<A, Boolean> f, Class<A> aClass) {
    ArrayList<A> buffer = new ArrayList<A>(as.length);
    for (A a : as) {
      if (f.apply(a))
        buffer.add(a);
    }
    A res[] = (A[]) Array.newInstance(aClass, buffer.size());
    res = buffer.toArray(res);
    return res;
  }

  public static Optional<Double> toOptional(OptionalDouble x) {
    if (x.isPresent())
      return Optional.of(x.getAsDouble());
    else
      return Optional.empty();
  }

  /* similar to id, but with cast sugar */
//  public static <A> A cast(Object a) {
//    return (A) a;
//  }

//  public static <A> boolean pass(A a) {
//    return true;
//  }

//  public static final Func1 ID = Utils::cast;
//  public static final Func1 TURE = x -> true;

  /*
  * functional ArrayList
  * */
  public static class FList<A, B> implements RichList<B> {
    protected final ArrayList<A> preValues;

    private boolean done = false;
    protected ArrayList<B> postValues;

    /**
     * @final
     */
    protected final Func1<A, B> mapper;
    /**
     * @final
     */
    protected final Func1<A, Boolean> filter;

    /**
     * @final
     */
    public Class<B> componentType;

    /**
     * @private
     */
    protected FList() {
      this.preValues = new ArrayList<A>();
      this.mapper = a -> (B) a;
      filter = a -> true;
      this.done = true;
      this.postValues = new ArrayList<B>();
      this.componentType = (Class<B>) Object.class;
    }

    public FList(ArrayList<B> postValues, Class<B> componentType) {
      this.preValues = (ArrayList<A>) postValues;
      this.mapper = a -> (B) a;
      this.filter = a -> true;
      this.done = true;
      this.postValues = postValues;
      this.componentType = componentType;
    }

    public FList(ArrayList<A> preValues, Func1<A, B> mapper, Func1<A, Boolean> filter, Class<B> componentType) {
      this.preValues = preValues;
      this.mapper = mapper;
      this.filter = filter;
      this.componentType = componentType;
    }

    @Override
    public Class<B> getComponentType() {
      return componentType;
    }

    @Override
    public ArrayList<B> list() {
      if (mapper == null || filter == null)
        throw new Error("Illegal Status");
      if (done) return postValues;
      postValues = preValues.stream()
        .filter(filter::apply)
        .map(mapper::apply)
        .collect(Collectors.toCollection(ArrayList::new));
      return postValues;
    }

    @Override
    public void setUnderneath(B[] bs, Class<B> bClass) {
      setUnderneath(Arrays.asList(bs), bClass);
    }

    @Override
    public void setUnderneath(Collection<B> collection, Class<B> bClass) {
      done = true;
      postValues = new ArrayList<B>(collection);
      componentType = bClass;
    }

    public void add(A a) {
      preValues.add(a);
    }

    public <C> FList<A, B> map(Func1<B, B> f) {
      return map(f, componentType);
    }

    public <C> FList<A, C> map(Func1<B, C> f, Class<C> cClass) {
      if (done)
        return new FList<A, C>((ArrayList<A>) postValues, a -> f.apply((B) a), a -> true, cClass);
      else
        return new FList<A, C>(preValues, a -> f.apply(mapper.apply(a)), filter, cClass);
    }

    public FList<A, B> filter(Func1<A, Boolean> f) {
      if (done)
        return new FList<A, B>((ArrayList<A>) postValues, a -> (B) a, f, componentType);
      else
        return new FList<A, B>(preValues, a -> (B) a, a -> filter.apply(a) && f.apply(a), componentType);
    }

    public static FList<?, Character> fromString(String s) {
      return new FList<>(toChars(s.toCharArray()), Character.class);
    }

    @Override
    public String toString() {
      if (Character.class.equals(componentType))
        return String.valueOf(toChars((ArrayList<Character>) list()));
      else
        return list().toString();
    }
  }

  public static class Promise<A> {
    private ArrayList<Consumer<A>> pendings = new ArrayList<>();
    private ArrayList<Consumer> failed = new ArrayList<>(); //TODO

    public <B> Promise<B> then(Func1<A, B> f) {
      Promise<B> next = new Promise<B>();
      pendings.add(a -> next.resolve(f.apply(a)));
      return next;
    }

    public void then(Consumer<A> f) {
      pendings.add(f::apply);
    }

    public void otherwise(Consumer f) {
      failed.add(f::apply);
    }

    public void resolve(A a) {
      pendings.forEach(c -> c.apply(a));
    }

    public void reject(Object error) {
      failed.forEach(c -> c.apply(error));
    }
  }

  public static <A> Stream<Tuple2<Integer, A>> zipWithIndex(Stream<A> stream) {
    final int[] idx = new int[1];
    return stream.sequential().map(a -> new Tuple2<Integer, A>(idx[0]++, a));
  }

  public static <A> void forEach(Stream<A> as, Consumer2<Integer, A> f) {
    final int[] i = new int[1];
    as.forEachOrdered(a -> f.apply(i[0]++, a));
  }

  public static <A> void forEach(Collection<A> as, Consumer2<Integer, A> f) {
    forEach(as.stream(), f);
  }

  public static <A> IO<Collection<A>> join(List<IO<A>> ios, Class<A> aClass) {
    Promise<Collection<A>> promise = new Promise<>();
    A as[] = (A[]) Array.newInstance(aClass, ios.size());
    Object es[] = new Object[ios.size()];
    AtomicInteger done = new AtomicInteger(0);
    AtomicInteger fail = new AtomicInteger(0);
    forEach(ios, (i, io) -> {
      io.promise.then(a -> {
        as[i] = a;
        if (done.incrementAndGet() + fail.get() == as.length) {
          if (done.get() == as.length)
            promise.resolve(new ArrayList<A>(Arrays.asList(as)));
          else
            promise.reject(es);
        }
      });
      io.promise.otherwise(e -> {
        es[i] = e;
        if (fail.incrementAndGet() + done.get() == as.length) {
          promise.reject(es);
        }
      });
    });
    return new IO<>(promise);
  }
//
//  /*
//  * functional Array
//  *
//  * non-resizable
//  * use native array directly, should be faster than LazyArrayList?
//  * */
//  public static class FArray<A> {
//    final A[] as;
//
//    private FArray() {
//      as = (A[]) new Object[0];
//    }
//
//    public FArray(A[] as) {
//      this.as = as;
//    }
//
//
//    @Override
//    public A get(int i) {
//      return as[i];
//    }
//
//    @Override
//    public void set(int i, A a) {
//      as[i] = a;
//    }
//
//    @Override
//    public int size() {
//      return as.length;
//    }
//
//    @Override
//    public A[] toArray() {
//      return as;
//    }
//
//    @Override
//    public Collection<A> toCollection() {
//      return null;
//    }
//
//    @Override
//    public CommonList<A> newInstance() {
//      return new FArray<A>();
//    }
//
//    @Override
//    public CommonList<A> newInstance(A[] as) {
//      return new FArray<A>(as);
//    }
//
//    @Override
//    public CommonList<A> newInstance(Collection<A> as, Class<A> aClass) {
//      A res[] = (A[]) Array.newInstance(aClass, as.size());
//      res = as.toArray(res);
//      return new FArray<A>(res);
//    }
//
//    @Override
//    public CommonList<A> newInstance(CommonList<A> as) {
//      return new FArray<A>(((FArray<A>) as).as);
//    }
//
//    @Override
//    public CommonList<A> filter(Func1<A, Boolean> mapper) {
//      return new FArray<A>(Utils.filter(as, mapper));
//    }
//
//    @Override
//    public CommonList<A> filter(Func1<A, Boolean> mapper, Class<A> aClass) {
//      return new FArray<A>(Utils.filter(as, mapper, aClass));
//    }
//
//    @Override
//    public <B> CommonList<B> map(Func1<A, B> mapper) {
//      return new FArray<B>(Utils.map(as, mapper));
//    }
//
//    @Override
//    public <B> CommonList<B> map(Func1<A, B> mapper, Class<B> bClass) {
//      return new FArray<B>(Utils.map(as, mapper, bClass));
//    }
//
//    @Override
//    public A reduce(Func2<A, A, A> mapper) {
//      A acc = as[0];
//      for (int i = 1; i < as.length; i++) {
//        acc = mapper.apply(acc, as[i]);
//      }
//      Arrays.asList(as).stream().reduce(new BinaryOperator<A>() {
//        @Override
//        public A apply(A a, A a2) {
//          return mapper.apply(a, a2);
//        }
//      });
//      return acc;
//    }
//
//    @Override
//    public <B> B foldl(Func2<B, A, B> mapper, B init) {
//      return null;
//    }
//
//    @Override
//    public A firstNonNull() {
//      return null;
//    }
//  }

}
