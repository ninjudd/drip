PREFIX=~/bin
SOURCES=$(wildcard src/org/flatland/drip/*.java)
CLASSES=$(SOURCES:.java=.class)
JAR=drip.jar

all: jar compile

%.class: %.java
	javac -classpath ${HOME}/.m2/repository/com/sun/jna/3.4.1/jna-3.4.1.jar ${SOURCES}

${JAR}: ${CLASSES}
	jar cf ${JAR} -C src/ org

jar: ${JAR}

compile: ${CLASSES}

clean:
	rm ${CLASSES}
	rm ${JAR}

install: jar
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
