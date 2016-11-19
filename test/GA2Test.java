import com.github.beenotung.javalib.GA;
import com.github.beenotung.javalib.GA2;

import java.applet.Applet;

import static com.github.beenotung.javalib.Utils.foldl;
import static com.github.beenotung.javalib.Utils.isVisible;
import static com.github.beenotung.javalib.Utils.println;

/**
 * Created by beenotung on 11/16/16.
 */
public class GA2Test {
  public static void main(String[] args) {
    final char[] target = "https://github.com/beenotung/javalib.git".toCharArray();
    println("start");
    GA2 ga = new GA2(new GA2.GARuntime(100, target.length, 0.25f, 0.012f, 0.2f),
      gene -> {
        float acc = 0f;
        for (int i = 0; i < target.length; i++) {
          acc += Math.abs(target[i] - gene[i]);
        }
        return acc;
      });
    ga.init();
    ga.next();
  }
}
