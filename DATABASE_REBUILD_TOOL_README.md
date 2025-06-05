# 数据库重建工具使用说明

## 概述
`DatabaseRebuildTool` 是一个用于航班系统数据库管理的工具，提供了完整的数据库重建、维护和验证功能。

## 功能特点

### 1. 完全重建数据库
- 删除现有数据库文件
- 重新创建所有表结构
- 插入示例数据
- 验证数据完整性

### 2. 仅重建表结构
- 保留数据库文件
- 重新创建表结构（如果已存在则跳过）
- 适用于修复表结构问题

### 3. 仅插入示例数据
- 清除现有数据
- 重新插入机场、飞机、航班等示例数据
- 适用于重置数据到初始状态

### 4. 验证数据库完整性
- 检查数据库文件是否存在
- 验证所有表的记录数量
- 检查外键关系是否正常
- 显示示例数据

## 使用方法

### 编译
```bash
cd /Users/apple/Documents/airplane_system
javac -cp ".:sqlite-jdbc-3.49.1.0.jar" src/DatabaseRebuildTool.java -d out
```

### 运行
```bash
java -cp "out:sqlite-jdbc-3.49.1.0.jar" DatabaseRebuildTool
```

### 交互式菜单
运行后会显示以下选项：
```
1. 完全重建数据库（删除现有数据库并重新创建）
2. 仅重建表结构（保留数据库文件，重建表）
3. 仅插入示例数据
4. 验证数据库完整性
5. 退出
```

### 自动化运行
可以通过管道输入选项号来自动运行：
```bash
# 完全重建数据库
echo "1" | java -cp "out:sqlite-jdbc-3.49.1.0.jar" DatabaseRebuildTool

# 验证数据库
echo "4" | java -cp "out:sqlite-jdbc-3.49.1.0.jar" DatabaseRebuildTool
```

## 数据库结构

工具会创建以下表：

### 1. airports（机场信息表）
- `airport_code`: 机场代码（主键）
- `airport_name`: 机场名称
- `city`: 所在城市
- `country`: 国家
- `timezone`: 时区
- `status`: 状态

### 2. aircraft（飞机信息表）
- `aircraft_id`: 飞机编号（主键）
- `aircraft_type`: 机型
- `total_seats`: 总座位数
- `first_class_seats`: 头等舱座位数
- `business_class_seats`: 商务舱座位数
- `economy_class_seats`: 经济舱座位数
- `manufacturer`: 制造商
- `year_manufactured`: 制造年份
- `status`: 状态

### 3. flights（航班信息表）
- `flight_id`: 航班ID（主键）
- `flight_number`: 航班号
- `aircraft_id`: 飞机编号（外键）
- `departure_airport`: 出发机场代码（外键）
- `arrival_airport`: 到达机场代码（外键）
- `departure_time`: 出发时间
- `arrival_time`: 到达时间
- `base_price`: 基础票价
- `available_seats`: 可用座位数
- `status`: 航班状态
- `gate`: 登机口
- `terminal`: 航站楼

### 4. seats（座位信息表）
- `seat_id`: 座位ID（主键）
- `flight_id`: 航班ID（外键）
- `seat_number`: 座位号
- `seat_class`: 座位等级
- `is_occupied`: 是否已被占用
- `passenger_name`: 乘客姓名
- `booking_time`: 预订时间

### 5. orders（订单表）
- `order_id`: 订单号（主键）
- `user_id`: 用户ID
- `flight_id`: 航班ID（外键）
- `passenger_name`: 乘客姓名
- `passenger_id`: 乘客身份证号
- `seat_number`: 座位号
- `ticket_price`: 票价
- `booking_time`: 订票时间
- `payment_status`: 支付状态
- `order_status`: 订单状态
- `payment_method`: 支付方式
- `payment_time`: 支付时间

### 6. refunds（退票记录表）
- `refund_id`: 退票ID（主键）
- `order_id`: 订单号（外键）
- `refund_reason`: 退票原因
- `refund_amount`: 退票金额
- `refund_fee`: 退票手续费
- `refund_time`: 退票时间
- `refund_status`: 退票状态
- `processed_by`: 处理人员

## 示例数据

工具会插入以下示例数据：

### 机场数据（10个）
- 北京首都国际机场 (PEK)
- 上海虹桥国际机场 (SHA)
- 上海浦东国际机场 (PVG)
- 广州白云国际机场 (CAN)
- 深圳宝安国际机场 (SZX)
- 成都天府国际机场 (CTU)
- 重庆江北国际机场 (CKG)
- 西安咸阳国际机场 (XIY)
- 杭州萧山国际机场 (HGH)
- 南京禄口国际机场 (NKG)

### 飞机数据（8架）
- A320, A330, A350 (空客)
- B737, B777, B787 (波音)

### 航班数据（8个航班）
- 覆盖主要城市间的航线
- 使用未来日期（2025年6-7月）
- 包含不同时间段的航班

## 故障排除

### 1. 编译错误
- 确保SQLite JDBC驱动文件 `sqlite-jdbc-3.49.1.0.jar` 存在
- 检查Java版本是否支持文本块语法（Java 15+）

### 2. 运行时错误
- 确保当前工作目录正确
- 检查文件权限
- 验证数据库文件路径

### 3. 数据库锁定
- 关闭所有访问数据库的程序
- 重启终端会话

### 4. 数据不一致
- 使用选项1完全重建数据库
- 检查外键约束

## 与其他模块的兼容性

工具创建的数据库结构与以下模块完全兼容：
- `FlightQueryModule` - 航班查询模块
- `OrderManagementModule` - 订单管理模块
- `PaymentModule` - 支付模块
- `RefundModule` - 退票模块
- 其他航班系统模块

## 注意事项

1. **数据备份**: 在使用选项1（完全重建）前，请备份重要数据
2. **时区设置**: 所有时间都使用 'Asia/Shanghai' 时区
3. **航班日期**: 示例航班使用未来日期，确保查询时使用正确的日期范围
4. **相对路径**: 工具使用相对路径 `airplane_system.db`，确保在正确的目录下运行

## 文件路径输出

工具会显示以下路径信息：
- 当前工作目录
- 数据库文件完整路径
- 数据库文件是否存在
- 数据库文件大小

这些信息有助于调试路径相关问题。
