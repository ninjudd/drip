prefix = ~/bin

all: jar

jar: compile
	jar cf drip.jar -C src/ org

compile: src/org/flatland/drip/*.java
	javac src/org/flatland/drip/*.java

clean:
	rm src/org/flatland/drip/*.class
	rm drip.jar

install:
	ln -sf $$PWD/bin/drip ${prefix}/drip

release: jar
	scp drip.jar pom.xml clojars@clojars.org:
