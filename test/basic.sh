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

function test_speed_increase {
    drip kill > /dev/null

    one="$(bench drip -cp clojure.jar clojure.main -e '(* 1 2)')"
    two="$(bench drip -cp clojure.jar clojure.main -e '(* 1 2)')"

    assert [[ $(($one / 5)) -gt $two ]]
}
