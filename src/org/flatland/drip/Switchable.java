package org.flatland.drip;

import java.io.File;
import java.io.IOException;
import java.io.FileDescriptor;

public interface Switchable {
  void flip() throws IllegalStateException, IOException;
  FileDescriptor getFD() throws IOException;
  File path();
}
