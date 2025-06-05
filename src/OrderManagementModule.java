import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.table.DefaultTableModel;

public class OrderManagementModule extends JFrame {

    private String currentUser; // 当前登录用户
    private JTable orderTable;
    private JComboBox<String> statusFilter;

    public OrderManagementModule(String username) {
        this.currentUser = username;
        setTitle("订单管理 - " + username);
        setSize(1000, 700); // 增大窗口以适应更大的字体
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 设置全局字体（增大3号）
        Font largerFont = new Font("微软雅黑", Font.PLAIN, 16); // 原14号增大到16号
        UIManager.put("Label.font", largerFont);
        UIManager.put("Button.font", largerFont);
        UIManager.put("ComboBox.font", largerFont);
        UIManager.put("Table.font", largerFont);
        UIManager.put("TableHeader.font", largerFont);

        // 顶部过滤面板
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        filterPanel.setBackground(Color.WHITE);

        JLabel filterLabel = new JLabel("订单状态:");
        filterLabel.setFont(largerFont);

        statusFilter = new JComboBox<>(new String[]{"全部", "待支付", "已完成", "已退票"});
        statusFilter.setFont(largerFont);        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(largerFont);

        JButton paymentBtn = new JButton("支付管理");
        paymentBtn.setFont(largerFont);
        paymentBtn.setBackground(new Color(76, 175, 80));
        paymentBtn.setForeground(Color.WHITE);

        JButton refundBtn = new JButton("退票管理");
        refundBtn.setFont(largerFont);
        refundBtn.setBackground(new Color(244, 67, 54));
        refundBtn.setForeground(Color.WHITE);        filterPanel.add(filterLabel);
        filterPanel.add(statusFilter);
        filterPanel.add(refreshBtn);
        filterPanel.add(paymentBtn);
        filterPanel.add(refundBtn);

        // 添加事件监听器
        refreshBtn.addActionListener(e -> loadOrders(null));
        paymentBtn.addActionListener(this::openPaymentModule);
        refundBtn.addActionListener(this::openRefundModule);        // 订单表格
        String[] columns = {"订单号", "航班号", "出发地", "目的地", "日期", "起飞时间", "降落时间", "状态", "金额"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderTable = new JTable(model);
        orderTable.setFont(largerFont);
        orderTable.setRowHeight(35); // 增大行高以适应更大的字体
        orderTable.getTableHeader().setFont(largerFont);

        JScrollPane tableScroll = new JScrollPane(orderTable);

        // 组装界面
        add(filterPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);

        // 事件处理
        refreshBtn.addActionListener(this::loadOrders);
        statusFilter.addActionListener(this::loadOrders);

        // 初始加载数据
        loadOrders(null);

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void loadOrders(ActionEvent e) {
        String selectedStatus = (String) statusFilter.getSelectedItem();
        DefaultTableModel model = (DefaultTableModel) orderTable.getModel();
        model.setRowCount(0);        Object[][] sampleData = {
                {"ORD1001", "MU5112", "北京", "上海", "2023-06-15", "08:00", "10:00", "已完成", 1580.0},
                {"ORD1002", "CA1833", "广州", "成都", "2023-06-20", "12:30", "14:45", "待支付", 890.0},
                {"ORD1003", "CZ3108", "深圳", "重庆", "2023-06-25", "15:20", "17:30", "已退票", 750.0}
        };        for (Object[] row : sampleData) {
            if ("全部".equals(selectedStatus) || selectedStatus.equals(row[7])) {
                model.addRow(row);
            }
        }

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "没有找到符合条件的订单", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }    private void openPaymentModule(ActionEvent e) {
        // 获取选中的订单
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            String orderId = (String) orderTable.getValueAt(selectedRow, 0);
            String status = (String) orderTable.getValueAt(selectedRow, 7);
            Double amount = (Double) orderTable.getValueAt(selectedRow, 8);
            
            if ("待支付".equals(status)) {
                // 隐藏当前窗口
                this.setVisible(false);
                PaymentModule paymentModule = new PaymentModule(orderId, amount);
                paymentModule.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                        OrderManagementModule.this.setVisible(true); // 返回订单管理窗口
                        loadOrders(null); // 刷新订单列表
                    }
                });
                paymentModule.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "只能处理待支付订单的支付！", "提示", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择一个订单！", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openRefundModule(ActionEvent e) {
        // 获取选中的订单
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            String orderId = (String) orderTable.getValueAt(selectedRow, 0);
            String status = (String) orderTable.getValueAt(selectedRow, 7);
            
            if ("已完成".equals(status)) {
                // 隐藏当前窗口
                this.setVisible(false);
                RefundModule refundModule = new RefundModule(orderId);
                refundModule.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                        OrderManagementModule.this.setVisible(true); // 返回订单管理窗口
                        loadOrders(null); // 刷新订单列表
                    }
                });
                refundModule.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "只能退已完成的订单！", "提示", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择一个订单！", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OrderManagementModule("testUser").setVisible(true));
    }
}
