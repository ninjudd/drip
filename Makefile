PREFIX=~/bin
JAVA_SRC=$(wildcard src/org/flatland/drip/*.java)
C_SRC=$(wildcard src/*.c)
CLASSES=$(JAVA_SRC:.java=.class)
BINARIES=$(subst src,bin,$(C_SRC:.c=))
JAR=drip.jar

all: compile jar

%.class: %.java
	javac ${JAVA_SRC}

bin/%: src/%.c
	gcc $< -o $@

${JAR}: ${CLASSES}
	jar cf ${JAR} -C src/ org

jar: ${JAR}

compile: ${BINARIES} ${CLASSES}

clean:
	rm -f ${BINARIES}
	rm -f ${CLASSES}
	rm -f ${JAR}

install: jar
	mkdir -p ${PREFIX}
	ln -sf $$PWD/bin/drip ${PREFIX}/drip

release: jar
	scp ${JAR} pom.xml clojars@clojars.org:

test: jar test/clojure.jar test/jruby.jar test/scala test/test/Main.class
	test/run

test/test/Main.class: test/test/Main.java
	javac $<

CLOJURE_URL=http://repo1.maven.org/maven2/org/clojure/clojure/1.4.0/clojure-1.4.0.jar
JRUBY_URL=http://jruby.org.s3.amazonaws.com/downloads/1.6.7.2/jruby-complete-1.6.7.2.jar
SCALA_URL=http://www.scala-lang.org/downloads/distrib/files/scala-2.9.2.tgz

test/clojure.jar:
	curl -# ${CLOJURE_URL} > $@

test/jruby.jar:
	curl -# ${JRUBY_URL} > $@

test/scala.tgz:
	curl -# ${SCALA_URL} > $@

test/scala: test/scala.tgz
	tar xzf $< -m -C test
	mv test/scala-* $@

.PHONY: all jar compile clean install release
