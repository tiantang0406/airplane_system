import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FlightQueryModule {
    
    /**
     * 数据库管理器 - 处理航班查询数据库操作
     */
    public static class DatabaseManager {
        private static final String DB_URL = "jdbc:sqlite:airplane_system.db";
        
        static {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                System.err.println("SQLite JDBC Driver not found.");
                e.printStackTrace();
            }
        }
        
        /**
         * 获取数据库连接
         */
        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(DB_URL);
        }
        
        /**
         * 查询航班信息
         */
        public static java.util.List<FlightInfo> searchFlights(String departureAirport, String arrivalAirport, String date) {
            java.util.List<FlightInfo> flights = new java.util.ArrayList<>();
            
            String sql = "SELECT f.*, a.aircraft_type, a.total_seats " +
                        "FROM flights f " +
                        "JOIN aircraft a ON f.aircraft_id = a.aircraft_id " +
                        "WHERE f.departure_airport LIKE ? " +
                        "AND f.arrival_airport LIKE ? " +
                        "AND DATE(f.departure_time) = ? " +
                        "AND f.status = 'scheduled' " +
                        "ORDER BY f.departure_time";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + departureAirport + "%");
                stmt.setString(2, "%" + arrivalAirport + "%");
                stmt.setString(3, date);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        FlightInfo flight = new FlightInfo();
                        flight.flightId = rs.getString("flight_id");
                        flight.flightNumber = rs.getString("flight_number");
                        flight.aircraftId = rs.getString("aircraft_id");
                        flight.departureAirport = rs.getString("departure_airport");
                        flight.arrivalAirport = rs.getString("arrival_airport");
                        flight.departureTime = rs.getString("departure_time");
                        flight.arrivalTime = rs.getString("arrival_time");
                        flight.basePrice = rs.getDouble("base_price");
                        flight.availableSeats = rs.getInt("available_seats");
                        flight.status = rs.getString("status");
                        flight.gate = rs.getString("gate");
                        flight.terminal = rs.getString("terminal");
                        flight.aircraftType = rs.getString("aircraft_type");
                        flight.totalSeats = rs.getInt("total_seats");
                        
                        flights.add(flight);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("查询航班数据失败: " + e.getMessage());
            }
            
            return flights;
        }        /**
         * 获取所有可用的机场列表（从airports表和flights表获取）
         */
        public static java.util.List<String> getAvailableAirports() {
            java.util.List<String> airports = new java.util.ArrayList<>();
            
            // 从airports表获取有航班的机场，显示为"城市(机场名称)"格式
            String sql = "SELECT DISTINCT a.city, a.airport_name, a.airport_code " +
                        "FROM airports a " +
                        "WHERE a.airport_code IN (" +
                        "    SELECT DISTINCT departure_airport FROM flights " +
                        "    UNION " +
                        "    SELECT DISTINCT arrival_airport FROM flights" +
                        ") " +
                        "AND a.status = 'active' " +
                        "ORDER BY a.city";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String city = rs.getString("city");
                    String airportName = rs.getString("airport_name");
                    if (city != null && !city.trim().isEmpty()) {
                        // 格式："城市(机场名称)" 例如："北京(首都国际机场)"
                        String displayName = city + "(" + airportName.replace(city, "").replace("国际机场", "").replace("机场", "") + ")";
                        airports.add(displayName);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("获取机场列表失败: " + e.getMessage());
                // 如果数据库查询失败，提供默认机场列表
                airports.add("北京(首都国际)");
                airports.add("上海(虹桥国际)");
                airports.add("上海(浦东国际)");
                airports.add("广州(白云国际)");
                airports.add("深圳(宝安国际)");
                airports.add("成都(天府国际)");
                airports.add("重庆(江北国际)");
                airports.add("西安(咸阳国际)");
                airports.add("杭州(萧山国际)");
                airports.add("南京(禄口国际)");
            }
            
            return airports;
        }
          /**
         * 将城市名称或显示名称转换为机场代码
         */
        public static String cityToAirportCode(String input) {
            if (input == null || input.trim().isEmpty()) {
                return input;
            }
            
            String cityName = input.trim();
            
            // 如果输入是"城市(机场名称)"格式，提取城市名称
            if (cityName.contains("(") && cityName.contains(")")) {
                cityName = cityName.substring(0, cityName.indexOf("(")).trim();
            }
            
            // 从数据库查询机场代码
            String sql = "SELECT airport_code FROM airports WHERE city = ? AND status = 'active' LIMIT 1";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cityName);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("airport_code");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("查询机场代码失败: " + e.getMessage());
            }
            
            // 如果数据库查询失败，使用备用映射
            java.util.Map<String, String> cityMapping = new java.util.HashMap<>();
            cityMapping.put("北京", "PEK");
            cityMapping.put("上海", "SHA");
            cityMapping.put("广州", "CAN");
            cityMapping.put("深圳", "SZX");
            cityMapping.put("成都", "CTU");
            cityMapping.put("杭州", "HGH");
            cityMapping.put("西安", "XIY");
            cityMapping.put("重庆", "CKG");
            cityMapping.put("南京", "NKG");
            
            return cityMapping.getOrDefault(cityName, input);        }        /**
         * 获取数据库中有航班的日期列表（未来30天内）
         */
        public static java.util.List<String> getAvailableDates() {
            java.util.List<String> dates = new java.util.ArrayList<>();
            
            // 从数据库查询有航班的日期
            String sql = "SELECT DISTINCT DATE(departure_time) as flight_date " +
                        "FROM flights " +
                        "WHERE DATE(departure_time) >= DATE('now') " +
                        "AND DATE(departure_time) <= DATE('now', '+30 days') " +
                        "AND status = 'scheduled' " +
                        "ORDER BY flight_date";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String flightDate = rs.getString("flight_date");
                    if (flightDate != null) {
                        dates.add(flightDate);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("获取航班日期失败: " + e.getMessage());
                // 数据库查询失败时，不添加任何默认日期，只返回空列表
                // 这样用户就知道没有可用的航班日期
            }
            
            return dates;
        }
        
        /**
         * 格式化航班显示信息
         */
        public static String formatFlightDisplay(FlightInfo flight, String cabinClass) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                
                java.util.Date depTime = inputFormat.parse(flight.departureTime);
                java.util.Date arrTime = inputFormat.parse(flight.arrivalTime);
                
                String timeRange = timeFormat.format(depTime) + "-" + timeFormat.format(arrTime);
                
                // 根据舱位等级计算价格
                double price = flight.basePrice;
                if ("商务舱".equals(cabinClass)) {
                    price *= 2.5; // 商务舱价格为经济舱的2.5倍
                }
                
                return String.format("%s %s %s ¥%.0f (余票%d) [%s->%s]", 
                    flight.flightNumber,
                    timeRange,
                    cabinClass,
                    price,
                    flight.availableSeats,
                    flight.departureAirport,
                    flight.arrivalAirport);
                    
            } catch (java.text.ParseException e) {
                // 如果解析失败，返回简化格式
                return String.format("%s %s %s ¥%.0f (余票%d)", 
                    flight.flightNumber,
                    flight.departureTime.substring(11, 16) + "-" + flight.arrivalTime.substring(11, 16),
                    cabinClass,
                    flight.basePrice,
                    flight.availableSeats);
            }
        }
    }
    
    /**
     * 航班信息类
     */
    public static class FlightInfo {
        public String flightId;
        public String flightNumber;
        public String aircraftId;
        public String departureAirport;
        public String arrivalAirport;
        public String departureTime;
        public String arrivalTime;
        public double basePrice;
        public int availableSeats;
        public String status;
        public String gate;
        public String terminal;
        public String aircraftType;
        public int totalSeats;
    }
    
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
        int labelWidth = 80; // 统一标签宽度        // 获取可用机场列表
        java.util.List<String> airports = DatabaseManager.getAvailableAirports();
        String[] airportArray = airports.toArray(new String[0]);
        
        // 获取可用日期列表
        java.util.List<String> dates = DatabaseManager.getAvailableDates();
        String[] dateArray = dates.toArray(new String[0]);

        // 出发地
        JLabel departureLabel = new JLabel("出发地：", SwingConstants.RIGHT);
        departureLabel.setPreferredSize(new Dimension(labelWidth, 20));
        departureLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(departureLabel, gbc);

        JComboBox<String> departureCombo = new JComboBox<>(airportArray);
        departureCombo.setEditable(true); // 允许用户输入
        departureCombo.setPreferredSize(new Dimension(200, 25));
        departureCombo.setToolTipText("请选择或输入出发城市");
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(departureCombo, gbc);

        // 目的地
        JLabel destinationLabel = new JLabel("目的地：", SwingConstants.RIGHT);
        destinationLabel.setPreferredSize(new Dimension(labelWidth, 20));
        destinationLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(destinationLabel, gbc);

        JComboBox<String> destinationCombo = new JComboBox<>(airportArray);
        destinationCombo.setEditable(true); // 允许用户输入
        destinationCombo.setPreferredSize(new Dimension(200, 25));
        destinationCombo.setToolTipText("请选择或输入目的地城市");
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(destinationCombo, gbc);        // 日期（明确显示格式）
        JLabel dateLabel = new JLabel("日期：", SwingConstants.RIGHT);
        dateLabel.setPreferredSize(new Dimension(labelWidth, 20));
        dateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(dateLabel, gbc);        JComboBox<String> dateCombo = new JComboBox<>(dateArray);
        dateCombo.setEditable(true); // 允许用户输入自定义日期
        dateCombo.setPreferredSize(new Dimension(200, 25));
        dateCombo.setToolTipText("请选择或输入出行日期（YYYY-MM-DD格式）");
        gbc.gridx = 1;
        gbc.gridy = 3;
        mainPanel.add(dateCombo, gbc);

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
        frame.add(mainPanel, BorderLayout.CENTER);        // 事件处理
        queryButton.addActionListener(e -> {
            String departure = "";
            String destination = "";
              // 从下拉框获取选中或输入的值
            Object depObj = departureCombo.getSelectedItem();
            Object destObj = destinationCombo.getSelectedItem();
            
            if (depObj != null) {
                departure = depObj.toString().trim();
                // 将城市名称转换为机场代码
                departure = DatabaseManager.cityToAirportCode(departure);
            }
            if (destObj != null) {
                destination = destObj.toString().trim();
                // 将城市名称转换为机场代码
                destination = DatabaseManager.cityToAirportCode(destination);
            }
            
            String date = "";
            Object dateObj = dateCombo.getSelectedItem();
            if (dateObj != null) {
                date = dateObj.toString().trim();
            }
            String cabin = (String) cabinCombo.getSelectedItem();
            
            // 输入验证
            if (departure.isEmpty() || destination.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "请填写所有查询条件", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!isValidDate(date)) {
                JOptionPane.showMessageDialog(frame, "日期无效（必须为未来日期且格式为YYYY-MM-DD）",
                        "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 从数据库查询航班
            java.util.List<FlightInfo> flights = DatabaseManager.searchFlights(departure, destination, date);
            
            if (flights.isEmpty()) {
                resultArea.setText("未找到符合条件的航班。\n\n搜索条件：\n" +
                    "出发地：" + departure + "\n" +
                    "目的地：" + destination + "\n" +
                    "日期：" + date + "\n" +
                    "舱位：" + cabin + "\n\n" +
                    "建议：\n1. 检查城市名称是否正确\n2. 尝试其他日期\n3. 联系客服获取更多航班信息");
            } else {
                StringBuilder result = new StringBuilder();
                result.append("查询结果 (").append(flights.size()).append("个航班)：\n\n");
                
                for (FlightInfo flight : flights) {
                    String flightDisplay = DatabaseManager.formatFlightDisplay(flight, cabin);
                    result.append(flightDisplay).append("\n");
                    
                    // 添加航班详细信息
                    if (flight.gate != null && !flight.gate.isEmpty()) {
                        result.append("  登机口：").append(flight.gate);
                        if (flight.terminal != null && !flight.terminal.isEmpty()) {
                            result.append(" (").append(flight.terminal).append("航站楼)");
                        }
                        result.append("\n");
                    }
                    
                    if (flight.aircraftType != null && !flight.aircraftType.isEmpty()) {
                        result.append("  机型：").append(flight.aircraftType).append("\n");
                    }
                    
                    result.append("\n");
                }
                
                result.append("提示：选择航班后可进入预订流程");
                resultArea.setText(result.toString());
            }
        });        returnButton.addActionListener(e -> {
            departureCombo.setSelectedIndex(-1); // 清空选择
            destinationCombo.setSelectedIndex(-1); // 清空选择
            dateCombo.setSelectedIndex(-1); // 清空日期选择
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
