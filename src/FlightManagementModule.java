import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class FlightManagementModule extends JFrame {

    private JTextField flightNumberField;
    private JTextField departureTimeField;
    private JTextField arrivalTimeField;
    private JComboBox<String> aircraftTypeCombo;
    private JButton submitBtn;
    private JTextArea resultArea;

    // 机型与座位数映射
    private static final Map<String, Integer> AIRCRAFT_SEATS = new HashMap<>();
    static {
        AIRCRAFT_SEATS.put("A320", 180);
        AIRCRAFT_SEATS.put("A330", 290);
        AIRCRAFT_SEATS.put("A350", 350);
        AIRCRAFT_SEATS.put("B737", 160);
        AIRCRAFT_SEATS.put("B777", 320);
        AIRCRAFT_SEATS.put("B787", 280);
    }

    public FlightManagementModule() {
        setTitle("航班管理系统");
        setSize(700, 500);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 设置全局字体
        Font contentFont = new Font("微软雅黑", Font.PLAIN, 16);
        Font titleFont = new Font("微软雅黑", Font.BOLD, 27); // 标题字号27
        UIManager.put("Label.font", contentFont);
        UIManager.put("Button.font", contentFont);
        UIManager.put("TextField.font", contentFont);
        UIManager.put("ComboBox.font", contentFont);
        UIManager.put("TextArea.font", contentFont);

        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel titleLabel = new JLabel("管理员添加新航班", SwingConstants.CENTER); // 修改标题内容
        titleLabel.setFont(titleFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // 航班号（带固定格式）
        JLabel flightNumberLabel = new JLabel("航班号（CAxxxx）:");
        flightNumberLabel.setFont(contentFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(flightNumberLabel, gbc);

        flightNumberField = new JTextField(20);
        flightNumberField.setFont(contentFont);
        flightNumberField.setToolTipText("输入2-3位字母+3-4位数字，如CA1234");
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(flightNumberField, gbc);

        // 起飞时间（带固定格式）
        JLabel departureLabel = new JLabel("起飞时间（yyyy-MM-dd HH:mm）:");
        departureLabel.setFont(contentFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(departureLabel, gbc);

        departureTimeField = new JTextField(20);
        departureTimeField.setFont(contentFont);
        departureTimeField.setToolTipText("输入格式: yyyy-MM-dd HH:mm");
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(departureTimeField, gbc);

        // 到达时间（带固定格式）
        JLabel arrivalLabel = new JLabel("到达时间（yyyy-MM-dd HH:mm）:");
        arrivalLabel.setFont(contentFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(arrivalLabel, gbc);

        arrivalTimeField = new JTextField(20);
        arrivalTimeField.setFont(contentFont);
        arrivalTimeField.setToolTipText("输入格式: yyyy-MM-dd HH:mm");
        gbc.gridx = 1;
        gbc.gridy = 3;
        mainPanel.add(arrivalTimeField, gbc);

        // 机型（包含座位数信息）
        JLabel aircraftTypeLabel = new JLabel("机型(座位数自动计算):");
        aircraftTypeLabel.setFont(contentFont);
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(aircraftTypeLabel, gbc);

        aircraftTypeCombo = new JComboBox<>(new String[]{
                "A320 (180座)",
                "A330 (230座)",
                "A350 (150座)",
                "B737 (160座)",
                "B777 (120座)",
                "B787 (280座)"
        });
        aircraftTypeCombo.setFont(contentFont);
        gbc.gridx = 1;
        gbc.gridy = 4;
        mainPanel.add(aircraftTypeCombo, gbc);

        // 提交按钮
        submitBtn = new JButton("提交航班信息");
        submitBtn.setFont(contentFont);
        submitBtn.setBackground(new Color(76, 175, 80)); // 绿色
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(submitBtn, gbc);

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
        submitBtn.addActionListener(this::submitFlightInfo);

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void submitFlightInfo(ActionEvent e) {
        // 获取输入值
        String flightNumber = flightNumberField.getText().trim();
        String departureTimeStr = departureTimeField.getText().trim();
        String arrivalTimeStr = arrivalTimeField.getText().trim();
        String aircraftTypeWithSeats = (String) aircraftTypeCombo.getSelectedItem();

        // 从机型选项中提取纯机型名称
        String aircraftType = aircraftTypeWithSeats.split(" ")[0];
        int seats = AIRCRAFT_SEATS.get(aircraftType);

        // 校验输入
        if (flightNumber.isEmpty() || departureTimeStr.isEmpty() || arrivalTimeStr.isEmpty()) {
            showError("所有字段都必须填写");
            return;
        }

        // 校验航班号格式
        if (!flightNumber.matches("[A-Z]{2,3}\\d{3,4}")) {
            showError("航班号格式不正确，示例: CA1234");
            return;
        }

        // 解析时间
        LocalDateTime departureTime, arrivalTime;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            departureTime = LocalDateTime.parse(departureTimeStr, formatter);
            arrivalTime = LocalDateTime.parse(arrivalTimeStr, formatter);

            if (arrivalTime.isBefore(departureTime)) {
                showError("到达时间不能早于起飞时间");
                return;
            }
        } catch (DateTimeParseException ex) {
            showError("时间格式不正确，请使用 yyyy-MM-dd HH:mm 格式");
            return;
        }

        // 检查航班冲突（模拟）
        if (simulateCheckFlightConflict(flightNumber, departureTime)) {
            showError("航班号已存在或时间冲突");
            return;
        }

        // 更新票价规则（模拟）
        double basePrice = simulateUpdatePriceRule(aircraftType, seats);

        // 保存航班信息（模拟）
        boolean saveResult = simulateSaveFlightInfo(
                flightNumber, departureTime, arrivalTime, aircraftType, seats, basePrice);

        if (saveResult) {
            resultArea.setText(String.format(
                    "航班添加成功!\n\n" +
                            "航班号: %s\n" +
                            "机型: %s (%d座)\n" +
                            "起飞时间: %s\n" +
                            "到达时间: %s\n" +
                            "基础票价: ¥%.2f",
                    flightNumber, aircraftType, seats,
                    departureTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    arrivalTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    basePrice));
        } else {
            showError("航班保存失败，请重试");
        }
    }

    private void showError(String message) {
        resultArea.setText("错误: " + message);
    }

    // 模拟检查航班冲突
    private boolean simulateCheckFlightConflict(String flightNumber, LocalDateTime departureTime) {
        // 实际应从数据库检查
        // 这里模拟10%的冲突概率
        return Math.random() < 0.1;
    }

    // 模拟更新票价规则
    private double simulateUpdatePriceRule(String aircraftType, int seats) {
        // 根据机型计算基础票价
        double basePrice = 1000.00; // 默认基础票价

        switch (aircraftType) {
            case "A330":
            case "B777":
                basePrice = 1500.00;
                break;
            case "A350":
            case "B787":
                basePrice = 1800.00;
                break;
        }

        // 座位数影响票价
        if (seats < 200) {
            basePrice *= 1.2; // 小型飞机票价上浮
        } else if (seats > 300) {
            basePrice *= 0.9; // 大型飞机票价下调
        }

        return basePrice;
    }

    // 模拟保存航班信息
    private boolean simulateSaveFlightInfo(String flightNumber, LocalDateTime departureTime,
                                           LocalDateTime arrivalTime, String aircraftType,
                                           int seats, double basePrice) {
        System.out.println("保存航班信息: " + flightNumber);
        // 模拟90%的成功率
        return Math.random() < 0.9;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlightManagementModule().setVisible(true));
    }
}
