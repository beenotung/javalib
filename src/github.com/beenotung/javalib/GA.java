package github.com.beenotung.javalib;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

import static github.com.beenotung.javalib.Utils.*;

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
  public interface GeneProfile<A> {
    byte[] bytes(A data);

    A data(byte[] bytes);

    double eval(byte[] bytes);

    default boolean isMinimizing() {
      return true;
    }
  }

  /**
   * <h1>logical view :</h1>
   * <ul>
   *     <li>Individual : { gene:byte[], fitness:double }</li>
   *     <li>Population : Individual</li>
   * </ul>
   * <p>
   * <h1>physical view : parallel array</h1>
   * <ul>
   *     <li>genes : Array of gene for individual-i</li>
   *     <li>fitnesses : Array of fitness for the individual-i</li>
   * </ul>
   * <p>
   * n_pop, l_gene, p_mutation, a_mutation can be changed in different turn
   */
  public static class GeneRuntimeStatus {
    public int n_pop;
    public int l_gene; // number of byte for each individual
    public double p_crossover;
    public double p_mutation; // possibility for individual to mutate
    public double a_mutation; // possibility for genome to mutate (when p_mutation satisfy)
    public byte[][] genes;
    public double[] fitnesses;
  }

  public static class GeneRuntime<A> {
    private GeneRuntimeStatus status;
    public GeneProfile<A> profile;
    Random[] randoms; // for concurrency access (it is using AtomicLong internal)
    boolean[] crossover_marks;

    public GeneRuntime(GeneProfile<A> profile) {
      this.profile = profile;
    }

    public void init(GeneRuntimeStatus initStatus) {
      this.status = initStatus;
      status.genes = new byte[status.n_pop][status.l_gene];
      status.fitnesses = new double[status.n_pop];
      crossover_marks = new boolean[status.n_pop];
      randoms = new Random[status.n_pop];
      par_foreach(status.n_pop, i_pop -> {
        (randoms[i_pop] = new Random())
          .nextBytes(status.genes[i_pop]);
        eval(i_pop);
      });
    }

    void eval(int i_pop) {
      status.fitnesses[i_pop] = profile.eval(status.genes[i_pop]);
    }

    public void reset() {
      this.init(Objects.requireNonNull(status));
    }

    /**
     * 1. crossover + mutation
     * 2. calc fitness
     * 3. update statics
     * */
    public void next() {
      /** 1. crossover + mutation
       *     1. mark gens to cross
       * */

      /* 1. */
      /*   1.1. */
      int sum = 0;
      for (double fitness : status.fitnesses) {
        sum += fitness;
      }

      for (int i = 0; i < status.fitnesses.length; i++) {
//        crossover_marks[i] =
      }
//      status.genes

    }

    public void runUntil(Function<GeneRuntime, Boolean> f) {
      while (f.apply(this))
        next();
    }
  }

  public static <A> GeneRuntime<A> create(GeneProfile<A> geneProfile, GeneRuntimeStatus initStatus) {
    return new GeneRuntime<A>(geneProfile);
  }
}
