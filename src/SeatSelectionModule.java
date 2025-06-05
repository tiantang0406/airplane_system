import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class SeatSelectionModule extends JFrame {

    private String orderId;
    private JComboBox<String> seatPreference;
    private JButton searchBtn;
    private JButton confirmBtn;
    private JPanel seatMapPanel;
    private Map<String, JButton> seatButtons = new HashMap<>();
    private String selectedSeat = null;

    public SeatSelectionModule(String orderId) {
        this.orderId = orderId;

        setTitle("选座值机 - 订单号: " + orderId);
        setSize(1200, 800); // 增大窗口宽度以适应12列
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 设置全局字体
        Font largerFont = new Font("微软雅黑", Font.PLAIN, 16);
        UIManager.put("Label.font", largerFont);
        UIManager.put("Button.font", largerFont);
        UIManager.put("ComboBox.font", largerFont);

        // 顶部输入面板
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inputPanel.setBackground(Color.WHITE);

        JLabel orderLabel = new JLabel("订单号: " + orderId);
        orderLabel.setFont(largerFont);

        JLabel prefLabel = new JLabel("座位偏好:");
        prefLabel.setFont(largerFont);

        seatPreference = new JComboBox<>(new String[]{"无偏好", "靠窗", "过道"});
        seatPreference.setFont(largerFont);

        searchBtn = new JButton("查询可选座位");
        searchBtn.setFont(largerFont);

        inputPanel.add(orderLabel);
        inputPanel.add(prefLabel);
        inputPanel.add(seatPreference);
        inputPanel.add(searchBtn);

        // 座位图面板 - 使用GridLayout
        seatMapPanel = new JPanel(new GridLayout(0, 12, 5, 5)); // 12列
        seatMapPanel.setBackground(Color.WHITE);
        seatMapPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 底部确认面板
        JPanel confirmPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        confirmPanel.setBackground(Color.WHITE);

        confirmBtn = new JButton("确认选座");
        confirmBtn.setFont(largerFont);
        confirmBtn.setEnabled(false);

        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(largerFont);

        confirmPanel.add(confirmBtn);
        confirmPanel.add(cancelBtn);

        // 组装界面
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(seatMapPanel), BorderLayout.CENTER);
        add(confirmPanel, BorderLayout.SOUTH);

        // 事件处理
        searchBtn.addActionListener(this::loadSeatMap);
        confirmBtn.addActionListener(this::confirmSelection);
        cancelBtn.addActionListener(e -> dispose());

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void loadSeatMap(ActionEvent e) {
        String preference = (String) seatPreference.getSelectedItem();

        // 清空现有座位图
        seatMapPanel.removeAll();
        seatButtons.clear();
        selectedSeat = null;
        confirmBtn.setEnabled(false);

        // 模拟从数据库获取座位图
        String[][] seatMap = simulateGetSeatMap(orderId, preference);

        // 显示座位图
        for (int i = 0; i < seatMap.length; i++) {
            for (int j = 0; j < seatMap[i].length; j++) {
                String seat = seatMap[i][j];
                JButton seatBtn = new JButton();
                seatBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                seatBtn.setPreferredSize(new Dimension(70, 40));

                // 处理特殊区域
                if (seat.equals("EXIT")) {
                    seatBtn.setText("安全出口");
                    seatBtn.setBackground(Color.ORANGE);
                    seatBtn.setEnabled(false);
                }
                else if (seat.equals("AISLE")) {
                    seatBtn.setText("过道");
                    seatBtn.setBackground(Color.LIGHT_GRAY);
                    seatBtn.setEnabled(false);
                }
                else if (seat.equals("TOILET")) {
                    seatBtn.setText("洗手间");
                    seatBtn.setBackground(Color.PINK);
                    seatBtn.setEnabled(false);
                }
                else if (seat.equals("WINDOW")) {
                    seatBtn.setText("窗户");
                    seatBtn.setBackground(new Color(173, 216, 230)); // 浅蓝色
                    seatBtn.setEnabled(false);
                }
                else if (seat.equals("EMPTY")) {
                    seatBtn.setText("");
                    seatBtn.setEnabled(false);
                    seatBtn.setBackground(Color.LIGHT_GRAY);
                }
                else if (seat.startsWith("O")) {
                    String seatNumber = seat.substring(1); // 提取座位号
                    seatBtn.setText(seatNumber);
                    seatBtn.setBackground(Color.WHITE);
                    seatBtn.addActionListener(ev -> selectSeat(seatBtn));
                }
                else {
                    seatBtn.setText(seat);
                    seatBtn.setEnabled(false);
                    seatBtn.setBackground(Color.WHITE);
                }

                seatMapPanel.add(seatBtn);
                seatButtons.put(seatBtn.getText(), seatBtn);
            }
        }

        seatMapPanel.revalidate();
        seatMapPanel.repaint();
    }

    private void selectSeat(JButton seatBtn) {
        // 重置所有座位按钮颜色
        seatButtons.values().forEach(btn -> {
            if (btn.isEnabled() && !btn.getText().matches("安全出口|过道|洗手间|窗户")) {
                btn.setBackground(Color.WHITE);
            }
        });

        // 设置选中座位
        selectedSeat = seatBtn.getText();
        seatBtn.setBackground(Color.CYAN);
        confirmBtn.setEnabled(true);
    }

    private void confirmSelection(ActionEvent e) {
        if (selectedSeat != null) {
            // 模拟更新数据库
            boolean success = simulateUpdateSeatStatus(orderId, selectedSeat);

            if (success) {
                // 生成电子登机牌
                generateBoardingPass(orderId, selectedSeat);
                JOptionPane.showMessageDialog(this,
                        "选座成功! 座位号: " + selectedSeat + "\n已生成电子登机牌",
                        "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "选座失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 模拟获取座位图（符合真实飞机布局）
    private String[][] simulateGetSeatMap(String orderId, String preference) {
        // 10行12列的飞机座位图
        String[][] seatMap = {
                {"EMPTY","EXIT",  "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EXIT", "EMPTY"}, // 第1行
                {"WINDOW","O1A",  "O1B", "AISLE", "O1C", "O1D", "O1E", "O1F","AISLE",  "O1G", "O1H", "WINDOW"}, // 第2行
                {"WINDOW","O2A",  "O2B", "AISLE", "O2C", "O2D", "O2E", "O2F","AISLE",  "O2G", "O2H", "WINDOW"}, // 第3行
                {"WINDOW","O3A",  "O3B", "AISLE", "O3C", "O3D", "O3E", "O3F","AISLE",  "O3G", "O3H", "WINDOW"}, // 第4行
                {"WINDOW","O4A",  "O4B", "AISLE", "O4C", "O4D", "O4E", "O4F","AISLE",  "O4G", "O4H", "WINDOW"}, // 第5行
                {"WINDOW","O5A",  "O5B", "AISLE", "O5C", "O5D", "O5E", "O5F","AISLE",  "O5G", "O5H", "WINDOW"}, // 第6行
                {"WINDOW","O6A",  "O6B", "AISLE", "O6C", "O6D", "O6E", "O6F","AISLE",  "O6G", "O6H", "WINDOW"}, // 第7行
                {"WINDOW","O7A",  "O7B", "AISLE", "O7C", "O7D", "O7E", "O7F","AISLE",  "O7G", "O7H", "WINDOW"}, // 第8行
                {"EMPTY","EXIT",  "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EXIT", "EMPTY"}, // 第9行
                {"EMPTY","TOILET",  "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "TOILET", "EMPTY"} // 第10行
        };



        return seatMap;
    }

    // 模拟更新座位状态
    private boolean simulateUpdateSeatStatus(String orderId, String seat) {
        System.out.println("更新订单 " + orderId + " 座位为: " + seat);
        return true;
    }

    // 生成电子登机牌
    private void generateBoardingPass(String orderId, String seat) {
        System.out.println("生成电子登机牌 - 订单: " + orderId + ", 座位: " + seat);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SeatSelectionModule("ORD1001").setVisible(true));
    }
}
