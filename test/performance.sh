function test_speed_increase {
    one=$(bench drip -cp clojure.jar clojure.main -e '(* 1 2)')
    assert [[ $? == 0 ]]
    two=$(bench drip -cp clojure.jar clojure.main -e '(* 1 2)')
    assert [[ $? == 0 ]]

    assert [[ $(($one / 5)) -gt $two ]]
}

function test_runtime_properties {
    one=$(bench drip -cp clojure.jar --Dfoo=bar clojure.main -e '(System/getProperty "foo")')
    assert [[ $? == 0 ]]
    two=$(bench drip -cp clojure.jar --Dfoo=baz clojure.main -e '(System/getProperty "foo")')
    assert [[ $? == 0 ]]

    assert [[ $(($one / 5)) -gt $two ]]

    assert_equal '"bar"' "$(drip -cp clojure.jar --Dfoo=bar clojure.main -e '(System/getProperty "foo")')"
    assert_equal '"baz"' "$(drip -cp clojure.jar --Dfoo=baz clojure.main -e '(System/getProperty "foo")')"
}
