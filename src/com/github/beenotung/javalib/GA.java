package com.github.beenotung.javalib;

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

    default boolean customMutate() {
      return false;
    }

    default A mutate(A a) {
      throw new Error("not impl");
    }

    default byte[] mutate(byte[] gene) {
//      byte[] res = bytes(mutate(data(gene)));
//      System.arraycopy(
//        res, 0,
//        gene, 0,
//        res.length);
      return bytes(mutate(data(gene)));
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
    public Integer[] index; // rank -> index (of parallel arrays)
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
    boolean[] breed_marks;

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
      final int sort_direction = profile.isMinimizing() ? 1 : -1;

      if (n_pop == status.n_pop && l_gene == status.l_gene)
        return;
      if (n_pop == 0) {
        /* create from blank */
        status.genes = new byte[status.n_pop][status.l_gene]; // init later
        status.fitnesses = new double[status.n_pop];
        status.index = new Integer[status.n_pop]; // init later
        crossover_marks = new boolean[status.n_pop];
        breed_marks = new boolean[status.n_pop];
        randoms = new ThreadLocalRandom[status.n_pop]; // init later
        par_foreach(status.n_pop, i_pop -> {
          (randoms[i_pop] = ThreadLocalRandom.current())
            .nextBytes(status.genes[i_pop]);
          eval(i_pop);
          status.index[i_pop] = i_pop;
        });
        Arrays.parallelSort(status.index, (a, b) -> sort_direction * Double.compare(status.fitnesses[a], status.fitnesses[b]));
        final int crossover_threshold = (int) Math.ceil(status.n_pop * status.p_crossover);
        par_foreach(status.n_pop, i -> {
          /* mark Top N */
          crossover_marks[i] = status.index[i] <= crossover_threshold;
          breed_marks[i] = status.index[i] > crossover_threshold;
        });
      } else if (n_pop > status.n_pop) {
        /* 'release' extra, (to reduce memory usage) */
        // TODO check gene size
        status.genes = Arrays.copyOf(status.genes, status.n_pop);
        status.fitnesses = Arrays.copyOf(status.fitnesses, status.n_pop);
        status.index = Arrays.copyOf(status.index, status.n_pop);
      } else /* n_pop < status.n_pop */ {
        /* create extra */
        byte[][] genes = new byte[status.n_pop][status.l_gene];
        foreach(n_pop, i_pop -> {
          // TODO check gene size
          genes[i_pop] = status.genes[i_pop];
        });
        // TODO init new gene
      }
      n_pop = status.n_pop;
      l_gene = status.l_gene;
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
      final int sort_direction = profile.isMinimizing() ? 1 : -1;
      final boolean custom_mutate = profile.customMutate();

      /** 1. crossover + mutation
       *     1.1 matching (a non-Top N match with any one that better than itself)
       *     1.2 crossover (in-place crossover)
       *     1.3 mutation
       * */
      final int crossover_threshold = round_up_to_even((int) (status.p_crossover * n_pop));
//      boolean changed = false;
//      while (or(crossover_marks)) {
//        /* 1.1 matching */
//        final int p1 = randoms[0].nextInt(n_pop);
//        final int p2 = randoms[0].nextInt(n_pop);
//        final int c1 = first_true(crossover_marks);
//        if (c1 == -1)
//          break;
//        crossover_marks[c1] = false;
//        final int c2 = first_true(crossover_marks);
//        if (c2 == -1)
//          break;
//        crossover_marks[c2] = false;
//        if ((p1 != p2) && breed_marks[p1] && breed_marks[p2] && crossover_marks[c1] && crossover_marks[c2]) {
//          crossover_marks[c1] = false;
//          crossover_marks[c2] = false;
//          /* 1.2 crossover */
//          par_foreach(l_gene, i_gene -> {
//            int i_random = i_gene % n_pop;
//            if (randoms[i_random].nextBoolean()) {
//              status.genes[c1][i_gene] = status.genes[p1][i_gene];
//              status.genes[c2][i_gene] = status.genes[p2][i_gene];
//            } else {
//              status.genes[c1][i_gene] = status.genes[p2][i_gene];
//              status.genes[c2][i_gene] = status.genes[p1][i_gene];
//            }
//          });
//        }
//      }
      /* 1.1 matching */
      par_foreach(n_pop, i_pop -> {
        if (breed_marks[i_pop])
          return;
        int[] p1 = new int[1];
        int[] p2 = new int[1];
        do {
          p1[0] = randoms[i_pop].nextInt(n_pop);
          p2[0] = randoms[i_pop].nextInt(n_pop);
        } while (!(breed_marks[p1[0]] && breed_marks[p2[0]]));
//        println("p1,p2", p1, p2);
         /* 1.2 crossover */
        par_foreach(l_gene, i_gene -> {
          int i_random = i_gene % n_pop;
          if (randoms[i_random].nextBoolean()) {
            status.genes[i_pop][i_gene] = status.genes[p1[0]][i_gene];
          } else {
            status.genes[i_pop][i_gene] = status.genes[p2[0]][i_gene];
          }
        });
      });
      /* 1.3 mutation */
      par_foreach(n_pop - 1, i_pop -> {
        /* mutation happens only if crossover happened */
        if (breed_marks[i_pop])
          return;
//        println("check mutate");
        if (randoms[i_pop].nextDouble() <= status.p_mutation) {
//          println("do mutate");
          if (custom_mutate)
//            profile.mutate(status.genes[i_pop]);
            status.genes[i_pop] = profile.mutate(status.genes[i_pop]);
          else
            foreach(l_gene, i_gene -> {
              if (randoms[i_pop].nextInt() <= status.a_mutation)
                status.genes[i_pop][i_gene] += randoms[i_pop].nextInt();
            });
        }
      });

      /* 2. */
      /* calc fitness */
      par_foreach(n_pop, this::eval);
      /* sort by fitness */
      Arrays.parallelSort(status.index, (a, b) -> sort_direction * Double.compare(status.fitnesses[a], status.fitnesses[b]));
      par_foreach(n_pop, i -> {
        /* mark Top N */
        crossover_marks[i] = status.index[i] <= crossover_threshold;
        breed_marks[i] = status.index[i] > crossover_threshold;
      });

      /* 3. update static */
      // TODO add statics (e.g. diversity)
    }

    public void runUntil(Function<GeneRuntime, Boolean> f) {
      while (f.apply(this))
        next();
    }

    public GeneRuntimeStatus viewStatus() {
      return status;
    }

    public void updateStatus(Consumer<GeneRuntimeStatus> f) {
      f.accept(status);
      onStatusChanged();
    }

    /**@param rank start from zero*/
    public A getDataByRank(int rank) {
      return profile.data(status.genes[status.index[rank]]);
    }

    /**@param rank start from zero*/
    public double getFitnessByRank(int rank) {
      return status.fitnesses[status.index[rank]];
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
