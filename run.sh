#!/usr/bin/env bash
# Compile and launch River Room. @author Brandon Mai
set -euo pipefail
cd "$(dirname "$0")"
if [ ! -f lib/acm.jar ]; then
    echo "Missing lib/acm.jar. Copy the included lib folder beside this script."
    exit 1
fi
mkdir -p out
if [ -f PokerPrototype.java ]; then
    javac -encoding UTF-8 -cp lib/acm.jar -d out ./*.java
else
    javac -encoding UTF-8 -cp lib/acm.jar -d out src/*.java
fi
java -cp "out:lib/acm.jar" PokerPrototype
