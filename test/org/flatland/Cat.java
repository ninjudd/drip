package org.flatland;
import java.io.*;

public class Cat {
  public static void main(String[] args) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    String line;
    while ((line = in.readLine()) != null)
      System.out.println(line);
  }
}
