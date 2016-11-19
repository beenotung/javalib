import com.github.beenotung.javalib.GA;

import java.util.concurrent.ThreadLocalRandom;

import static com.github.beenotung.javalib.Utils.*;

/**
 * Created by beenotung on 11/16/16.
 */
public class GATest {
  static String c(byte[] xs) {
    StringBuffer buf = new StringBuffer();
    for (byte x : xs) {
      if (isVisible((char) x))
        buf.append((char) x);
      else
        buf.append(' ');
    }
    return buf.toString();
  }

  public static void main(String[] args) {
    final String target_s = "https://github.com/beenotung/javalib.git";
    final char[] target = target_s.toCharArray();
    println("start");
    GA.Param param = new GA.Param() {
      @Override
      public GA.IEval I_EVAL() {
        return (gene) -> {
          float acc = 0f;
          for (int i = 0; i < target.length; i++) {
            acc += Math.abs(target[i] - gene[i]);
//            acc += Math.abs(target[i] - gene[i]) / 2 + 1;
//            if (target[i] != gene[i])
//              acc += Math.abs(target[i] - gene[i]) + 10;
          }
          return acc;
        };
      }

      @Override
      public GA.IRandomGene I_RANDOM_GENE() {
        return new GA.IRandomGene() {
          @Override
          public void randomGene(byte[] gene, int offset, int length) {
            mkStream(offset, length).forEach(i -> gene[i] = (byte) randomVisibleChar());
          }
        };
      }

      @Override
      public GA.IMutation I_MUTATION() {
        return new GA.IMutation() {
          @Override
          public void mutation(GA.GARuntime gaRuntime, byte[] gene, ThreadLocalRandom r) {
            foreach(gene.length, i -> {
              if (r.nextFloat() <= gaRuntime.a_mutation) {
                gene[i] += r.nextInt(3) - 1;
              }
            });
          }
        };
      }
    };
    final int n_pop = 100;
    GA ga = new GA(new GA.GARuntime(n_pop, target.length, 0.25f, 0.9f, 1f / target.length * 4), param);
    ga.init();
    final float[] best = {1f};
    int turn = 0;
    for (; best[0] != 0; turn++) {
      ga.next();
      try {
        int finalTurn = turn;
        ga.useRuntime(gaRuntime -> {
          best[0] = gaRuntime.getFitnessByRank(0);
//            println(target_s);
          println(
            finalTurn,
            c(gaRuntime.getGeneByRank(0)),
            best[0],
            "\t",
            c(gaRuntime.getGeneByRank(n_pop - 1)),
            gaRuntime.getFitnessByRank(n_pop - 1)
          );
        });
        Thread.sleep(15);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    int finalTurn1 = turn;
    ga.useRuntime(r -> {
      println("final:", c(r.getGeneByRank(0)));
      println("turn used:", finalTurn1);
      println("population:", r.n_pop);
      println("crossover policy: top", 100 * r.p_crossover + "%");
      println("mutation probability:", 100 * r.p_mutation + "%");
      println("mutation amount:", 100 * r.a_mutation + "%");
    });
  }
}
