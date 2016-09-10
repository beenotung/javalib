package github.com.beenotung.javalib;

import java.util.LinkedList;
import java.util.function.Function;

/**
 * @deprecated
 */
public class Functional {
  public interface IFunc<A, B> {
    B apply(A a) throws Exception;
  }

  public static <A, B> IFunc<A, B> func(Function<A, B> f) {
    return new IFunc<A, B>() {
      @Override
      public B apply(A a) {
        return f.apply(a);
      }
    };
  }

  public interface IApply0 {
    void apply();
  }

  public interface IApply1<A> {
    void apply(A a);
  }

  //  public interface IMM<M extends IMonad> extends IFunc<Object, M> {
//  }
  public interface IMM<M extends IMonad<A>, A> {
    M unit(A a);
  }

  public interface IMonad<A> {
    <B> IMM<IMonad<B>, B> unapply();

    <B> IMonad<B> map(IFunc<A, B> f) throws Exception;

    /* alias of flatmap */
    <B> IMonad<B> bind(IFunc<A, ? extends IMonad<B>> f) throws Exception;

    /* alias of apply */
    void ap(IApply1<A> f) throws Exception;
  }

  public interface IMMonad<M extends IMonad<?>> {
    IMonad<?> join();
  }

  public static <A> A id(A a) {
    return a;
  }

  public static <A> IMonad<A> unit(A a) {
    return new IMonad<A>() {
      @Override
      public <B> IMM<IMonad<B>, B> unapply() {
        return a -> unit(a);
      }

      @Override
      public <B> IMonad<B> map(IFunc<A, B> f) throws Exception {
        return this.bind(a -> (IMonad<B>) this.unapply().unit(f.apply(a)));
      }

      @Override
      public <B> IMonad<B> bind(IFunc<A, ? extends IMonad<B>> f) throws Exception {
        return f.apply(a);
      }

      @Override
      public void ap(IApply1<A> f) {
        f.apply(a);
      }
    };
  }

  public interface IMaybe<A> extends IMonad<A> {
    void branch(IApply1<A> some, IApply0 none);
  }

  public static <A> IMaybe<A> maybe(A a) {
    IMonad<A> m = unit(a);
    return new IMaybe<A>() {
      @Override
      public void branch(IApply1<A> some, IApply0 none) {
        if (a == null)
          none.apply();
        else
          some.apply(a);
      }

      @Override
      public <B> IMM<IMonad<B>, B> unapply() {
        return a -> maybe(a);
      }

      @Override
      public <B> IMonad<B> map(IFunc<A, B> f) throws Exception {
        return a == null ? (IMonad<B>) m : m.map(f);
      }

      @Override
      public <B> IMonad<B> bind(IFunc<A, ? extends IMonad<B>> f) throws Exception {
        return f.apply(a);
      }

      @Override
      public void ap(IApply1<A> f) {
        f.apply(a);
      }
    };
  }

  public static <A, B, C> IFunc<A, C> compo(
    IFunc<B, C> f,
    IFunc<A, B> g
  ) {
    return a -> f.apply(g.apply(a));
  }

  public interface IFunctor<A, B> {
    <C> IFunctor<A, C> fmap(IFunc<B, C> f);
  }

  public static <A, B> IFunctor<A, B> functor(IFunc<A, B> f) {
    return new IFunctor<A, B>() {
      @Override
      public <C> IFunctor<A, C> fmap(IFunc<B, C> g) {
        return functor(compo(g, f));
      }
    };
  }


  public interface IList<A> extends IMonad<A> {
    A head() throws Exception;

    IList<A> tail() throws Exception;

    IList<A> concat(IList<A> xs);

    IList<A> prepend(A a);

    long size();

    <B> B foldr(IFunc<Pair<A, B>, B> f, B acc) throws Exception;

    <B> B foldl(IFunc<Pair<B, A>, B> f, B acc) throws Exception;

    LinkedList<A> toJList() throws Exception;

    IList<A> reverse() throws Exception;

