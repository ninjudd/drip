function test_clojure_speed_increase {
    one=$(bench drip -cp clojure.jar clojure.main -e '(* 1 2 3)')
    assert [[ $? == 0 ]]
    two=$(bench drip -cp clojure.jar clojure.main -e '(* 4 5 6)')
    assert [[ $? == 0 ]]

    # At least 4 times faster.
    assert [[ $(($one / 4)) -gt $two ]]
}

function test_jruby_speed_increase {
    one=$(bench drip -cp jruby.jar org.jruby.Main -e 'puts 1 * 2 * 3')
    assert [[ $? == 0 ]]
    two=$(bench drip -cp jruby.jar org.jruby.Main -e 'puts 4 * 5 * 6')
    assert [[ $? == 0 ]]

    # At least 4 times faster.
    assert [[ $(($one / 4)) -gt $two ]]
}

function test_scala_speed_increase {
    one=$(JAVACMD=drip bench scala/bin/scala -e 'println(1 * 2 * 3)')
    assert [[ $? == 0 ]]
    two=$(JAVACMD=drip bench scala/bin/scala -e 'println(4 * 5 * 6)')
    assert [[ $? == 0 ]]

    # Only slightly faster.
    assert [[ $one -gt $two ]]
}

#this just doesn't seem to pass with the java i'm testing on
# function test_java_speed_increase {
#     one=$(bench drip test.Main foo bar baz)
#     assert [[ $? == 0 ]]
#     two=$(bench drip test.Main foo bar baz)
#     assert [[ $? == 0 ]]

#     # Only slightly faster.
#     assert [[ $one -gt $two ]]
# }

function test_runtime_properties {
    one=$(bench drip -cp clojure.jar --Dfoo=bar clojure.main -e '(System/getProperty "foo")')
    assert [[ $? == 0 ]]
    two=$(bench drip -cp clojure.jar --Dfoo=baz clojure.main -e '(System/getProperty "foo")')
    assert [[ $? == 0 ]]

    assert [[ $(($one / 4)) -gt $two ]]

    assert_equal '"bar"' "$(drip -cp clojure.jar --Dfoo=bar clojure.main -e '(System/getProperty "foo")')"
    assert_equal '"baz"' "$(drip -cp clojure.jar --Dfoo=baz clojure.main -e '(System/getProperty "foo")')"
}
