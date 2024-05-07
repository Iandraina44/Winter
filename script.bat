set lib=lib
set bin=bin
set src=src\*.java



javac -cp "%lib%\*"   -d    "%bin%"   %src%


cmd /k