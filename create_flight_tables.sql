-- 航班管理相关数据库表设计
-- 适用于airplane_system.db

-- 1. 机场信息表
CREATE TABLE IF NOT EXISTS airports (
    airport_code TEXT PRIMARY KEY,     -- 机场代码 (如: PEK, SHA, CAN)
    airport_name TEXT NOT NULL,        -- 机场名称
    city TEXT NOT NULL,                -- 所在城市
    country TEXT NOT NULL DEFAULT '中国',  -- 国家
    timezone TEXT DEFAULT 'Asia/Shanghai', -- 时区
    status TEXT DEFAULT 'active'       -- 状态: active/inactive
);

-- 2. 飞机信息表
CREATE TABLE IF NOT EXISTS aircraft (
    aircraft_id TEXT PRIMARY KEY,      -- 飞机编号
    aircraft_type TEXT NOT NULL,       -- 机型 (A320, A330, B737等)
    total_seats INTEGER NOT NULL,      -- 总座位数
    first_class_seats INTEGER DEFAULT 0, -- 头等舱座位数
    business_class_seats INTEGER DEFAULT 0, -- 商务舱座位数
    economy_class_seats INTEGER NOT NULL, -- 经济舱座位数
    manufacturer TEXT,                 -- 制造商
    year_manufactured INTEGER,         -- 制造年份
    status TEXT DEFAULT 'active'       -- 状态: active/maintenance/retired
);

-- 3. 航班信息表
CREATE TABLE IF NOT EXISTS flights (
    flight_id TEXT PRIMARY KEY,        -- 航班ID (内部使用)
    flight_number TEXT NOT NULL,       -- 航班号 (如: MU5112, CA1833)
    aircraft_id TEXT NOT NULL,         -- 飞机编号
    departure_airport TEXT NOT NULL,   -- 出发机场代码
    arrival_airport TEXT NOT NULL,     -- 到达机场代码
    departure_time DATETIME NOT NULL,  -- 出发时间
    arrival_time DATETIME NOT NULL,    -- 到达时间
    base_price DECIMAL(10,2) NOT NULL, -- 基础票价
    available_seats INTEGER NOT NULL,  -- 可用座位数
    status TEXT DEFAULT 'scheduled',   -- 航班状态: scheduled/boarding/departed/arrived/cancelled/delayed
    gate TEXT,                         -- 登机口
    terminal TEXT,                     -- 航站楼
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (aircraft_id) REFERENCES aircraft(aircraft_id),
    FOREIGN KEY (departure_airport) REFERENCES airports(airport_code),
    FOREIGN KEY (arrival_airport) REFERENCES airports(airport_code)
);

-- 4. 座位信息表
CREATE TABLE IF NOT EXISTS seats (
    seat_id TEXT PRIMARY KEY,          -- 座位ID
    flight_id TEXT NOT NULL,           -- 航班ID
    seat_number TEXT NOT NULL,         -- 座位号 (如: 1A, 12F, 25C)
    seat_class TEXT NOT NULL,          -- 座位等级: first/business/economy
    is_occupied BOOLEAN DEFAULT FALSE, -- 是否已被占用
    passenger_name TEXT,               -- 乘客姓名
    booking_time DATETIME,             -- 预订时间
    FOREIGN KEY (flight_id) REFERENCES flights(flight_id),
    UNIQUE(flight_id, seat_number)
);

-- 5. 订单表
CREATE TABLE IF NOT EXISTS orders (
    order_id TEXT PRIMARY KEY,         -- 订单号
    user_id TEXT NOT NULL,             -- 用户ID
    flight_id TEXT NOT NULL,           -- 航班ID
    passenger_name TEXT NOT NULL,      -- 乘客姓名
    passenger_id TEXT NOT NULL,        -- 乘客身份证号
    seat_number TEXT,                  -- 座位号
    ticket_price DECIMAL(10,2) NOT NULL, -- 票价
    booking_time DATETIME DEFAULT CURRENT_TIMESTAMP, -- 订票时间
    payment_status TEXT DEFAULT 'pending', -- 支付状态: pending/paid/refunded
    order_status TEXT DEFAULT 'active',    -- 订单状态: active/cancelled/completed
    payment_method TEXT,               -- 支付方式
    payment_time DATETIME,             -- 支付时间
    FOREIGN KEY (flight_id) REFERENCES flights(flight_id),
    FOREIGN KEY (user_id) REFERENCES users(username)
);

