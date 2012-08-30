PREFIX=~/bin
SOURCES=$(wildcard src/org/flatland/drip/*.java)
CLASSES=$(SOURCES:.java=.class)
JAR=drip.jar

all: jar

%.class: %.java
	javac ${SOURCES}

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

test: jar
	./test/test

.PHONY: all jar compile clean install release
