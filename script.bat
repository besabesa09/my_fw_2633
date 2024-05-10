mkdir "temp"

set jar="D:\ITU\S4\Web Dynamique\Clone_Sprint0\my_fw_2633\lib\*"
set javaako="D:\ITU\S4\Web Dynamique\Clone_Sprint0\my_fw_2633\src\"

@REM Compilation des fichiers dans le répertoire src et ses sous-répertoires
for /r ".\src" %%f in (*.java) do (
    javac -cp %jar% -d "temp" "%%f"
)

@set libTest="D:\ITU\S4\Web Dynamique\Clone_Sprint0\Test\lib"

jar cvf "lodyFrame.jar" -C lodyFrame/ .
move "lodyFrame.jar" %libTest%

rmdir /q/s "temp"