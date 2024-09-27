mkdir "temp"

set jar="D:\ITU\S5\FrameWork\my_fw_2633\lib\*"
set javaako="D:\ITU\S5\FrameWork\my_fw_2633\src\"

@REM Copie des fichiers .java dans le répertoire temp
for /r ".\src" %%f in (*.java) do (
    copy "%%f" "temp\%%~nf.java"
)

@REM Création du répertoire compiler dans temp
mkdir "compiler"

@REM Changement de répertoire courant vers temp
cd temp

@REM Compilation des fichiers dans le répertoire courant et ses sous-répertoires
javac -cp %jar% -d "../compiler" *.java

cd ..

@set libTest="D:\ITU\S5\FrameWork\Test\lib"

jar cvf "sprint_2.jar" -C compiler/ .
move "sprint_2.jar" %libTest%

rmdir /q/s "temp"
rmdir /q/s "compiler"