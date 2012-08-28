package org.flatland.drip;
import java.lang.reflect.Method;
import java.io.*;

public class Main {
  public static void main(String[] args) throws Exception {
    String class_name = args[0];
    String fifo_dir   = args[1];

    reopenStreams(fifo_dir);
    Method main = mainMethod(class_name);

    Method init       = mainMethod(System.getenv("DRIP_INIT_CLASS"));
    String init_args  = System.getenv("DRIP_INIT");
    if (init_args != null) mainInvoke(init == null ? main : init, init_args);

    mainInvoke(main, readline());
    System.exit(0);
  }

  private static Method mainMethod(String class_name)
    throws ClassNotFoundException, NoSuchMethodException {
    if (class_name != null) {
      return Class.forName(class_name).getMethod("main", String[].class);
    } else {
      return null;
    }
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
