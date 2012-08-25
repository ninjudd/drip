package org.flatland.decaf;
import java.io.*;
import java.nio.channels.FileChannel;

public class DelayedFileInputStream extends InputStream implements Closable{
  final String path;
  private FileInputStream in;

  DelayedFileInputStream(String path) {
    this.path = path;
  }

  private void open() {
    if (in == null) {
      this.in = new FileInputStream(path);
    }
  }

  public int read() throws IOException {
    open();
    return in.read();
  }

  public int read(byte[] b) throws IOException {
    open();
    return in.read(b);
  }


  public int read(byte[] b, int off, int len) throws IOException {
    open();
    return in.read(b, off, len);
  }

  public long skip(long n) throws IOException {
    open();
    return in.skip(n);
  }

  public int available() throws IOException {
    open();
    return in.available();
  }    

  public void close() throws IOException {
    open();
    in.close();
  }

  public final FileDescriptor getFD() throws IOException {
    open();
    return in.getFD();
  }

  public FileChannel getChannel() {
    open();
    return in.getChannel();
  }

  protected void finalize() throws IOException {
    open();
    return in.finalize();
  }
}
