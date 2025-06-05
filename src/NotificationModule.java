import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class NotificationModule extends JFrame {

    private JComboBox<String> eventTypeCombo;
    private JTextField contactField;
    private JButton sendBtn;
    private JTextArea resultArea;

    // 消息模板库
    private static final Map<String, String> MESSAGE_TEMPLATES = new HashMap<>();
    static {
        MESSAGE_TEMPLATES.put("支付成功", "尊敬的客户，您的订单{orderId}已支付成功，金额：{amount}元。");
        MESSAGE_TEMPLATES.put("航班延误", "尊敬的旅客，您预订的航班{flightNo}将延误至{time}，请合理安排行程。");
        MESSAGE_TEMPLATES.put("改签成功", "您的航班改签已完成，新航班号：{flightNo}，起飞时间：{time}。");
        MESSAGE_TEMPLATES.put("退票成功", "您的订单{orderId}退票已完成，退款{amount}元将在3-5个工作日内到账。");
    }

    public NotificationModule() {
        setTitle("系统通知模块");
        setSize(600, 400);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 设置全局字体
        Font largerFont = new Font("微软雅黑", Font.PLAIN, 16);
        UIManager.put("Label.font", largerFont);
        UIManager.put("Button.font", largerFont);
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

        // 事件类型选择
        JLabel eventLabel = new JLabel("事件类型:");
        eventLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(eventLabel, gbc);

        eventTypeCombo = new JComboBox<>(new String[]{
                "支付成功", "航班延误", "改签成功", "退票成功"
        });
        eventTypeCombo.setFont(largerFont);
        gbc.gridx = 1;
        gbc.gridy = 0;
        mainPanel.add(eventTypeCombo, gbc);

        // 联系方式输入
        JLabel contactLabel = new JLabel("联系方式:");
        contactLabel.setFont(largerFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(contactLabel, gbc);

        contactField = new JTextField(20);
        contactField.setFont(largerFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(contactField, gbc);

        // 发送按钮
        sendBtn = new JButton("发送通知");
        sendBtn.setFont(largerFont);
        sendBtn.setBackground(new Color(100, 181, 246)); // 蓝色
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(sendBtn, gbc);

        // 结果显示
        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollPane, gbc);

        // 组装界面
        add(mainPanel, BorderLayout.CENTER);

        // 事件处理
        sendBtn.addActionListener(this::sendNotification);

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void sendNotification(ActionEvent e) {
        String eventType = (String) eventTypeCombo.getSelectedItem();
        String contact = contactField.getText().trim();

        // 验证输入
        if (contact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入联系方式", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 匹配消息模板
        String template = MESSAGE_TEMPLATES.get(eventType);
        if (template == null) {
            JOptionPane.showMessageDialog(this, "未找到匹配的消息模板", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 生成通知内容（模拟填充变量）
        String message = fillTemplate(template, eventType);

        // 模拟调用通知接口
        boolean sendResult = simulateSendNotification(contact, message);

        // 记录发送状态
        if (sendResult) {
            resultArea.append("通知发送成功:\n");
            resultArea.append("联系方式: " + contact + "\n");
            resultArea.append("通知内容: " + message + "\n\n");
        } else {
            resultArea.append("通知发送失败，请重试!\n\n");
        }
    }

    private String fillTemplate(String template, String eventType) {
        // 模拟填充模板变量 - 实际应根据业务数据填充
        switch (eventType) {
            case "支付成功":
                return template.replace("{orderId}", "ORD20230615001")
                        .replace("{amount}", "1280.00");
            case "航班延误":
                return template.replace("{flightNo}", "MU5112")
                        .replace("{time}", "2023-06-16 14:30");
            case "改签成功":
                return template.replace("{flightNo}", "MU5113")
                        .replace("{time}", "2023-06-16 08:00");
            case "退票成功":
                return template.replace("{orderId}", "ORD20230615002")
                        .replace("{amount}", "960.00");
            default:
                return template;
        }
    }

    private boolean simulateSendNotification(String contact, String message) {
        // 模拟发送通知 - 80%成功率
        System.out.println("模拟发送通知到: " + contact);
        System.out.println("内容: " + message);
        return Math.random() > 0.2;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NotificationModule().setVisible(true));
    }
}
