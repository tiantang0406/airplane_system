import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RescheduleUpgradeModule extends JFrame {

    private String orderId;
    private JComboBox<String> originalFlightCombo;
    private JComboBox<String> targetFlightCombo;
    private JComboBox<String> classCombo;
    private JButton checkBtn;
    private JButton confirmBtn;
    private JLabel resultLabel;
    private double priceDifference = 0;

    public RescheduleUpgradeModule(String orderId) {
        this.orderId = orderId;

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

        originalFlightCombo = new JComboBox<>(new String[]{
                "MU5112 北京-上海 2023-06-15",
                "CA1833 广州-成都 2023-06-20",
                "CZ3108 深圳-重庆 2023-06-25"
        });
        originalFlightCombo.setFont(largerFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(originalFlightCombo, gbc);

        // 选择目标航班
        JLabel flightLabel = new JLabel("选择目标航班:");
        flightLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        mainPanel.add(flightLabel, gbc);

        targetFlightCombo = new JComboBox<>(new String[]{
                "MU5113 北京-上海 2023-06-16",
                "MU5115 北京-上海 2023-06-17",
                "CA1835 北京-上海 2023-06-18"
        });
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
    }

    private void checkAvailability(ActionEvent e) {
        String originalFlight = (String) originalFlightCombo.getSelectedItem();
        String targetFlight = (String) targetFlightCombo.getSelectedItem();
        String targetClass = (String) classCombo.getSelectedItem();

        if (originalFlight.equals(targetFlight)) {
            JOptionPane.showMessageDialog(this, "原航班和目标航班不能相同", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 模拟检查可用性和计算差价
        AvailabilityResult result = simulateCheckAvailability(originalFlight, targetFlight, targetClass);

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
    }

    // 模拟检查可用性
    private AvailabilityResult simulateCheckAvailability(String originalFlight, String targetFlight, String targetClass) {
        AvailabilityResult result = new AvailabilityResult();

        // 模拟数据 - 实际应从数据库获取
        result.isAvailable = true;
        result.remainingSeats = (int)(Math.random() * 10) + 1; // 1-10随机余票

        // 计算原航班价格
        double originalPrice = 1200.00; // 默认经济舱价格
        if (originalFlight.contains("CA1833")) originalPrice = 1500.00;
        else if (originalFlight.contains("CZ3108")) originalPrice = 900.00;

        // 计算目标航班价格
        double targetPrice = originalPrice;

        if (targetClass.equals("商务舱")) targetPrice = 2800.00;
        else if (targetClass.equals("头等舱")) targetPrice = 3800.00;

        // 不同航班可能有不同价格
        if (targetFlight.contains("MU5113")) targetPrice *= 1.1;
        else if (targetFlight.contains("CA1835")) targetPrice *= 0.9;

        result.priceDifference = targetPrice - originalPrice;

        return result;
    }

    // 模拟支付
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
    }

    // 可用性结果内部类
    private class AvailabilityResult {
        boolean isAvailable;
        int remainingSeats;
        double priceDifference;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RescheduleUpgradeModule("ORD1001").setVisible(true));
    }
}
