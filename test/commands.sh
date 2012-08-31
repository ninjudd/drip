function test_version_command {
    local version=$(drip version 2>&1)
    assert_match "^drip version" "$version"
    assert_match "java version"  "$version"
}

function test_kill_command {
    drip kill > /dev/null
    assert_equal "No idle Drip JVM running" "$(drip kill)"

    assert drip -cp clojure.jar clojure.main -e 'nil'

    assert_equal "" "$(drip kill)"
    assert_equal "No idle Drip JVM running" "$(drip kill)"
}
