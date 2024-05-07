set lib=lib
set bin=bin
set src=src\*.java
set jarname=Winter

set projectlib=D:\S4\webdynamique\SPRINTS\DeployWinter\lib

javac -cp "%lib%\*"   -d    "%bin%"   %src%

jar cf %jarname%.jar -C ".\%bin%" .

copy %jarname%.jar "%projectlib%\%jarname%.jar"
