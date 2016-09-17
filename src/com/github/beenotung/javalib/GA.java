package com.github.beenotung.javalib;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
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

    double eval(A data);

    default double eval(byte[] bytes) {
      return eval(data(bytes));
    }

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
    // 'alias' to status.n_pop, sync from status (single-direction)
    private int n_pop = 0;
    // 'alias' to status.l_gene, sync from status (single-direction)
    private int l_gene;
    public GeneProfile<A> profile;
    ThreadLocalRandom[] randoms; // for concurrency access (it is using AtomicLong internal)
    boolean[] crossover_marks;

    public GeneRuntime(GeneProfile<A> profile) {
      this.profile = profile;
    }

    public void init(GeneRuntimeStatus initStatus) {
      this.status = initStatus;
      onStatusChanged();
      reset();
    }

    /**
     * update arrays in status
     * local (this.) n_pop and l_gene are old value
     *     - used to check against status.n_pop and status.l_gene
     *     - update local copy at the end of this method
     * */
    void onStatusChanged() {
      if (n_pop == status.n_pop && l_gene == status.l_gene)
        return;
      if (n_pop == 0) {
        /* create from blank */
        status.genes = new byte[status.n_pop][status.l_gene]; // init later
        status.fitnesses = new double[status.n_pop];
        status.index = new Integer[status.n_pop]; // init later
        status.reverseIndex = new int[status.n_pop]; // init later
        crossover_marks = new boolean[status.n_pop];
        randoms = new ThreadLocalRandom[status.n_pop]; // init later
        par_foreach(status.n_pop, i_pop -> {
          (randoms[i_pop] = ThreadLocalRandom.current())
            .nextBytes(status.genes[i_pop]);
          eval(i_pop);
        });
        Arrays.parallelSort(status.index, (a, b) -> Double.compare(status.fitnesses[a], status.fitnesses[b]));
        final int crossover_threshold = (int) Math.ceil(status.n_pop * status.p_crossover);
        par_foreach(status.n_pop, i -> {
          /* mark Top N */
          crossover_marks[i] = status.index[i] < crossover_threshold;
          /* update reverse index (for parallel arrays) */
          status.reverseIndex[status.index[i]] = i;
        });
      } else if (n_pop > status.n_pop) {
        /* 'release' extra, (to reduce memory usage) */
        // TODO check gene size
        status.genes = Arrays.copyOf(status.genes, status.n_pop);
        status.fitnesses = Arrays.copyOf(status.fitnesses, status.n_pop);
        status.index = Arrays.copyOf(status.index, status.n_pop);
        status.reverseIndex = Arrays.copyOf(status.reverseIndex, n_pop);
      } else /* n_pop < status.n_pop */ {
        /* create extra */
        byte[][] genes = new byte[status.n_pop][status.l_gene];
        foreach(n_pop, i_pop -> {
          // TODO check gene size
          genes[i_pop] = status.genes[i_pop];
        });
        // TODO init new gene
      }
    }

    void eval(int i_pop) {
      status.fitnesses[i_pop] = profile.eval(status.genes[i_pop]);
    }

    public void reset() {
      Objects.requireNonNull(status);
      // TODO init gene code
    }

    /**
     * 1. crossover + mutation
     * 2. calc fitness
     * 3. update statics
     * */
    public void next() {
      final int n_pop = status.n_pop;

      /** 1. crossover + mutation
       *     1.1 matching (a non-Top N match with any one that better than itself)
       *     1.2 crossover (in-place crossover)
       *     1.3 mutation
       * */
      final int crossover_threshold = (int) (status.p_crossover * n_pop);
      /* 1.1 matching */
      par_foreach(n_pop, bad_pop -> {
//        println("par_foreach", "bad_pop", bad_pop);
        if (crossover_marks[bad_pop]) {
          final int[] good_pop = new int[1];
          do {
            good_pop[0] = randoms[bad_pop].nextInt(n_pop);
          } while (status.index[bad_pop] > status.index[good_pop[0]]);
//          println("good", good_pop, "bad", bad_pop);
          /* 1.2 crossover */
          foreach(status.l_gene, i_gene -> {
            if (randoms[bad_pop].nextBoolean()) {
//              println("copy on", i_gene);
              status.genes[bad_pop][i_gene] = status.genes[good_pop[0]][i_gene];
            }
          });
        }
      });
      /* 1.3 mutation */
      par_foreach(n_pop,i_pop->{
        // TODO mutation
//        status.genes[i_pop].
      });

      /* 2. */
      /* calc fitness */
      par_foreach(n_pop, i_pop -> eval(i_pop));
      /* sort by fitness */
      Arrays.parallelSort(status.index, (a, b) -> Double.compare(status.fitnesses[a], status.fitnesses[b]));
      par_foreach(n_pop, i -> {
        /* mark Top N */
        crossover_marks[i] = status.index[i] < crossover_threshold;
        /* update reverse index (for parallel arrays) */
        status.reverseIndex[status.index[i]] = i;
      });

      /* 3. update static */
      // TODO add statics (e.g. diversity)
    }

    public void runUntil(Function<GeneRuntime, Boolean> f) {
      while (f.apply(this))
        next();
    }

    GeneRuntimeStatus viewStatus() {
      return status;
    }

    void updateStatus(Consumer<GeneRuntimeStatus> f) {
      f.accept(status);
      onStatusChanged();
    }
  }

  public static <A> GeneRuntime<A> create(GeneProfile<A> geneProfile) {
    return new GeneRuntime<A>(geneProfile);
  }

  public static <A> GeneRuntime<A> create(GeneProfile<A> geneProfile, GeneRuntimeStatus initStatus) {
    GeneRuntime<A> res = new GeneRuntime<A>(geneProfile);
    res.init(initStatus);
    return res;
  }
}
