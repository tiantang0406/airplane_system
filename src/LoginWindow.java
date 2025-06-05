import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginWindow {
    // 显示主菜单的方法
    private static void showMainMenu(JFrame loginFrame, String username, String role) {
        // 清空登录窗口内容
        loginFrame.getContentPane().removeAll();
        loginFrame.setTitle("系统主菜单 - " + username + " (" + role + ")");
        loginFrame.setSize(600, 500);

        // 创建主菜单面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // 顶部欢迎信息
        JPanel headerPanel = new JPanel(new FlowLayout());
        headerPanel.setBackground(new Color(240, 248, 255));
        headerPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel welcomeLabel = new JLabel("欢迎您，" + username + "！当前角色：" + role);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(25, 118, 210));
        headerPanel.add(welcomeLabel);

        // 功能按钮区域
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font buttonFont = new Font("微软雅黑", Font.PLAIN, 16);
        Dimension buttonSize = new Dimension(200, 45);

        int row = 0;        // 根据角色显示不同的功能按钮
        if ("管理员".equals(role)) {
            // 管理员功能
            JButton userMgmtBtn = createMenuButton("用户管理", buttonFont, buttonSize, new Color(76, 175, 80));
            userMgmtBtn.addActionListener(_ -> {
                loginFrame.setVisible(false); // 隐藏主菜单窗口
                UserManagementModule userModule = new UserManagementModule();
                userModule.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                userModule.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                        loginFrame.setVisible(true); // 返回主菜单
                    }
                });
                userModule.setVisible(true);
            });            gbc.gridx = 0;
            gbc.gridy = row++;
            buttonPanel.add(userMgmtBtn, gbc);

            JButton flightMgmtBtn = createMenuButton("航班管理", buttonFont, buttonSize, new Color(33, 150, 243));
            flightMgmtBtn.addActionListener(_ -> {
                loginFrame.setVisible(false); // 隐藏主菜单窗口
                FlightManagementModule flightModule = new FlightManagementModule();
                flightModule.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                flightModule.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                        loginFrame.setVisible(true); // 返回主菜单
                    }
                });
                flightModule.setVisible(true);
            });            gbc.gridx = 0;
            gbc.gridy = row++;
            buttonPanel.add(flightMgmtBtn, gbc);
        } else if ("用户".equals(role)) {
            // 普通用户功能
            JButton queryBtn = createMenuButton("航班查询", buttonFont, buttonSize, new Color(33, 150, 243));
            queryBtn.addActionListener(_ -> {
                loginFrame.setVisible(false); // 隐藏主菜单窗口
                // 创建一个新的Frame来包装FlightQueryModule
                JFrame queryFrame = new JFrame("航班查询");
                queryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                queryFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                        loginFrame.setVisible(true); // 返回主菜单
                    }
                });
                // 在新线程中启动FlightQueryModule
                SwingUtilities.invokeLater(() -> {
                    FlightQueryModule.main(null);
                    // 找到FlightQueryModule创建的JFrame并添加监听器
                    java.awt.Window[] windows = java.awt.Window.getWindows();
                    for (java.awt.Window window : windows) {
                        if (window instanceof JFrame && "航班查询窗口".equals(((JFrame) window).getTitle())) {
                            window.addWindowListener(new java.awt.event.WindowAdapter() {
                                @Override
                                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                                    loginFrame.setVisible(true); // 返回主菜单
                                }
                            });
                            break;
                        }
                    }
                });
            });            gbc.gridx = 0;
            gbc.gridy = row++;
            buttonPanel.add(queryBtn, gbc);            JButton bookingBtn = createMenuButton("订票服务", buttonFont, buttonSize, new Color(156, 39, 176));
            bookingBtn.addActionListener(_ -> {
                BookingService.openBookingModule(loginFrame, username);
            });gbc.gridx = 0;
            gbc.gridy = row++;
            buttonPanel.add(bookingBtn, gbc);

            // 我的订单
            JButton orderBtn = createMenuButton("我的订单", buttonFont, buttonSize, new Color(33, 150, 243));
            orderBtn.addActionListener(_ -> {
                loginFrame.setVisible(false); // 隐藏主菜单窗口
                OrderManagementModule orderModule = new OrderManagementModule(username);
                orderModule.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                orderModule.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                        loginFrame.setVisible(true); // 返回主菜单
                    }
                });
                orderModule.setVisible(true);
            });
            gbc.gridx = 0;
            gbc.gridy = row++;
            buttonPanel.add(orderBtn, gbc);            JButton seatBtn = createMenuButton("选座服务", buttonFont, buttonSize, new Color(255, 152, 0));
            seatBtn.addActionListener(_ -> {
                // 为了演示，这里使用示例订单ID
                String demoOrderId = JOptionPane.showInputDialog(loginFrame, 
                    "请输入订单号进行选座:", "选座服务", JOptionPane.QUESTION_MESSAGE);
                if (demoOrderId != null && !demoOrderId.trim().isEmpty()) {
                    loginFrame.setVisible(false); // 隐藏主菜单窗口
                    SeatSelectionModule seatModule = new SeatSelectionModule(demoOrderId.trim(), username);
                    seatModule.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    seatModule.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                            loginFrame.setVisible(true); // 返回主菜单
                        }
                    });
                    seatModule.setVisible(true);
                }
            });
            gbc.gridx = 0;
            gbc.gridy = row++;
            buttonPanel.add(seatBtn, gbc);        } else if ("客服".equals(role)) {
            // 客服功能
            JButton orderMgmtBtn = createMenuButton("订单管理", buttonFont, buttonSize, new Color(76, 175, 80));
            orderMgmtBtn.addActionListener(_ -> {
                loginFrame.setVisible(false); // 隐藏主菜单窗口
                OrderManagementModule orderModule = new OrderManagementModule(username);
                orderModule.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                orderModule.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                        loginFrame.setVisible(true); // 返回主菜单
                    }
                });
                orderModule.setVisible(true);
            });
            gbc.gridx = 0;
            gbc.gridy = row++;
            buttonPanel.add(orderMgmtBtn, gbc);            JButton refundBtn = createMenuButton("退票服务", buttonFont, buttonSize, new Color(244, 67, 54));
            refundBtn.addActionListener(_ -> {
                // 为了演示，这里使用示例订单ID
                String demoOrderId = JOptionPane.showInputDialog(loginFrame, 
                    "请输入订单号进行退票:", "退票服务", JOptionPane.QUESTION_MESSAGE);
                if (demoOrderId != null && !demoOrderId.trim().isEmpty()) {
                    loginFrame.setVisible(false); // 隐藏主菜单窗口
                    RefundModule refundModule = new RefundModule(demoOrderId.trim(), username);
                    refundModule.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    refundModule.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                            loginFrame.setVisible(true); // 返回主菜单
                        }
                    });
                    refundModule.setVisible(true);
                }
            });
            gbc.gridx = 0;
            gbc.gridy = row++;
            buttonPanel.add(refundBtn, gbc);            JButton rescheduleBtn = createMenuButton("改签服务", buttonFont, buttonSize, new Color(255, 193, 7));
            rescheduleBtn.addActionListener(_ -> {
                // 为了演示，这里使用示例订单ID
                String demoOrderId = JOptionPane.showInputDialog(loginFrame, 
                    "请输入订单号进行改签:", "改签服务", JOptionPane.QUESTION_MESSAGE);
                if (demoOrderId != null && !demoOrderId.trim().isEmpty()) {
                    loginFrame.setVisible(false); // 隐藏主菜单窗口
                    RescheduleUpgradeModule rescheduleModule = new RescheduleUpgradeModule(demoOrderId.trim(), username);
                    rescheduleModule.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    rescheduleModule.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                            loginFrame.setVisible(true); // 返回主菜单
                        }
                    });
                    rescheduleModule.setVisible(true);
                }
            });
            gbc.gridx = 0;
            gbc.gridy = row++;
            buttonPanel.add(rescheduleBtn, gbc);
        }


        // 底部按钮区域
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBackground(Color.WHITE);

        JButton logoutBtn = new JButton("退出登录");
        logoutBtn.setFont(buttonFont);
        logoutBtn.setPreferredSize(new Dimension(120, 35));
        logoutBtn.setBackground(new Color(244, 67, 54));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);        logoutBtn.addActionListener(_ -> {
            int choice = JOptionPane.showConfirmDialog(loginFrame, "确定要退出登录吗？", "确认", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                loginFrame.dispose();
                main(null); // 重新显示登录窗口
            }
        });

        bottomPanel.add(logoutBtn);

        // 组装界面
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        loginFrame.add(mainPanel);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.revalidate();
        loginFrame.repaint();
    }

    // 创建菜单按钮的辅助方法
    private static JButton createMenuButton(String text, Font font, Dimension size, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setPreferredSize(size);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());

        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    public static void main(String[] args) {
        // 创建主窗口（尺寸增大到400x300）
        JFrame frame = new JFrame("登录窗口");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        // 设置背景色
        frame.getContentPane().setBackground(Color.WHITE);

        // 主面板使用GridBagLayout实现灵活布局
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // 添加灰色边框
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // 增大组件间距

        // 标题（增大字号到24）
        JLabel titleLabel = new JLabel("登录窗口");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 27));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        // 账号标签和输入框（加长输入框）
        JLabel userLabel = new JLabel("账号：");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 19)); // 增大标签字号
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(userLabel, gbc);

        // 账号输入框设置
        JTextField userField = new JTextField(20); // 明确指定列数
        userField.setMinimumSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL; // 关键！让输入框水平扩展
        mainPanel.add(userField, gbc);

        // 密码标签和输入框
        JLabel passLabel = new JLabel("密码：");
        passLabel.setFont(new Font("微软雅黑", Font.PLAIN, 19));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(passLabel, gbc);

        // 密码输入框设置
        JPasswordField passField = new JPasswordField(20);
        passField.setMinimumSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; // 关键！
        mainPanel.add(passField, gbc);

        // 按钮面板（使用GridLayout实现并排按钮）
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        JButton loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        loginButton.setPreferredSize(new Dimension(120, 35)); // 增大按钮尺寸

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                String password = new String(passField.getPassword());

                UserManagementModule userManagement = new UserManagementModule();
                UserManagementModule.UserCredential credential = userManagement.authenticateUser(username, password);
                if (credential != null) {
                    // 登录成功，显示主菜单
                    showMainMenu(frame, username, credential.getRole());
                } else {
                    JOptionPane.showMessageDialog(frame, "账号或密码错误，或账户未激活！", "登录失败", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton returnButton = new JButton("返回");
        returnButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        returnButton.setPreferredSize(new Dimension(120, 35));

        buttonPanel.add(loginButton);
        buttonPanel.add(returnButton);

        // 将按钮面板添加到主布局
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);

        // 添加主面板到窗口
        frame.add(mainPanel, BorderLayout.CENTER);        // 居中显示窗口
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

/**
 * 订票服务类 - 提供真正的数据库订票功能
 */
class BookingService {
    private static final String DB_URL = "jdbc:sqlite:airplane_system.db";
    
    /**
     * 打开订票模块
     */
    public static void openBookingModule(JFrame parentFrame, String username) {
        JDialog bookingDialog = new JDialog(parentFrame, "订票服务", true);
        bookingDialog.setSize(600, 500);
        bookingDialog.setLayout(new BorderLayout());
        
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 标题
        JLabel titleLabel = new JLabel("航班预订", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 航班选择面板
        JPanel flightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 航班下拉框
        gbc.gridx = 0; gbc.gridy = 0;
        flightPanel.add(new JLabel("选择航班:"), gbc);
        
        JComboBox<FlightInfo> flightComboBox = new JComboBox<>();
        loadAvailableFlights(flightComboBox);
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        flightPanel.add(flightComboBox, gbc);
        
        // 乘客信息
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        flightPanel.add(new JLabel("乘客姓名:"), gbc);
        
        JTextField passengerNameField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        flightPanel.add(passengerNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        flightPanel.add(new JLabel("身份证号:"), gbc);
        
        JTextField idCardField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        flightPanel.add(idCardField, gbc);
        
        mainPanel.add(flightPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton bookButton = new JButton("预订航班");
        JButton cancelButton = new JButton("取消");
        
        bookButton.addActionListener(e -> {
            FlightInfo selectedFlight = (FlightInfo) flightComboBox.getSelectedItem();
            String passengerName = passengerNameField.getText().trim();
            String idCard = idCardField.getText().trim();
            
            if (selectedFlight == null) {
                JOptionPane.showMessageDialog(bookingDialog, "请选择航班！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (passengerName.isEmpty()) {
                JOptionPane.showMessageDialog(bookingDialog, "请输入乘客姓名！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (idCard.isEmpty()) {
                JOptionPane.showMessageDialog(bookingDialog, "请输入身份证号！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 创建订单
            String orderId = createOrder(username, selectedFlight, passengerName, idCard);
            if (orderId != null) {
                JOptionPane.showMessageDialog(bookingDialog, 
                    "订票成功！\n" +
                    "订单号: " + orderId + "\n" +
                    "航班: " + selectedFlight.getDisplayString() + "\n" +
                    "乘客: " + passengerName + "\n" +
                    "金额: ¥" + String.format("%.2f", selectedFlight.price) + "\n" +
                    "请前往'我的订单'完成支付", 
                    "订票成功", JOptionPane.INFORMATION_MESSAGE);
                bookingDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(bookingDialog, "订票失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(_ -> bookingDialog.dispose());
        
        buttonPanel.add(bookButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        bookingDialog.add(mainPanel);
        bookingDialog.setLocationRelativeTo(parentFrame);
        bookingDialog.setVisible(true);
    }
    
    /**
     * 从数据库加载可用航班
     */
    private static void loadAvailableFlights(JComboBox<FlightInfo> comboBox) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT f.flight_id, f.flight_number, f.base_price, f.available_seats, " +
                 "f.departure_time, f.arrival_time, " +
                 "dep.city as departure_city, arr.city as arrival_city " +
                 "FROM flights f " +
                 "JOIN airports dep ON f.departure_airport = dep.airport_code " +
                 "JOIN airports arr ON f.arrival_airport = arr.airport_code " +
                 "WHERE f.status = 'scheduled' AND f.available_seats > 0 " +
                 "AND f.departure_time > datetime('now') " +
                 "ORDER BY f.departure_time")) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FlightInfo flight = new FlightInfo();
                    flight.flightId = rs.getString("flight_id");
                    flight.flightNumber = rs.getString("flight_number");
                    flight.price = rs.getDouble("base_price");
                    flight.availableSeats = rs.getInt("available_seats");
                    flight.departureTime = rs.getString("departure_time");
                    flight.arrivalTime = rs.getString("arrival_time");
                    flight.departureCity = rs.getString("departure_city");
                    flight.arrivalCity = rs.getString("arrival_city");
                    
                    comboBox.addItem(flight);
                }
            }
            
            if (comboBox.getItemCount() == 0) {
                JOptionPane.showMessageDialog(null, "暂无可预订航班", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "加载航班信息失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 创建订单并保存到数据库
     */
    private static String createOrder(String userId, FlightInfo flight, String passengerName, String passengerId) {
        String orderId = "ORD" + System.currentTimeMillis();
        
        String sql = "INSERT INTO orders (order_id, user_id, flight_id, passenger_name, passenger_id, " +
                    "ticket_price, booking_time, payment_status, order_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, datetime('now'), 'pending', 'active')";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, orderId);
            stmt.setString(2, userId);
            stmt.setString(3, flight.flightId);
            stmt.setString(4, passengerName);
            stmt.setString(5, passengerId);
            stmt.setDouble(6, flight.price);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // 更新航班可用座位数
                updateFlightSeats(flight.flightId, -1);
                System.out.println("订单创建成功: " + orderId);
                return orderId;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("创建订单失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 更新航班座位数
     */
    private static void updateFlightSeats(String flightId, int seatChange) {
        String sql = "UPDATE flights SET available_seats = available_seats + ? WHERE flight_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, seatChange);
            stmt.setString(2, flightId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 航班信息类
     */
    static class FlightInfo {
        String flightId;
        String flightNumber;
        double price;
        int availableSeats;
        String departureTime;
        String arrivalTime;
        String departureCity;
        String arrivalCity;
        
        public String getDisplayString() {
            return String.format("%s - %s→%s - ¥%.0f (余票:%d)", 
                flightNumber, departureCity, arrivalCity, price, availableSeats);
        }
        
        @Override
        public String toString() {
            return getDisplayString();
        }
    }
}