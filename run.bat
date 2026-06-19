@echo off
set FX="C:\openjfx-26.0.1_windows-x64_bin-sdk\javafx-sdk-26.0.1\lib"

echo [1/2] Finding and compiling all Java files dynamically...
if not exist out mkdir out

REM Create a temporary list of all .java files in the src folder
dir /s /b src\*.java > sources.txt

REM Compile everything using the generated list
javac --module-path %FX% --add-modules javafx.controls,javafx.fxml,javafx.graphics -d out -sourcepath src @sources.txt

REM Clean up the temporary file list
del sources.txt

if %errorlevel% equ 0 (
    echo [2/2] Launching Application...
    java --module-path %FX% --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp out ui.MainApp
) else (
    echo.
    echo [ERROR] Compilation failed. Check code errors above.
)
pause