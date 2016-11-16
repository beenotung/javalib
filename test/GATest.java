import com.github.beenotung.javalib.GA;
import com.github.beenotung.javalib.Utils;

import java.util.function.Function;

import static com.github.beenotung.javalib.Utils.isVisible;
import static com.github.beenotung.javalib.Utils.println;

/**
 * Created by beenotung on 11/16/16.
 */
public class GATest {
  public static void main(String[] args) {
    final String target = "https://github.com/beenotung/javalib.git";
    println("start");
    GA.GeneProfile<String> profile = new GA.GeneProfile<String>() {
      @Override
      public boolean isMinimizing() {
        return true;
      }

      @Override
      public byte[] bytes(String data) {
        byte[] res = new byte[data.length()];
        int i = 0;
        for (char c : data.toCharArray()) {
          res[i++] = (byte) c;
        }
        return res;
      }

      @Override
      public String data(byte[] bytes) {
        String acc = "";
        for (byte c : bytes) {
          if (isVisible((char) c)) {
            acc += (char) c;
          } else {
            acc += ' ';
          }
        }
        return acc;
      }

      @Override
      public double eval(String data) {
        int acc = 0;
        for (int i = 0; i < data.length(); i++) {
          acc += Math.abs(data.charAt(i) - target.charAt(i));
        }
        return acc;
      }
    };
    GA.GeneRuntime<String> ga = GA.create(profile);
    GA.GeneRuntimeStatus initStatus = new GA.GeneRuntimeStatus();
    initStatus.n_pop = 100;
    initStatus.l_gene = target.length();
    initStatus.p_crossover = 0.25;
    initStatus.p_mutation = 0.0125;
    initStatus.a_mutation = 0.02;
    ga.init(initStatus);
    ga.runUntil(runtime -> {
      println(
        runtime.getFitnessByRank(0),
        runtime.getDataByRank(0)
      );
      return runtime.getFitnessByRank(0) == 0;
    });
    println("end");
  }
}
