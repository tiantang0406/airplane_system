@echo off
rem 航班管理系统编译脚本 (Windows版)

echo 开始编译航班管理系统...

rem 确保out目录存在
if not exist out mkdir out

rem 清理之前的编译文件
echo 清理之前的编译文件...
del /Q out\*.class 2>nul

rem 编译Java文件
echo 编译Java源文件...
javac -cp "sqlite-jdbc-3.49.1.0.jar" -d out src\*.java

rem 检查编译结果
if %ERRORLEVEL% EQU 0 (
    echo ✅ 编译成功！
    echo 编译文件已保存到 out\ 目录
    echo.
    echo 可以使用以下命令运行程序：
    echo   java -cp "out;sqlite-jdbc-3.49.1.0.jar" FlightManagementModule
    echo   java -cp "out;sqlite-jdbc-3.49.1.0.jar" LoginWindow
    echo   java -cp "out;sqlite-jdbc-3.49.1.0.jar" FlightQueryModule
    echo.
    echo 编译的类文件列表：
    dir out\*.class /B
) else (
    echo ❌ 编译失败！请检查错误信息。
    pause
    exit /b 1
)

pause
