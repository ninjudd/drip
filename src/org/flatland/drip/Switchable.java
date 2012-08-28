package org.flatland.drip;

import java.io.IOException;

public interface Switchable {
  void flip() throws IllegalStateException, IOException;
}
