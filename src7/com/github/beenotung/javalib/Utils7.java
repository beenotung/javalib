package com.github.beenotung.javalib;

import com.sun.istack.internal.Nullable;

public class Utils7 {
  public interface Function<A, B> extends Applicative<B> {
    B apply(A a);
  }


  public interface Supplier<A> {
    A apply();
  }

  public interface Applicative<A> {
    A apply();

    <B> Applicative<B> composite(Function<A, B> f);
  }

  public static class Lazy<A> {
    private final Supplier<A> f;
    private A a = null;
    private boolean done = false;

    public Lazy(Supplier<A> f) {
      this.f = f;
    }

    public A get() {
      if (!done) {
        done = true;
        a = f.apply();
      }
      return a;
    }
  }

  public static <A> Lazy<A> lazy(final A a) {
    return new Lazy<>(new Supplier<A>() {
      @Override
      public A apply() {
        return a;
      }
    });
  }

  public interface Functor<A> extends Applicative<A> {
    <B> Functor<B> fmap(Function<A, B> f);
  }

  public static <A> Functor<A> functor(final Lazy<A> a) {
    return new Functor<A>() {
      @Override
      public A apply() {
        return a.get();
      }

      @Override
      public <B> Applicative<B> composite(Function<A, B> f) {
        return functor(lazy(f.apply(a.get())));
      }

      @Override
      public <B> Functor<B> fmap(Function<A, B> f) {
        return functor(lazy(f.apply(a.get())));
      }
    };
  }

  public static <A> Applicative<A> pure(final A a) {
    return new Applicative<A>() {
      @Override
      public A apply() {
        return a;
      }

      @Override
      public <B> Applicative<B> composite(Function<A, B> f) {
        return pure(f.apply(a));
      }
    };
  }

  public interface Maybe<A> extends Functor<A> {
  }

  public static <A> Maybe<A> some(final A a) {
    return new Maybe<A>() {
      @Override
      public <B> Functor<B> fmap(Function<A, B> f) {
        return some(f.apply(a));
      }

      @Override
      public A apply() {
        return a;
      }

      @Override
      public <B> Applicative<B> composite(Function<A, B> f) {
        return some(f.apply(a));
      }
    };
  }

  public static <A> Maybe<A> none() {
    return new Maybe<A>() {
      @Override
      public <B> Functor<B> fmap(Function<A, B> f) {
        return none();
      }

      @Override
      public A apply() {
        return null;
      }

      @Override
      public <B> Applicative<B> composite(Function<A, B> f) {
        return none();
      }
    };
  }

  public static <A> Maybe<A> fromNullable(@Nullable A a) {
    return a == null
      ? (Maybe<A>) Utils7.none()
      : some(a);
  }
}
