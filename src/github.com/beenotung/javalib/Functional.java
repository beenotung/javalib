package github.com.beenotung.javalib;

import java.util.LinkedList;
import java.util.function.Function;

import static github.com.beenotung.javalib.Utils.println;

public class Functional {
  public interface IFunc<A, B> {
    B apply(A a);
  }

  public static <A, B> IFunc<A, B> func(Function<A, B> f) {
    return new IFunc<A, B>() {
      @Override
      public B apply(A a) {
        return f.apply(a);
      }
    };
  }

  public interface IApply<A> {
    void apply(A a);
  }

  //  public interface IMM<M extends IMonad> extends IFunc<Object, M> {
//  }
  public interface IMM<M extends IMonad<A>, A> {
    M unit(A a);
  }

  public interface IMonad<A> {
    <B> IMM<IMonad<B>, B> unapply();

    <B> IMonad<B> map(IFunc<A, B> f);

    /* alias of flatmap */
    <B> IMonad<B> bind(IFunc<A, ? extends IMonad<B>> f);

    /* alias of apply */
    void ap(IApply<A> f);
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
      public <B> IMonad<B> map(IFunc<A, B> f) {
        return this.bind(a -> (IMonad<B>) this.unapply().unit(f.apply(a)));
      }

      @Override
      public <B> IMonad<B> bind(IFunc<A, ? extends IMonad<B>> f) {
        return f.apply(a);
      }

      @Override
      public void ap(IApply<A> f) {
        f.apply(a);
      }
    };
  }

  public interface IMaybe<A> extends IMonad<A> {
  }

  public static <A> IMaybe<A> maybe(A a) {
    IMonad<A> m = unit(a);
    return new IMaybe<A>() {
      @Override
      public <B> IMM<IMonad<B>, B> unapply() {
        return a -> maybe(a);
      }

      @Override
      public <B> IMonad<B> map(IFunc<A, B> f) {
        return a == null ? (IMonad<B>) m : m.map(f);
      }

      @Override
      public <B> IMonad<B> bind(IFunc<A, ? extends IMonad<B>> f) {
        return f.apply(a);
      }

      @Override
      public void ap(IApply<A> f) {
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
    A head();

    IList<A> tail();

    IList<A> concat(IList<A> xs);

    IList<A> prepend(A a);

    long size();

    <B> B foldr(IFunc<Pair<A, B>, B> f, B acc);

    <B> B foldl(IFunc<Pair<B, A>, B> f, B acc);

    LinkedList<A> toJList();

    IList<A> reverse();

    @Override
    String toString();

    @Override
    <B> IList<B> map(IFunc<A, B> f);

    @Override
    <B> IList<B> bind(IFunc<A, ? extends IMonad<B>> f);
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
      public A head() {
        throw new Error("empty list");
      }

      @Override
      public IList<A> tail() {
        throw new Error("empty list");
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
      public <B> B foldr(IFunc<Pair<A, B>, B> f, B acc) {
        return acc;
      }

      @Override
      public <B> B foldl(IFunc<Pair<B, A>, B> f, B acc) {
        return acc;
      }

      @Override
      public void ap(IApply<A> f) {
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
      public <B> IList<B> map(IFunc<A, B> f) {
        return this.bind(a -> (IList<B>) this.unapply().unit(f.apply(a)));
      }

      @Override
      public <B> IList<B> bind(IFunc<A, ? extends IMonad<B>> f) {
        IList<B> h = (IList<B>) f.apply(head);
        IList<B> t = tail.bind(f);
        return h.concat(t);
      }

      @Override
      public LinkedList<A> toJList() {
        return this.foldr(new IFunc<Pair<A, LinkedList<A>>, LinkedList<A>>() {
          @Override
          public LinkedList<A> apply(Pair<A, LinkedList<A>> pair) {
            pair.b().add(pair.a());
            return pair.b();
          }
        }, new LinkedList<A>());
      }

      @Override
      public IList<A> reverse() {
        return foldr(new IFunc<Pair<A, IList<A>>, IList<A>>() {
          @Override
          public IList<A> apply(Pair<A, IList<A>> pair) {
            return pair.b().prepend(pair.a());
          }
        }, Nil);
      }

      @Override
      public <B> B foldr(IFunc<Pair<A, B>, B> f, B acc) {
        return f.apply(pair(head, tail.foldr(f, acc)));
      }

      @Override
      public <B> B foldl(IFunc<Pair<B, A>, B> f, B acc) {
        IList<A> t = tail.prepend(head).prepend(null);
        for (; ; ) {
          t = t.tail();
          if (t.equals(Nil))
            break;
          acc = f.apply(pair(acc, t.head()));
        }
        return acc;
      }

      @Override
      public <B> IMM<IMonad<B>, B> unapply() {
        return a -> list(a);
      }

      @Override
      public void ap(IApply<A> f) {
        throw new Error("unsupported");
      }

      @Override
      public String toString() {
        String s = foldl(new IFunc<Pair<String, A>, String>() {
          @Override
          public String apply(Pair<String, A> pair) {
            return pair.a() + ", " + pair.b();
          }
        }, "");
        return "[" + s.substring(2) + "]";
      }
    };
  }

  public static <A> IList<A> fromArray(A[] xs) {
    IList<A> res = list();
    for (int i = xs.length - 1; i >= 0; i--) {
      res = res.prepend(xs[i]);
    }
    return res;
  }

  public static <A> IList<A> createList(IFunc<Long, A> f, long size) {
    IList<A> res = list();
    for (long i = size - 1; i >= 0; i--) {
      res = res.prepend(f.apply(i));
    }
    return res;
  }
}
