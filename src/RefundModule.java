import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RefundModule extends JFrame {

    private String orderId;
    private JComboBox<String> flightCombo;
    private JComboBox<String> reasonCombo;
    private JButton checkBtn;
    private JButton confirmBtn;
    private JLabel resultLabel;
    private double refundAmount = 0;

    public RefundModule(String orderId) {
        this.orderId = orderId;

        setTitle("退票申请");
        setSize(700, 550); // 增大窗口尺寸以适应更大的字体
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 设置全局字体（增大3号）
        Font largerFont = new Font("微软雅黑", Font.PLAIN, 19); // 原16增大到19
        Font titleFont = new Font("微软雅黑", Font.BOLD, 27); // 标题27号
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
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题（27号，居中）
        JLabel titleLabel = new JLabel("退票申请", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // 选择航班
        JLabel flightLabel = new JLabel("选择航班:");
        flightLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(flightLabel, gbc);

        flightCombo = new JComboBox<>(new String[]{"MU5112 北京-上海 2023-06-15",
                "CA1833 广州-成都 2023-06-20",
                "CZ3108 深圳-重庆 2023-06-25"});
        flightCombo.setFont(largerFont);
        flightCombo.setPreferredSize(new Dimension(300, 35)); // 增大下拉框
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(flightCombo, gbc);

        // 退票原因
        JLabel reasonLabel = new JLabel("退票原因:");
        reasonLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        mainPanel.add(reasonLabel, gbc);

        reasonCombo = new JComboBox<>(new String[]{
                "行程变更",
                "航班延误/取消",
                "价格因素",
                "个人原因",
                "其他原因"
        });
        reasonCombo.setFont(largerFont);
        reasonCombo.setPreferredSize(new Dimension(300, 35));
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(reasonCombo, gbc);

        // 检查退票规则按钮
        checkBtn = new JButton("检查退票规则");
        checkBtn.setFont(largerFont);
        checkBtn.setBackground(new Color(255, 193, 7)); // 琥珀色
        checkBtn.setPreferredSize(new Dimension(200, 40)); // 增大按钮
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(checkBtn, gbc);

        // 结果显示
        resultLabel = new JLabel(" ", SwingConstants.CENTER);
        resultLabel.setFont(largerFont);
        resultLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(resultLabel, gbc);

        // 底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        buttonPanel.setBackground(Color.WHITE);

        confirmBtn = new JButton("确认退票");
        confirmBtn.setFont(largerFont);
        confirmBtn.setBackground(new Color(244, 67, 54)); // 红色
        confirmBtn.setPreferredSize(new Dimension(180, 45)); // 增大按钮
        confirmBtn.setEnabled(false);

        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(largerFont);
        cancelBtn.setPreferredSize(new Dimension(180, 45));

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);

        // 组装界面
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 事件处理
        checkBtn.addActionListener(this::checkRefundPolicy);
        confirmBtn.addActionListener(this::processRefund);
        cancelBtn.addActionListener(e -> dispose());

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void checkRefundPolicy(ActionEvent e) {
        String selectedFlight = (String) flightCombo.getSelectedItem();
        String reason = (String) reasonCombo.getSelectedItem();

        if (selectedFlight == null || selectedFlight.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择要退票的航班", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 模拟检查退票规则
        RefundResult result = simulateCheckRefundPolicy(orderId, selectedFlight);

        if (result.isRefundable) {
            refundAmount = result.amount;
            resultLabel.setText(String.format(
                    "<html><center>航班: %s<br>退票原因: %s<br>可退金额: <b>¥%.2f</b><br>手续费: ¥%.2f<br>起飞时间: %s</center></html>",
                    selectedFlight, reason, result.amount, result.fee, result.departureTime));
            resultLabel.setForeground(new Color(0, 100, 0)); // 深绿色
            confirmBtn.setEnabled(true);
        } else {
            resultLabel.setText("<html><center><b>该航班不符合退票条件</b><br>" + result.message + "</center></html>");
            resultLabel.setForeground(Color.RED);
            confirmBtn.setEnabled(false);
        }
    }

    private void processRefund(ActionEvent e) {
        String selectedFlight = (String) flightCombo.getSelectedItem();
        String reason = (String) reasonCombo.getSelectedItem();

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("<html><center>确认退票?<br>航班: %s<br>退票原因: %s<br>退款金额: ¥%.2f</center></html>",
                        selectedFlight, reason, refundAmount),
                "确认退票", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // 模拟退款处理
            boolean success = simulateProcessRefund(orderId, refundAmount);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        String.format("退票成功! 退款金额: ¥%.2f 将在3-5个工作日内退回", refundAmount),
                        "退票成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "退款处理失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 模拟检查退票规则
    private RefundResult simulateCheckRefundPolicy(String orderId, String flightInfo) {
        RefundResult result = new RefundResult();

        // 从航班信息中提取日期
        String flightDateStr = flightInfo.substring(flightInfo.length() - 10);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date now = new Date();
            Date departure = sdf.parse(flightDateStr);

            long diffHours = (departure.getTime() - now.getTime()) / (60 * 60 * 1000);

            // 根据航班设置不同票价
            double ticketPrice = 1000.00; // 默认
            if (flightInfo.contains("MU5112")) ticketPrice = 1200.00;
            else if (flightInfo.contains("CA1833")) ticketPrice = 1500.00;
            else if (flightInfo.contains("CZ3108")) ticketPrice = 900.00;

            if (diffHours > 24) {
                // 起飞前24小时以上可全额退款
                result.isRefundable = true;
                result.amount = ticketPrice;
                result.fee = 0.00;
                result.departureTime = flightDateStr + " 14:00";
                result.message = "起飞前24小时以上可全额退款";
            } else if (diffHours > 2) {
                // 起飞前2-24小时收取20%手续费
                result.isRefundable = true;
                result.amount = ticketPrice * 0.8;
                result.fee = ticketPrice * 0.2;
                result.departureTime = flightDateStr + " 14:00";
                result.message = "起飞前2-24小时收取20%手续费";
            } else {
                result.isRefundable = false;
                result.message = "起飞前2小时内不可退票";
            }
        } catch (Exception e) {
            result.isRefundable = false;
            result.message = "航班信息异常";
        }

        return result;
    }

    // 模拟退款处理
    private boolean simulateProcessRefund(String orderId, double amount) {
        System.out.println("处理退款 - 订单: " + orderId + ", 金额: " + amount);
        // 实际应调用支付系统退款接口
        return true; // 模拟总是成功
    }

    // 退款结果内部类
    private class RefundResult {
        boolean isRefundable;
        double amount;
        double fee;
        String departureTime;
        String message;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RefundModule("ORD1001").setVisible(true));
    }
}
