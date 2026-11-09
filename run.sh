#!/usr/bin/env bash
# Compile and launch River Room. @author Brandon Mai
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -cp lib/acm.jar -d out src/*.java
java -cp "out:lib/acm.jar" PokerPrototype
