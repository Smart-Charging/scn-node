#!/usr/bin/env bash

cd scn-node
git pull
./gradlew -Pprofile=test -x test build
sudo service scn-node restart
