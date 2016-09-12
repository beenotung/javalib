package beenotung.javalib;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.*;

//
public class Utils_old {
  public static RichList<Double> toList(double as[]) {
    Double[] xs = tabulate(as.length, i -> as[i], Double.class);
    return new FList<Object, Double>(xs, Double.class);
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

  public static class Tuple2<T1, T2> {
    public final T1 _1;
    public final T2 _2;

    public Tuple2(T1 t1, T2 t2) {
      _1 = t1;
      _2 = t2;
    }

    @Override
    public String toString() {
      return github.com.beenotung.javalib.Utils.toString(_1, _2);
    }

    public <T3> Tuple2<T1, T3> newVal(Func1<T2, T3> f) {
      return new Tuple2<T1, T3>(_1, f.apply(_2));
    }
  }

  public static <A, B, C> Func1<A, Func1<B, C>> curry(Func1<Tuple2<A, B>, C> f) {
    return a -> b -> f.apply(new Tuple2<A, B>(a, b));
  }

  public static <A, B, C> Func1<Tuple2<A, B>, C> uncurry(Func1<A, Func1<B, C>> f) {
    return ab -> f.apply(ab._1).apply(ab._2);
  }

  public static <A, B, C> Func1<B, Func1<A, C>> flip(Func1<A, Func1<B, C>> f) {
    return b -> a -> f.apply(a).apply(b);
  }

  public static <A> Func1<?, A> _const(A a) {
    return b -> a;
  }

  public static <A> Func1<Func1<A, A>, Func1<A, A>> until(Func1<A, Boolean> f) {
    return g -> h -> {
      A t;
      do {
        t = g.apply(h);
      } while (!f.apply(t));
      return t;
    };
  }

  public static <A, B> B seq(Func1<A, B> f) {
    Lazy<B> l = (Lazy<B>) f;
    return l.apply(null);
  }

  public static <A, B> Func1<A, B> map(Func1<A, B> f) {
    return a -> f.apply(a);
  }

  public static <A> Func1<RichList<A>, RichList<A>> append(RichList<A> a) {
    return b -> {
      RichList<A> c = a.newInstance();
      List<A> cs = c.list();
      cs.addAll(a.list());
      cs.addAll(b.list());
      c.setUnderneath(cs, a.getComponentType());
      return c;
    };
  }

  public static <A> Func1<RichList<A>, RichList<A>> filter(Func1<A, Boolean> f) {
    return a -> a.newInstance((RichList<A>) a.stream().filter(f::apply), a.getComponentType());
  }

  public static <A> Func1<RichList<A>, RichList<RichList<A>>> span(Func1<A, Boolean> f) {
    return a -> {
      ArrayList<A> b = new ArrayList<A>(a.length());
      ArrayList<A> c = new ArrayList<A>(a.length());
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
      Class<RichList<A>> aClass = (Class<RichList<A>>) a.getClass();
      RichList<A> as[] = (RichList[]) Array.newInstance(aClass, 2);
      as[0] = a.newInstance(b, a.getComponentType());
      as[1] = a.newInstance(c, a.getComponentType());
      return a.newInstance(as, aClass);
    };
  }

  public static boolean not(boolean b) {
    return !b;
  }

  public static <A> A id(A a) {
    return a;
  }

  /* TODO clean up up to here */

  public static <A> RichList<A> reverse(RichList<A> a) {
    final int n = a.length();
    ArrayList<A> bs = new ArrayList<A>(n);
    List<A> as = a.list();
    for (int i = n - 1; i >= 0; i--) {
      bs.add(as.get(i));
    }
    return a.newInstance(bs);
  }

  public static <A> Func1<RichList<A>, RichList<RichList<A>>> _break(Func1<A, Boolean> f) {
    return a -> reverse(span(f).apply(a));
  }

  public static Boolean and(RichList<Boolean> a) {
    return a.stream().allMatch(github.com.beenotung.javalib.Utils::id);
  }

  public static Boolean or(RichList<Boolean> a) {
    return a.stream().anyMatch(github.com.beenotung.javalib.Utils::id);
  }

  public static <A> Func1<RichList<A>, Boolean> any(Func1<A, Boolean> f) {
    return a -> a.stream().anyMatch(f::apply);
  }

  public static <A> Func1<RichList<A>, Boolean> all(Func1<A, Boolean> f) {
    return a -> a.stream().allMatch(f::apply);
  }

  public static <A> RichList<A> concat(RichList<RichList<A>> ass) {
    ArrayList<A> as = ass.stream().sequential()
      .flatMap(x -> x.stream())
      .collect(Collectors.toCollection(ArrayList::new));
    if (as.size() == 0)
      return ass.newInstance(as, (Class<A>) ass.getComponentType());
    else
      return ass.newInstance(as, getComponentType(as));
  }

  public static <A, B> Func1<RichList<A>, RichList<B>> concatMap(Func1<A, RichList<B>> f) {
    return a -> {
      Stream<B> bs = a.stream().flatMap(x -> f.apply(x).stream());
      ArrayList<B> b = collect(bs);
      if (b.size() == 0)
        return a.newInstance(b, (Class<B>) a.getComponentType());
      else
        return a.newInstance(b, getComponentType(b));
    };
  }

  public static <A, B> Func1<RichList<B>, RichList<Tuple2<A, B>>> zip(RichList<A> as) {
    Func1<A, Func1<B, Tuple2<A, B>>> f = a -> b -> new Tuple2<A, B>(a, b);
    return b -> zipWith(f).apply(as).apply(b);
  }

  public static <A, B> Tuple2<RichList<A>, RichList<B>> unzip(RichList<Tuple2<A, B>> xs) {
    final int n = xs.length();
    ArrayList<A> as = new ArrayList<A>(n);
    ArrayList<B> bs = new ArrayList<B>(n);
    Tuple2<ArrayList<A>, ArrayList<B>> res = new Tuple2(new ArrayList<A>(n), new ArrayList<B>(n));
    xs.stream().forEachOrdered(x -> {
      as.add(x._1);
      bs.add(x._2);
    });
    return new Tuple2(xs.newInstance(as, getComponentType(as, (Class<A>) xs.getComponentType())),
      xs.newInstance(bs, getComponentType(bs, (Class<B>) xs.getComponentType()))
    );
  }

  public static Func1<String, RichList<String>> split(String pattern) {
    return s -> new FList<Object, String>(s.split(pattern), String.class);
  }

  public static Func1<RichList<String>, String> unsplit(String pattern) {
    return s -> s.stream().sequential().reduce("", (acc, c) -> acc + pattern + c).substring(1);
  }

  public static RichList<String> lines(String s) {
    return split("\n").apply(s);
  }

  public static RichList<String> words(String s) {
    return split(" ").apply(s);
  }

  public static String unlines(RichList<String> ss) {
    return unsplit("\n").apply(ss);
  }

  public static String unwords(RichList<String> ss) {
    return unsplit(" ").apply(ss);
  }

  public static <A, B, C> Func1<RichList<A>, Func1<RichList<B>, RichList<C>>> zipWith(Func1<A, Func1<B, C>> f) {
    return a -> b -> {
      final int n = Math.min(a.length(), b.length());
      ArrayList<C> cs = new ArrayList<>(n);
      List<A> as = a.list();
      List<B> bs = b.list();
      for (int i = 0; i < n; i++) {
        cs.add(f.apply(as.get(i)).apply(bs.get(i)));
      }
      Class<C> cClass = a.length() < b.length() ? (Class<C>) a.getComponentType() : (Class<C>) b.getComponentType();
      return a.newInstance(cs, getComponentType(cs, cClass));
    };
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

    public A get() {
      return Objects.requireNonNull(value());
    }

    public A getOrElse(A defaultValue) {
      return github.com.beenotung.javalib.Utils.and(value(), defaultValue);
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

  @Deprecated
  public interface RichStream<A> extends Stream<A> {
    Class<A> getComponentType();

    default <B> B foldl(Func2<B, A, B> f, B init) {
      return github.com.beenotung.javalib.Utils.foldl(this, f, init);
    }

    default <B> B fold(Func2<B, A, B> f, B init) {
      return github.com.beenotung.javalib.Utils.fold(this, f, init);
    }

    default A[] toTypedArray() {
      return castArray(toArray(), getComponentType());
    }

    /**
     * @throws ClassCastException if this stream is not ? extends Number
     */
    default Optional<Double> average() {
      return github.com.beenotung.javalib.Utils.average(this.as());
    }

    /* cast the type */
    default <B> Stream<B> as() {
      //return this.map(x -> (B) x);
      return (Stream<B>) this;
    }
  }

  @Deprecated
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

  @Deprecated
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
      return github.com.beenotung.javalib.Utils.toArray(stream(), getComponentType());
    }

    default RichStream<A> richStream() {
      return github.com.beenotung.javalib.Utils.richStream(stream(), getComponentType());
    }

    default RichList<A> newInstance() {
      return newInstance(getComponentType());
    }

    default RichList<A> newInstance(A a) {
      return newInstance(a, getComponentType());
    }

    default <A> RichList<A> newInstance(A a, Class<A> aClass) {
      A[] as = (A[]) Array.newInstance(aClass, 1);
      as[0] = a;
      return newInstance(as, aClass);
    }

    default <A> RichList<A> newInstance(Class<A> aClass) {
      return newInstance((A[]) Array.newInstance(aClass, 0), aClass);
    }

    default RichList<A> newInstance(RichList<A> as) {
      return newInstance(as, getComponentType());
    }

    default <A> RichList<A> newInstance(RichList<A> as, Class<A> aClass) {
      return newInstance(as.list(), aClass);
    }

    default RichList<A> newInstance(Collection<A> as) {
      return newInstance(as, getComponentType());
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

    default RichList<A> newInstance(Stream<A> as) {
      return newInstance(as, getComponentType());
    }

    default <A> RichList<A> newInstance(Stream<A> as, Class<A> aClass) {
      ArrayList<A> bs = as.collect(Collectors.toCollection(ArrayList::new));
      return newInstance(bs, aClass);
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
      A[][] ass = github.com.beenotung.javalib.Utils.empty(n_group, i -> sizes[i], aClass);
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
      RichList<A>[] res = github.com.beenotung.javalib.Utils.empty(n_group, getClass());
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
      final Integer[] sizes = github.com.beenotung.javalib.Utils.fill(n_group, size);
      foreach(n - size * n_group, i -> sizes[i]++);
      A[][] ass = github.com.beenotung.javalib.Utils.empty(n_group, i -> sizes[i], aClass);
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
      RichList<A>[] res = github.com.beenotung.javalib.Utils.empty(n_group, getClass());
      foreach(n_group, i -> res[i] = newInstance(ass[i], aClass));
      return newInstance(res, (Class<RichList<A>>) getClass());
    }
  }

  public static <A> List<A> list(Stream<A> stream) {
    ArrayList<A> res = new ArrayList<A>();
    stream.forEachOrdered(a -> res.add(a));
    return res;
  }

  public static <A> RichList<A> richList(List<A> list, Class<A> aClass) {
    return new FL(new ArrayList(list), aClass);
  }

  public static <A> RichList<A> richList(Stream<A> stream, Class<A> aClass) {
    ArrayList<A> as = new ArrayList<A>();
    stream.forEachOrdered(a -> as.add(a));
    return richList(as, aClass);
  }

  /* temp name, will replace FList after finish composing */
  public static class FL<A> implements RichList<A> {
    private final ArrayList<A> values;
    private Class<A> componentType = (Class<A>) Object.class;

    protected FL() {
      this.values = new ArrayList<A>();
    }

    public FL(ArrayList<A> values, Class<A> componentType) {
      this.values = values;
      this.componentType = componentType;
    }

    @Override
    public Class<A> getComponentType() {
      return componentType;
    }

    @Override
    public List<A> list() {
      return values;
    }

    @Override
    public void setUnderneath(A[] as, Class<A> aClass) {
      this.setUnderneath(Arrays.asList(as), aClass);
    }

    @Override
    public void setUnderneath(Collection<A> collection, Class<A> aClass) {
      values.clear();
      values.addAll(collection);
      componentType = aClass;
    }
  }

  public static Stream<Integer> mkIntStream(int n) {
    return mkIntStream(0, n);
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
      return newInstance(as, github.com.beenotung.javalib.Utils.getComponentType(as));
    }

    <A> CommonList<A> newInstance(Collection<A> as, Class<A> aClass);

    default <A> CommonList<A> newInstance(CommonList<A> as) {
      return newInstance(as, (Class<A>) getComponentType());
    }

    <A> CommonList<A> newInstance(CommonList<A> as, Class<A> aClass);

    default <A> CommonList<A> fill(int n, A a) {
      return newInstance(github.com.beenotung.javalib.Utils.fill(n, a));
    }

    default <A> CommonList<A> tabulate(int n, Func1<Integer, A> f) {
      return newInstance(github.com.beenotung.javalib.Utils.tabulate(n, f));
    }

    default CommonList<A> filter(Func1<A, Boolean> f) {
      A[] as = mkIntStream(size())
        .map(this::get)
        .filter(a -> f.apply(a))
        .toArray(github.com.beenotung.javalib.Utils.genArrayFunc(getComponentType()));
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
      return github.com.beenotung.javalib.Utils.foldl(stream(), f, init);
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
      Integer sizes[] = github.com.beenotung.javalib.Utils.fill(n_group, size);
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
      Integer[] sizes = github.com.beenotung.javalib.Utils.fill(n_group, size);
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
  public static <A, B> B[] map(A as[], Func1<A, B> f) {
    final int n = as.length;
    Object os[] = (Object[]) Array.newInstance(Object.class, n);
    map(as, f, (B[]) os);
    B b = firstNonNull((B[]) os).get();
    return b == null ? (B[]) os : castArray(os, (Class<B>) b.getClass());
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
}
