import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.sql.*;

public class SeatSelectionModule extends JFrame {

    private static final String DB_URL = "jdbc:sqlite:airplane_system.db";
    private String orderId;
    private String currentUser;
    private JComboBox<String> seatPreference;
    private JButton searchBtn;
    private JButton confirmBtn;
    private JPanel seatMapPanel;
    private Map<String, JButton> seatButtons = new HashMap<>();
    private String selectedSeat = null;
      // 订单信息
    private String flightNumber;
    private String passengerName;
    private String flightId;
    private String departureAirport;
    private String arrivalAirport;

    public SeatSelectionModule(String orderId, String currentUser) {
        this.orderId = orderId;
        this.currentUser = currentUser;

        // 加载订单数据
        if (!loadOrderFromDatabase()) {
            JOptionPane.showMessageDialog(this, "订单信息加载失败或订单不存在！", "错误", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }        setTitle("选座值机 - " + flightNumber + " (" + passengerName + ")");
        setSize(1200, 800);
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
        
        JLabel flightLabel = new JLabel("航班: " + flightNumber + " " + departureAirport + "→" + arrivalAirport);
        flightLabel.setFont(largerFont);
        
        JLabel passengerLabel = new JLabel("乘客: " + passengerName);
        passengerLabel.setFont(largerFont);

        JLabel prefLabel = new JLabel("座位偏好:");
        prefLabel.setFont(largerFont);

        seatPreference = new JComboBox<>(new String[]{"无偏好", "靠窗", "过道"});
        seatPreference.setFont(largerFont);

        searchBtn = new JButton("查询可选座位");
        searchBtn.setFont(largerFont);        inputPanel.add(orderLabel);
        inputPanel.add(flightLabel);
        inputPanel.add(passengerLabel);
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
        cancelBtn.addActionListener(_ -> dispose());        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    /**
     * 从数据库加载订单信息
     */
    private boolean loadOrderFromDatabase() {
        String sql = "SELECT o.order_id, o.user_id, o.passenger_name, o.seat_number, o.payment_status, " +
                    "f.flight_id, f.flight_number, f.departure_airport, f.arrival_airport, f.departure_time " +
                    "FROM orders o " +
                    "JOIN flights f ON o.flight_id = f.flight_id " +
                    "WHERE o.order_id = ? AND o.user_id = ? AND o.payment_status = 'paid'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, orderId);
            stmt.setString(2, currentUser);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    passengerName = rs.getString("passenger_name");
                    flightId = rs.getString("flight_id");                    flightNumber = rs.getString("flight_number");
                    departureAirport = rs.getString("departure_airport");
                    arrivalAirport = rs.getString("arrival_airport");
                    
                    // 检查是否已经选座
                    String currentSeat = rs.getString("seat_number");
                    if (currentSeat != null && !currentSeat.trim().isEmpty()) {
                        int choice = JOptionPane.showConfirmDialog(this, 
                            "您已选择座位: " + currentSeat + "\n是否要重新选座？", 
                            "已选座位", JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.NO_OPTION) {
                            dispose();
                            return false;
                        }
                    }
                    
                    return true;
                } else {
                    return false; // 订单不存在或不属于当前用户或状态不是已支付
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }    private void loadSeatMap(ActionEvent e) {
        String preference = (String) seatPreference.getSelectedItem();

        // 清空现有座位图
        seatMapPanel.removeAll();
        seatButtons.clear();
        selectedSeat = null;
        confirmBtn.setEnabled(false);

        // 从数据库获取座位占用情况
        String[][] seatMap = getSeatMapFromDatabase(flightId, preference);

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
                    seatBtn.addActionListener(_ -> selectSeat(seatBtn));
                }
                else if (seat.startsWith("X")) {
                    String seatNumber = seat.substring(1); // 提取座位号
                    seatBtn.setText(seatNumber);
                    seatBtn.setBackground(Color.RED); // 已占用显示红色
                    seatBtn.setEnabled(false);
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
    }    private void confirmSelection(ActionEvent e) {
        if (selectedSeat != null) {
            // 更新数据库中的座位信息
            boolean success = updateSeatInDatabase(orderId, selectedSeat);

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

    /**
     * 从数据库获取座位图，包含已占用的座位信息
     */
    private String[][] getSeatMapFromDatabase(String flightId, String preference) {
        // 基础座位图布局
        String[][] seatMap = {
                {"EMPTY","EXIT",  "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EXIT", "EMPTY"},
                {"WINDOW","O1A",  "O1B", "AISLE", "O1C", "O1D", "O1E", "O1F","AISLE",  "O1G", "O1H", "WINDOW"},
                {"WINDOW","O2A",  "O2B", "AISLE", "O2C", "O2D", "O2E", "O2F","AISLE",  "O2G", "O2H", "WINDOW"},
                {"WINDOW","O3A",  "O3B", "AISLE", "O3C", "O3D", "O3E", "O3F","AISLE",  "O3G", "O3H", "WINDOW"},
                {"WINDOW","O4A",  "O4B", "AISLE", "O4C", "O4D", "O4E", "O4F","AISLE",  "O4G", "O4H", "WINDOW"},
                {"WINDOW","O5A",  "O5B", "AISLE", "O5C", "O5D", "O5E", "O5F","AISLE",  "O5G", "O5H", "WINDOW"},
                {"WINDOW","O6A",  "O6B", "AISLE", "O6C", "O6D", "O6E", "O6F","AISLE",  "O6G", "O6H", "WINDOW"},
                {"WINDOW","O7A",  "O7B", "AISLE", "O7C", "O7D", "O7E", "O7F","AISLE",  "O7G", "O7H", "WINDOW"},
                {"EMPTY","EXIT",  "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EXIT", "EMPTY"},
                {"EMPTY","TOILET",  "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "TOILET", "EMPTY"}
        };

        // 从数据库获取已占用的座位
        String sql = "SELECT seat_number FROM orders WHERE flight_id = ? AND seat_number IS NOT NULL AND seat_number != ''";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, flightId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String occupiedSeat = rs.getString("seat_number");
                    
                    // 将已占用的座位标记为 X + 座位号
                    for (int i = 0; i < seatMap.length; i++) {
                        for (int j = 0; j < seatMap[i].length; j++) {
                            if (seatMap[i][j].equals("O" + occupiedSeat)) {
                                seatMap[i][j] = "X" + occupiedSeat;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return seatMap;
    }

    /**
     * 更新数据库中的座位信息
     */
    private boolean updateSeatInDatabase(String orderId, String seatNumber) {
        String sql = "UPDATE orders SET seat_number = ? WHERE order_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, seatNumber);
            stmt.setString(2, orderId);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("更新订单 " + orderId + " 座位为: " + seatNumber);
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }    // 生成电子登机牌
    private void generateBoardingPass(String orderId, String seat) {
        System.out.println("生成电子登机牌 - 订单: " + orderId + ", 座位: " + seat + ", 航班: " + flightNumber);
        // 这里可以扩展为生成实际的登机牌PDF或显示登机牌信息窗口
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SeatSelectionModule("ORD1001", "user1").setVisible(true));
    }
}
