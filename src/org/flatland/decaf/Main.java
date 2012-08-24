package org.flatland.decaf;
import java.lang.reflect.Method;

public class Main {
  public static void main(String[] init_args) throws Exception {
    String class_name = readline();
    String [] args    = readline().split("\t");

    Method main = Class.forName(class_name).getMethod("main", String[].class);
    main.invoke(null, (Object) args);
    System.exit(0);
  }

  public static String readline() throws java.io.IOException {
    StringBuffer s = new StringBuffer();

    while (true) {
      char c = (char) System.in.read();
      if (c == '\n' || c == -1) break;
      s.append(c);
    }
    return s.toString();
  }
}
