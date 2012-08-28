package org.flatland.drip;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
  static private Scanner s;
  static private List<Openable> lazyStreams;

  public static void main(String[] args) throws Exception {
    String class_name = args[0];
    String fifo_dir   = args[1];

    reopenStreams(fifo_dir);
    Method main = mainMethod(class_name);

    Method init      = mainMethod(System.getenv("DRIP_INIT_CLASS"));
    String init_args = System.getenv("DRIP_INIT");
    if (init_args != null) {
      invoke(init == null ? main : init, splitArgs(init_args, "\n"));
    }

    String main_args    = readline();
    String runtime_args = readline();
    setProperties(runtime_args);

    for (Openable o : lazyStreams) {
      o.forceOpen();
    }

    invoke(main, splitArgs(main_args, "\u0000"));
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

  private static String[] splitArgs(String args, String delim) {
    Scanner s = new Scanner(args);
    s.useDelimiter(delim);

    LinkedList<String> arglist = new LinkedList<String>();
    while (s.hasNext()) {
      arglist.add(s.next());
    }
    return arglist.toArray(new String[0]);
  }

  private static void invoke(Method main, String[] args) throws Exception {
    main.invoke(null, (Object)args);
  }

  private static void setProperties(String runtime_args) {
    Matcher m = Pattern.compile("-D([^=]+)=([^\u0000]+)").matcher(runtime_args);

    while (m.find()) {
      System.setProperty(m.group(1), m.group(2));
    }
  }

  private static void setEnv(Map<String, String> newEnv) throws NoSuchFieldException, IllegalAccessException {
    Map<String, String> env = System.getenv();
    Class<?> classToHack = env.getClass();
    if (!(classToHack.getName().equals("java.util.Collections$UnmodifiableMap"))) {
      throw new RuntimeException("Don't know how to hack " + classToHack);
    }

    Field field = classToHack.getDeclaredField("m");
    field.setAccessible(true);
    field.set(env, newEnv);
    field.setAccessible(false);
  }

  private static void reopenStreams(String fifo_dir) throws FileNotFoundException, IOException {
    System.in.close();
    System.out.close();
    System.err.close();

    DelayedFileInputStream stdin = new DelayedFileInputStream(fifo_dir + "/in");
    DelayedFileOutputStream stdout = new DelayedFileOutputStream(fifo_dir + "/out");
    DelayedFileOutputStream stderr = new DelayedFileOutputStream(fifo_dir + "/err");
    lazyStreams = Arrays.<Openable>asList(stdin, stdout, stderr);

    System.setIn(new BufferedInputStream(stdin));
    System.setOut(new PrintStream(stdout));
    System.setErr(new PrintStream(stderr));
    s = new Scanner(System.in);
  }

  public static String readline() throws IOException {
    if (s.hasNextLine()) {
      return s.nextLine();
    } else {
      return "";
    }
  }
}
