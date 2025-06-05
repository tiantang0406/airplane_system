import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseTest {
    // 使用相对路径
    private static final String DB_URL = "jdbc:sqlite:airplane_system.db";
    
    public static void main(String[] args) {
        System.out.println("开始测试数据库连接...");
        System.out.println("当前工作目录: " + System.getProperty("user.dir"));
        System.out.println("数据库URL: " + DB_URL);
        
        // 检查数据库文件是否存在
        java.io.File dbFile = new java.io.File("airplane_system.db");
        System.out.println("数据库文件完整路径: " + dbFile.getAbsolutePath());
        System.out.println("数据库文件是否存在: " + dbFile.exists());
        
        // 测试1: 加载SQLite JDBC驱动
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("✓ SQLite JDBC驱动加载成功");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ SQLite JDBC驱动未找到: " + e.getMessage());
            return;
        }
        
        // 测试2: 数据库连接
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            System.out.println("✓ 数据库连接成功");
            
            // 测试3: 查询表
            String sql = "SELECT name FROM sqlite_master WHERE type='table'";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                System.out.println("数据库中的表:");
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("name"));
                }
            }
            
            // 测试4: 查询航班数据
            sql = "SELECT COUNT(*) as count FROM flights";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("✓ 航班表中有 " + count + " 条记录");
                }
            }
            
            // 测试5: 查询机场数据
            sql = "SELECT COUNT(*) as count FROM airports";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("✓ 机场表中有 " + count + " 条记录");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("✗ 数据库操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
