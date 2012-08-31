PATH=../bin:$PATH
clojure=http://repo1.maven.org/maven2/org/clojure/clojure/1.4.0/clojure-1.4.0.jar
jruby=http://jruby.org.s3.amazonaws.com/downloads/1.6.7.2/jruby-complete-1.6.7.2.jar

mkdir -p tmp

for lang in clojure jruby; do
    jar=tmp/$lang.jar
    if [[ ! -e $jar ]]; then
        echo Downloading $lang
        curl -# ${!lang} > $jar
    fi
done

scala=http://www.scala-lang.org/downloads/distrib/files/scala-2.9.2.tgz

for lang in scala; do
    tgz=tmp/$lang.tgz
    if [[ ! -e $tgz ]]; then
        echo Downloading $lang
        curl -# ${!lang} > $tgz
        tar -xzf $tgz -C tmp
        mv tmp/$lang-* tmp/$lang
    fi
done
