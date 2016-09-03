package github.com.beenotung.javalib;

import java.util.function.Function;

public class Functional {
  public interface IFunc<A, B> {
    B apply(A a);
  }

  public interface IApply<A> {
    void apply(A a);
  }

  public interface IMonad<A> {
    IMM constructor();

    <B> IMonad<B> map(IFunc<A, B> f);

    /* alias of flatmap */
    <B> IMonad<B> bind(IFunc<A, IMonad<B>> f);

    /* alias of apply */
    void ap(IApply<A> f);
  }

  public interface IMM<M extends IMonad> {
    <A> M unit(A a);
  }

  public static final Monad monad = new Monad();

  public static class Monad implements IMM {
    @Override
    public IMonad unit(Object a) {
      return new IMonad() {
        @Override
        public IMonad map(IFunc f) {
          return this.bind(a -> this.constructor().unit(f.apply(a)));
        }

        @Override
        public IMonad bind(IFunc f) {
          return (IMonad) f.apply(a);
        }

        @Override
        public IMM constructor() {
          return monad;
        }

        @Override
        public void ap(IApply f) {
          f.apply(a);
        }
      };
    }
  }

  public static Maybe maybe = new Maybe();

  public static class Maybe extends Monad {
    @Override
    public IMonad unit(Object a) {
      final IMonad m = monad.unit(a);
      return new IMonad() {
        @Override
        public IMM constructor() {
          return maybe;
        }

        @Override
        public IMonad map(IFunc f) {
          return a == null ? m : m.map(f);
        }

        @Override
        public IMonad bind(IFunc f) {
          return a == null ? m : m.bind(f);
        }

        @Override
        public void ap(IApply f) {
          m.ap(f);
        }
      };
    }
  }
  public static class List<T> extends Monad{
    @Override
    public IMonad unit(Object a) {
      return new IMonad() {
        @Override
        public IMM constructor() {
          return null;
        }

        @Override
        public IMonad map(IFunc f) {
          return null;
        }

        @Override
        public IMonad bind(IFunc f) {
          return null;
        }

        @Override
        public void ap(IApply f) {

        }
      };
    }
  }
}
