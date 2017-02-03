#!/usr/bin/env bash

ENV="local"

if [ "$1" = "dev" ]
then
    ENV="$1"

    echo "Environment : $ENV"

    export environment=$ENV

    sbt clean 'endtoend:test'

elif [ "$1" = "qa" ]
then
    ENV="$1"

    echo "Environment : $ENV"

    export environment=$ENV

    sbt clean 'endtoend:test'

elif [ "$1" = "staging" ]
then
    ENV="$1"

    echo "Environment : $ENV"

    export environment=$ENV

    sbt clean 'endtoend:test'

else

     ENV="local"

     echo "Environment : $ENV"

     export environment=$ENV

     sbt clean 'endtoend:test'

fi