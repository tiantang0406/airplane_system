import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.File;
import java.util.Scanner;

/**
 * 数据库重建工具
 * 功能：
 * 1. 删除现有数据库文件
 * 2. 重新创建数据库
 * 3. 执行SQL脚本创建表结构
 * 4. 插入示例数据
 * 5. 验证数据库完整性
 */
public class DatabaseRebuildTool {
    
    private static final String DB_FILE = "airplane_system.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;
    
    public static void main(String[] args) {
        DatabaseRebuildTool tool = new DatabaseRebuildTool();
        
        System.out.println("=== 航班系统数据库重建工具 ===");
        System.out.println("当前工作目录: " + System.getProperty("user.dir"));
        System.out.println();
        
        // 显示选项菜单
        Scanner scanner = new Scanner(System.in);
        System.out.println("请选择操作：");
        System.out.println("1. 完全重建数据库（删除现有数据库并重新创建）");
        System.out.println("2. 仅重建表结构（保留数据库文件，重建表）");
        System.out.println("3. 仅插入示例数据");
        System.out.println("4. 验证数据库完整性");
        System.out.println("5. 退出");
        System.out.print("请输入选项 (1-5): ");
        
        int choice = scanner.nextInt();
        
        try {
            switch (choice) {
                case 1:
                    tool.fullRebuild();
                    break;
                case 2:
                    tool.rebuildTables();
                    break;
                case 3:
                    tool.insertSampleData();
                    break;
                case 4:
                    tool.validateDatabase();
                    break;
                case 5:
                    System.out.println("退出程序。");
                    return;
                default:
                    System.out.println("无效选项。");
                    return;
            }
        } finally {
            scanner.close();
        }
    }
    
    /**
     * 完全重建数据库
     */
    public void fullRebuild() {
        System.out.println("\n=== 开始完全重建数据库 ===");
        
        // 1. 删除现有数据库文件
        deleteDatabase();
        
        // 2. 重新创建数据库
        createDatabase();
        
        // 3. 执行SQL脚本
        executeSQL();
        
        // 4. 验证结果
        validateDatabase();
        
        System.out.println("=== 数据库重建完成 ===");
    }
    
    /**
     * 仅重建表结构
     */
    public void rebuildTables() {
        System.out.println("\n=== 开始重建表结构 ===");
        
        // 检查数据库文件是否存在
        File dbFile = new File(DB_FILE);
        if (!dbFile.exists()) {
            System.out.println("数据库文件不存在，将创建新数据库。");
            createDatabase();
        }
        
        // 执行SQL脚本
        executeSQL();
        
        // 验证结果
        validateDatabase();
        
        System.out.println("=== 表结构重建完成 ===");
    }
    
    /**
     * 删除现有数据库文件
     */
    private void deleteDatabase() {
        File dbFile = new File(DB_FILE);
        if (dbFile.exists()) {
            if (dbFile.delete()) {
                System.out.println("✓ 已删除现有数据库文件: " + dbFile.getAbsolutePath());
            } else {
                System.err.println("✗ 无法删除数据库文件: " + dbFile.getAbsolutePath());
                return;
            }
        } else {
            System.out.println("! 数据库文件不存在，跳过删除步骤");
        }
    }
    