    IList<A> filter(IFunc<A, Boolean> f) throws Exception;

    IList<A> take(Long n) throws Exception;

    IList<A> drop(Long n) throws Exception;

    /* f: (acc,current)=>result */
    A reduce(IFunc<Pair<A, A>, A> f) throws Exception;

    @Override
    String toString();

    @Override
    <B> IList<B> map(IFunc<A, B> f) throws Exception;

    @Override
    <B> IList<B> bind(IFunc<A, ? extends IMonad<B>> f) throws Exception;
  }

  public interface Pair<A, B> {
    A a();

    B b();
  }

  public static <A, B> Pair<A, B> pair(A a, B b) {
    return new Pair<A, B>() {
      @Override
      public A a() {
        return a;
      }

      @Override
      public B b() {
        return b;
      }
    };
  }

  public static final IList Nil = list();

  public static <A> IList<A> list() {
    return Nil == null ? new IList<A>() {

      @Override
      public A head() throws Exception {
        throw new Exception("empty list");
      }

      @Override
      public IList<A> tail() throws Exception {
        throw new Exception("empty list");
      }

      @Override
      public IList<A> concat(IList<A> xs) {
        return xs;
      }

      @Override
      public IList<A> prepend(A a) {
        return list(a, this);
      }

      @Override
      public long size() {
        return 0;
      }


      @Override
      public <B> IMM<IMonad<B>, B> unapply() {
        return a -> list(a);
      }

      @Override
      public <B> IList<B> map(IFunc<A, B> f) {
        return Nil;
      }

      @Override
      public <B> IList<B> bind(IFunc<A, ? extends IMonad<B>> f) {
        return Nil;
      }

      @Override
      public LinkedList<A> toJList() {
        return new LinkedList<A>();
      }

      @Override
      public IList<A> reverse() {
        return Nil;
      }

      @Override
      public IList<A> filter(IFunc<A, Boolean> f) {
        return Nil;
      }

      @Override
      public IList<A> take(Long n) {
        return Nil;
      }

      @Override
      public IList<A> drop(Long n) {
        return Nil;
      }

      @Override
      public A reduce(IFunc<Pair<A, A>, A> f) throws Exception {
        throw new Exception("empty list");
      }

      @Override
      public <B> B foldr(IFunc<Pair<A, B>, B> f, B acc) {
        return acc;
      }

      @Override
      public <B> B foldl(IFunc<Pair<B, A>, B> f, B acc) {
        return acc;
      }

      @Override
      public void ap(IApply1<A> f) {
      }

      @Override
      public String toString() {
        return "[]";
      }
    } : Nil;
  }

  public static <A> IList<A> list(final A head) {
    return list(head, Nil);
  }

