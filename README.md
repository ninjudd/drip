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

    curl -L http://decaf.flatland.org > ~/bin/drip
    chmod 755 ~/bin/drip

**Checkout** &mdash; *If you want to hack on Drip or follow the latest
development, this is the way to go.*

    git clone https://github.com/flatland/drip.git
    cd drip && lein jar
    ln -s $PWD/bin/drip ~/bin/drip

# Usage

You can call `drip` with the same arguments as `java`. Try it. The first time
you execute `drip` with a new arguments, it will take longer, because it has to
spin up a JVM from scratch, but after that it will be fast.

For example, to start a Clojure repl with drip:

    drip -cp clojure.jar clojure.main


## With Leiningen

You can use Drip with [Leiningen](https://github.com/technomancy/leiningen) to
make tasks [faster](https://gist.github.com/3491702). For now, you'll need to
install Drip and then check out the `drip` branch of Leiningen. Then add the
following to your `~/.lein/leinrc` file:

    LEIN_JAVA_CMD=drip

Leiningen support for Drip is still experimental, so please report any problems
you have in #leiningen on freenode.
