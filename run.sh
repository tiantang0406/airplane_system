#!/bin/bash

# 航班管理系统运行脚本
echo "=== 航班管理系统运行脚本 ==="
echo ""

# 检查是否已编译
if [ ! -d "out" ] || [ ! -f "out/FlightManagementModule.class" ]; then
    echo "❌ 未找到编译文件，请先运行编译脚本："
    echo "  ./compile.sh"
    exit 1
fi

# 显示可用的模块
echo "请选择要运行的模块："
echo "1. 登录窗口 (LoginWindow)"
echo "2. 航班管理模块 (FlightManagementModule)"
echo "3. 航班查询模块 (FlightQueryModule)"
echo "4. 用户管理模块 (UserManagementModule)"
echo "5. 用户管理模块新版 (UserManagementModuleNew)"
echo "6. 订单管理模块 (OrderManagementModule)"
echo "7. 座位选择模块 (SeatSelectionModule)"
echo "8. 支付模块 (PaymentModule)"
echo "9. 退票模块 (RefundModule)"
echo "10. 改签升舱模块 (RescheduleUpgradeModule)"
echo "11. 通知模块 (NotificationModule)"
echo "12. 数据库测试 (DatabaseTest)"
echo "13. 数据库重建工具 (DatabaseRebuildTool)"
echo ""

# 如果提供了参数，直接运行对应模块
if [ $# -eq 1 ]; then
    choice=$1
else
    read -p "请输入选项 (1-13): " choice
fi

# 根据选择运行对应模块
case $choice in
    1)
        echo "启动登录窗口..."
        java -cp "out:sqlite-jdbc-3.49.1.0.jar" LoginWindow
        ;;
    2)
        echo "启动航班管理模块..."
        java -cp "out:sqlite-jdbc-3.49.1.0.jar" FlightManagementModule
        ;;
    3)
        echo "启动航班查询模块..."
        java -cp "out:sqlite-jdbc-3.49.1.0.jar" FlightQueryModule
        ;;
    4)
        echo "启动用户管理模块..."
        java -cp "out:sqlite-jdbc-3.49.1.0.jar" UserManagementModule
        ;;
    5)
        echo "启动用户管理模块新版..."
        java -cp "out:sqlite-jdbc-3.49.1.0.jar" UserManagementModuleNew
        ;;
    6)
        echo "启动订单管理模块..."
        java -cp "out:sqlite-jdbc-3.49.1.0.jar" OrderManagementModule
        ;;
    7)
        echo "启动座位选择模块..."
        java -cp "out:sqlite-jdbc-3.49.1.0.jar" SeatSelectionModule
        ;;
    8)
        echo "启动支付模块..."
        java -cp "out:sqlite-jdbc-3.49.1.0.jar" PaymentModule
        ;;
    9)
        echo "启动退票模块..."
        java -cp "out:sqlite-jdbc-3.49.1.0.jar" RefundModule
        ;;
    10)
        echo "启动改签升舱模块..."
        java -cp "out:sqlite-jdbc-3.49.1.0.jar" RescheduleUpgradeModule
        ;;
    11)
        echo "启动通知模块..."
        java -cp "out:sqlite-jdbc-3.49.1.0.jar" NotificationModule
        ;;
    12)
        echo "运行数据库测试..."
        java -cp "out:sqlite-jdbc-3.49.1.0.jar" DatabaseTest
        ;;
    13)
        echo "启动数据库重建工具..."
        java -cp "out:sqlite-jdbc-3.49.1.0.jar" DatabaseRebuildTool
        ;;
    *)
        echo "❌ 无效选项，请输入 1-13 之间的数字。"
        exit 1
        ;;
esac
