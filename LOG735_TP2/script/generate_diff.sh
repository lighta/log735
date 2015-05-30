#!/bin/sh

cd `dirname $0`
cd ..
git diff c926cc4bd32b0e8f3212435aa3b5cace43620e7d HEAD -- src > ./doc/fullsrc.diff
