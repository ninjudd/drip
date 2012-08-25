package org.flatland.decaf;
import java.lang.reflect.Method;
import java.io.*;

public class Main {
  public static void main(String[] args) throws Exception {
    String class_name = args[0];
    String fifo_dir   = args[1];

    reopenStreams(fifo_dir);
    Method main = Class.forName(class_name).getMethod("main", String[].class);

    String decaf_init = System.getenv("DECAF_INIT");
    if (decaf_init != null) mainInvoke(main, decaf_init);

    mainInvoke(main, readline());
    System.exit(0);
  }

  public static void mainInvoke(Method main, String argstring) throws Exception {
    String [] args = argstring.trim().split("\t");
    if (args.length == 1 && args[0].equals("")) {
      args = null;
    }
    main.invoke(null, (Object) args);
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
