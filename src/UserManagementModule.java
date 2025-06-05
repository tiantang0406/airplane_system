import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserManagementModule extends JFrame {

    // 主标签页组件
    private JTabbedPane tabbedPane;
    
    // 查询管理标签页组件
    private JTextField usernameField;
    private JTextField phoneField;
    private JComboBox<String> roleCombo;
    private JButton searchBtn;
    private JButton resetPwdBtn;
    private JButton freezeBtn;
    private JButton assignRoleBtn;
    private JTextArea resultArea;
    
    // 创建用户标签页组件
    private JTextField createUsernameField;
    private JTextField createPhoneField;
    private JPasswordField createPasswordField;
    private JComboBox<String> createRoleCombo;
    private JButton createUserBtn;
    private JTextArea createResultArea;
    
    // 删除用户标签页组件
    private JTextField deleteUsernameField;
    private JButton deleteUserBtn;
    private JTextArea deleteResultArea;

    /**
     * 验证用户身份并返回凭据
     * @param username 用户名
     * @param password 密码
     * @return 验证成功返回用户凭据，失败返回null
     */
    public UserCredential authenticateUser(String username, String password) {
        // 从数据库获取用户信息（包括密码）
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    String userStatus = rs.getString("status");
                    String userRole = rs.getString("role");
                    
                    // 验证密码（明文比较）和用户状态
                    if (password.equals(storedPassword) && "active".equals(userStatus)) {
                        return new UserCredential(username, userRole);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null; // 用户不存在、密码错误或用户已冻结
    }
    
    /**
     * 用户身份凭据类 - 简化版本，只包含用户ID和角色
     */
    public static class UserCredential {
        private final String userId;
        private final String role;
        private final long timestamp;
        
        public UserCredential(String userId, String role) {
            this.userId = userId;
            this.role = role;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getUserId() { return userId; }
        public String getRole() { return role; }
        public long getTimestamp() { return timestamp; }
    }

    /**
     * 数据库管理类 - 处理用户数据库操作
     */
    private static final DatabaseUserManager USER_DB = new DatabaseUserManager();
    
    /**
     * 数据库用户管理器 - 提供类似HashMap的接口
     */
    private static class DatabaseUserManager {
        
        /**
         * 根据用户名获取用户（类似HashMap.get()）
         */
        public User get(String username) {
            return DatabaseManager.getUserByUsername(username);
        }
        
        /**
         * 检查是否包含指定用户名（类似HashMap.containsKey()）
         */
        public boolean containsKey(String username) {
            return get(username) != null;
        }
        
        /**
         * 获取所有用户值的集合（类似HashMap.values()）
         */
        public java.util.Collection<User> values() {
            java.util.List<User> users = new java.util.ArrayList<>();
            String sql = "SELECT * FROM users";
            try (Connection conn = DatabaseManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    users.add(new User(
                        rs.getString("username"),
                        rs.getString("phone"),
                        rs.getString("role"),
                        rs.getString("status")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return users;
        }
    }

    public static class DatabaseManager {
        private static final String DB_URL = "jdbc:sqlite:airplane_system.db";
        
        static {
            try {
                Class.forName("org.sqlite.JDBC");
                initializeDatabase();
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
         * 根据用户名查询用户
         */
        public static User getUserByUsername(String username) {
            String sql = "SELECT * FROM users WHERE username = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                            rs.getString("username"),
                            rs.getString("phone"),
                            rs.getString("role"),
                            rs.getString("status")
                        );
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
        
        /**
         * 根据手机号查询用户
         */
        public static User getUserByPhone(String phone) {
            String sql = "SELECT * FROM users WHERE phone = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, phone);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                            rs.getString("username"),
                            rs.getString("phone"),
                            rs.getString("role"),
                            rs.getString("status")
                        );
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
        
        /**
         * 更新用户角色
         */
        public static boolean updateUserRole(String username, String role) {
            String sql = "UPDATE users SET role = ? WHERE username = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, role);
                stmt.setString(2, username);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        /**
         * 更新用户状态（冻结/解冻）
         */
        public static boolean updateUserStatus(String username, String status) {
            String sql = "UPDATE users SET status = ? WHERE username = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setString(2, username);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        /**
         * 重置用户密码
         */
        public static boolean resetUserPassword(String username, String newPassword) {
            String sql = "UPDATE users SET password = ? WHERE username = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newPassword);
                stmt.setString(2, username);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        /**
         * 创建新用户
         */
        public static boolean createUser(String username, String password, String phone, String role) {
            String sql = "INSERT INTO users (username, password, phone, role, status) VALUES (?, ?, ?, ?, 'active')";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, phone);
                stmt.setString(4, role);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        /**
         * 删除用户
         */
        public static boolean deleteUser(String username) {
            String sql = "DELETE FROM users WHERE username = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        /**
         * 检查用户名是否已存在
         */
        public static boolean userExists(String username) {
            return getUserByUsername(username) != null;
        }
        
        /**
         * 初始化数据库
         */
        private static void initializeDatabase() {
            String createTableSQL = 
                "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY, " +
                "password TEXT NOT NULL, " +
                "phone TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "status TEXT NOT NULL" +
                ")";
            
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
                
                // 检查是否已有数据
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        String[] insertSQL = {
                            "INSERT INTO users VALUES ('admin', 'admin123', '13800001111', '管理员', 'active')",
                            "INSERT INTO users VALUES ('user1', 'user123', '13800002222', '用户', 'active')",
                            "INSERT INTO users VALUES ('user2', 'user456', '13800003333', '客服', 'inactive')"
                        };
                        
                        for (String sql : insertSQL) {
                            stmt.execute(sql);
                        }
                        System.out.println("用户数据已初始化");
                    }
                }
            } catch (SQLException e) {
                System.err.println("初始化数据库错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public UserManagementModule() {
        setTitle("用户管理系统");
        setSize(800, 600);
        getContentPane().setBackground(Color.WHITE);

        // 设置全局字体
        Font largerFont = new Font("微软雅黑", Font.PLAIN, 16);
        UIManager.put("Label.font", largerFont);
        UIManager.put("Button.font", largerFont);
        UIManager.put("TextField.font", largerFont);
        UIManager.put("ComboBox.font", largerFont);
        UIManager.put("TextArea.font", largerFont);

        // 创建标签页
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(largerFont);
        
        // 添加三个标签页
        tabbedPane.addTab("用户查询与管理", createSearchPanel());
        tabbedPane.addTab("创建新用户", createUserPanel());
        tabbedPane.addTab("删除用户", createDeletePanel());
        
        add(tabbedPane, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * 创建用户查询与管理面板
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel titleLabel = new JLabel("用户查询与管理", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // 用户名输入
        JLabel usernameLabel = new JLabel("用户名:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(usernameField, gbc);

        // 手机号输入
        JLabel phoneLabel = new JLabel("手机号:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(phoneLabel, gbc);

        phoneField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(phoneField, gbc);

        // 角色选择
        JLabel roleLabel = new JLabel("分配角色:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(roleLabel, gbc);

        roleCombo = new JComboBox<>(new String[]{"用户", "客服", "管理员"});
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(roleCombo, gbc);

        // 搜索按钮
        searchBtn = new JButton("查询用户");
        searchBtn.setBackground(new Color(100, 181, 246));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(searchBtn, gbc);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        resetPwdBtn = new JButton("重置密码");
        resetPwdBtn.setBackground(new Color(255, 193, 7));
        resetPwdBtn.setEnabled(false);

        freezeBtn = new JButton("冻结/解冻");
        freezeBtn.setBackground(new Color(244, 67, 54));
        freezeBtn.setEnabled(false);

        assignRoleBtn = new JButton("分配角色");
        assignRoleBtn.setBackground(new Color(76, 175, 80));
        assignRoleBtn.setEnabled(false);

        buttonPanel.add(resetPwdBtn);
        buttonPanel.add(freezeBtn);
        buttonPanel.add(assignRoleBtn);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        // 结果显示
        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(scrollPane, gbc);

        // 事件处理
        searchBtn.addActionListener(this::searchUser);
        resetPwdBtn.addActionListener(this::resetPassword);
        freezeBtn.addActionListener(this::toggleFreeze);
        assignRoleBtn.addActionListener(this::assignRole);

        return panel;
    }

    /**
     * 创建用户创建面板
     */
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel titleLabel = new JLabel("创建新用户", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // 用户名输入
        JLabel usernameLabel = new JLabel("用户名:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(usernameLabel, gbc);

        createUsernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(createUsernameField, gbc);

        // 密码输入
        JLabel passwordLabel = new JLabel("密码:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        createPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(createPasswordField, gbc);

        // 手机号输入
        JLabel phoneLabel = new JLabel("手机号:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(phoneLabel, gbc);

        createPhoneField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(createPhoneField, gbc);

        // 角色选择
        JLabel roleLabel = new JLabel("角色:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(roleLabel, gbc);

        createRoleCombo = new JComboBox<>(new String[]{"用户", "客服", "管理员"});
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(createRoleCombo, gbc);

        // 创建按钮
        createUserBtn = new JButton("创建用户");
        createUserBtn.setBackground(new Color(76, 175, 80));
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(createUserBtn, gbc);

        // 结果显示
        createResultArea = new JTextArea(10, 40);
        createResultArea.setEditable(false);
        createResultArea.setLineWrap(true);
        createResultArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(createResultArea);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(scrollPane, gbc);

        // 事件处理
        createUserBtn.addActionListener(this::createUser);

        return panel;
    }

    /**
     * 创建用户删除面板
     */
    private JPanel createDeletePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel titleLabel = new JLabel("删除用户", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // 用户名输入
        JLabel usernameLabel = new JLabel("要删除的用户名:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(usernameLabel, gbc);

        deleteUsernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(deleteUsernameField, gbc);

        // 删除按钮
        deleteUserBtn = new JButton("删除用户");
        deleteUserBtn.setBackground(new Color(244, 67, 54));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(deleteUserBtn, gbc);

        // 结果显示
        deleteResultArea = new JTextArea(10, 40);
        deleteResultArea.setEditable(false);
        deleteResultArea.setLineWrap(true);
        deleteResultArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(deleteResultArea);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(scrollPane, gbc);

        // 事件处理
        deleteUserBtn.addActionListener(this::deleteUser);

        return panel;
    }

    // 搜索用户事件处理
    private void searchUser(ActionEvent e) {
        String username = usernameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (username.isEmpty() && phone.isEmpty()) {
            resultArea.setText("请输入用户名或手机号");
            return;
        }

        User user = null;
        if (!username.isEmpty()) {
            user = USER_DB.get(username);
        } else {
            user = DatabaseManager.getUserByPhone(phone);
        }

        if (user != null) {
            resultArea.setText(String.format(
                    "用户信息:\n" +
                            "用户名: %s\n" +
                            "手机号: %s\n" +
                            "当前角色: %s\n" +
                            "账户状态: %s",
                    user.username,
                    user.phone,
                    user.role,
                    user.status.equals("active") ? "正常" : "已冻结"));

            // 填充查询到的信息
            usernameField.setText(user.username);
            phoneField.setText(user.phone);
            roleCombo.setSelectedItem(user.role);

            // 启用操作按钮
            resetPwdBtn.setEnabled(true);
            freezeBtn.setEnabled(true);
            assignRoleBtn.setEnabled(true);

            // 更新冻结按钮文本
            freezeBtn.setText(user.status.equals("active") ? "冻结账户" : "解冻账户");
        } else {
            resultArea.setText("未找到匹配的用户");
            resetPwdBtn.setEnabled(false);
            freezeBtn.setEnabled(false);
            assignRoleBtn.setEnabled(false);
        }
    }

    // 重置密码事件处理（修复：添加数据库持久化）
    private void resetPassword(ActionEvent e) {
        String username = usernameField.getText().trim();
        if (!USER_DB.containsKey(username)) {
            resultArea.append("\n\n用户不存在");
            return;
        }

        // 生成随机密码
        String tempPassword = generateTempPassword();

        // 更新数据库中的密码
        boolean dbUpdateResult = DatabaseManager.resetUserPassword(username, tempPassword);
        
        if (dbUpdateResult) {
            // 模拟发送密码（实际应调用短信/邮件服务）
            boolean sendResult = simulateSendPassword(username, tempPassword);

            if (sendResult) {
                resultArea.append("\n\n密码重置成功!\n");
                resultArea.append("临时密码已发送到用户手机: " + tempPassword);
                resultArea.append("\n数据库已更新");
            } else {
                resultArea.append("\n\n密码重置成功，但发送失败，请重试发送");
                resultArea.append("\n新密码: " + tempPassword);
            }
        } else {
            resultArea.append("\n\n密码重置失败，数据库更新错误");
        }
    }

    // 冻结/解冻事件处理（修复：添加数据库持久化）
    private void toggleFreeze(ActionEvent e) {
        String username = usernameField.getText().trim();
        User user = USER_DB.get(username);
        if (user == null) {
            resultArea.append("\n\n用户不存在");
            return;
        }

        String newStatus = user.status.equals("active") ? "inactive" : "active";
        
        // 更新数据库中的用户状态
        boolean dbUpdateResult = DatabaseManager.updateUserStatus(username, newStatus);
        
        if (dbUpdateResult) {
            // 同时更新内存中的对象
            user.status = newStatus;

            resultArea.append("\n\n账户状态已更新!\n");
            resultArea.append("新状态: " + (newStatus.equals("active") ? "已解冻" : "已冻结"));
            resultArea.append("\n数据库已更新");

            // 更新按钮文本
            freezeBtn.setText(newStatus.equals("active") ? "冻结账户" : "解冻账户");
        } else {
            resultArea.append("\n\n状态更新失败，数据库更新错误");
        }
    }

    // 分配角色事件处理（修复：添加数据库持久化）
    private void assignRole(ActionEvent e) {
        String username = usernameField.getText().trim();
        User user = USER_DB.get(username);
        if (user == null) {
            resultArea.append("\n\n用户不存在");
            return;
        }

        String newRole = (String) roleCombo.getSelectedItem();
        
        // 更新数据库中的用户角色
        boolean dbUpdateResult = DatabaseManager.updateUserRole(username, newRole);
        
        if (dbUpdateResult) {
            // 同时更新内存中的对象
            user.role = newRole;

            resultArea.append("\n\n角色分配成功!\n");
            resultArea.append("新角色: " + newRole);
            resultArea.append("\n数据库已更新");
        } else {
            resultArea.append("\n\n角色分配失败，数据库更新错误");
        }
    }

    // 创建用户事件处理
    private void createUser(ActionEvent e) {
        String username = createUsernameField.getText().trim();
        String password = new String(createPasswordField.getPassword()).trim();
        String phone = createPhoneField.getText().trim();
        String role = (String) createRoleCombo.getSelectedItem();

        // 输入验证
        if (username.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            createResultArea.setText("请填写所有必填字段");
            return;
        }

        // 验证用户名格式（简单验证）
        if (username.length() < 3) {
            createResultArea.setText("用户名长度至少3个字符");
            return;
        }

        // 验证密码格式
        if (password.length() < 6) {
            createResultArea.setText("密码长度至少6个字符");
            return;
        }

        // 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            createResultArea.setText("请输入有效的手机号");
            return;
        }

        // 检查用户名是否已存在
        if (DatabaseManager.userExists(username)) {
            createResultArea.setText("用户名已存在，请选择其他用户名");
            return;
        }

        // 创建用户
        boolean result = DatabaseManager.createUser(username, password, phone, role);
        
        if (result) {
            createResultArea.setText(String.format(
                "用户创建成功!\n" +
                "用户名: %s\n" +
                "手机号: %s\n" +
                "角色: %s\n" +
                "状态: 激活",
                username, phone, role
            ));
            
            // 清空输入字段
            createUsernameField.setText("");
            createPasswordField.setText("");
            createPhoneField.setText("");
            createRoleCombo.setSelectedIndex(0);
        } else {
            createResultArea.setText("用户创建失败，请检查输入信息或联系系统管理员");
        }
    }

    // 删除用户事件处理
    private void deleteUser(ActionEvent e) {
        String username = deleteUsernameField.getText().trim();

        if (username.isEmpty()) {
            deleteResultArea.setText("请输入要删除的用户名");
            return;
        }

        // 检查用户是否存在
        User user = DatabaseManager.getUserByUsername(username);
        if (user == null) {
            deleteResultArea.setText("用户不存在");
            return;
        }

        // 显示确认对话框
        int choice = JOptionPane.showConfirmDialog(
            this,
            String.format("确定要删除用户 '%s' 吗？\n\n用户信息:\n手机号: %s\n角色: %s\n\n此操作不可撤销！", 
                         username, user.phone, user.role),
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            boolean result = DatabaseManager.deleteUser(username);
            
            if (result) {
                deleteResultArea.setText(String.format(
                    "用户删除成功!\n" +
                    "已删除用户: %s\n" +
                    "删除时间: %s",
                    username,
                    new java.util.Date().toString()
                ));
                
                // 清空输入字段
                deleteUsernameField.setText("");
            } else {
                deleteResultArea.setText("用户删除失败，请联系系统管理员");
            }
        } else {
            deleteResultArea.setText("删除操作已取消");
        }
    }

    private String generateTempPassword() {
        // 生成8位随机密码
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    private boolean simulateSendPassword(String username, String password) {
        System.out.println("模拟发送密码到用户: " + username + ", 密码: " + password);
        return Math.random() > 0.2; // 80%成功率
    }

    // 用户信息内部类
    private static class User {
        String username;
        String phone;
        String role;
        String status;

        User(String username, String phone, String role, String status) {
            this.username = username;
            this.phone = phone;
            this.role = role;
            this.status = status;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserManagementModule().setVisible(true));
    }
}
