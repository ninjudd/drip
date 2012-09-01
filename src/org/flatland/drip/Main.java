package org.flatland.drip;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
  private List<Switchable> lazyStreams;
  private String mainClass;
  private File fifoDir;

  public Main(String mainClass, String fifoDir) {
    this.mainClass = mainClass.replace('/', '.');
    this.fifoDir  = new File(fifoDir);
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

    Method main = mainMethod(mainClass);

    String initClass = System.getenv("DRIP_INIT_CLASS");
    String initArgs  = System.getenv("DRIP_INIT");
    Method init;

    if (initClass == null) {
      init = main;
      initClass = mainClass;
    } else {
      init = mainMethod(initClass);
    }
    if (initArgs == null) initArgs = defaultInitArgs(initClass);
    if (initArgs != null) invoke(init, split(initArgs, "\n"));

    startIdleKiller();

    Scanner fromBash = new Scanner(new File(fifoDir, "to_jvm"));
    FileOutputStream toBash = new FileOutputStream(new File(fifoDir, "from_jvm"));

    String mainArgs    = readString(fromBash);
    String runtimeArgs = readString(fromBash);
    String environment = readString(fromBash);

    for (Switchable o : lazyStreams) {
      o.flip();
    }

    mergeEnv(parseEnv(environment));
    setProperties(runtimeArgs);

    invoke(main, split(mainArgs, "\u0000"));

    fromBash.close();
    toBash.close();
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

  private String defaultInitArgs(String className) {
    if (className.equals("org.jruby.Main") || className.equals("clojure.main")) {
      return "-e\nnil";
    } else {
      return null;
    }
  }

  private String[] split(String str, String delim) {
    if (str.length() == 0) {
      return null;
    } else {
      Scanner s = new Scanner(str);
      s.useDelimiter(delim);

      LinkedList<String> list = new LinkedList<String>();
      while (s.hasNext()) {
        list.add(s.next());
      }
      return list.toArray(new String[0]);
    }
  }

  private void invoke(Method main, String[] args) throws Exception {
    if (args != null) {
      main.invoke(null, (Object)args);
    }
  }

  private void setProperties(String runtimeArgs) {
    Matcher m = Pattern.compile("-D([^=]+)=([^\u0000]+)").matcher(runtimeArgs);

    while (m.find()) {
      System.setProperty(m.group(1), m.group(2));
    }
  }

  private Map<String, String> parseEnv(String str) {
    Map<String, String> env = new HashMap<String, String>();

    for (String line: split(str, "\u0000")) {
      String[] var = line.split("=", 2);
      env.put(var[0], var[1]);
    }
    return env;
  }

  @SuppressWarnings("unchecked") // we're hacking a map with reflection
  private void mergeEnv(Map<String, String> newEnv)
    throws NoSuchFieldException, IllegalAccessException {
    Map<String, String> env = System.getenv();
    Class<?> classToHack = env.getClass();
    if (!(classToHack.getName().equals("java.util.Collections$UnmodifiableMap"))) {
      throw new RuntimeException("Don't know how to hack " + classToHack);
    }

    Field field = classToHack.getDeclaredField("m");
    field.setAccessible(true);
    ((Map<String,String>)field.get(env)).putAll(newEnv);
    field.setAccessible(false);
  }

  private void reopenStreams() throws FileNotFoundException, IOException {
    SwitchableOutputStream stderr = new SwitchableOutputStream(System.err, new File(fifoDir, "err"));
    SwitchableOutputStream stdout = new SwitchableOutputStream(System.out, new File(fifoDir, "out"));
    SwitchableInputStream stdin = new SwitchableInputStream(System.in, new File(fifoDir, "in"));
    lazyStreams = Arrays.<Switchable>asList(stderr, stdout, stdin);

    System.setErr(new PrintStream(stderr));
    System.setOut(new PrintStream(stdout));
    System.setIn(new BufferedInputStream(stdin));
  }

  private static final Pattern EVERYTHING = Pattern.compile(".+", Pattern.DOTALL);
  private String readString(Scanner s) throws IOException {
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
