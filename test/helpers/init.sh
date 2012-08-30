PATH=../bin:$PATH
clojure_url=http://repo1.maven.org/maven2/org/clojure/clojure/1.4.0/clojure-1.4.0.jar
clojure_jar=tmp/clojure-1.4.0.jar

if [ ! -e $clojure_jar ]; then
    curl $clojure_url > $clojure_jar
fi
