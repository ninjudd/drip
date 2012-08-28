package org.flatland.drip;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
  static private Scanner s;

  public static void main(String[] args) throws Exception {
    String class_name = args[0];
    String fifo_dir   = args[1];

    reopenStreams(fifo_dir);
    Method main = mainMethod(class_name);

    Method init       = mainMethod(System.getenv("DRIP_INIT_CLASS"));
    String init_args  = System.getenv("DRIP_INIT");
    if (init_args != null) mainInvoke(init == null ? main : init, init_args);

    String main_args    = readline();
    String runtime_args = readline();
    setProperties(runtime_args);

    mainInvoke(main, main_args);
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

  private static void mainInvoke(Method main, String main_args) throws Exception {
    Scanner s = new Scanner(main_args);
    s.useDelimiter("\t");
    LinkedList<String> args = new LinkedList<String>();
    while (s.hasNext()) {
      args.add(s.next());
    }
    main.invoke(null, (Object)args.toArray(new String[0]));
  }

  private static void setProperties(String runtime_args) {
    Matcher m = Pattern.compile("-D([^=]+)=([^\\t]+)").matcher(runtime_args);

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
    System.setIn(new BufferedInputStream(new DelayedFileInputStream(fifo_dir + "/in")));
    System.setOut(new PrintStream(new DelayedFileOutputStream(fifo_dir + "/out")));
    System.setErr(new PrintStream(new DelayedFileOutputStream(fifo_dir + "/err")));
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
