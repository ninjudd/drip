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
  private File fifoDir;

  public Main(String className, String fifoDir) {
    this.className = className.replace('/', '.');
    this.fifoDir = new File(fifoDir);
  }

  private void killAfterTimeout() {
    String idleTimeStr = System.getenv("DRIP_SHUTDOWN"); // in minutes
    int idleTime;
    if (idleTimeStr == null) {
      idleTime = 4 * 60; // four hours
    } else {
      idleTime = Integer.parseInt(idleTimeStr);
    }

    try {
      Thread.sleep(idleTime * 60 * 1000); // convert minutes to ms
    } catch (InterruptedException e) {
      System.err.println("drip: Interrutped??");
      return; // I guess someone wanted to kill the timeout thread?
    }

    File lockDir = new File(fifoDir, "lock");
    if (lockDir.mkdir()) {
      System.exit(0);
    } else {
      // someone is already connected; let the process finish
    }
  }

  private void startIdleKiller() {
    Thread idleKiller = new Thread() {
        public void run() {
          killAfterTimeout();
        }
      };

    idleKiller.setDaemon(true);
    idleKiller.start();
  }

  public void start() throws Exception {
    reopenStreams();
    Method main = mainMethod(className);

    Method init = mainMethod(System.getenv("DRIP_INIT_CLASS"));
    String initArgs = System.getenv("DRIP_INIT");
    if (initArgs != null) {
      invoke(init == null ? main : init, splitArgs(initArgs, "\n"));
    }

    startIdleKiller();

    for (Switchable o : lazyStreams) {
      o.flip();
    }

    String mainArgs    = readLine();
    String runtimeArgs = readLine();
    setProperties(runtimeArgs);

    invoke(main, splitArgs(mainArgs, "\u0000"));
  }

  public static void main(String[] args) throws Exception {
    new Main(args[0], args[1]).start();
  }

  private Method mainMethod(String className)
    throws ClassNotFoundException, NoSuchMethodException {
    if (className != null) {
      return Class.forName(className, true, ClassLoader.getSystemClassLoader())
        .getMethod("main", String[].class);
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

  private void reopenStreams() throws FileNotFoundException, IOException {
    SwitchableFileInputStream stdin = new SwitchableFileInputStream(System.in, new File(fifoDir, "in"));
    SwitchableFileOutputStream stdout = new SwitchableFileOutputStream(System.out, new File(fifoDir, "out"));
    SwitchableFileOutputStream stderr = new SwitchableFileOutputStream(System.err, new File(fifoDir, "err"));
    lazyStreams = Arrays.<Switchable>asList(stdin, stdout, stderr);

    System.setIn(new BufferedInputStream(stdin));
    System.setOut(new PrintStream(stdout));
    System.setErr(new PrintStream(stderr));
    s = new Scanner(System.in);
  }

  private static final Pattern EVERYTHING = Pattern.compile(".+", Pattern.DOTALL);
  private String readLine() throws IOException {
    s.useDelimiter(":");
    int numChars = s.nextInt();
    s.skip(":");

    String arg;
    if (numChars == 0) { // horizon treats 0 as "unbounded"
      arg = "";
    } else {
      arg = s.findWithinHorizon(EVERYTHING, numChars);
      if (arg.length() != numChars) {
        throw new IOException("Expected " + numChars + " characters but found only " + arg.length() + " in string: \"" + arg + "\"");
      }
    }

    String terminator = s.findWithinHorizon(",", 1);
    if (!(terminator.equals(","))) {
      throw new IOException("Instead of comma terminator after \"" + arg + "\", found " + terminator);
    }
    return arg;
  }
}
