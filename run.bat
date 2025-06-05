@echo off
chcp 65001 >nul

REM 航班管理系统运行脚本
echo === 航班管理系统运行脚本 ===
echo.

REM 检查是否已编译
if not exist "out" (
    echo ❌ 未找到编译文件，请先运行编译脚本：
    echo   compile.bat
    exit /b 1
)

if not exist "out\FlightManagementModule.class" (
    echo ❌ 未找到编译文件，请先运行编译脚本：
    echo   compile.bat
    exit /b 1
)

REM 显示可用的模块
echo 请选择要运行的模块：
echo 1. 登录窗口 (LoginWindow)
echo 2. 航班管理模块 (FlightManagementModule)
echo 3. 航班查询模块 (FlightQueryModule)
echo 4. 用户管理模块 (UserManagementModule)
echo 5. 用户管理模块新版 (UserManagementModuleNew)
echo 6. 订单管理模块 (OrderManagementModule)
echo 7. 座位选择模块 (SeatSelectionModule)
echo 8. 支付模块 (PaymentModule)
echo 9. 退票模块 (RefundModule)
echo 10. 改签升舱模块 (RescheduleUpgradeModule)
echo 11. 通知模块 (NotificationModule)
echo 12. 数据库测试 (DatabaseTest)
echo 13. 数据库重建工具 (DatabaseRebuildTool)
echo.

set /p choice=请输入选项 (1-13): 

REM 根据选择运行对应模块
if "%choice%"=="1" (
    echo 启动登录窗口...
    java -cp "out;sqlite-jdbc-3.49.1.0.jar" LoginWindow
) else if "%choice%"=="2" (
    echo 启动航班管理模块...
    java -cp "out;sqlite-jdbc-3.49.1.0.jar" FlightManagementModule
) else if "%choice%"=="3" (
    echo 启动航班查询模块...
    java -cp "out;sqlite-jdbc-3.49.1.0.jar" FlightQueryModule
) else if "%choice%"=="4" (
    echo 启动用户管理模块...
    java -cp "out;sqlite-jdbc-3.49.1.0.jar" UserManagementModule
) else if "%choice%"=="5" (
    echo 启动用户管理模块新版...
    java -cp "out;sqlite-jdbc-3.49.1.0.jar" UserManagementModuleNew
) else if "%choice%"=="6" (
    echo 启动订单管理模块...
    java -cp "out;sqlite-jdbc-3.49.1.0.jar" OrderManagementModule
) else if "%choice%"=="7" (
    echo 启动座位选择模块...
    java -cp "out;sqlite-jdbc-3.49.1.0.jar" SeatSelectionModule
) else if "%choice%"=="8" (
    echo 启动支付模块...
    java -cp "out;sqlite-jdbc-3.49.1.0.jar" PaymentModule
) else if "%choice%"=="9" (
    echo 启动退票模块...
    java -cp "out;sqlite-jdbc-3.49.1.0.jar" RefundModule
) else if "%choice%"=="10" (
    echo 启动改签升舱模块...
    java -cp "out;sqlite-jdbc-3.49.1.0.jar" RescheduleUpgradeModule
) else if "%choice%"=="11" (
    echo 启动通知模块...
    java -cp "out;sqlite-jdbc-3.49.1.0.jar" NotificationModule
) else if "%choice%"=="12" (
    echo 运行数据库测试...
    java -cp "out;sqlite-jdbc-3.49.1.0.jar" DatabaseTest
) else if "%choice%"=="13" (
    echo 启动数据库重建工具...
    java -cp "out;sqlite-jdbc-3.49.1.0.jar" DatabaseRebuildTool
) else (
    echo ❌ 无效选项，请输入 1-13 之间的数字。
    exit /b 1
)

pause
