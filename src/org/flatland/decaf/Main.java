package org.flatland.decaf;
import java.lang.reflect.Method;
import java.io.*;

public class Main {
  public static void main(String[] init_args) throws Exception {
    String class_name = init_args[0];
    String fifo_dir = init_args[1];

    reopenStreams(fifo_dir);
    Method main = Class.forName(class_name).getMethod("main", String[].class);

    String [] args = readline().substring(1).split("\t");

    if (args.length == 1 && args[0].equals("")) {
      args = null;
    }

    main.invoke(null, (Object) args);
    System.exit(0);
  }

  public static void reopenStreams(String fifo_dir) throws FileNotFoundException, IOException {
    System.in.close();
    System.out.close();
    System.err.close();
    System.setIn(new BufferedInputStream(new DelayedFileInputStream(fifo_dir + "/in")));
    System.setOut(new PrintStream(new DelayedFileOutputStream(fifo_dir + "/out")));
    System.setErr(new PrintStream(new DelayedFileOutputStream(fifo_dir + "/err")));
  }

  public static String readline() throws IOException {
    StringBuffer s = new StringBuffer();

    while (true) {
      char c = (char) System.in.read();
      if (c == '\n' || c == -1) break;
      s.append(c);
    }
    return s.toString();
  }
}
