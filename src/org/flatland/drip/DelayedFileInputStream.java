package org.flatland.drip;
import java.io.*;

public class DelayedFileInputStream extends InputStream implements Openable{
  final String path;
  private FileInputStream fs;

  DelayedFileInputStream(String path) {
    this.path = path;
  }

  public void forceOpen() {
    fs();
  }

  private synchronized FileInputStream fs() {
    if (fs == null) {
      try {
        this.fs = new FileInputStream(path);
      } catch (FileNotFoundException e) {
      }
    }
    return fs;
  }

  public int read() throws IOException {
    return fs().read();
  }

  public int read(byte[] b) throws IOException {
    return fs().read(b);
  }

  public int read(byte[] b, int off, int len) throws IOException {
    return fs().read(b, off, len);
  }

  public long skip(long n) throws IOException {
    return fs().skip(n);
  }

  public int available() throws IOException {
    return fs().available();
  }

  public void close() throws IOException {
    fs().close();
  }
}
