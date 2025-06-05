import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
            buttonPanel.add(queryBtn, gbc);

            JButton bookingBtn = createMenuButton("订票服务", buttonFont, buttonSize, new Color(156, 39, 176));
            bookingBtn.addActionListener(_ -> {
                // 简单的订票服务对话框
                String[] flights = {"FL001 - PEK→SHA - ¥1580", "FL002 - PEK→CAN - ¥890", "FL003 - SHA→SZX - ¥780"};
                String selectedFlight = (String) JOptionPane.showInputDialog(loginFrame,
                    "请选择要预订的航班:", "订票服务", JOptionPane.QUESTION_MESSAGE,
                    null, flights, flights[0]);
                
                if (selectedFlight != null) {
                    String passengerName = JOptionPane.showInputDialog(loginFrame, 
                        "请输入乘客姓名:", "订票信息", JOptionPane.QUESTION_MESSAGE);
                    if (passengerName != null && !passengerName.trim().isEmpty()) {
                        String idCard = JOptionPane.showInputDialog(loginFrame, 
                            "请输入身份证号:", "订票信息", JOptionPane.QUESTION_MESSAGE);
                        if (idCard != null && !idCard.trim().isEmpty()) {
                            String orderId = "ORD" + System.currentTimeMillis();
                            JOptionPane.showMessageDialog(loginFrame, 
                                "订票成功！\n" +
                                "订单号: " + orderId + "\n" +
                                "航班: " + selectedFlight + "\n" +
                                "乘客: " + passengerName + "\n" +
                                "请前往支付完成订单", 
                                "订票成功", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            });            gbc.gridx = 0;
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
                    RescheduleUpgradeModule rescheduleModule = new RescheduleUpgradeModule(demoOrderId.trim());
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
        frame.add(mainPanel, BorderLayout.CENTER);

        // 居中显示窗口
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}