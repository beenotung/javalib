package github.com.beenotung.javalib;

import java.io.PrintStream;
import java.util.Scanner;

public class Utils {
  public static void print(Object msg) {
    System.out.print(msg);
  }

  public static void println() {
    System.out.println();
  }

  public static void println(Object msg) {
    System.out.println(msg);
  }

  public static final Scanner in = new Scanner(System.in);
  public static final PrintStream out = System.out;
}
