PATH=../bin:$PATH

function bench {
    # Can't believe it is this hard to get the timing in milliseconds.
    local t
    t="$( (export TIMEFORMAT='%3R'; time "$@" &> /dev/null) 2>&1 )"
    [[ $? == 0 ]] || echo "Error executing: $@" >&2
    t="$(tr -d '.' <<< "$t")"
    t=$((10#$t))
    echo $t
}
