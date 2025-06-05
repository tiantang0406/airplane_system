import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class FlightQueryModule {
    
    public static void main(String[] args) {
        // 创建主窗口
        JFrame frame = new JFrame("航班查询窗口");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.WHITE);

        // 主面板使用GridBagLayout实现精确对齐
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);

        // 标题（严格居中显示）
        JLabel titleLabel = new JLabel("航班查询窗口", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 27));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // 使用固定宽度标签确保冒号对齐
        int labelWidth = 80; // 统一标签宽度

        // 出发地
        JLabel departureLabel = new JLabel("出发地：", SwingConstants.RIGHT);
        departureLabel.setPreferredSize(new Dimension(labelWidth, 20));
        departureLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(departureLabel, gbc);

        JTextField departureField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(departureField, gbc);

        // 目的地
        JLabel destinationLabel = new JLabel("目的地：", SwingConstants.RIGHT);
        destinationLabel.setPreferredSize(new Dimension(labelWidth, 20));
        destinationLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(destinationLabel, gbc);

        JTextField destinationField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(destinationField, gbc);

        // 日期（明确显示格式）
        JLabel dateLabel = new JLabel("日期（YYYY-MM-DD）：", SwingConstants.RIGHT);
        dateLabel.setPreferredSize(new Dimension(labelWidth + 40, 20)); // 加宽标签
        dateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(dateLabel, gbc);

        JFormattedTextField dateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        dateField.setColumns(15);
        dateField.setToolTipText("请输入格式：YYYY-MM-DD");
        gbc.gridx = 1;
        gbc.gridy = 3;
        mainPanel.add(dateField, gbc);

        // 舱位等级
        JLabel cabinLabel = new JLabel("舱位等级：", SwingConstants.RIGHT);
        cabinLabel.setPreferredSize(new Dimension(labelWidth, 20));
        cabinLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(cabinLabel, gbc);

        JComboBox<String> cabinCombo = new JComboBox<>(new String[]{"经济舱", "商务舱"});
        cabinCombo.setPreferredSize(new Dimension(200, 25));
        cabinCombo.setSelectedIndex(0); // 默认选中经济舱
        gbc.gridx = 1;
        gbc.gridy = 4;
        mainPanel.add(cabinCombo, gbc);

        // 按钮面板（完全匹配图片样式）
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        JButton queryButton = new JButton("查询");
        queryButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        queryButton.setPreferredSize(new Dimension(120, 35));
        queryButton.setBackground(new Color(173, 216, 230)); // 浅蓝色背景
        queryButton.setFocusPainted(false); // 去除焦点边框

        JButton returnButton = new JButton("返回");
        returnButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        returnButton.setPreferredSize(new Dimension(120, 35));
        returnButton.setBackground(new Color(173, 216, 230)); // 浅蓝色背景
        returnButton.setFocusPainted(false);

        buttonPanel.add(queryButton);
        buttonPanel.add(returnButton);

        // 将按钮面板添加到主布局
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        // 结果区域
        JTextArea resultArea = new JTextArea(10, 30);
        resultArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultArea.setEditable(false);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainPanel.add(resultScroll, gbc);

        // 添加主面板到窗口
        frame.add(mainPanel, BorderLayout.CENTER);

        // 事件处理
        queryButton.addActionListener(e -> {
            if(!isValidDate(dateField.getText())) {
                JOptionPane.showMessageDialog(frame, "日期无效（必须为未来日期且格式为YYYY-MM-DD）",
                        "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String result = "MU5112 08:00-10:00 经济舱 ¥1200 (余票5)\n"
                    + "CA1833 12:30-14:45 商务舱 ¥2400 (余票2)";
            resultArea.setText(result);
        });

        returnButton.addActionListener(e -> {
            departureField.setText("");
            destinationField.setText("");
            dateField.setValue(null);
            cabinCombo.setSelectedIndex(0);
            resultArea.setText("");
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static boolean isValidDate(String dateStr) {
        try {
            if(!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return false;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dateStr).after(new java.util.Date());
        } catch (Exception e) {
            return false;
        }
    }
}
