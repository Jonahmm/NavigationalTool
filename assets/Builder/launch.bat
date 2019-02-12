@echo off
SET SCALE=%1
IF NOT DEFINED SCALE SET SCALE=6
echo Launching Builder v 1.1 with scale %SCALE%:1...
start /b java -jar Builder-1.1.jar %SCALE%