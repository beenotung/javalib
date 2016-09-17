package com.github.beenotung.javalib;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static com.github.beenotung.javalib.Utils.*;

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
    Integer[] index; // for sorting
    int[] reverseIndex;
  }

  public static class GeneRuntime<A> {
    private GeneRuntimeStatus status;
    public GeneProfile<A> profile;
    ThreadLocalRandom[] randoms; // for concurrency access (it is using AtomicLong internal)
    boolean[] crossover_marks;

    public GeneRuntime(GeneProfile<A> profile) {
      this.profile = profile;
    }

    public void init(GeneRuntimeStatus initStatus) {
      final int n_pop = initStatus.n_pop;
      this.status = initStatus;
      status.genes = new byte[n_pop][status.l_gene];
      status.fitnesses = new double[n_pop];
      status.index = (Integer[]) tabulate(n_pop, Utils::id);
      status.reverseIndex = new int[n_pop];
      crossover_marks = new boolean[n_pop];
      randoms = new ThreadLocalRandom[n_pop];
      par_foreach(n_pop, i_pop -> {
        (randoms[i_pop] = ThreadLocalRandom.current())
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
      final int n_pop = status.n_pop;
      /** 1. crossover + mutation
       *     1. mark gens to cross
       * */

      /* 1. */
      /*   1.1.  sort by fitness
      *    1.2.  mark top N
      *    1.3   matching (in-place crossover (kill-parent)
      * */

      /* 1.1 */
      final int crossover_threshold = (int) (status.p_crossover * n_pop);
      Arrays.parallelSort(status.index, (a, b) -> Double.compare(status.fitnesses[a], status.fitnesses[b]));
      /* 1.2 */
      par_foreach(n_pop, i -> {
        crossover_marks[i] = status.index[i] < crossover_threshold;
        status.reverseIndex[status.index[i]] = i;
      });
      /* 1.3 */
      boolean allDone = false;
      par_foreach(n_pop, bad_pop -> {
        if (crossover_marks[bad_pop]) {
          int good_pop;
          do {
            good_pop = randoms[bad_pop].nextInt(n_pop);
          } while (crossover_marks[good_pop]);
        }
      });
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
