function test_clojure {
    assert_equal 36 "$(drip -cp clojure.jar clojure.main -e '(* 6 6)')"
    assert_equal 49 "$(drip -cp clojure.jar clojure.main -e '(* 7 7)')"
    assert_equal 64 "$(drip -cp clojure.jar clojure.main -e '(* 8 8)')"
}

function test_ruby {
    assert_equal 36 "$(drip -cp jruby.jar org.jruby.Main -e 'puts 6 * 6')"
    assert_equal 49 "$(drip -cp jruby.jar org.jruby.Main -e 'puts 7 * 7')"
    assert_equal 64 "$(drip -cp jruby.jar org.jruby.Main -e 'puts 8 * 8')"
}

function test_exit_status {
    drip -cp clojure.jar clojure.main -e '(System/exit 123)'
    assert_equal 123 $?

    drip -cp clojure.jar clojure.main -e 'nil'
    assert_equal 0 $?

    drip -cp clojure.jar clojure.main -e 'invalid' 2> /dev/null
    assert_equal 1 $?
}

function test_runtime_environment {
    export foo=bar
    assert_equal '"bar"' "$(drip -cp clojure.jar clojure.main -e '(System/getenv "foo")')"
    assert_equal '"bar"' "$(drip -cp clojure.jar clojure.main -e '(System/getenv "foo")')"

    export foo=baz
    assert_equal '"baz"' "$(drip -cp clojure.jar clojure.main -e '(System/getenv "foo")')"
}

function test_jar_option {
    assert_equal 36 "$(drip -jar clojure.jar -e '(* 6 6)')"
    assert_equal 49 "$(drip -jar clojure.jar -e '(* 7 7)')"
    assert_equal 64 "$(drip -jar clojure.jar -e '(* 8 8)')"
}

function test_hashing {
    # Not the best test, but at least if verifies that two separate JVMs are started.
    assert_equal 36 "$(drip -cp clojure.jar clojure.main -e '(* 6 6)')"
    assert_equal 36 "$(drip -cp jruby.jar org.jruby.Main -e 'puts 6 * 6')"
}

function test_slashes_in_class {
    assert_equal 36 "$(drip -cp clojure.jar clojure/main -e '(* 6 6)')"
    assert_equal 49 "$(drip -cp clojure.jar clojure/main -e '(* 7 7)')"

    export DRIP_INIT_CLASS=clojure/main
    assert_equal 36 "$(drip -cp clojure.jar clojure/main -e '(* 6 6)')"
}
