#!/bin/bash

set -e

sbt test it:test component:test
