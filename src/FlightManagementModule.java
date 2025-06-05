import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class FlightManagementModule extends JFrame {

    // 数据库连接常量
    private static final String DB_URL = "jdbc:sqlite:airplane_system.db";
    
    private JTextField flightNumberField;
    private JTextField departureAirportField;
    private JTextField arrivalAirportField;
    private JTextField departureTimeField;
    private JTextField arrivalTimeField;
    private JComboBox<String> aircraftTypeCombo;
    private JTextField basePriceField;
    private JButton submitBtn;
    private JButton queryBtn;
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
        setSize(800, 600);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 设置全局字体
        Font contentFont = new Font("微软雅黑", Font.PLAIN, 16);
        Font titleFont = new Font("微软雅黑", Font.BOLD, 20);
        UIManager.put("Label.font", contentFont);
        UIManager.put("Button.font", contentFont);
        UIManager.put("TextField.font", contentFont);
        UIManager.put("ComboBox.font", contentFont);
        UIManager.put("TextArea.font", contentFont);

        // 标题
        JLabel titleLabel = new JLabel("航班管理系统", JLabel.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 航班号
        JLabel flightNumberLabel = new JLabel("航班号:");
        flightNumberLabel.setFont(contentFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(flightNumberLabel, gbc);

        flightNumberField = new JTextField(20);
        flightNumberField.setFont(contentFont);
        flightNumberField.setToolTipText("输入航班号，如MU5112");
        gbc.gridx = 1;
        gbc.gridy = 0;
        mainPanel.add(flightNumberField, gbc);

        // 出发机场
        JLabel departureAirportLabel = new JLabel("出发机场:");
        departureAirportLabel.setFont(contentFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(departureAirportLabel, gbc);

        departureAirportField = new JTextField(20);
        departureAirportField.setFont(contentFont);
        departureAirportField.setToolTipText("输入出发机场三字码，如PEK");
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(departureAirportField, gbc);

        // 到达机场
        JLabel arrivalAirportLabel = new JLabel("到达机场:");
        arrivalAirportLabel.setFont(contentFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(arrivalAirportLabel, gbc);

        arrivalAirportField = new JTextField(20);
        arrivalAirportField.setFont(contentFont);
        arrivalAirportField.setToolTipText("输入到达机场三字码，如SHA");
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(arrivalAirportField, gbc);

        // 起飞时间
        JLabel departureLabel = new JLabel("起飞时间（yyyy-MM-dd HH:mm）:");
        departureLabel.setFont(contentFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(departureLabel, gbc);

        departureTimeField = new JTextField(20);
        departureTimeField.setFont(contentFont);
        departureTimeField.setToolTipText("输入格式: 2024-06-15 08:00");
        gbc.gridx = 1;
        gbc.gridy = 3;
        mainPanel.add(departureTimeField, gbc);

        // 到达时间
        JLabel arrivalLabel = new JLabel("到达时间（yyyy-MM-dd HH:mm）:");
        arrivalLabel.setFont(contentFont);
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(arrivalLabel, gbc);

        arrivalTimeField = new JTextField(20);
        arrivalTimeField.setFont(contentFont);
        arrivalTimeField.setToolTipText("输入格式: 2024-06-15 10:30");
        gbc.gridx = 1;
        gbc.gridy = 4;
        mainPanel.add(arrivalTimeField, gbc);

        // 机型
        JLabel aircraftTypeLabel = new JLabel("机型:");
        aircraftTypeLabel.setFont(contentFont);
        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(aircraftTypeLabel, gbc);

        aircraftTypeCombo = new JComboBox<>(new String[]{
                "A320 (180座)",
                "A330 (290座)",
                "A350 (350座)",
                "B737 (160座)",
                "B777 (320座)",
                "B787 (280座)"
        });
        aircraftTypeCombo.setFont(contentFont);
        gbc.gridx = 1;
        gbc.gridy = 5;
        mainPanel.add(aircraftTypeCombo, gbc);

        // 基础票价
        JLabel basePriceLabel = new JLabel("基础票价:");
        basePriceLabel.setFont(contentFont);
        gbc.gridx = 0;
        gbc.gridy = 6;
        mainPanel.add(basePriceLabel, gbc);

        basePriceField = new JTextField(20);
        basePriceField.setFont(contentFont);
        basePriceField.setToolTipText("输入基础票价，如580");
        gbc.gridx = 1;
        gbc.gridy = 6;
        mainPanel.add(basePriceField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        submitBtn = new JButton("添加航班");
        submitBtn.setFont(contentFont);
        submitBtn.setBackground(new Color(76, 175, 80));
        submitBtn.setForeground(Color.WHITE);
        buttonPanel.add(submitBtn);

        queryBtn = new JButton("查询航班");
        queryBtn.setFont(contentFont);
        queryBtn.setBackground(new Color(33, 150, 243));
        queryBtn.setForeground(Color.WHITE);
        buttonPanel.add(queryBtn);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(buttonPanel, gbc);

        // 结果显示
        resultArea = new JTextArea(12, 50);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(contentFont);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainPanel.add(scrollPane, gbc);

        // 组装界面
        add(mainPanel, BorderLayout.CENTER);

        // 事件处理
        submitBtn.addActionListener(this::submitFlightInfo);
        queryBtn.addActionListener(this::queryFlightInfo);

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void submitFlightInfo(ActionEvent e) {
        // 获取输入值
        String flightNumber = flightNumberField.getText().trim();
        String departureAirport = departureAirportField.getText().trim();
        String arrivalAirport = arrivalAirportField.getText().trim();
        String departureTime = departureTimeField.getText().trim();
        String arrivalTime = arrivalTimeField.getText().trim();
        String basePriceStr = basePriceField.getText().trim();

        // 验证输入
        if (flightNumber.isEmpty() || departureAirport.isEmpty() || arrivalAirport.isEmpty() ||
            departureTime.isEmpty() || arrivalTime.isEmpty() || basePriceStr.isEmpty()) {
            resultArea.setText("错误：所有字段都必须填写！");
            return;
        }

        try {
            // 验证时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime depTime = LocalDateTime.parse(departureTime, formatter);
            LocalDateTime arrTime = LocalDateTime.parse(arrivalTime, formatter);
            
            if (!arrTime.isAfter(depTime)) {
                resultArea.setText("错误：到达时间必须晚于起飞时间！");
                return;
            }

            double basePrice = Double.parseDouble(basePriceStr);
            if (basePrice <= 0) {
                resultArea.setText("错误：票价必须大于0！");
                return;
            }

            // 获取选中的机型
            String selectedAircraft = (String) aircraftTypeCombo.getSelectedItem();
            String aircraftType = selectedAircraft.substring(0, selectedAircraft.indexOf(' '));
            
            // 保存到数据库
            boolean success = saveFlightToDatabase(flightNumber, departureAirport, arrivalAirport, 
                                                 departureTime, arrivalTime, aircraftType, basePrice);
            
            if (success) {
                resultArea.setText("航班信息保存成功！\n" +
                                 "航班号: " + flightNumber + "\n" +
                                 "航线: " + departureAirport + " → " + arrivalAirport + "\n" +
                                 "起飞时间: " + departureTime + "\n" +
                                 "到达时间: " + arrivalTime + "\n" +
                                 "机型: " + aircraftType + "\n" +
                                 "基础票价: ¥" + basePrice);
                clearFields();
            } else {
                resultArea.setText("错误：保存航班信息失败，请检查数据库连接或航班号是否重复！");
            }

        } catch (DateTimeParseException ex) {
            resultArea.setText("错误：时间格式不正确，请使用 yyyy-MM-dd HH:mm 格式！");
        } catch (NumberFormatException ex) {
            resultArea.setText("错误：票价格式不正确，请输入有效数字！");
        } catch (Exception ex) {
            resultArea.setText("错误：" + ex.getMessage());
        }
    }

    // 数据库操作方法
    private boolean saveFlightToDatabase(String flightNumber, String departureAirport, 
                                       String arrivalAirport, String departureTime, 
                                       String arrivalTime, String aircraftType, double basePrice) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // 首先检查航班号是否已存在
            String checkSql = "SELECT COUNT(*) FROM flights WHERE flight_number = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, flightNumber);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // 航班号已存在
                }
            }

            // 生成航班ID
            String flightId = "FL" + System.currentTimeMillis();
            
            // 根据机型获取可用座位数
            int availableSeats = AIRCRAFT_SEATS.getOrDefault(aircraftType, 180);
            
            // 随机选择一个飞机
            String aircraftId = getRandomAircraftByType(aircraftType);
            
            // 插入航班信息
            String insertSql = "INSERT INTO flights (flight_id, flight_number, aircraft_id, " +
                             "departure_airport, arrival_airport, departure_time, arrival_time, " +
                             "base_price, available_seats, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'scheduled')";
            
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, flightId);
                stmt.setString(2, flightNumber);
                stmt.setString(3, aircraftId);
                stmt.setString(4, departureAirport);
                stmt.setString(5, arrivalAirport);
                stmt.setString(6, departureTime + ":00"); // 添加秒数
                stmt.setString(7, arrivalTime + ":00");
                stmt.setDouble(8, basePrice);
                stmt.setInt(9, availableSeats);
                
                int result = stmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getRandomAircraftByType(String aircraftType) {
        // 根据机型返回对应的飞机ID
        switch (aircraftType) {
            case "A320": return "B-001A";
            case "A330": return "B-002A";
            case "A350": return "B-003A";
            case "B737": return "B-004B";
            case "B777": return "B-005B";
            case "B787": return "B-006B";
            default: return "B-001A";
        }
    }

    private void clearFields() {
        flightNumberField.setText("");
        departureAirportField.setText("");
        arrivalAirportField.setText("");
        departureTimeField.setText("");
        arrivalTimeField.setText("");
        basePriceField.setText("");
        aircraftTypeCombo.setSelectedIndex(0);
    }

    private void queryFlightInfo(ActionEvent e) {
        String flightNumber = flightNumberField.getText().trim();
        if (flightNumber.isEmpty()) {
            resultArea.setText("请输入要查询的航班号！");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "SELECT f.*, a.airport_name as dep_name, b.airport_name as arr_name, " +
                        "ac.aircraft_type, ac.total_seats " +
                        "FROM flights f " +
                        "LEFT JOIN airports a ON f.departure_airport = a.airport_code " +
                        "LEFT JOIN airports b ON f.arrival_airport = b.airport_code " +
                        "LEFT JOIN aircraft ac ON f.aircraft_id = ac.aircraft_id " +
                        "WHERE f.flight_number = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, flightNumber);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    StringBuilder result = new StringBuilder();
                    result.append("=== 航班查询结果 ===\n");
                    result.append("航班号: ").append(rs.getString("flight_number")).append("\n");
                    result.append("出发机场: ").append(rs.getString("departure_airport"))
                          .append(" (").append(rs.getString("dep_name")).append(")\n");
                    result.append("到达机场: ").append(rs.getString("arrival_airport"))
                          .append(" (").append(rs.getString("arr_name")).append(")\n");
                    result.append("起飞时间: ").append(rs.getString("departure_time")).append("\n");
                    result.append("到达时间: ").append(rs.getString("arrival_time")).append("\n");
                    result.append("机型: ").append(rs.getString("aircraft_type")).append("\n");
                    result.append("总座位数: ").append(rs.getInt("total_seats")).append("\n");
                    result.append("可用座位: ").append(rs.getInt("available_seats")).append("\n");
                    result.append("基础票价: ¥").append(rs.getDouble("base_price")).append("\n");
                    result.append("航班状态: ").append(rs.getString("status")).append("\n");
                    
                    resultArea.setText(result.toString());
                } else {
                    resultArea.setText("未找到航班号为 " + flightNumber + " 的航班信息！");
                }
            }
        } catch (SQLException ex) {
            resultArea.setText("查询失败：" + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlightManagementModule().setVisible(true));
    }
}
