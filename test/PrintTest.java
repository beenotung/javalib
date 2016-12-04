import com.github.beenotung.javalib.Utils;

import static com.github.beenotung.javalib.Utils.println;
import static com.github.beenotung.javalib.Utils.tabulate;

/**
 * Created by beenotung on 12/4/16.
 */
public class PrintTest {
  public static void main(String[] args) {
    println("long array", tabulate(100, i -> (char) (i % 26 + 'a'), Character.class));
    println("long array", tabulate(100, i -> (int) (i % 26 + 'a'), Integer.class));
  }
}
