import static com.github.beenotung.javalib.Utils.array_equal;
import static com.github.beenotung.javalib.Utils.println;

/**
 * Created by beenotung on 12/4/16.
 */
public class ArrayTest {
  public static void main(String[] args) {
    int[] a1 = {1, 2, 3, 4, 5};
    int[] a2 = {1, 2, 3, 4, 5};
    println("true?", array_equal(a1, 0, a2, 0, 5));

    int[] a3 = {1, 2, 3, 4, 1};
    int[] a4 = {1, 2, 3, 4, 5};
    println("false?", array_equal(a3, 0, a4, 0, 5));

    int[] a5 = {1, 2, 3, 4, 1};
    int[] a6 = {99, 1, 2, 3, 4, 5};
    println("true?", array_equal(a5, 0, a6, 1, 4));
  }
}
