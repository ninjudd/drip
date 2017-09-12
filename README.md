Drip is a launcher for the Java Virtual Machine that provides much faster
startup times than the `java` command. The `drip` script is intended to be a
drop-in replacement for the `java` command, only faster.

Drip is a single bash script and a little bit of C and Java code. It is intended
to work with any JVM-based language and anywhere bash is available.

# How does it work?

Unlike other tools intended to solve the JVM startup problem (e.g. Nailgun,
Cake), Drip does not use a persistent JVM. There are many pitfalls to using a
persistent JVM, which we discovered while working on the Cake build tool for
Clojure. The main problem is that the state of the persistent JVM gets dirty
over time, producing strange errors and requiring liberal use of `cake kill`
whenever any error is encountered, *just in case* dirty state is the cause.

Instead of going down this road, Drip uses a different strategy. It keeps a
fresh JVM spun up in reserve with the correct classpath and other JVM options
so you can quickly connect and use it when needed, then throw it away. Drip
hashes the JVM options and stores information about how to connect to the JVM
in a directory with the hash value as its name.

# Installation

The following instructions assume that `~/bin` is on your `$PATH`. If that is
not the case, you can substitute your favorite location.

**Standalone** &mdash; *We recommend this to get started quickly.*

    curl -L https://raw.githubusercontent.com/ninjudd/drip/master/bin/drip > ~/bin/drip
    chmod 755 ~/bin/drip

**Checkout** &mdash; *If you want to hack on Drip or follow the latest
development, this is the way to go.*

    git clone https://github.com/ninjudd/drip.git
    cd drip && make prefix=~/bin install

**Homebrew** &mdash; *This is a convenient way to brew drip on OS X.*

    brew install drip

Note: Installing brew requires `gcc`. Here are [instructions](http://stackoverflow.com/questions/9353444)
for how to install it on OS X Mountain Lion.

# Usage

You can call `drip` with the same arguments as `java`. Try it. The first time
you execute `drip` with new arguments, it will take as long as a plain `java`
command, because it has to spin up a JVM from scratch, but after that it will be
fast.

For example, to start a Clojure repl with drip:

    drip -cp clojure.jar clojure.main

The Drip JVM will eventually shut itself down if you never connect to it. The
time limit defaults to four hours, but you can change this by setting the
`DRIP_SHUTDOWN` environment variable before calling `drip` to set a timeout, in
minutes:

    DRIP_SHUTDOWN=30 drip -cp clojure.jar clojure.main

This creates a Clojure repl as usual, either by starting up a new one or
connecting to a waiting JVM. But the JVM that is spun up to serve future
requests with the same classpath will have a 30-minute timeout to deactivation.

### JVM Language Integration

For more information about how to integrate Drip with your favorite JVM
language, check out the [wiki](https://github.com/ninjudd/drip/wiki).

# Advanced settings

Drip supports the following advanced settings.

## Pre-Initialization

By default, Drip only loads your main class at startup, but you can tell Drip to
run additional code at startup. This can be used to load classes or execute any
initialization code you like. For a language like Clojure, which compiles code
on-the-fly, this can be used to precompile commonly used code by requiring it.

To tell Drip how to initialize a new JVM, use the `DRIP_INIT` and
`DRIP_INIT_CLASS` environment variables. `DRIP_INIT` should be a
newline-separated list of args to be passed to the `main()` function of
`DRIP_INIT_CLASS`. `DRIP_INIT_CLASS` defaults to the main class the JVM was
started with.

## System Properties

Sometimes, you need to set Java system properties, but you don't want them to be
included in the JVM options used for hashing. In this case, use two dashes
instead of one, and the options won't be passed to the JVM at startup, instead
they will be passed at runtime. Keep in mind that any system properties passed
this way will not be set during initialization.

## Environment Variables

Drip passes all environment variables exported at runtime to the JVM and merges
them into the map returned by `System.getenv`. Keep in mind that the environment
isn't modified until we connect to the JVM; during initialization, the
environment will be derived from the previous process that launched the spare
JVM. 

# License

Drip is licensed under the EPL Eclipse Public License. See LICENSE for
details.
