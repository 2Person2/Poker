@echo off
rem Compile and launch River Room. @author Brandon Mai
setlocal
cd /d "%~dp0"
if not exist lib\acm.jar (
    echo Missing lib\acm.jar. Copy the included lib folder beside this script.
    exit /b 1
)
if not exist out mkdir out
if exist PokerPrototype.java (
    javac -encoding UTF-8 -cp lib\acm.jar -d out *.java
) else (
    javac -encoding UTF-8 -cp lib\acm.jar -d out src\*.java
)
if errorlevel 1 exit /b 1
java -cp "out;lib\acm.jar" PokerPrototype
exit /b %errorlevel%
