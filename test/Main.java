import java.util.ArrayList;

import static com.github.beenotung.javalib.Utils.*;

public class Main {
  public static void main(String[] args) throws Throwable {
//    println("test parallel array sort");
//    final int n = 10;
//    int[] values = new int[n];
//    Integer[] index = new Integer[n];
//    par_foreach(n, i -> index[i] = i);
//    par_foreach(n, i -> values[i] = random.nextInt(100));
//    Arrays.sort(index, (a, b) -> Integer.compare(values[a], values[b]));
//    println("values", values);
//    println("index", index);
//    print("ordered:");
//    foreach(n, i -> print(" " + values[index[i]]));
//    println();
//    if (random.nextDouble() < 1)
//      throw new Error("term");
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
    println("testing", "end");
  }
}
