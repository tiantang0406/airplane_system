import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class RescheduleUpgradeModule extends JFrame {

    // 数据库连接常量
    private static final String DB_URL = "jdbc:sqlite:airplane_system.db";
    
    private String orderId;
    private String currentUser;
    private JComboBox<String> originalFlightCombo;
    private JComboBox<String> targetFlightCombo;
    private JComboBox<String> classCombo;
    private JButton checkBtn;
    private JButton confirmBtn;
    private JLabel resultLabel;
    private double priceDifference = 0;    public RescheduleUpgradeModule(String orderId, String currentUser) {
        this.orderId = orderId;
        this.currentUser = currentUser;

        setTitle("改期/升舱");
        setSize(750, 600);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 设置全局字体
        Font largerFont = new Font("微软雅黑", Font.PLAIN, 16);
        Font titleFont = new Font("微软雅黑", Font.BOLD, 27);
        UIManager.put("Label.font", largerFont);
        UIManager.put("Button.font", largerFont);
        UIManager.put("ComboBox.font", largerFont);

        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel titleLabel = new JLabel("改期/升舱申请", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // 原航班选择
        JLabel originalLabel = new JLabel("选择原航班:");
        originalLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(originalLabel, gbc);

        // 从数据库加载用户的订单
        List<String> userOrders = DatabaseManager.getUserOrderFlights(currentUser);
        originalFlightCombo = new JComboBox<>(userOrders.toArray(new String[0]));
        originalFlightCombo.setFont(largerFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(originalFlightCombo, gbc);        // 选择目标航班
        JLabel flightLabel = new JLabel("选择目标航班:");
        flightLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        mainPanel.add(flightLabel, gbc);

        // 从数据库加载可用航班
        List<String> availableFlights = DatabaseManager.getAvailableFlights();
        targetFlightCombo = new JComboBox<>(availableFlights.toArray(new String[0]));
        targetFlightCombo.setFont(largerFont);
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(targetFlightCombo, gbc);

        // 选择舱位（移除了超级经济舱）
        JLabel classLabel = new JLabel("选择舱位:");
        classLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        mainPanel.add(classLabel, gbc);

        classCombo = new JComboBox<>(new String[]{"经济舱", "商务舱", "头等舱"});
        classCombo.setFont(largerFont);
        gbc.gridx = 1;
        gbc.gridy = 3;
        mainPanel.add(classCombo, gbc);

        // 检查可用性按钮
        checkBtn = new JButton("检查可用性");
        checkBtn.setFont(largerFont);
        checkBtn.setBackground(new Color(100, 181, 246)); // 蓝色
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(checkBtn, gbc);

        // 结果显示
        resultLabel = new JLabel(" ", SwingConstants.CENTER);
        resultLabel.setFont(largerFont);
        resultLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        mainPanel.add(resultLabel, gbc);

        // 底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);

        confirmBtn = new JButton("确认改签");
        confirmBtn.setFont(largerFont);
        confirmBtn.setBackground(new Color(76, 175, 80)); // 绿色
        confirmBtn.setEnabled(false);

        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(largerFont);

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);

        // 组装界面
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 事件处理
        checkBtn.addActionListener(this::checkAvailability);
        confirmBtn.addActionListener(this::processReschedule);
        cancelBtn.addActionListener(e -> dispose());

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }    private void checkAvailability(ActionEvent e) {
        String originalFlight = (String) originalFlightCombo.getSelectedItem();
        String targetFlight = (String) targetFlightCombo.getSelectedItem();
        String targetClass = (String) classCombo.getSelectedItem();

        if (originalFlight == null || targetFlight == null) {
            JOptionPane.showMessageDialog(this, "请选择原航班和目标航班", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (originalFlight.equals(targetFlight)) {
            JOptionPane.showMessageDialog(this, "原航班和目标航班不能相同", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 从航班字符串中提取航班号
        String originalFlightNumber = extractFlightNumber(originalFlight);
        String targetFlightNumber = extractFlightNumber(targetFlight);

        // 检查可用性和计算差价
        AvailabilityResult result = checkAvailabilityFromDatabase(originalFlightNumber, targetFlightNumber, targetClass);

        if (result.isAvailable) {
            priceDifference = result.priceDifference;
            String message = String.format(
                    "<html><center>原航班: %s<br>目标航班: %s<br>舱位: %s<br>%s: <b>¥%.2f</b><br>余票: %d张</center></html>",
                    originalFlight,
                    targetFlight,
                    targetClass,
                    result.priceDifference >= 0 ? "需补差价" : "可退款",
                    Math.abs(result.priceDifference),
                    result.remainingSeats);

            resultLabel.setText(message);
            resultLabel.setForeground(result.priceDifference >= 0 ?
                    new Color(220, 0, 0) : new Color(0, 100, 0));
            confirmBtn.setEnabled(true);
            confirmBtn.setText(result.priceDifference >= 0 ? "支付并改签" : "确认改签");
        } else {
            resultLabel.setText("<html><center><b>所选航班/舱位无余量</b></center></html>");
            resultLabel.setForeground(Color.RED);
            confirmBtn.setEnabled(false);
        }
    }

    /**
     * 从航班信息字符串中提取航班号
     */
    private String extractFlightNumber(String flightInfo) {
        if (flightInfo != null && !flightInfo.isEmpty()) {
            return flightInfo.split(" ")[0]; // 获取第一个空格前的航班号
        }
        return "";
    }

    /**
     * 从数据库检查可用性
     */
    private AvailabilityResult checkAvailabilityFromDatabase(String originalFlightNumber, String targetFlightNumber, String targetClass) {
        AvailabilityResult result = new AvailabilityResult();
        
        // 获取原航班价格
        double originalPrice = DatabaseManager.getFlightBasePrice(originalFlightNumber);
        
        // 检查目标航班可用性和价格
        AvailabilityInfo targetInfo = DatabaseManager.checkFlightAvailability(targetFlightNumber, targetClass);
        
        if (targetInfo != null && targetInfo.isAvailable) {
            result.isAvailable = true;
            result.remainingSeats = targetInfo.remainingSeats;
            result.priceDifference = targetInfo.finalPrice - originalPrice;
        } else {
            result.isAvailable = false;
            result.remainingSeats = 0;
            result.priceDifference = 0;
        }
        
        return result;
    }

    private void processReschedule(ActionEvent e) {
        String originalFlight = (String) originalFlightCombo.getSelectedItem();
        String targetFlight = (String) targetFlightCombo.getSelectedItem();
        String targetClass = (String) classCombo.getSelectedItem();

        if (priceDifference > 0) {
            // 需要支付差价
            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("<html><center>需支付差价: ¥%.2f<br>确认支付并改签?</center></html>", priceDifference),
                    "确认支付", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean paymentSuccess = simulatePayment(priceDifference);
                if (paymentSuccess) {
                    completeReschedule(originalFlight, targetFlight, targetClass);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "支付失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            // 无需支付或退款
            completeReschedule(originalFlight, targetFlight, targetClass);
        }
    }

    private void completeReschedule(String originalFlight, String targetFlight, String targetClass) {
        // 模拟改签操作
        boolean success = simulateReschedule(originalFlight, targetFlight, targetClass);

        if (success) {
            // 生成改签确认单
            String confirmation = generateConfirmation(originalFlight, targetFlight, targetClass);
            JOptionPane.showMessageDialog(this,
                    "<html><center>改签成功!<br><br>" + confirmation + "</center></html>",
                    "改签完成", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "改签处理失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }    // 模拟支付
    private boolean simulatePayment(double amount) {
        System.out.println("模拟支付: ¥" + amount);
        return true; // 模拟总是成功
    }

    // 模拟改签操作
    private boolean simulateReschedule(String originalFlight, String targetFlight, String targetClass) {
        System.out.println("改签操作 - 原航班: " + originalFlight +
                ", 新航班: " + targetFlight +
                ", 舱位: " + targetClass);
        return true; // 模拟总是成功
    }

    // 生成改签确认单
    private String generateConfirmation(String originalFlight, String targetFlight, String targetClass) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return String.format(
                "改签确认单<br>" +
                        "原航班: %s<br>" +
                        "新航班: %s<br>" +
                        "舱位: %s<br>" +
                        "处理时间: %s<br>" +
                        "差价: ¥%.2f",
                originalFlight,
                targetFlight,
                targetClass,
                sdf.format(new Date()),
                priceDifference);
    }    // 可用性结果内部类
    private class AvailabilityResult {
        boolean isAvailable;
        int remainingSeats;
        double priceDifference;
    }

    /**
     * 数据库管理器 - 处理改期相关的数据库操作
     */
    public static class DatabaseManager {
        static {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                System.err.println("SQLite JDBC Driver not found.");
                e.printStackTrace();
            }
        }

        /**
         * 获取数据库连接
         */
        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(DB_URL);
        }        /**
         * 获取用户的订单航班列表
         */
        public static List<String> getUserOrderFlights(String userId) {
            List<String> flights = new ArrayList<>();
            
            // 根据用户角色决定查询范围
            String sql;
            boolean isNormalUser = isNormalUser(userId);
            
            if (isNormalUser) {
                // 普通用户只能查看自己的订单
                sql = "SELECT DISTINCT o.order_id, f.flight_number, " +
                      "ap1.city || '-' || ap2.city as route, " +
                      "DATE(f.departure_time) as dep_date " +
                      "FROM orders o " +
                      "JOIN flights f ON o.flight_id = f.flight_id " +
                      "JOIN airports ap1 ON f.departure_airport = ap1.airport_code " +
                      "JOIN airports ap2 ON f.arrival_airport = ap2.airport_code " +
                      "WHERE o.user_id = ? AND o.payment_status = 'paid' " +
                      "AND o.order_status = 'active' " +
                      "ORDER BY f.departure_time";
            } else {
                // 客服和管理员可以查看所有订单
                sql = "SELECT DISTINCT o.order_id, f.flight_number, " +
                      "ap1.city || '-' || ap2.city as route, " +
                      "DATE(f.departure_time) as dep_date, o.user_id " +
                      "FROM orders o " +
                      "JOIN flights f ON o.flight_id = f.flight_id " +
                      "JOIN airports ap1 ON f.departure_airport = ap1.airport_code " +
                      "JOIN airports ap2 ON f.arrival_airport = ap2.airport_code " +
                      "WHERE o.payment_status = 'paid' " +
                      "AND o.order_status = 'active' " +
                      "ORDER BY f.departure_time";
            }

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                if (isNormalUser) {
                    stmt.setString(1, userId);
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String flightInfo;
                        if (isNormalUser) {
                            flightInfo = String.format("%s %s %s",
                                rs.getString("flight_number"),
                                rs.getString("route"),
                                rs.getString("dep_date"));
                        } else {
                            // 对于客服和管理员，显示订单所属用户
                            flightInfo = String.format("%s %s %s (用户:%s)",
                                rs.getString("flight_number"),
                                rs.getString("route"),
                                rs.getString("dep_date"),
                                rs.getString("user_id"));
                        }
                        flights.add(flightInfo);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("获取用户订单航班失败: " + e.getMessage());
            }

            return flights;
        }

        /**
         * 获取可用的目标航班列表
         */
        public static List<String> getAvailableFlights() {
            List<String> flights = new ArrayList<>();
            String sql = "SELECT f.flight_number, " +
                        "ap1.city || '-' || ap2.city as route, " +
                        "DATE(f.departure_time) as dep_date " +
                        "FROM flights f " +
                        "JOIN airports ap1 ON f.departure_airport = ap1.airport_code " +
                        "JOIN airports ap2 ON f.arrival_airport = ap2.airport_code " +
                        "WHERE f.status = 'scheduled' " +
                        "AND f.available_seats > 0 " +
                        "AND datetime(f.departure_time) > datetime('now') " +
                        "ORDER BY f.departure_time";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String flightInfo = String.format("%s %s %s",
                            rs.getString("flight_number"),
                            rs.getString("route"),
                            rs.getString("dep_date"));
                        flights.add(flightInfo);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("获取可用航班失败: " + e.getMessage());
            }

            return flights;
        }

        /**
         * 获取航班的基础价格
         */
        public static double getFlightBasePrice(String flightNumber) {
            String sql = "SELECT base_price FROM flights WHERE flight_number = ?";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, flightNumber);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("base_price");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("获取航班价格失败: " + e.getMessage());
            }
            
            return 0.0;
        }

        /**
         * 检查航班可用性
         */
        public static AvailabilityInfo checkFlightAvailability(String flightNumber, String seatClass) {
            String sql = "SELECT f.available_seats, f.base_price, a.first_class_seats, " +
                        "a.business_class_seats, a.economy_class_seats " +
                        "FROM flights f " +
                        "JOIN aircraft a ON f.aircraft_id = a.aircraft_id " +
                        "WHERE f.flight_number = ? AND f.status = 'scheduled'";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, flightNumber);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        AvailabilityInfo info = new AvailabilityInfo();
                        info.isAvailable = rs.getInt("available_seats") > 0;
                        info.basePrice = rs.getDouble("base_price");
                        
                        // 根据舱位类型计算价格
                        switch (seatClass) {
                            case "头等舱":
                                info.finalPrice = info.basePrice * 3.5; // 头等舱倍数
                                info.remainingSeats = rs.getInt("first_class_seats");
                                break;
                            case "商务舱":
                                info.finalPrice = info.basePrice * 2.5; // 商务舱倍数
                                info.remainingSeats = rs.getInt("business_class_seats");
                                break;
                            default: // 经济舱
                                info.finalPrice = info.basePrice;
                                info.remainingSeats = rs.getInt("economy_class_seats");
                                break;
                        }
                        
                        // 模拟剩余座位（实际应该查询orders表）
                        info.remainingSeats = Math.max(1, (int)(Math.random() * info.remainingSeats));
                        
                        return info;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("检查航班可用性失败: " + e.getMessage());
            }
            
            return null;
        }

        /**
         * 判断是否为普通用户（需要权限限制）
         */
        public static boolean isNormalUser(String userId) {
            // 检查用户角色
            String sql = "SELECT role FROM users WHERE username = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String role = rs.getString("role");
                        // 只有角色为"用户"的才需要限制，客服和管理员不需要限制
                        return "用户".equals(role);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("获取用户角色失败: " + e.getMessage());
            }
            // 默认当作普通用户处理（安全考虑）
            return true;
        }
    }

    /**
     * 航班可用性信息类
     */
    public static class AvailabilityInfo {
        public boolean isAvailable;
        public double basePrice;
        public double finalPrice;
        public int remainingSeats;
    }    public static void main(String[] args) {
        // 测试不同用户角色的权限控制
        System.out.println("测试改期/升舱模块的权限控制:");
        System.out.println("1. user1 (用户) - 只能查看自己的订单");
        System.out.println("2. admin (管理员) - 可以查看任何订单，显示订单所属用户");
        System.out.println("3. user2 (客服) - 可以查看任何订单，显示订单所属用户");
        
        SwingUtilities.invokeLater(() -> {
            // 可以修改这里测试不同用户角色
            // new RescheduleUpgradeModule("ORD1001", "user1").setVisible(true);  // 普通用户
            new RescheduleUpgradeModule("ORD1001", "admin").setVisible(true);   // 管理员
            // new RescheduleUpgradeModule("ORD1001", "user2").setVisible(true);   // 客服
        });
    }
}
