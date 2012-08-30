function test_basic {
    assert_equal 36 "$(drip -cp $clojure_jar clojure.main -e '(* 6 6)')"
    assert_equal 49 "$(drip -cp $clojure_jar clojure.main -e '(* 7 7)')"
    assert_equal 81 "$(drip -cp $clojure_jar clojure.main -e '(* 9 9)')"
}
