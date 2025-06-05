import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class OrderManagementModule extends JFrame {

    private String currentUser; // 当前登录用户
    private JTable orderTable;
    private JComboBox<String> statusFilter;

    /**
     * 数据库管理器 - 处理订单数据库操作
     */
    public static class DatabaseManager {
        private static final String DB_URL = "jdbc:sqlite:airplane_system.db";
        
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
        }
        
        /**
         * 获取用户的订单列表（包含航班信息）
         */
        public static java.util.List<OrderInfo> getUserOrders(String userId, String statusFilter) {
            java.util.List<OrderInfo> orders = new java.util.ArrayList<>();
            
            String sql = "SELECT o.order_id, o.user_id, o.flight_id, o.passenger_name, " +
                        "o.passenger_id, o.seat_number, o.ticket_price, o.booking_time, " +
                        "o.payment_status, o.order_status, o.payment_method, o.payment_time, " +
                        "f.flight_number, f.departure_airport, f.arrival_airport, " +
                        "f.departure_time, f.arrival_time " +
                        "FROM orders o " +
                        "JOIN flights f ON o.flight_id = f.flight_id " +
                        "WHERE o.user_id = ?";
            
            if (statusFilter != null && !"全部".equals(statusFilter)) {
                // 状态映射：UI显示 -> 数据库值
                String dbStatus = mapUIStatusToDBStatus(statusFilter);
                if (dbStatus != null) {
                    sql += " AND o.payment_status = ?";
                }
            }
            
            sql += " ORDER BY o.booking_time DESC";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                
                int paramIndex = 2;
                if (statusFilter != null && !"全部".equals(statusFilter)) {
                    String dbStatus = mapUIStatusToDBStatus(statusFilter);
                    if (dbStatus != null) {
                        stmt.setString(paramIndex, dbStatus);
                    }
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        OrderInfo order = new OrderInfo();
                        order.orderId = rs.getString("order_id");
                        order.userId = rs.getString("user_id");
                        order.flightId = rs.getString("flight_id");
                        order.passengerName = rs.getString("passenger_name");
                        order.passengerId = rs.getString("passenger_id");
                        order.seatNumber = rs.getString("seat_number");
                        order.ticketPrice = rs.getDouble("ticket_price");
                        order.bookingTime = rs.getString("booking_time");
                        order.paymentStatus = rs.getString("payment_status");
                        order.orderStatus = rs.getString("order_status");
                        order.paymentMethod = rs.getString("payment_method");
                        order.paymentTime = rs.getString("payment_time");
                        order.flightNumber = rs.getString("flight_number");
                        order.departureAirport = rs.getString("departure_airport");
                        order.arrivalAirport = rs.getString("arrival_airport");
                        order.departureTime = rs.getString("departure_time");
                        order.arrivalTime = rs.getString("arrival_time");
                        
                        orders.add(order);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("获取订单数据失败: " + e.getMessage());
            }
            
            return orders;
        }
        
        /**
         * 将UI显示的状态映射到数据库状态
         */
        private static String mapUIStatusToDBStatus(String uiStatus) {
            switch (uiStatus) {
                case "待支付": return "pending";
                case "已完成": return "paid";
                case "已退票": return "refunded";
                default: return null;
            }
        }
        
        /**
         * 将数据库状态映射到UI显示状态
         */
        public static String mapDBStatusToUIStatus(String dbStatus) {
            switch (dbStatus) {
                case "pending": return "待支付";
                case "paid": return "已完成";
                case "refunded": return "已退票";
                default: return dbStatus;
            }
        }
        
        /**
         * 更新订单支付状态
         */
        public static boolean updateOrderPaymentStatus(String orderId, String paymentStatus, String paymentMethod) {
            String sql = "UPDATE orders SET payment_status = ?, payment_method = ?, payment_time = CURRENT_TIMESTAMP WHERE order_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, paymentStatus);
                stmt.setString(2, paymentMethod);
                stmt.setString(3, orderId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        /**
         * 更新订单状态（退票等）
         */
        public static boolean updateOrderStatus(String orderId, String orderStatus, String paymentStatus) {
            String sql = "UPDATE orders SET order_status = ?, payment_status = ? WHERE order_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, orderStatus);
                stmt.setString(2, paymentStatus);
                stmt.setString(3, orderId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    
    /**
     * 订单信息类
     */
    public static class OrderInfo {
        public String orderId;
        public String userId;
        public String flightId;
        public String passengerName;
        public String passengerId;
        public String seatNumber;
        public double ticketPrice;
        public String bookingTime;
        public String paymentStatus;
        public String orderStatus;
        public String paymentMethod;
        public String paymentTime;
        public String flightNumber;
        public String departureAirport;
        public String arrivalAirport;
        public String departureTime;
        public String arrivalTime;
    }

    public OrderManagementModule(String username) {
        this.currentUser = username;
        setTitle("订单管理 - " + username);
        setSize(1000, 700); // 增大窗口以适应更大的字体
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 设置全局字体（增大3号）
        Font largerFont = new Font("微软雅黑", Font.PLAIN, 16); // 原14号增大到16号
        UIManager.put("Label.font", largerFont);
        UIManager.put("Button.font", largerFont);
        UIManager.put("ComboBox.font", largerFont);
        UIManager.put("Table.font", largerFont);
        UIManager.put("TableHeader.font", largerFont);

        // 顶部过滤面板
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        filterPanel.setBackground(Color.WHITE);

        JLabel filterLabel = new JLabel("订单状态:");
        filterLabel.setFont(largerFont);

        statusFilter = new JComboBox<>(new String[]{"全部", "待支付", "已完成", "已退票"});
        statusFilter.setFont(largerFont);        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(largerFont);

        JButton paymentBtn = new JButton("支付管理");
        paymentBtn.setFont(largerFont);
        paymentBtn.setBackground(new Color(76, 175, 80));
        paymentBtn.setForeground(Color.WHITE);

        JButton refundBtn = new JButton("退票管理");
        refundBtn.setFont(largerFont);
        refundBtn.setBackground(new Color(244, 67, 54));
        refundBtn.setForeground(Color.WHITE);        filterPanel.add(filterLabel);
        filterPanel.add(statusFilter);
        filterPanel.add(refreshBtn);
        filterPanel.add(paymentBtn);
        filterPanel.add(refundBtn);

        // 添加事件监听器
        refreshBtn.addActionListener(_ -> loadOrders(null));
        paymentBtn.addActionListener(this::openPaymentModule);
        refundBtn.addActionListener(this::openRefundModule);        // 订单表格
        String[] columns = {"订单号", "航班号", "出发地", "目的地", "日期", "起飞时间", "降落时间", "状态", "金额"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderTable = new JTable(model);
        orderTable.setFont(largerFont);
        orderTable.setRowHeight(35); // 增大行高以适应更大的字体
        orderTable.getTableHeader().setFont(largerFont);

        JScrollPane tableScroll = new JScrollPane(orderTable);

        // 组装界面
        add(filterPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);

        // 事件处理
        refreshBtn.addActionListener(this::loadOrders);
        statusFilter.addActionListener(this::loadOrders);

        // 初始加载数据
        loadOrders(null);

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }    private void loadOrders(ActionEvent e) {
        String selectedStatus = (String) statusFilter.getSelectedItem();
        DefaultTableModel model = (DefaultTableModel) orderTable.getModel();
        model.setRowCount(0);

        // 从数据库获取订单数据
        java.util.List<OrderInfo> orders = DatabaseManager.getUserOrders(currentUser, selectedStatus);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        
        for (OrderInfo order : orders) {
            try {
                // 解析日期时间
                java.util.Date departureDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(order.departureTime);
                java.util.Date arrivalDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(order.arrivalTime);
                
                String departureDate = dateFormat.format(departureDateTime);
                String departureTime = timeFormat.format(departureDateTime);
                String arrivalTime = timeFormat.format(arrivalDateTime);
                
                // 映射数据库状态到UI显示状态
                String displayStatus = DatabaseManager.mapDBStatusToUIStatus(order.paymentStatus);
                
                Object[] row = {
                    order.orderId,
                    order.flightNumber,
                    order.departureAirport,
                    order.arrivalAirport,
                    departureDate,
                    departureTime,
                    arrivalTime,
                    displayStatus,
                    order.ticketPrice
                };
                
                model.addRow(row);
            } catch (java.text.ParseException ex) {
                // 如果日期解析失败，使用原始数据
                Object[] row = {
                    order.orderId,
                    order.flightNumber,
                    order.departureAirport,
                    order.arrivalAirport,
                    order.departureTime.substring(0, 10), // 取日期部分
                    order.departureTime.substring(11, 16), // 取时间部分
                    order.arrivalTime.substring(11, 16),
                    DatabaseManager.mapDBStatusToUIStatus(order.paymentStatus),
                    order.ticketPrice
                };
                model.addRow(row);
            }
        }

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                String.format("用户 %s 没有找到符合条件的订单", currentUser), 
                "提示", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }    private void openPaymentModule(ActionEvent e) {        // 获取选中的订单
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            String orderId = (String) orderTable.getValueAt(selectedRow, 0);
            String status = (String) orderTable.getValueAt(selectedRow, 7);
            
            if ("待支付".equals(status)) {// 隐藏当前窗口
                this.setVisible(false);
                PaymentModule paymentModule = new PaymentModule(orderId, currentUser);
                paymentModule.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                        OrderManagementModule.this.setVisible(true); // 返回订单管理窗口
                        loadOrders(null); // 刷新订单列表
                    }
                });
                paymentModule.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "只能处理待支付订单的支付！", "提示", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择一个订单！", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }    private void openRefundModule(ActionEvent e) {
        // 获取选中的订单
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            String orderId = (String) orderTable.getValueAt(selectedRow, 0);
            String status = (String) orderTable.getValueAt(selectedRow, 7);
            
            if ("已完成".equals(status)) {
                // 隐藏当前窗口
                this.setVisible(false);
                RefundModule refundModule = new RefundModule(orderId, currentUser);
                refundModule.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                        OrderManagementModule.this.setVisible(true); // 返回订单管理窗口
                        loadOrders(null); // 刷新订单列表
                    }
                });
                refundModule.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "只能退已完成的订单！", "提示", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择一个订单！", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OrderManagementModule("user2").setVisible(true));
    }
}
