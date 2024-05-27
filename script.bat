mkdir "mybin"
set mybin="T:\Fianarana\S4\WebDyn\My Frame\FrameWork\mybin"
set ref="T:\Fianarana\S4\WebDyn\My Frame\FrameWork\lib\*"

@REM Compilation des fichiers dans le répertoire src et ses sous-répertoires
for /r ".\src" %%f in (*.java) do (
   copy "%%f" "mybin\%%~nf.java"
)
cd mybin
javac -cp %ref% -d "../mybin" *.java
 for /r "." %%f in (*.java) do (
    del "%%f" 
 )
 cd .. 

@REM Définition des chemins
set bin="T:\Fianarana\S4\WebDyn\My Frame\Framework\bin"
set mylib="T:/Fianarana/S4/WebDyn/My Frame/Test/lib"
set jar=my-sprint-zero.jar

@REM Création du fichier JAR
jar -cvf %jar% -C %mybin% .

echo D | xcopy /q/y %jar% %mylib%
@echo Done
