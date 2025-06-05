import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginWindow {
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
        JTextField userField = new JTextField(20);  // 明确指定列数
        userField.setMinimumSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;  // 关键！让输入框水平扩展
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
        gbc.fill = GridBagConstraints.HORIZONTAL;  // 关键！
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
                    String role = credential.getRole();
                    // 根据角色导航到不同模块
                    // 您可以根据实际需求调整这里的模块导航逻辑
                    if ("管理员".equals(role)) {
                        SwingUtilities.invokeLater(() -> new UserManagementModule().setVisible(true));
                    } else if ("用户".equals(role)) {
                        // FlightQueryModule 使用 main 方法启动，我们直接调用它
                        SwingUtilities.invokeLater(() -> FlightQueryModule.main(null));
                    } else if ("客服".equals(role)) {
                        // OrderManagementModule 需要用户名作为参数
                        SwingUtilities.invokeLater(() -> new OrderManagementModule(username).setVisible(true));
                    } else {
                        // 其他角色或默认处理
                        JOptionPane.showMessageDialog(frame, "登录成功，但角色未配置导航。", "提示", JOptionPane.INFORMATION_MESSAGE);
                    }
                    frame.dispose(); // 关闭登录窗口
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