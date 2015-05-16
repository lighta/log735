#!/bin/sh

cd `dirname $0`
BASE_SRC=../src/main/java/ens/etsmtl/ca
diff -ur $BASE_SRC/q1 $BASE_SRC/q2 > ../doc/q1_q2.diff
diff -ur $BASE_SRC/q2 $BASE_SRC/q3 > ../doc/q2_q3.diff
diff -ur $BASE_SRC/q3 $BASE_SRC/q5 > ../doc/q3_q5.diff
diff -ur $BASE_SRC/q5 $BASE_SRC/q6 > ../doc/q5_q6.diff
