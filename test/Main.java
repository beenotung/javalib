import com.github.beenotung.javalib.GA;
import com.github.beenotung.javalib.GA.GeneProfile;
import com.github.beenotung.javalib.GA.GeneRuntimeStatus;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;

import static com.github.beenotung.javalib.Utils.*;

public class Main {
  public static void main(String[] args) throws Throwable {
    println("testing list utils");
    ArrayList<Integer> as = list(tabulate(10, i -> i, Integer.class));
    as.add(10);
    as.add(11);
    ArrayList<Integer> bs = list(as.stream().map(a -> a * 10));
    println("as", as);
    println("bs", bs);
    println();
    ArrayList<Character> cs = list("this is a long text");
    println("size", cs.size());
    println("upper", string(cs.stream().map(Character::toUpperCase)));
    println("no e", string(cs.stream().filter(c -> !c.equals('e'))));
    println("full", cs);
    println("groups", group(cs, 4, Character.class));
    println("even-groups", evenGroup(cs, 4));
    println();
    println("test stream utils");
    println("avg:", mkStream(1000).average().getAsDouble());
    println();
    println("testing", "GA");
    GeneRuntimeStatus initStatus = new GeneRuntimeStatus();
    initStatus.n_pop = 1000;
    initStatus.l_gene = Double.BYTES;
    initStatus.p_crossover = 0.25;
    initStatus.p_mutation = 0.001;
    initStatus.a_mutation = 0.0005;
    final double[] x_max = {Double.MIN_VALUE};
    final double[] x_min = {Double.MAX_VALUE};
    GA.GeneRuntime<Double> ga = GA.create(new GeneProfile<Double>() {
      @Override
      public byte[] bytes(Double data) {
        byte[] res = new byte[Double.BYTES];
        ByteBuffer.wrap(res).putDouble(data);
        return res;
      }

      @Override
      public Double data(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
      }

      @Override
      public double eval(Double x) {
        // ~ -12
        if (x > x_max[0])
          x_max[0] = x;
        else if (x < x_min[0])
          x_min[0] = x;
//        println(x);
        return -0.25 * x * x - 6 * x + 2;
      }

      @Override
      public boolean isMinimizing() {
        return false;
      }
    }, initStatus);
    foreach(10000, i -> {
      ga.next();
      GeneRuntimeStatus status = ga.viewStatus();
      byte[] best = status.genes[status.reverseIndex[0]];
      double x = ga.profile.data(best);
      double y = ga.profile.eval(x);
      println(i + " : best gene", "x", x, "\ty", y);
    });
    GeneRuntimeStatus status = ga.viewStatus();
    byte[] best = status.genes[status.reverseIndex[0]];
    double x = ga.profile.data(best);
    double y = ga.profile.eval(x);
    println("final best gene", "x", x, "\ty", y);
    println("x_max", x_max, "x_min", x_min);
    println("real solution", "x", -12d, "y", ga.profile.eval(-12d));
    println();
    println("testing", "end");
  }
}