    /**
     * 创建新数据库
     */
    private void createDatabase() {
        try {
            // 加载SQLite JDBC驱动
            Class.forName("org.sqlite.JDBC");
            System.out.println("✓ SQLite JDBC驱动加载成功");
            
            // 创建数据库连接（如果数据库不存在会自动创建）
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                if (conn != null) {
                    System.out.println("✓ 数据库创建成功: " + new File(DB_FILE).getAbsolutePath());
                }
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("✗ SQLite JDBC驱动未找到: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("✗ 数据库创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行SQL脚本
     */
    private void executeSQL() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("开始创建表结构...");
            
            // 创建表结构
            createTables(stmt);
            
            // 插入基础数据
            insertBaseData(stmt);
            
            System.out.println("✓ 数据库结构和数据创建完成");
            
        } catch (SQLException e) {
            System.err.println("✗ 执行SQL失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建表结构
     */
    private void createTables(Statement stmt) throws SQLException {
        // 1. 机场信息表
        System.out.println("  ✓ 创建表: airports");
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS airports (
                airport_code TEXT PRIMARY KEY,
                airport_name TEXT NOT NULL,
                city TEXT NOT NULL,
                country TEXT NOT NULL DEFAULT '中国',
                timezone TEXT DEFAULT 'Asia/Shanghai',
                status TEXT DEFAULT 'active'
            )
            """);
        
        // 2. 飞机信息表
        System.out.println("  ✓ 创建表: aircraft");
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS aircraft (
                aircraft_id TEXT PRIMARY KEY,
                aircraft_type TEXT NOT NULL,
                total_seats INTEGER NOT NULL,
                first_class_seats INTEGER DEFAULT 0,
                business_class_seats INTEGER DEFAULT 0,
                economy_class_seats INTEGER NOT NULL,
                manufacturer TEXT,
                year_manufactured INTEGER,
                status TEXT DEFAULT 'active'
            )
            """);
        
        // 3. 航班信息表
        System.out.println("  ✓ 创建表: flights");
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS flights (
                flight_id TEXT PRIMARY KEY,
                flight_number TEXT NOT NULL,
                aircraft_id TEXT NOT NULL,
                departure_airport TEXT NOT NULL,
                arrival_airport TEXT NOT NULL,
                departure_time DATETIME NOT NULL,
                arrival_time DATETIME NOT NULL,
                base_price DECIMAL(10,2) NOT NULL,
                available_seats INTEGER NOT NULL,
                status TEXT DEFAULT 'scheduled',
                gate TEXT,
                terminal TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (aircraft_id) REFERENCES aircraft(aircraft_id),
                FOREIGN KEY (departure_airport) REFERENCES airports(airport_code),
                FOREIGN KEY (arrival_airport) REFERENCES airports(airport_code)
            )
            """);
        
        // 4. 座位信息表
        System.out.println("  ✓ 创建表: seats");
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS seats (
                seat_id TEXT PRIMARY KEY,
                flight_id TEXT NOT NULL,
                seat_number TEXT NOT NULL,
                seat_class TEXT NOT NULL,
                is_occupied BOOLEAN DEFAULT FALSE,
                passenger_name TEXT,
                booking_time DATETIME,
                FOREIGN KEY (flight_id) REFERENCES flights(flight_id),
                UNIQUE(flight_id, seat_number)
            )
            """);
        
        // 5. 订单表
        System.out.println("  ✓ 创建表: orders");
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS orders (
                order_id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                flight_id TEXT NOT NULL,
                passenger_name TEXT NOT NULL,
                passenger_id TEXT NOT NULL,
                seat_number TEXT,
                ticket_price DECIMAL(10,2) NOT NULL,
                booking_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                payment_status TEXT DEFAULT 'pending',
                order_status TEXT DEFAULT 'active',
                payment_method TEXT,
                payment_time DATETIME,
                FOREIGN KEY (flight_id) REFERENCES flights(flight_id)
            )
            """);
        
        // 6. 退票记录表
        System.out.println("  ✓ 创建表: refunds");
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS refunds (
                refund_id TEXT PRIMARY KEY,
                order_id TEXT NOT NULL,
                refund_reason TEXT,
                refund_amount DECIMAL(10,2) NOT NULL,
                refund_fee DECIMAL(10,2) DEFAULT 0,
                refund_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                refund_status TEXT DEFAULT 'processing',
                processed_by TEXT,
                FOREIGN KEY (order_id) REFERENCES orders(order_id)
            )
            """);
        
        // 创建索引
        System.out.println("  ✓ 创建索引");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_flights_departure_time ON flights(departure_time)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_flights_route ON flights(departure_airport, arrival_airport)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_flights_number ON flights(flight_number)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_flight ON orders(flight_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_seats_flight ON seats(flight_id)");
    }
    
    /**
     * 插入基础数据
     */
    private void insertBaseData(Statement stmt) throws SQLException {
        System.out.println("开始插入基础数据...");
        
        // 插入机场信息
        System.out.println("  ✓ 插入机场数据");
        stmt.execute("INSERT OR REPLACE INTO airports VALUES ('PEK', '北京首都国际机场', '北京', '中国', 'Asia/Shanghai', 'active')");
        stmt.execute("INSERT OR REPLACE INTO airports VALUES ('SHA', '上海虹桥国际机场', '上海', '中国', 'Asia/Shanghai', 'active')");
        stmt.execute("INSERT OR REPLACE INTO airports VALUES ('PVG', '上海浦东国际机场', '上海', '中国', 'Asia/Shanghai', 'active')");
        stmt.execute("INSERT OR REPLACE INTO airports VALUES ('CAN', '广州白云国际机场', '广州', '中国', 'Asia/Shanghai', 'active')");
        stmt.execute("INSERT OR REPLACE INTO airports VALUES ('SZX', '深圳宝安国际机场', '深圳', '中国', 'Asia/Shanghai', 'active')");
        stmt.execute("INSERT OR REPLACE INTO airports VALUES ('CTU', '成都天府国际机场', '成都', '中国', 'Asia/Shanghai', 'active')");
        stmt.execute("INSERT OR REPLACE INTO airports VALUES ('CKG', '重庆江北国际机场', '重庆', '中国', 'Asia/Shanghai', 'active')");
        stmt.execute("INSERT OR REPLACE INTO airports VALUES ('XIY', '西安咸阳国际机场', '西安', '中国', 'Asia/Shanghai', 'active')");
        stmt.execute("INSERT OR REPLACE INTO airports VALUES ('HGH', '杭州萧山国际机场', '杭州', '中国', 'Asia/Shanghai', 'active')");
        stmt.execute("INSERT OR REPLACE INTO airports VALUES ('NKG', '南京禄口国际机场', '南京', '中国', 'Asia/Shanghai', 'active')");
        
        // 插入飞机信息
        System.out.println("  ✓ 插入飞机数据");
        stmt.execute("INSERT OR REPLACE INTO aircraft VALUES ('B-001A', 'A320', 180, 8, 24, 148, 'Airbus', 2018, 'active')");
        stmt.execute("INSERT OR REPLACE INTO aircraft VALUES ('B-002A', 'A330', 290, 12, 36, 242, 'Airbus', 2019, 'active')");
        stmt.execute("INSERT OR REPLACE INTO aircraft VALUES ('B-003A', 'A350', 350, 16, 48, 286, 'Airbus', 2020, 'active')");
        stmt.execute("INSERT OR REPLACE INTO aircraft VALUES ('B-004B', 'B737', 160, 8, 20, 132, 'Boeing', 2017, 'active')");
        stmt.execute("INSERT OR REPLACE INTO aircraft VALUES ('B-005B', 'B777', 320, 16, 40, 264, 'Boeing', 2019, 'active')");
        stmt.execute("INSERT OR REPLACE INTO aircraft VALUES ('B-006B', 'B787', 280, 12, 32, 236, 'Boeing', 2020, 'active')");
        stmt.execute("INSERT OR REPLACE INTO aircraft VALUES ('B-007A', 'A320', 180, 8, 24, 148, 'Airbus', 2021, 'active')");
        stmt.execute("INSERT OR REPLACE INTO aircraft VALUES ('B-008B', 'B737', 160, 8, 20, 132, 'Boeing', 2018, 'active')");
        
        // 插入航班数据（使用未来日期）
        System.out.println("  ✓ 插入航班数据");
        stmt.execute("INSERT OR REPLACE INTO flights VALUES ('FL001', 'MU5112', 'B-001A', 'PEK', 'SHA', '2025-06-15 08:00:00', '2025-06-15 10:30:00', 580.00, 150, 'scheduled', 'A12', 'T2', datetime('now'), datetime('now'))");
        stmt.execute("INSERT OR REPLACE INTO flights VALUES ('FL002', 'CA1833', 'B-002A', 'CAN', 'CTU', '2025-06-20 12:30:00', '2025-06-20 14:45:00', 890.00, 200, 'scheduled', 'B08', 'T1', datetime('now'), datetime('now'))");
        stmt.execute("INSERT OR REPLACE INTO flights VALUES ('FL003', 'CZ3108', 'B-003A', 'SZX', 'CKG', '2025-06-25 15:20:00', '2025-06-25 17:30:00', 750.00, 280, 'scheduled', 'C15', 'T3', datetime('now'), datetime('now'))");
        stmt.execute("INSERT OR REPLACE INTO flights VALUES ('FL004', 'MU8866', 'B-004B', 'PVG', 'XIY', '2025-06-28 09:15:00', '2025-06-28 12:00:00', 920.00, 140, 'scheduled', 'D20', 'T1', datetime('now'), datetime('now'))");
        stmt.execute("INSERT OR REPLACE INTO flights VALUES ('FL005', 'CA9527', 'B-005B', 'HGH', 'NKG', '2025-06-30 16:45:00', '2025-06-30 18:15:00', 420.00, 280, 'scheduled', 'E05', 'T2', datetime('now'), datetime('now'))");
        
        // 添加更多未来日期的航班
        stmt.execute("INSERT OR REPLACE INTO flights VALUES ('FL006', 'MU1234', 'B-006B', 'PEK', 'CAN', '2025-07-05 07:30:00', '2025-07-05 10:45:00', 1200.00, 250, 'scheduled', 'F10', 'T2', datetime('now'), datetime('now'))");
        stmt.execute("INSERT OR REPLACE INTO flights VALUES ('FL007', 'CA5678', 'B-007A', 'SHA', 'CTU', '2025-07-10 14:20:00', '2025-07-10 17:10:00', 980.00, 160, 'scheduled', 'G08', 'T1', datetime('now'), datetime('now'))");
        stmt.execute("INSERT OR REPLACE INTO flights VALUES ('FL008', 'CZ9999', 'B-008B', 'PVG', 'SZX', '2025-07-15 19:00:00', '2025-07-15 21:30:00', 780.00, 130, 'scheduled', 'H12', 'T2', datetime('now'), datetime('now'))");
        
        // 插入示例订单数据
        System.out.println("  ✓ 插入订单数据");
        stmt.execute("INSERT OR REPLACE INTO orders VALUES ('ORD1001', 'user1', 'FL001', '张三', '110101199001011234', '12A', 1580.00, '2025-06-10 14:30:00', 'paid', 'completed', 'alipay', '2025-06-10 15:00:00')");
        stmt.execute("INSERT OR REPLACE INTO orders VALUES ('ORD1002', 'user1', 'FL002', '李四', '110101199001015678', '8F', 890.00, '2025-06-18 09:20:00', 'pending', 'active', NULL, NULL)");
        stmt.execute("INSERT OR REPLACE INTO orders VALUES ('ORD1003', 'user2', 'FL003', '王五', '110101199001019999', '15C', 750.00, '2025-06-20 11:45:00', 'refunded', 'cancelled', 'wechat', '2025-06-20 12:00:00')");
    }
    
    /**
     * 仅插入示例数据
     */
    public void insertSampleData() {
        System.out.println("\n=== 开始插入示例数据 ===");
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // 清除现有数据
            System.out.println("清除现有数据...");
            stmt.execute("DELETE FROM refunds");
            stmt.execute("DELETE FROM orders");
            stmt.execute("DELETE FROM seats");
            stmt.execute("DELETE FROM flights");
            stmt.execute("DELETE FROM aircraft");
            stmt.execute("DELETE FROM airports");
            
            // 插入机场数据
            System.out.println("插入机场数据...");
            String[] airportInserts = {
                "INSERT INTO airports VALUES ('PEK', '北京首都国际机场', '北京', '中国', 'Asia/Shanghai', 'active')",
                "INSERT INTO airports VALUES ('SHA', '上海虹桥国际机场', '上海', '中国', 'Asia/Shanghai', 'active')",
                "INSERT INTO airports VALUES ('PVG', '上海浦东国际机场', '上海', '中国', 'Asia/Shanghai', 'active')",
                "INSERT INTO airports VALUES ('CAN', '广州白云国际机场', '广州', '中国', 'Asia/Shanghai', 'active')",
                "INSERT INTO airports VALUES ('SZX', '深圳宝安国际机场', '深圳', '中国', 'Asia/Shanghai', 'active')",
                "INSERT INTO airports VALUES ('CTU', '成都天府国际机场', '成都', '中国', 'Asia/Shanghai', 'active')",
                "INSERT INTO airports VALUES ('CKG', '重庆江北国际机场', '重庆', '中国', 'Asia/Shanghai', 'active')",
                "INSERT INTO airports VALUES ('XIY', '西安咸阳国际机场', '西安', '中国', 'Asia/Shanghai', 'active')",
                "INSERT INTO airports VALUES ('HGH', '杭州萧山国际机场', '杭州', '中国', 'Asia/Shanghai', 'active')",
                "INSERT INTO airports VALUES ('NKG', '南京禄口国际机场', '南京', '中国', 'Asia/Shanghai', 'active')"
            };
            
            for (String sql : airportInserts) {
                stmt.execute(sql);
            }
            
            // 插入飞机数据
            System.out.println("插入飞机数据...");
            String[] aircraftInserts = {
                "INSERT INTO aircraft VALUES ('B-001A', 'A320', 180, 8, 24, 148, 'Airbus', 2018, 'active')",
                "INSERT INTO aircraft VALUES ('B-002A', 'A330', 290, 12, 36, 242, 'Airbus', 2019, 'active')",
                "INSERT INTO aircraft VALUES ('B-003A', 'A350', 350, 16, 48, 286, 'Airbus', 2020, 'active')",
                "INSERT INTO aircraft VALUES ('B-004B', 'B737', 160, 8, 20, 132, 'Boeing', 2017, 'active')",
                "INSERT INTO aircraft VALUES ('B-005B', 'B777', 320, 16, 40, 264, 'Boeing', 2019, 'active')",
                "INSERT INTO aircraft VALUES ('B-006B', 'B787', 280, 12, 32, 236, 'Boeing', 2020, 'active')",
                "INSERT INTO aircraft VALUES ('B-007A', 'A320', 180, 8, 24, 148, 'Airbus', 2021, 'active')",
                "INSERT INTO aircraft VALUES ('B-008B', 'B737', 160, 8, 20, 132, 'Boeing', 2018, 'active')"
            };
            
            for (String sql : aircraftInserts) {
                stmt.execute(sql);
            }
            
            // 插入航班数据（使用未来日期）
            System.out.println("插入航班数据...");
            String[] flightInserts = {
                "INSERT INTO flights VALUES ('FL001', 'MU5112', 'B-001A', 'PEK', 'SHA', '2025-06-15 08:00:00', '2025-06-15 10:30:00', 580.00, 150, 'scheduled', 'A12', 'T2', datetime('now'), datetime('now'))",
                "INSERT INTO flights VALUES ('FL002', 'CA1833', 'B-002A', 'CAN', 'CTU', '2025-06-20 12:30:00', '2025-06-20 14:45:00', 890.00, 200, 'scheduled', 'B08', 'T1', datetime('now'), datetime('now'))",
                "INSERT INTO flights VALUES ('FL003', 'CZ3108', 'B-003A', 'SZX', 'CKG', '2025-06-25 15:20:00', '2025-06-25 17:30:00', 750.00, 280, 'scheduled', 'C15', 'T3', datetime('now'), datetime('now'))",
                "INSERT INTO flights VALUES ('FL004', 'MU8866', 'B-004B', 'PVG', 'XIY', '2025-06-28 09:15:00', '2025-06-28 12:00:00', 920.00, 140, 'scheduled', 'D20', 'T1', datetime('now'), datetime('now'))",
                "INSERT INTO flights VALUES ('FL005', 'CA9527', 'B-005B', 'HGH', 'NKG', '2025-06-30 16:45:00', '2025-06-30 18:15:00', 420.00, 280, 'scheduled', 'E05', 'T2', datetime('now'), datetime('now'))",
                // 添加更多未来日期的航班
                "INSERT INTO flights VALUES ('FL006', 'MU1234', 'B-006B', 'PEK', 'CAN', '2025-07-05 07:30:00', '2025-07-05 10:45:00', 1200.00, 250, 'scheduled', 'F10', 'T2', datetime('now'), datetime('now'))",
                "INSERT INTO flights VALUES ('FL007', 'CA5678', 'B-007A', 'SHA', 'CTU', '2025-07-10 14:20:00', '2025-07-10 17:10:00', 980.00, 160, 'scheduled', 'G08', 'T1', datetime('now'), datetime('now'))",
                "INSERT INTO flights VALUES ('FL008', 'CZ9999', 'B-008B', 'PVG', 'SZX', '2025-07-15 19:00:00', '2025-07-15 21:30:00', 780.00, 130, 'scheduled', 'H12', 'T2', datetime('now'), datetime('now'))"
            };
            
            for (String sql : flightInserts) {
                stmt.execute(sql);
            }
            
            System.out.println("✓ 示例数据插入完成");
            
        } catch (SQLException e) {
            System.err.println("✗ 插入示例数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证数据库完整性
     */
    public void validateDatabase() {
        System.out.println("\n=== 开始验证数据库 ===");
        
        File dbFile = new File(DB_FILE);
        if (!dbFile.exists()) {
            System.err.println("✗ 数据库文件不存在");
            return;
        }
        
        System.out.println("✓ 数据库文件存在: " + dbFile.getAbsolutePath());
        System.out.println("  文件大小: " + dbFile.length() + " 字节");
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // 检查表结构
            System.out.println("\n检查表结构:");
            String[] tables = {"airports", "aircraft", "flights", "seats", "orders", "refunds"};
            
            for (String table : tables) {
                try {
                    var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("✓ " + table + " 表: " + count + " 条记录");
                    }
                } catch (SQLException e) {
                    System.err.println("✗ " + table + " 表不存在或有问题: " + e.getMessage());
                }
            }
            
            // 检查外键关系
            System.out.println("\n检查数据完整性:");
            try {
                var rs = stmt.executeQuery(
                    "SELECT f.flight_number, a.aircraft_type, dep.city as dep_city, arr.city as arr_city " +
                    "FROM flights f " +
                    "JOIN aircraft a ON f.aircraft_id = a.aircraft_id " +
                    "JOIN airports dep ON f.departure_airport = dep.airport_code " +
                    "JOIN airports arr ON f.arrival_airport = arr.airport_code " +
                    "LIMIT 5"
                );
                
                int count = 0;
                while (rs.next()) {
                    if (count == 0) {
                        System.out.println("✓ 航班关联数据正常，示例:");
                    }
                    System.out.println("  " + rs.getString("flight_number") + 
                                     " (" + rs.getString("aircraft_type") + ") " +
                                     rs.getString("dep_city") + " -> " + rs.getString("arr_city"));
                    count++;
                }
                
                if (count == 0) {
                    System.out.println("! 没有找到完整的航班数据");
                }
                
            } catch (SQLException e) {
                System.err.println("✗ 数据关联检查失败: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            System.err.println("✗ 数据库连接失败: " + e.getMessage());
        }
        
        System.out.println("\n=== 验证完成 ===");
    }
}
