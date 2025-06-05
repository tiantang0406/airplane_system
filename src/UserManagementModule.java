import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class UserManagementModule extends JFrame {

    private JTextField usernameField;
    private JTextField phoneField;
    private JComboBox<String> roleCombo;
    private JButton searchBtn;
    private JButton resetPwdBtn;
    private JButton freezeBtn;
    private JButton assignRoleBtn;
    private JTextArea resultArea;

    // 模拟用户数据库
    private static final Map<String, User> USER_DB = new HashMap<>();
    static {
        USER_DB.put("admin", new User("admin", "13800001111", "管理员", "active"));
        USER_DB.put("user1", new User("user1", "13800002222", "用户", "active"));
        USER_DB.put("user2", new User("user2", "13800003333", "客服", "inactive"));
    }

    public UserManagementModule() {
        setTitle("用户管理系统");
        setSize(700, 500);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 设置全局字体
        Font largerFont = new Font("微软雅黑", Font.PLAIN, 19);
        Font titleFont = new Font("微软雅黑", Font.BOLD, 27);
        UIManager.put("Label.font", largerFont);
        UIManager.put("Button.font", largerFont);
        UIManager.put("TextField.font", largerFont);
        UIManager.put("ComboBox.font", largerFont);
        UIManager.put("TextArea.font", largerFont);

        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel titleLabel = new JLabel("用户账户管理", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // 用户名输入
        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(largerFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(usernameField, gbc);

        // 手机号输入
        JLabel phoneLabel = new JLabel("手机号:");
        phoneLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(phoneLabel, gbc);

        phoneField = new JTextField(20);
        phoneField.setFont(largerFont);
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(phoneField, gbc);

        // 角色选择
        JLabel roleLabel = new JLabel("分配角色:");
        roleLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(roleLabel, gbc);

        roleCombo = new JComboBox<>(new String[]{"用户", "客服", "管理员"});
        roleCombo.setFont(largerFont);
        gbc.gridx = 1;
        gbc.gridy = 3;
        mainPanel.add(roleCombo, gbc);

        // 搜索按钮
        searchBtn = new JButton("查询用户");
        searchBtn.setFont(largerFont);
        searchBtn.setBackground(new Color(100, 181, 246)); // 蓝色
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(searchBtn, gbc);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        resetPwdBtn = new JButton("重置密码");
        resetPwdBtn.setFont(largerFont);
        resetPwdBtn.setBackground(new Color(255, 193, 7)); // 琥珀色
        resetPwdBtn.setEnabled(false);

        freezeBtn = new JButton("冻结/解冻");
        freezeBtn.setFont(largerFont);
        freezeBtn.setBackground(new Color(244, 67, 54)); // 红色
        freezeBtn.setEnabled(false);

        assignRoleBtn = new JButton("分配角色");
        assignRoleBtn.setFont(largerFont);
        assignRoleBtn.setBackground(new Color(76, 175, 80)); // 绿色
        assignRoleBtn.setEnabled(false);

        buttonPanel.add(resetPwdBtn);
        buttonPanel.add(freezeBtn);
        buttonPanel.add(assignRoleBtn);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

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
        mainPanel.add(scrollPane, gbc);

        // 组装界面
        add(mainPanel, BorderLayout.CENTER);

        // 事件处理
        searchBtn.addActionListener(this::searchUser);
        resetPwdBtn.addActionListener(this::resetPassword);
        freezeBtn.addActionListener(this::toggleFreeze);
        assignRoleBtn.addActionListener(this::assignRole);

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

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
            for (User u : USER_DB.values()) {
                if (u.phone.equals(phone)) {
                    user = u;
                    break;
                }
            }
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

    private void resetPassword(ActionEvent e) {
        String username = usernameField.getText().trim();
        if (!USER_DB.containsKey(username)) {
            resultArea.setText("用户不存在");
            return;
        }

        // 生成随机密码
        String tempPassword = generateTempPassword();

        // 模拟发送密码（实际应调用短信/邮件服务）
        boolean sendResult = simulateSendPassword(username, tempPassword);

        if (sendResult) {
            resultArea.append("\n\n密码重置成功!\n");
            resultArea.append("临时密码已发送到用户手机: " + tempPassword);
        } else {
            resultArea.append("\n\n密码发送失败，请重试");
        }
    }

    private void toggleFreeze(ActionEvent e) {
        String username = usernameField.getText().trim();
        User user = USER_DB.get(username);
        if (user == null) {
            resultArea.setText("用户不存在");
            return;
        }

        String newStatus = user.status.equals("active") ? "inactive" : "active";
        user.status = newStatus;

        resultArea.append("\n\n账户状态已更新!\n");
        resultArea.append("新状态: " + (newStatus.equals("active") ? "已解冻" : "已冻结"));

        // 更新按钮文本
        freezeBtn.setText(newStatus.equals("active") ? "冻结账户" : "解冻账户");
    }

    private void assignRole(ActionEvent e) {
        String username = usernameField.getText().trim();
        User user = USER_DB.get(username);
        if (user == null) {
            resultArea.setText("用户不存在");
            return;
        }

        String newRole = (String) roleCombo.getSelectedItem();
        user.role = newRole;

        resultArea.append("\n\n角色分配成功!\n");
        resultArea.append("新角色: " + newRole);
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
