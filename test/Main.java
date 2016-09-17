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
    initStatus.n_pop = 10;
    initStatus.l_gene = Double.BYTES;
    initStatus.p_crossover = 0.25;
    initStatus.p_mutation = 0.01;
    initStatus.a_mutation = 0.001;
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
        return 3 * x * x - 4 * x + 2;
      }

      @Override
      public boolean isMinimizing() {
        return true;
      }
    }, initStatus);
    foreach(10,i->{
      ga.next();
      println("best gene",ga.profile.data(ga.));
    });
    println();
    println("testing", "end");
  }
}
