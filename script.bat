set lib=lib
set bin=bin
set src=src\*.java
set srcse=src\
set jarname=Winter

set projectlib=D:\ITU\SPRINTS\DeployWinter\lib

javac -parameters -cp "%lib%\*"   -d    "%bin%"   %src%



jar cf %jarname%.jar -C ".\%bin%" .

copy %jarname%.jar "%projectlib%\%jarname%.jar"
