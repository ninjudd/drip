prefix=~/bin
java_src=$(wildcard src/org/flatland/drip/*.java)
c_src=$(wildcard src/*.c)
classes=$(java_src:.java=.class)
binaries=$(subst src,bin,$(c_src:.c=))
jar=drip.jar

all: compile jar

%.class: %.java
	javac ${java_src}

bin/%: src/%.c
	gcc $< -o $@

${jar}: ${classes}
	jar cf ${jar} -C src/ org

jar: ${jar}

compile: ${binaries} ${classes}

clean:
	rm -f ${binaries}
	rm -f ${classes}
	rm -f ${jar}

install: jar
	mkdir -p ${prefix}
	ln -sf $$PWD/bin/drip ${prefix}/drip

release: jar
	scp ${jar} pom.xml clojars@clojars.org:

test: jar test/clojure.jar test/jruby.jar test/scala test/test/Main.class
	test/run

test/test/Main.class: test/test/Main.java
	javac $<

clojure_url=http://repo1.maven.org/maven2/org/clojure/clojure/1.4.0/clojure-1.4.0.jar
jruby_url=http://jruby.org.s3.amazonaws.com/downloads/1.6.7.2/jruby-complete-1.6.7.2.jar
scala_url=http://www.scala-lang.org/downloads/distrib/files/scala-2.9.2.tgz

test/clojure.jar:
	curl -# ${clojure_url} > $@

test/jruby.jar:
	curl -# ${jruby_url} > $@

test/scala.tgz:
	curl -# ${scala_url} > $@

test/scala: test/scala.tgz
	tar xzf $< -m -C test
	mv test/scala-* $@

.PHONY: all jar compile clean install release
