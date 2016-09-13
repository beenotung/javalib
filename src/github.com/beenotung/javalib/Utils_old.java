package beenotung.javalib;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//
public class Utils_old {

  /* TODO clean up up to here */

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
