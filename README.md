Decaf is a launcher for the Java Virtual Machine that provides much faster
startup times than the `java` command. The `decaf` script is intended to be a
drop-in replacement for the `java` command, only faster.

# How does it work?

Unlike other tools intended to solve the JVM startup problem (e.g. Nailgun,
cake, jark), Decaf does not use a persistent JVM. There are many pitfalls to
using a persistent JVM, which we discovered while working on the cake build tool
for Clojure. The main problem is that the state of the persistent JVM gets dirty
over time, producing strange errors and requiring liberal use `cake kill`
whenever any error is encountered, *just in case* dirty state is the cause.

Instead of going down this perilous road again, Decaf uses a different
strategy. It keeps a fresh JVM spun up in reserve with the correct classpath and
other JVM options so you can quickly connect and use it when needed.

## Simplicity and portability

Decaf is a single bash script and a small amount of Java code. It is intended to
work with any JVM-based language and anywhere bash is available.

# Installation

The following instructions assume that `~/bin` is on your `$PATH`. If that is
not the case, you can substitute your favorite location.

## Standalone

This is what we'd recommend to get started quickly.

    curl https://raw.github.com/flatland/decaf/master/bin/decaf > ~/bin/decaf
    chmod 755 ~/bin/decaf

## Git Checkout

If you'd like to hack on Decaf of follow the latest development, this is the way
to go.

    git clone https://github.com/flatland/decaf.git
    ln -s $PWD/decaf/bin/decaf ~/bin/decaf

# Usage

You can call `decaf` with the same arguments as `java`. Try it. The first time
you execute `decaf` with a new arguments, it will take longer, because it has to
spin up a JVM from scratch, but after that it will be fast.

For example, to start a Clojure repl with decaf:

    decaf -cp clojure.jar clojure.main

