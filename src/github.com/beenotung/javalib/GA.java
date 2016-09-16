package github.com.beenotung.javalib;

import java.util.function.Function;

/**
 * Instead of having package of ga, they are compacted into a public class statically
 * <p>
 * When the user application has to be done in a single file,
 * just append this file into the user application Main class file
 * (after remove the *public* modifier of this class and the *package* statement)
 * <p>
 * The population can growth
 * Read about *Bloat*
 * http://dces.essex.ac.uk/staff/poli/gp-field-guide/113Bloat.html
 * http://variable-variability.blogspot.com/2011/04/idea-to-combat-bloat-in-genetic.html
 */
public class GA {
  public interface Gene<A> {
  }

  public interface GeneParam<A, G extends Gene<A>> {
    byte[] bytes(G data);

    G gene(byte[] bytes);

    double eval(byte[] bytes);

    default boolean isMinimizing() {
      return true;
    }
  }

  public static class GeneRuntimeStatus {
    public long n_pop;
  }

  public static class GeneRuntime {
    public final GeneRuntimeStatus status;

    public GeneRuntime(GeneRuntimeStatus status) {
      this.status = status;
    }

    /* init or reset */
    public void init() {
    }

    public void next() {
    }

    public void runUntil(Function<GeneRuntime, Boolean> f) {
      while (f.apply(this))
        next();
    }
  }

  public static void init(GeneParam geneParam, GeneRuntimeStatus initStatus) {
  }
}
