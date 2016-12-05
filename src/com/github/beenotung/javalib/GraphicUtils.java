package com.github.beenotung.javalib;

import java.awt.*;

/**
 * Created by beenotung on 12/5/16.
 */
public class GraphicUtils {
  public static class Screen {
    public static final boolean IS_HEADLESS;
    public static final int HEIGHT;
    public static final int WIDTH;
    public static final double RATIO;

    static {
      IS_HEADLESS = GraphicsEnvironment.isHeadless();
      if (IS_HEADLESS) {
        WIDTH = 0;
        HEIGHT = 0;
        RATIO = 0.5d;
      } else {
        Rectangle bound = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        WIDTH = (int) bound.getWidth();
        HEIGHT = (int) bound.getHeight();
        RATIO = 1d * WIDTH / HEIGHT;
      }
    }
  }
}
