package com.github.beenotung.javalib;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.github.beenotung.javalib.Utils.*;

/**
 * Created by beenotung on 11/18/16.
 * totally in-place parallel array
 */
public class GA2 {
  public static class GARuntime {
    public int n_pop;
    public int l_gene; // number of byte for each individual
    public float p_crossover; // threshed of top p percentage
    public float p_mutation; // possibility for individual to mutate
    public float a_mutation; // possibility for genome to mutate (when p_mutation satisfy)
    public byte[][] genes;
    public float[] fitnesses;
    public Integer[] index; // rank -> index (of parallel arrays)
    public boolean isMinimizing = true;

    public GARuntime(int n_pop, int l_gene, float p_crossover, float p_mutation, float a_mutation) {
      this.n_pop = n_pop;
      this.l_gene = l_gene;
      this.p_crossover = p_crossover;
      this.p_mutation = p_mutation;
      this.a_mutation = a_mutation;
    }

    public byte[] getGeneByRank(int rank) {
      return genes[index[rank]];
    }

    public float getFitnessByRank(int rank) {
      return fitnesses[index[rank]];
    }
  }

  public interface IRandomGene {
    default void randomGene(byte[] gene, int offset, int length) {
      length = Math.min(length, gene.length - offset);
      ThreadLocalRandom r = ThreadLocalRandom.current();
      if (length == gene.length)
        r.nextBytes(gene);
      else if (length > 0)
        mkStream(offset, length).forEach(i -> gene[i] = (byte) r.nextInt());
    }
  }

  public interface IMatching {
    /**
     * @return 3x array : flatten array of two parents and a child e.g. [p1,p2,child, p1,p2,child, p1,p2,child]
     * */
    default void match(GARuntime gaRuntime, AtomicReference<int[]> matchesRef) {
      final int margin = (int) (gaRuntime.n_pop * gaRuntime.p_crossover) + 1;
      final int n_child = gaRuntime.n_pop - margin - 1;
      int[] matches;
      if (matchesRef.get().length == n_child * 3) {
        matches = matchesRef.get();
      } else {
        matches = new int[n_child * 3];
        matchesRef.set(matches);
      }
      final ThreadLocalRandom r = ThreadLocalRandom.current();
      foreach(n_child, i -> {
        final int offset = i * 3;
        matches[offset] = r.nextInt(margin);
        matches[offset + 1] = r.nextInt(margin);
        matches[offset + 2] = r.nextInt(margin, gaRuntime.n_pop);
      });
    }
  }

  public interface ICrossover {
    default void crossover(byte[] p1, byte[] p2, byte[] child) {
      foreach(child.length, i -> {
        if (ThreadLocalRandom.current().nextBoolean()) {
          child[i] = p1[i];
        } else {
          child[i] = p2[i];
        }
      });
    }
  }

  public interface IMutation {
    void mutation(GARuntime gaRuntime, byte[] gene, ThreadLocalRandom r);
  }

  public interface IEval {
    float eval(byte[] gene);
  }

  public static $MODULE $MODULE = new $MODULE();

  private static class $MODULE {
    public IMutation DefaultIMutation() {
      return (gaRuntime, gene, r) -> foreach(gene.length, i -> {
        if (r.nextFloat() < gaRuntime.a_mutation) {
          gene[i] ^= 255;
        }
      });
    }
  }

  private final GARuntime gaRuntime;
  public IRandomGene iRandomGene;
  public IMatching iMatching;
  public ICrossover iCrossover;
  public IMutation iMutation;
  public IEval iEval;
  private final AtomicReference<int[]> matchesRef; /* avoid making garbage */

  private void randomGene(byte[] gene) {
    iRandomGene.randomGene(gene, 0, gene.length);
  }

  private void randomGene(byte[] gene, int offset) {
    iRandomGene.randomGene(gene, offset, gene.length - offset);
  }

  void sort() {
    synchronized (gaRuntime) {
      final int sort_direction = gaRuntime.isMinimizing ? 1 : -1;
      Arrays.parallelSort(gaRuntime.index, (a, b) -> sort_direction * Double.compare(gaRuntime.fitnesses[a], gaRuntime.fitnesses[b]));
    }
  }

  public void init() {
    synchronized (gaRuntime) {
      gaRuntime.genes = new byte[gaRuntime.n_pop][gaRuntime.l_gene];
      par_foreach(gaRuntime.n_pop, i -> {
        gaRuntime.genes[i] = new byte[gaRuntime.l_gene];
        randomGene(gaRuntime.genes[i]);
      });
      gaRuntime.fitnesses = new float[gaRuntime.n_pop];
      gaRuntime.index = tabulate(gaRuntime.n_pop, Utils::id, Integer.class);
      par_foreach(gaRuntime.n_pop, i -> gaRuntime.fitnesses[i] = iEval.eval(gaRuntime.genes[i]));
      sort();
    }
  }


