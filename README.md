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
over time, producing strange errors and requiring liberal use `cake kill`
whenever any error is encountered, *just in case* dirty state is the cause.

Instead of going down this perilous road again, Drip uses a different
strategy. It keeps a fresh JVM spun up in reserve with the correct classpath and
other JVM options so you can quickly connect and use it when needed.

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
you execute `drip` with a new arguments, it will take longer, because it has to
spin up a JVM from scratch, but after that it will be fast.

For example, to start a Clojure repl with drip:

    drip -cp clojure.jar clojure.main

### JVM Language Integration

For more information about how to integrate Drip with your favorite JVM
language, check out the
[JVM Language Integration](https://github.com/flatland/drip/wiki/JVM-Language-Integration)
page on the wiki.