-- 6. 退票记录表
CREATE TABLE IF NOT EXISTS refunds (
    refund_id TEXT PRIMARY KEY,        -- 退票ID
    order_id TEXT NOT NULL,            -- 订单号
    refund_reason TEXT,                -- 退票原因
    refund_amount DECIMAL(10,2) NOT NULL, -- 退票金额
    refund_fee DECIMAL(10,2) DEFAULT 0,   -- 退票手续费
    refund_time DATETIME DEFAULT CURRENT_TIMESTAMP, -- 退票时间
    refund_status TEXT DEFAULT 'processing', -- 退票状态: processing/approved/rejected/completed
    processed_by TEXT,                 -- 处理人员
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- 插入基础数据

-- 插入机场信息
INSERT OR REPLACE INTO airports VALUES 
('PEK', '北京首都国际机场', '北京', '中国', 'Asia/Shanghai', 'active'),
('SHA', '上海虹桥国际机场', '上海', '中国', 'Asia/Shanghai', 'active'),
('PVG', '上海浦东国际机场', '上海', '中国', 'Asia/Shanghai', 'active'),
('CAN', '广州白云国际机场', '广州', '中国', 'Asia/Shanghai', 'active'),
('SZX', '深圳宝安国际机场', '深圳', '中国', 'Asia/Shanghai', 'active'),
('CTU', '成都天府国际机场', '成都', '中国', 'Asia/Shanghai', 'active'),
('CKG', '重庆江北国际机场', '重庆', '中国', 'Asia/Shanghai', 'active'),
('XIY', '西安咸阳国际机场', '西安', '中国', 'Asia/Shanghai', 'active'),
('HGH', '杭州萧山国际机场', '杭州', '中国', 'Asia/Shanghai', 'active'),
('NKG', '南京禄口国际机场', '南京', '中国', 'Asia/Shanghai', 'active');

-- 插入飞机信息
INSERT OR REPLACE INTO aircraft VALUES 
('B-001A', 'A320', 180, 8, 24, 148, 'Airbus', 2018, 'active'),
('B-002A', 'A330', 290, 12, 36, 242, 'Airbus', 2019, 'active'),
('B-003A', 'A350', 350, 16, 48, 286, 'Airbus', 2020, 'active'),
('B-004B', 'B737', 160, 8, 20, 132, 'Boeing', 2017, 'active'),
('B-005B', 'B777', 320, 16, 40, 264, 'Boeing', 2019, 'active'),
('B-006B', 'B787', 280, 12, 32, 236, 'Boeing', 2020, 'active'),
('B-007A', 'A320', 180, 8, 24, 148, 'Airbus', 2021, 'active'),
('B-008B', 'B737', 160, 8, 20, 132, 'Boeing', 2018, 'active');

-- 插入示例航班数据
INSERT OR REPLACE INTO flights VALUES 
('FL001', 'MU5112', 'B-001A', 'PEK', 'SHA', '2024-06-15 08:00:00', '2024-06-15 10:30:00', 580.00, 150, 'scheduled', 'A12', 'T2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('FL002', 'CA1833', 'B-002A', 'CAN', 'CTU', '2024-06-20 12:30:00', '2024-06-20 14:45:00', 890.00, 200, 'scheduled', 'B08', 'T1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('FL003', 'CZ3108', 'B-003A', 'SZX', 'CKG', '2024-06-25 15:20:00', '2024-06-25 17:30:00', 750.00, 280, 'scheduled', 'C15', 'T3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('FL004', 'MU8866', 'B-004B', 'PVG', 'XIY', '2024-06-28 09:15:00', '2024-06-28 12:00:00', 920.00, 140, 'scheduled', 'D20', 'T1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('FL005', 'CA9527', 'B-005B', 'HGH', 'NKG', '2024-06-30 16:45:00', '2024-06-30 18:15:00', 420.00, 280, 'scheduled', 'E05', 'T2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入示例订单数据（与OrderManagementModule中的数据保持一致）
INSERT OR REPLACE INTO orders VALUES 
('ORD1001', 'user1', 'FL001', '张三', '110101199001011234', '12A', 1580.00, '2024-06-10 14:30:00', 'paid', 'completed', 'alipay', '2024-06-10 15:00:00'),
('ORD1002', 'user1', 'FL002', '李四', '110101199001015678', '8F', 890.00, '2024-06-18 09:20:00', 'pending', 'active', NULL, NULL),
('ORD1003', 'user2', 'FL003', '王五', '110101199001019999', '15C', 750.00, '2024-06-20 11:45:00', 'refunded', 'cancelled', 'wechat', '2024-06-20 12:00:00');

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_flights_departure_time ON flights(departure_time);
CREATE INDEX IF NOT EXISTS idx_flights_route ON flights(departure_airport, arrival_airport);
CREATE INDEX IF NOT EXISTS idx_flights_number ON flights(flight_number);
CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_flight ON orders(flight_id);
CREATE INDEX IF NOT EXISTS idx_seats_flight ON seats(flight_id);
