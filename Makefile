prefix=~/bin
java_src = $(shell find src/org/ -type f -name '*.java')
c_src=$(wildcard src/*.c)
classes=$(subst src,classes,$(java_src:.java=.class))
binaries=$(subst src,bin,$(c_src:.c=))
jar=drip.jar
javac=javac

all: compile jar

classes/%.class: src/%.java
	mkdir -p classes/
	${javac} ${java_src} -d classes/

bin/%: src/%.c
	gcc $< -o $@

${jar}: ${classes}
	jar cf ${jar} -C src/ org
	jar uf ${jar} -C classes/ org

jar: ${jar}

compile: ${binaries} ${classes}

clean:
	rm -rf classes
	rm -f ${binaries}
	rm -f ${jar}

install: jar compile
	mkdir -p ${prefix}
	ln -sf $$PWD/bin/drip ${prefix}/drip

tag:
	@[ -n "${version}" ] ||\
          (echo "Version variable required." && exit 1)
	@(git diff-files --quiet && git diff-index --cached --quiet HEAD) ||\
          (echo "Working directory must be clean." && exit 1)
	sed -i~ 's/<version>.*<\/version>/<version>${version}<\/version>/' pom.xml
	sed -i~ 's/DRIP_VERSION=.*/DRIP_VERSION=${version}/' bin/drip
	git commit -am "${version}"
	git push origin
	git tag ${version}
	git push origin ${version}

release: jar
	scp ${jar} pom.xml clojars@clojars.org:

test: jar test/clojure.jar test/jruby.jar test/scala test/test/Main.class
	test/run

test/test/Main.class: test/test/Main.java
	${javac} $<

clojure_url=http://repo1.maven.org/maven2/org/clojure/clojure/1.4.0/clojure-1.4.0.jar
jruby_url=http://jruby.org.s3.amazonaws.com/downloads/1.7.0/jruby-complete-1.7.0.jar
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
