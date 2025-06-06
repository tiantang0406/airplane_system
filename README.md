# 飞机管理系统 - 完整功能说明

## 系统概述
这是一个基于Java Swing和SQLite数据库的完整飞机管理系统，具备用户认证、角色权限管理、航班管理、订单处理、支付退票等功能。

## 技术架构
- **前端界面**: Java Swing
- **数据库**: SQLite (airplane_system.db)
- **JDBC驱动**: sqlite-jdbc-3.49.1.0.jar

## 数据库结构

### 核心表格
1. **users** - 用户信息表
2. **airports** - 机场信息表
3. **aircraft** - 飞机信息表
4. **flights** - 航班信息表
5. **orders** - 订单信息表

### 数据库特性
- 完整的外键约束
- 索引优化查询性能
- 预置基础数据（机场、飞机、航班、订单）

## 功能模块

### 1. 用户认证系统 (LoginWindow.java)
- **登录验证**: 集成UserManagementModule进行数据库验证
- **角色权限**: 支持三种角色（管理员、用户、客服）
- **主菜单**: 登录成功后根据角色显示不同功能选项

#### 测试账号
- 管理员: admin/admin123
- 普通用户: user1/user123  
- 客服: user2/user456

### 2. 角色权限功能分配

#### 管理员 (admin)
- 用户管理 (UserManagementModule)
- 航班管理 (FlightManagementModule)

#### 普通用户 (user1)
- 航班查询 (FlightQueryModule)
- 预订服务 (BookingServiceModule)
- 座位选择 (SeatSelectionModule)

#### 客服 (user2)
- 订单管理 (OrderManagementModule)
- 退票服务 (RefundModule)
- 改签服务 (RescheduleUpgradeModule)

### 3. 航班管理系统 (FlightManagementModule.java)
- **添加航班**: 完整的航班信息录入和验证
- **查询航班**: 支持航班号查询，显示详细信息
- **数据库集成**: 自动保存到flights表
- **机型管理**: 支持6种主流机型（A320, A330, A350, B737, B777, B787）

#### 功能特性
- 时间格式验证 (yyyy-MM-dd HH:mm)
- 票价合理性检查
- 航班号重复性检查
- 机场代码关联查询

### 4. 订单管理系统 (OrderManagementModule.java)
- **订单显示**: 表格形式展示订单信息
- **状态筛选**: 支持按订单状态筛选
- **支付管理**: 集成PaymentModule处理待支付订单
- **退票管理**: 集成RefundModule处理已完成订单

#### 订单字段
- 订单号、航班号、航线、日期、时间、状态、金额

### 5. 支付模块 (PaymentModule.java)
- **多种支付方式**: 支付宝、微信支付、银行卡
- **金额显示**: 自动显示订单金额
- **支付确认**: 模拟支付流程

### 6. 退票模块 (RefundModule.java)
- **退票申请**: 选择航班和退票原因
- **手续费计算**: 根据退票时间计算手续费
- **退款金额**: 自动计算实际退款金额

## 导航流程
```
登录 → 主菜单 → 功能模块 → 返回主菜单
```
- 所有子模块关闭时自动返回主菜单
- 避免直接退出系统，提供良好的用户体验

## 编译和运行

### 编译
```bash
cd /Users/apple/Documents/airplane_system
javac -cp ".:sqlite-jdbc-3.49.1.0.jar" src/*.java
```

### 运行
```bash
java -cp ".:sqlite-jdbc-3.49.1.0.jar" LoginWindow
```

## 使用步骤

### 1. 启动系统
运行LoginWindow，使用测试账号登录

### 2. 管理员操作
- 登录后选择"航班管理"
- 添加新航班: 填写完整信息并提交
- 查询航班: 输入航班号查看详情

### 3. 客服操作
- 登录后选择"订单管理"
- 处理待支付订单: 选择订单点击"支付管理"
- 处理退票申请: 选择已完成订单点击"退票管理"

### 4. 数据持久化
所有操作都会自动保存到SQLite数据库，支持数据持久化存储。

## 系统特色

### 1. 完整的MVC架构
- 界面层: Swing组件
- 业务层: 各功能模块
- 数据层: SQLite数据库

### 2. 角色权限控制
- 基于角色的访问控制
- 菜单动态生成
- 权限细粒度管理

### 3. 数据库设计
- 标准化数据库设计
- 完整性约束
- 性能优化索引

### 4. 用户体验
- 统一的界面风格
- 清晰的操作流程
- 友好的错误提示

## 扩展功能
系统已预留扩展接口，可以轻松添加:
- 座位选择模块
- 通知系统
- 报表统计
- 更多支付方式
- 航班状态实时更新

## 总结
这是一个功能完整、架构合理的飞机管理系统，具备企业级应用的基本特征，可以作为学习和实际项目的参考。
