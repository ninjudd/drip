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
  private Scanner s;
  private List<Switchable> lazyStreams;
  private String className;
  private String fifoDir;

  public Main(String className, String fifoDir) {
    this.className = className;
    this.fifoDir = fifoDir;
  }

  public void start() throws Exception {
    reopenStreams(fifoDir);
    Method main = mainMethod(className);

    Method init = mainMethod(System.getenv("DRIP_INIT_CLASS"));
    String initArgs = System.getenv("DRIP_INIT");
    if (initArgs != null) {
      invoke(init == null ? main : init, splitArgs(initArgs, "\n"));
    }

    for (Switchable o : lazyStreams) {
      o.flip();
    }

    String mainArgs = readLine();
    String runtimeArgs = readLine();

    invoke(main, splitArgs(mainArgs, "\u0000"));
  }

  public static void main(String[] args) throws Exception {
    new Main(args[0], args[1]).start();
  }

  private Method mainMethod(String className)
    throws ClassNotFoundException, NoSuchMethodException {
    if (className != null) {
      return Class.forName(className).getMethod("main", String[].class);
    } else {
      return null;
    }
  }

  private String[] splitArgs(String args, String delim) {
    Scanner s = new Scanner(args);
    s.useDelimiter(delim);

    LinkedList<String> arglist = new LinkedList<String>();
    while (s.hasNext()) {
      arglist.add(s.next());
    }
    return arglist.toArray(new String[0]);
  }

  private void invoke(Method main, String[] args) throws Exception {
    main.invoke(null, (Object)args);
  }

  private void setProperties(String runtimeArgs) {
    Matcher m = Pattern.compile("-D([^=]+)=([^\u0000]+)").matcher(runtimeArgs);

    while (m.find()) {
      System.setProperty(m.group(1), m.group(2));
    }
  }

  private void setEnv(Map<String, String> newEnv) throws NoSuchFieldException, IllegalAccessException {
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

  private void reopenStreams(String fifo_dir) throws FileNotFoundException, IOException {
    SwitchableFileInputStream stdin = new SwitchableFileInputStream(System.in, fifo_dir + "/in");
    SwitchableFileOutputStream stdout = new SwitchableFileOutputStream(System.out, fifo_dir + "/out");
    SwitchableFileOutputStream stderr = new SwitchableFileOutputStream(System.err, fifo_dir + "/err");
    lazyStreams = Arrays.<Switchable>asList(stdin, stdout, stderr);

    System.setIn(new BufferedInputStream(stdin));
    System.setOut(new PrintStream(stdout));
    System.setErr(new PrintStream(stderr));
    s = new Scanner(System.in);
  }

  private String readLine() throws IOException {
    if (s.hasNextLine()) {
      return s.nextLine();
    } else {
      return "";
    }
  }
}
