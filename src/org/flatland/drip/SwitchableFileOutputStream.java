package org.flatland.drip;
import java.io.*;

public class SwitchableFileOutputStream extends OutputStream implements Switchable {
  private final File pathToNewOut;

  private OutputStream out;
  private boolean switched;

  public SwitchableFileOutputStream(OutputStream old, File path) {
    this.out = old;
    this.pathToNewOut = path;
    this.switched = false;
  }

  public synchronized void flip() throws IllegalStateException, IOException {
    if (switched) {
      throw new IllegalStateException("Already switched to secondary output");
    }
    switched = true;
    out.close();

    out = new FileOutputStream(pathToNewOut);
  }

  public void write(int b) throws IOException {
    out.write(b);
  }

  public void write(byte[] b) throws IOException {
    out.write(b);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
  }

  public void close() throws IOException {
    out.close();
  }
}