  public static <A> IList<A> list(final A head, final IList<A> tail) {
    return new IList<A>() {
      @Override
      public A head() {
        return head;
      }

      @Override
      public IList<A> tail() {
        return tail;
      }

      @Override
      public IList<A> concat(IList<A> xs) {
        return list(head, tail.concat(xs));
      }

      @Override
      public IList<A> prepend(A a) {
        return list(a, this);
      }

      @Override
      public long size() {
        return 1 + tail.size();
      }

      @Override
      public <B> IList<B> map(IFunc<A, B> f) throws Exception {
        return this.bind(a -> (IList<B>) this.unapply().unit(f.apply(a)));
      }

      @Override
      public <B> IList<B> bind(IFunc<A, ? extends IMonad<B>> f) throws Exception {
        IList<B> h = (IList<B>) f.apply(head);
        IList<B> t = tail.bind(f);
        return h.concat(t);
      }

      @Override
      public LinkedList<A> toJList() throws Exception {
        return this.foldr(new IFunc<Pair<A, LinkedList<A>>, LinkedList<A>>() {
          @Override
          public LinkedList<A> apply(Pair<A, LinkedList<A>> pair) {
            pair.b().add(pair.a());
            return pair.b();
          }
        }, new LinkedList<A>());
      }

      @Override
      public IList<A> reverse() throws Exception {
        return foldl(new IFunc<Pair<IList<A>, A>, IList<A>>() {
          @Override
          public IList<A> apply(Pair<IList<A>, A> p) {
            return p.a().prepend(p.b());
          }
        }, Nil);
      }

      @Override
      public IList<A> filter(IFunc<A, Boolean> f) throws Exception {
        return foldl(new IFunc<Pair<IList<A>, A>, IList<A>>() {
          @Override
          public IList<A> apply(Pair<IList<A>, A> pair) throws Exception {
            if (f.apply(pair.b()))
              return pair.a().prepend(pair.b());
            else
              return pair.a();
          }
        }, Nil).reverse();
      }

      @Override
      public IList<A> take(Long n) throws Exception {
        Pair<IList<A>, Long> res = foldl(new IFunc<Pair<Pair<IList<A>, Long>, A>, Pair<IList<A>, Long>>() {
          @Override
          public Pair<IList<A>, Long> apply(Pair<Pair<IList<A>, Long>, A> p) throws Exception {
            if (p.a().b() <= 0)
              throw early_terminate;
            return pair(p.a().a().prepend(p.b()), p.a().b() - 1);
          }
        }, pair(Nil, n));
        return res.a().reverse();
      }

      @Override
      public IList<A> drop(Long n) throws Exception {
        Pair<IList<A>, Long> res = foldl(new IFunc<Pair<Pair<IList<A>, Long>, A>, Pair<IList<A>, Long>>() {
          @Override
          public Pair<IList<A>, Long> apply(Pair<Pair<IList<A>, Long>, A> p) throws Exception {
            if (p.a().b() <= 0)
              throw early_terminate;
            return pair(p.a().a().tail(), p.a().b() - 1);
          }
        }, pair(this, n));
        return res.a();
      }

      @Override
      public A reduce(IFunc<Pair<A, A>, A> f) throws Exception {
        return tail.foldl(new IFunc<Pair<A, A>, A>() {
          @Override
          public A apply(Pair<A, A> p) throws Exception {
            return f.apply(pair(p.a(), p.b()));
          }
        }, head);
      }

      @Override
      public <B> B foldr(IFunc<Pair<A, B>, B> f, B acc) throws Exception {
        return f.apply(pair(head, tail.foldr(f, acc)));
      }

      @Override
      public <B> B foldl(IFunc<Pair<B, A>, B> f, B acc) throws Exception {
        IList<A> t = tail.prepend(head).prepend(null);
        try {
          for (; ; ) {
            t = t.tail();
            if (t.equals(Nil))
              break;
            acc = f.apply(pair(acc, t.head()));
          }
        } catch (Exception e) {
          if (!e.equals(early_terminate))
            throw e;
        }
        return acc;
      }

      @Override
      public <B> IMM<IMonad<B>, B> unapply() {
        return a -> list(a);
      }

      @Override
      public void ap(IApply1<A> f) throws Exception {
        throw new Exception("unsupported");
      }

      @Override
      public String toString() {
        String s = null;
        try {
          s = foldl(new IFunc<Pair<String, A>, String>() {
            @Override
            public String apply(Pair<String, A> pair) {
              return pair.a() + ", " + pair.b();
            }
          }, "");
        } catch (Exception e) {
          e.printStackTrace();
        }
        return "[" + s.substring(2) + "]";
      }
    };
  }

  public static Exception early_terminate = new Exception("early_terminate");

  public static <A> IList<A> fromArray(A[] xs) {
    IList<A> res = list();
    for (int i = xs.length - 1; i >= 0; i--) {
      res = res.prepend(xs[i]);
    }
    return res;
  }

  public static <A> IList<A> createList(IFunc<Long, A> f, long size) throws Exception {
    IList<A> res = list();
    for (long i = size - 1; i >= 0; i--) {
      res = res.prepend(f.apply(i));
    }
    return res;
  }
}
