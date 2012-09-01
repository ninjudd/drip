package org.flatland.drip;
import java.io.*;

// not safe to use flip() while any other threads are accessing this object in any way
public class SwitchableInputStream extends InputStream implements Switchable {
  private final File pathToNewIn;

  private InputStream in;
  private boolean switched;

  public SwitchableInputStream(InputStream oldIn, File path) {
    this.in = oldIn;
    this.pathToNewIn = path;
    this.switched = false;
  }

  public void flip() throws IllegalStateException, IOException {
    if (switched) {
      throw new IllegalStateException("Already switched to secondary input");
    }
    switched = true;
    in.close();

    in = new FileInputStream(pathToNewIn);
  }

  public int read() throws IOException {
    return in.read();
  }

  public int read(byte[] b) throws IOException {
    return in.read(b);
  }

  public int read(byte[] b, int off, int len) throws IOException {
    return in.read(b, off, len);
  }

  public long skip(long n) throws IOException {
    return in.skip(n);
  }

  public int available() throws IOException {
    return in.available();
  }

  public void close() throws IOException {
    in.close();
  }
}
