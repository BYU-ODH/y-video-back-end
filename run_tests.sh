#! /usr/bin/env bash

set -x  # echo commands as they are run

TEST_HOME='test/clj/'

pushd ${TEST_HOME}
mkdir -p testing/{dest,trash,temp,src,log}  # TODO this might be unnecessary
lein with-profile test cloverage
popd
