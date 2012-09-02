PATH=../bin:$PATH

function setup {
    drip kill > /dev/null
}

function bench {
    # Can't believe it is this hard to get the timing in milliseconds.
    local t
    t=$( (export TIMEFORMAT='%3R'; time "$@" &> /dev/null) 2>&1 )
    status=$?
    t=$(tr -d '.' <<< "$t")
    t=$((10#$t))
    echo $t
    return $status
}
