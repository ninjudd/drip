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
