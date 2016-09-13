import java.util.ArrayList;

import static github.com.beenotung.javalib.Utils.*;

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
    println("upper", cs.stream().map(Character::toUpperCase));
    println("no e", cs.stream().filter(c -> !c.equals('e')));
    println("full", cs);
    println("groups", group(cs, 4));
    println("even-groups", evenGroup(cs, 4));
    println();
    println("test stream utils");
    println("avg:", mkStream(1000).average().getAsDouble());
    println();
    println("testing", "println");
  }
}
