import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class PaymentModule extends JFrame {

    private double orderAmount;
    private String orderId;
    private String currentUser;
    private JComboBox<String> paymentMethod;
    private JButton confirmBtn;
    private JButton cancelBtn;
    private JLabel orderInfoLabel;
    private JLabel flightInfoLabel;
    
    // 数据库URL
    private static final String DB_URL = "jdbc:sqlite:airplane_system.db";

    public PaymentModule(String orderId, String currentUser) {
        this.orderId = orderId;
        this.currentUser = currentUser;
        
        // 从数据库获取订单信息
        if (!loadOrderFromDatabase()) {
            JOptionPane.showMessageDialog(null, "订单不存在或无权限访问！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setTitle("支付订单 - " + orderId);
        setSize(700, 450);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 设置全局字体（增大3号）
        Font largerFont = new Font("微软雅黑", Font.PLAIN, 19);
        Font titleFont = new Font("微软雅黑", Font.BOLD, 22);

        // 应用全局字体设置
        UIManager.put("Label.font", largerFont);
        UIManager.put("Button.font", largerFont);
        UIManager.put("ComboBox.font", largerFont);
        UIManager.put("OptionPane.messageFont", largerFont);

        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // 订单信息标题
        JLabel titleLabel = new JLabel("支付订单", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        // 订单基本信息
        orderInfoLabel = new JLabel("订单金额: ¥" + String.format("%.2f", orderAmount));
        orderInfoLabel.setFont(titleFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(orderInfoLabel, gbc);
        
        // 航班信息
        flightInfoLabel = new JLabel();
        flightInfoLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainPanel.add(flightInfoLabel, gbc);

        // 支付方式选择
        JLabel methodLabel = new JLabel("选择支付方式:");
        methodLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(methodLabel, gbc);

        paymentMethod = new JComboBox<>(new String[]{"微信支付", "支付宝", "银行卡"});
        paymentMethod.setFont(largerFont);
        paymentMethod.setPreferredSize(new Dimension(200, 35));
        gbc.gridx = 1;
        gbc.gridy = 3;
        mainPanel.add(paymentMethod, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        buttonPanel.setBackground(Color.WHITE);

        confirmBtn = new JButton("确认支付");
        confirmBtn.setFont(largerFont);
        confirmBtn.setPreferredSize(new Dimension(150, 40));
        confirmBtn.setBackground(new Color(76, 175, 80));

        cancelBtn = new JButton("取消支付");
        cancelBtn.setFont(largerFont);
        cancelBtn.setPreferredSize(new Dimension(150, 40));
        cancelBtn.setBackground(new Color(244, 67, 54));

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);

        // 组装界面
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 事件处理
        confirmBtn.addActionListener(this::processPayment);
        cancelBtn.addActionListener(_ -> dispose());

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    /**
     * 从数据库加载订单信息
     */
    private boolean loadOrderFromDatabase() {
        String sql = "SELECT o.order_id, o.user_id, o.flight_id, o.passenger_name, " +
                    "o.passenger_id, o.seat_number, o.ticket_price, o.booking_time, " +
                    "o.payment_status, o.order_status, " +
                    "f.flight_number, f.departure_airport, f.arrival_airport, " +
                    "f.departure_time, f.arrival_time " +
                    "FROM orders o " +
                    "JOIN flights f ON o.flight_id = f.flight_id " +
                    "WHERE o.order_id = ? AND o.user_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, orderId);
            stmt.setString(2, currentUser);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // 检查订单状态
                    String paymentStatus = rs.getString("payment_status");
                    if (!"pending".equals(paymentStatus)) {
                        JOptionPane.showMessageDialog(this, 
                            "订单状态不是待支付，无法进行支付操作！", 
                            "提示", JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                      // 设置订单金额
                    this.orderAmount = rs.getDouble("ticket_price");
                    
                    // 提取航班信息用于界面更新
                    final String flightNumber = rs.getString("flight_number");
                    final String departureAirport = rs.getString("departure_airport");
                    final String arrivalAirport = rs.getString("arrival_airport");
                    final String passengerName = rs.getString("passenger_name");
                    
                    // 更新界面显示
                    SwingUtilities.invokeLater(() -> {
                        if (orderInfoLabel != null) {
                            orderInfoLabel.setText("订单金额: ¥" + String.format("%.2f", orderAmount));
                        }
                        if (flightInfoLabel != null) {
                            String flightInfo = String.format("航班: %s  %s → %s  乘客: %s", 
                                flightNumber,
                                departureAirport,
                                arrivalAirport,
                                passengerName);
                            flightInfoLabel.setText(flightInfo);
                        }
                    });
                    
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "加载订单信息失败: " + e.getMessage(), 
                "数据库错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }    private void processPayment(ActionEvent e) {
        String method = (String) paymentMethod.getSelectedItem();
        String methodCode = mapPaymentMethodToCode(method);

        // 显示支付中提示（使用大字体）
        JOptionPane.showMessageDialog(this, "支付处理中...", "请稍候",
                JOptionPane.INFORMATION_MESSAGE);

        // 模拟调用支付API
        boolean paymentSuccess = simulatePaymentAPI(method);

        if (paymentSuccess) {
            // 更新数据库中的订单状态
            if (updateOrderPaymentStatus(orderId, "paid", methodCode)) {
                JOptionPane.showMessageDialog(this, "支付成功!", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "支付成功但状态更新失败", "警告",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "支付失败，请重试", "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 将支付方式映射为数据库代码
     */
    private String mapPaymentMethodToCode(String method) {
        switch (method) {
            case "微信支付": return "wechat";
            case "支付宝": return "alipay";
            case "银行卡": return "bankcard";
            default: return "other";
        }
    }
    
    /**
     * 更新订单支付状态到数据库
     */
    private boolean updateOrderPaymentStatus(String orderId, String paymentStatus, String paymentMethod) {
        String sql = "UPDATE orders SET payment_status = ?, payment_method = ?, payment_time = CURRENT_TIMESTAMP WHERE order_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paymentStatus);
            stmt.setString(2, paymentMethod);
            stmt.setString(3, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("更新订单支付状态失败: " + e.getMessage());
            return false;
        }
    }    // 模拟支付API调用
    private boolean simulatePaymentAPI(String method) {
        try {
            Thread.sleep(1500); // 模拟网络延迟
            return Math.random() > 0.2; // 80%成功率
        } catch (InterruptedException ex) {
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 用于测试的构造函数调用
            new PaymentModule("ORD1002", "user1").setVisible(true);
        });
    }
}