  /**
   * 1. matching
   * 2. crossover
   * 3. mutation
   * 4. sort
   * */
  public void next() {
    synchronized (gaRuntime) {
      /* 1. matching  */
      iMatching.match(gaRuntime, matchesRef);
      final int[] matches = matchesRef.get();
      /* 2. crossover */
      par_foreach(matches.length / 3, i -> {
        final int offset = i * 3;
        iCrossover.crossover(gaRuntime.genes[gaRuntime.index[matches[offset]]], gaRuntime.genes[gaRuntime.index[matches[offset + 1]]], gaRuntime.genes[gaRuntime.index[matches[offset + 2]]]);
//        ThreadLocalRandom r = ThreadLocalRandom.current();
//        if (r.nextFloat() <= gaRuntime.p_mutation) {
//          iMutation.mutation(gaRuntime.genes[gaRuntime.index[matches[offset + 2]]], r);
//        }
      });
      /* 3. mutation  */
      par_foreach(gaRuntime.n_pop, i -> {
        final ThreadLocalRandom r = ThreadLocalRandom.current();
        if (r.nextFloat() <= gaRuntime.p_mutation) {
          iMutation.mutation(gaRuntime, gaRuntime.genes[i], r);
        }
      });
      /* 4. sort      */
      par_foreach(gaRuntime.n_pop, i -> gaRuntime.fitnesses[i] = iEval.eval(gaRuntime.genes[i]));
      sort();
    }
  }

  public void update_n_pop(int n) {
    synchronized (gaRuntime) {
      final int min_n = Math.min(gaRuntime.n_pop, n);
      {
        byte[][] new_genes = new byte[n][gaRuntime.l_gene];
        System.arraycopy(gaRuntime.genes, 0, new_genes, 0, min_n);
        gaRuntime.genes = new_genes;
      }
      {
        float[] new_finesses = new float[n];
        System.arraycopy(gaRuntime.fitnesses, 0, new_finesses, 0, min_n);
        gaRuntime.fitnesses = new_finesses;
      }
      gaRuntime.index = tabulate(n, Utils::id, Integer.class);

      mkStream(gaRuntime.n_pop, n - gaRuntime.n_pop).parallel().forEach(i -> {
        gaRuntime.genes[i] = new byte[gaRuntime.l_gene];
        randomGene(gaRuntime.genes[i]);
        gaRuntime.fitnesses[i] = iEval.eval(gaRuntime.genes[i]);
      });
      sort();

      gaRuntime.n_pop = n;
    }
  }

  public void update_l_gene(int l) {
    synchronized (gaRuntime) {
      final int min_l = Math.min(gaRuntime.l_gene, l);
      par_foreach(gaRuntime.n_pop, i -> {
        {
          byte[] new_gene = new byte[l];
          System.arraycopy(gaRuntime.genes[i], 0, new_gene, 0, min_l);
          gaRuntime.genes[i] = new_gene;
        }
        randomGene(gaRuntime.genes[i], min_l);
        gaRuntime.fitnesses[i] = iEval.eval(gaRuntime.genes[i]);
      });
      sort();
      gaRuntime.l_gene = l;
    }
  }

  public interface Param {
    default IRandomGene I_RANDOM_GENE() {
      return new IRandomGene() {
      };
    }

    default ICrossover I_CROSSOVER() {
      return new ICrossover() {
      };
    }

    default IMatching I_MATCHING() {
      return new IMatching() {
      };
    }

    default IMutation I_MUTATION() {
      return null;
    }

    IEval I_EVAL();
  }

  public GA2(GARuntime gaRuntime, IEval iEval) {
    this(gaRuntime, () -> iEval);
  }

  public GA2(GARuntime gaRuntime, Param param) {
    this.gaRuntime = gaRuntime;
    this.iRandomGene = param.I_RANDOM_GENE();
    this.iCrossover = param.I_CROSSOVER();
    this.iMatching = param.I_MATCHING();
    this.iMutation = param.I_MUTATION() == null ? $MODULE.DefaultIMutation() : param.I_MUTATION();
    this.iEval = param.I_EVAL();
    this.matchesRef = new AtomicReference<>(new int[gaRuntime.n_pop]);
  }

  public void useRuntime(Consumer<GARuntime> f) {
    synchronized (gaRuntime) {
      f.accept(gaRuntime);
    }
  }
}
