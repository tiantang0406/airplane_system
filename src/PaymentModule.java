import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PaymentModule extends JFrame {

    private double orderAmount;
    private String orderId;
    private JComboBox<String> paymentMethod;
    private JButton confirmBtn;
    private JButton cancelBtn;

    public PaymentModule(String orderId, double amount) {
        this.orderId = orderId;
        this.orderAmount = amount;

        setTitle("支付订单 - " + orderId);
        setSize(600, 350); // 增大窗口尺寸以适应更大的字体
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 设置全局字体（增大3号）
        Font largerFont = new Font("微软雅黑", Font.PLAIN, 19); // 原16号增大到19号
        Font titleFont = new Font("微软雅黑", Font.BOLD, 22); // 标题字体更大

        // 应用全局字体设置
        UIManager.put("Label.font", largerFont);
        UIManager.put("Button.font", largerFont);
        UIManager.put("ComboBox.font", largerFont);
        UIManager.put("OptionPane.messageFont", largerFont); // 对话框字体

        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25)); // 增大内边距

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // 增大组件间距
        gbc.anchor = GridBagConstraints.WEST;

        // 订单信息（使用更大字体）
        JLabel amountLabel = new JLabel("订单金额: ¥" + String.format("%.2f", orderAmount));
        amountLabel.setFont(titleFont); // 使用更大的标题字体
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(amountLabel, gbc);

        // 支付方式选择
        JLabel methodLabel = new JLabel("选择支付方式:");
        methodLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(methodLabel, gbc);

        paymentMethod = new JComboBox<>(new String[]{"微信支付", "支付宝", "银行卡"});
        paymentMethod.setFont(largerFont);
        paymentMethod.setPreferredSize(new Dimension(200, 35)); // 增大下拉框尺寸
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(paymentMethod, gbc);

        // 按钮面板（增大按钮尺寸）
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        buttonPanel.setBackground(Color.WHITE);

        confirmBtn = new JButton("确认支付");
        confirmBtn.setFont(largerFont);
        confirmBtn.setPreferredSize(new Dimension(150, 40)); // 增大按钮尺寸
        confirmBtn.setBackground(new Color(76, 175, 80)); // 绿色

        cancelBtn = new JButton("取消支付");
        cancelBtn.setFont(largerFont);
        cancelBtn.setPreferredSize(new Dimension(150, 40)); // 增大按钮尺寸
        cancelBtn.setBackground(new Color(244, 67, 54)); // 红色

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);

        // 组装界面
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 事件处理
        confirmBtn.addActionListener(this::processPayment);
        cancelBtn.addActionListener(e -> dispose());

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void processPayment(ActionEvent e) {
        String method = (String) paymentMethod.getSelectedItem();

        // 显示支付中提示（使用大字体）
        JOptionPane.showMessageDialog(this, "支付处理中...", "请稍候",
                JOptionPane.INFORMATION_MESSAGE);

        // 模拟调用支付API
        boolean paymentSuccess = simulatePaymentAPI(method);

        if (paymentSuccess) {
            // 更新订单状态
            updateOrderStatus(orderId, "已支付");
            JOptionPane.showMessageDialog(this, "支付成功!", "提示",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "支付失败，请重试", "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // 模拟支付API调用
    private boolean simulatePaymentAPI(String method) {
        try {
            Thread.sleep(1500); // 模拟网络延迟
            return Math.random() > 0.2; // 80%成功率
        } catch (InterruptedException ex) {
            return false;
        }
    }

    // 模拟更新订单状态
    private void updateOrderStatus(String orderId, String status) {
        System.out.println("更新订单 " + orderId + " 状态为: " + status);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PaymentModule("ORD1001", 1280.50).setVisible(true));
    }
}