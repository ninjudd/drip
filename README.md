Drip is a launcher for the Java Virtual Machine that provides much faster
startup times than the `java` command. The `drip` script is intended to be a
drop-in replacement for the `java` command, only faster.

Drip is a single bash script and a small amount of Java code. It is intended to
work with any JVM-based language and anywhere bash is available.

# How does it work?

Unlike other tools intended to solve the JVM startup problem (e.g. Nailgun,
cake, jark), Drip does not use a persistent JVM. There are many pitfalls to
using a persistent JVM, which we discovered while working on the cake build tool
for Clojure. The main problem is that the state of the persistent JVM gets dirty
over time, producing strange errors and requiring liberal use of `cake kill`
whenever any error is encountered, *just in case* dirty state is the cause.

Instead of going down this road, Drip uses a different strategy. It keeps a
fresh JVM spun up in reserve with the correct classpath and other JVM options
so you can quickly connect and use it when needed. Drip hashes the JVM options
and stores information about how to connect to that JVM in a directory with
the hash value as its name.

# Installation

The following instructions assume that `~/bin` is on your `$PATH`. If that is
not the case, you can substitute your favorite location.

**Standalone** &mdash; *We recommend this to get started quickly.*

    curl -L http://drip.flatland.org > ~/bin/drip
    chmod 755 ~/bin/drip

**Checkout** &mdash; *If you want to hack on Drip or follow the latest
development, this is the way to go.*

    git clone https://github.com/flatland/drip.git
    cd drip && make PREFIX=~/bin install

# Usage

You can call `drip` with the same arguments as `java`. Try it. The first time
you execute `drip` with new arguments, it will take as long as a plain `java` command,
because it has to spin up a JVM from scratch, but after that it will be fast.

For example, to start a Clojure repl with drip:

    drip -cp clojure.jar clojure.main

The Drip JVM will eventually shut itself down if you never connect to it. The
time limit defaults to four hours, but you can change this by setting the
`DRIP_SHUTDOWN` environment variable before calling `drip` to set a timeout, in
minutes:

    DRIP_TIMEOUT=30 drip -cp clojure.jar clojure.main

This creates a Clojure repl as usual, either by starting up a new one or
connecting to a waiting JVM. But the JVM that is spun up to serve future
requests with the same classpath will have a 30-minute timeout to deactivation.

### JVM Language Integration

For more information about how to integrate Drip with your favorite JVM
language, check out the [wiki](https://github.com/flatland/drip/wiki).
