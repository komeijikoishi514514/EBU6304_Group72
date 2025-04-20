package com.wealthassistant.ui.panels;

import com.wealthassistant.dao.DataAccessException;
import com.wealthassistant.model.AppSettings;
import com.wealthassistant.service.BudgetService;
import com.wealthassistant.service.TransactionService;
import com.wealthassistant.ui.MainFrame;
import com.wealthassistant.ui.components.SummaryCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final MainFrame mainFrame;
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final AppSettings settings;
    
    private JComboBox<Integer> yearComboBox;
    private JComboBox<Month> monthComboBox;
    private JPanel summaryPanel;
    private JPanel budgetPanel;
    private JPanel alertPanel;
    
    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.transactionService = new TransactionService();
        this.budgetService = new BudgetService();
        this.settings = AppSettings.getInstance();
        
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(MainFrame.BACKGROUND_COLOR);
        
        initComponents();
    }
    
    private void initComponents() {
        // 顶部日期选择器
        JPanel datePanel = createDatePanel();
        add(datePanel, BorderLayout.NORTH);
        
        // 主面板
        JPanel mainPanel = new JPanel(new GridLayout(3, 1, 0, 20));
        mainPanel.setBackground(MainFrame.BACKGROUND_COLOR);
        
        // 摘要卡片
        summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setBackground(MainFrame.BACKGROUND_COLOR);
        
        // 添加标题
        JPanel summaryContainer = createSectionPanel("Financial Summary", summaryPanel);
        mainPanel.add(summaryContainer);
        
        // 预算面板
        budgetPanel = new JPanel(new BorderLayout());
        budgetPanel.setBackground(MainFrame.BACKGROUND_COLOR);
        JPanel budgetContainer = createSectionPanel("Budget Status", budgetPanel);
        mainPanel.add(budgetContainer);
        
        // 警告面板
        alertPanel = new JPanel(new BorderLayout());
        alertPanel.setBackground(MainFrame.BACKGROUND_COLOR);
        JPanel alertContainer = createSectionPanel("Alerts", alertPanel);
        mainPanel.add(alertContainer);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 快捷操作面板
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);
        
        // 加载初始数据
        updateDashboard();
    }
    
    private JPanel createSectionPanel(String title, JPanel contentPanel) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(MainFrame.BACKGROUND_COLOR);
        
        // 创建带圆角的面板边框
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                title
        );
        titledBorder.setTitleFont(MainFrame.SUBTITLE_FONT);
        titledBorder.setTitleColor(MainFrame.TEXT_COLOR);
        
        container.setBorder(titledBorder);
        container.add(contentPanel, BorderLayout.CENTER);
        
        return container;
    }
    
    private JPanel createDatePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(MainFrame.BACKGROUND_COLOR);
        
        // 添加标题标签
        JLabel titleLabel = new JLabel("Current View: ");
        titleLabel.setFont(MainFrame.SUBTITLE_FONT);
        titleLabel.setForeground(MainFrame.TEXT_COLOR);
        panel.add(titleLabel);
        
        // 年份选择器
        yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear - 5; year <= currentYear + 5; year++) {
            yearComboBox.addItem(year);
        }
        yearComboBox.setSelectedItem(currentYear);
        yearComboBox.setFont(MainFrame.NORMAL_FONT);
        yearComboBox.setPreferredSize(new Dimension(100, 30));
        
        // 月份选择器 - 使用月份名称而不是枚举
        monthComboBox = new JComboBox<>();
        for (Month month : Month.values()) {
            // 使用本地化的月份名称
            monthComboBox.addItem(month);
        }
        // 自定义渲染器以显示本地化月份名称
        monthComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Month) {
                    value = ((Month) value).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        monthComboBox.setSelectedItem(LocalDate.now().getMonth());
        monthComboBox.setFont(MainFrame.NORMAL_FONT);
        monthComboBox.setPreferredSize(new Dimension(100, 30));
        
        JLabel yearLabel = new JLabel("Year:");
        yearLabel.setFont(MainFrame.NORMAL_FONT);
        panel.add(yearLabel);
        panel.add(yearComboBox);
        
        JLabel monthLabel = new JLabel("Month:");
        monthLabel.setFont(MainFrame.NORMAL_FONT);
        panel.add(monthLabel);
        panel.add(monthComboBox);
        
        JButton updateButton = new JButton("Update");
        updateButton.setFont(MainFrame.NORMAL_FONT);
        updateButton.setBackground(MainFrame.PRIMARY_COLOR);
        updateButton.setForeground(Color.WHITE);
        updateButton.setFocusPainted(false);
        updateButton.setBorderPainted(false);
        updateButton.addActionListener(e -> updateDashboard());
        panel.add(updateButton);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(MainFrame.BACKGROUND_COLOR);
        
        // 标题
        JLabel titleLabel = new JLabel("Quick Actions");
        titleLabel.setFont(MainFrame.SUBTITLE_FONT);
        titleLabel.setForeground(MainFrame.TEXT_COLOR);
        
        // 创建按钮
        JButton addTransactionButton = createStyledButton("Add Transaction", "", MainFrame.PRIMARY_COLOR);
        addTransactionButton.addActionListener(e -> mainFrame.navigateTo(MainFrame.MANUAL_ENTRY));
        
        JButton importButton = createStyledButton("Import Transactions", "", new Color(52, 152, 219));
        importButton.addActionListener(e -> mainFrame.navigateTo(MainFrame.IMPORT_TRANSACTIONS));
        
        JButton viewHistoryButton = createStyledButton("View History", "", new Color(155, 89, 182));
        viewHistoryButton.addActionListener(e -> mainFrame.navigateTo(MainFrame.TRANSACTION_HISTORY));
        
        JButton viewReportsButton = createStyledButton("View Reports", "", new Color(241, 196, 15));
        viewReportsButton.addActionListener(e -> mainFrame.navigateTo(MainFrame.REPORTS));
        
        // 创建包含标题和按钮的面板
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(MainFrame.BACKGROUND_COLOR);
        titlePanel.add(titleLabel);
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsPanel.setBackground(MainFrame.BACKGROUND_COLOR);
        buttonsPanel.add(addTransactionButton);
        buttonsPanel.add(importButton);
        buttonsPanel.add(viewHistoryButton);
        buttonsPanel.add(viewReportsButton);
        
        // 将标题和按钮添加到主面板
        panel.setLayout(new BorderLayout());
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(buttonsPanel, BorderLayout.CENTER);
        
        // 给面板添加边框
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));
        
        return panel;
    }
    
    private JButton createStyledButton(String text, String icon, Color color) {
        JButton button;
        if (icon != null && !icon.isEmpty()) {
            button = new JButton(icon + " " + text);
        } else {
            button = new JButton(text);
        }
        
        button.setFont(MainFrame.NORMAL_FONT);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    public void updateDashboard() {
        try {
            int year = (int) yearComboBox.getSelectedItem();
            Month month = (Month) monthComboBox.getSelectedItem();
            
            updateSummaryCards(year, month);
            updateBudgetPanel(year, month);
            updateAlertPanel(year, month);
            
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Error loading data: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateSummaryCards(int year, Month month) throws DataAccessException {
        summaryPanel.removeAll();
        
        // 收入卡片
        double totalIncome = transactionService.getTotalIncomeForMonth(year, month);
        SummaryCard incomeCard = new SummaryCard(
                "Total Income", 
                String.format("%s %.2f", settings.getDefaultCurrency(), totalIncome), 
                new Color(46, 204, 113),
                "Total income for the month");
        
        // 支出卡片
        double totalExpense = transactionService.getTotalExpenseForMonth(year, month);
        SummaryCard expenseCard = new SummaryCard(
                "Total Expense", 
                String.format("%s %.2f", settings.getDefaultCurrency(), totalExpense), 
                new Color(231, 76, 60),
                "Total expense for the month");
        
        // 净余额卡片
        double netBalance = totalIncome - totalExpense;
        Color balanceColor = netBalance >= 0 ? new Color(52, 152, 219) : new Color(231, 76, 60);
        SummaryCard balanceCard = new SummaryCard(
                "Net Balance", 
                String.format("%s %.2f", settings.getDefaultCurrency(), netBalance), 
                balanceColor,
                "Remaining amount after income minus expense");
        
        summaryPanel.add(incomeCard);
        summaryPanel.add(expenseCard);
        summaryPanel.add(balanceCard);
        
        summaryPanel.revalidate();
        summaryPanel.repaint();
    }
    
    private void updateBudgetPanel(int year, Month month) throws DataAccessException {
        budgetPanel.removeAll();
        
        // 获取预算状态
        Map<String, BudgetService.BudgetStatus> budgetStatus = budgetService.checkBudgetStatus(year, month);
        
        if (budgetStatus.isEmpty()) {
            budgetPanel.add(new JLabel("No budget data found, please set a budget in the settings."), BorderLayout.CENTER);
        } else {
            // 创建表格显示预算状态
            String[] columnNames = {"Category", "Spent", "Limit", "Percentage", "Status"};
            Object[][] data = new Object[budgetStatus.size()][5];
            
            int i = 0;
            for (Map.Entry<String, BudgetService.BudgetStatus> entry : budgetStatus.entrySet()) {
                String category = entry.getKey();
                BudgetService.BudgetStatus status = entry.getValue();
                
                data[i][0] = category;
                data[i][1] = String.format("%.2f", status.getSpent());
                data[i][2] = String.format("%.2f", status.getLimit());
                data[i][3] = String.format("%.1f%%", status.getPercentage());
                
                switch (status.getStatus()) {
                    case NORMAL:
                        data[i][4] = "Normal";
                        break;
                    case WARNING:
                        data[i][4] = "Near Limit";
                        break;
                    case OVER_BUDGET:
                        data[i][4] = "Over Budget";
                        break;
                }
                
                i++;
            }
            
            JTable budgetTable = new JTable(data, columnNames) {
                @Override
                public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                    Component c = super.prepareRenderer(renderer, row, column);
                    
                    if (column == 4) {
                        if (data[row][4].equals("Over Budget")) {
                            c.setForeground(new Color(231, 76, 60));
                        } else if (data[row][4].equals("Near Limit")) {
                            c.setForeground(new Color(243, 156, 18));
                        } else {
                            c.setForeground(new Color(46, 204, 113));
                        }
                    } else {
                        c.setForeground(Color.BLACK);
                    }
                    
                    return c;
                }
            };
            
            budgetTable.setRowHeight(30);
            budgetPanel.add(new JScrollPane(budgetTable), BorderLayout.CENTER);
        }
        
        budgetPanel.revalidate();
        budgetPanel.repaint();
    }
    
    private void updateAlertPanel(int year, Month month) throws DataAccessException {
        alertPanel.removeAll();
        
        Map<String, BudgetService.BudgetStatus> budgetStatus = budgetService.checkBudgetStatus(year, month);
        
        // 查找超出预算的类别
        boolean hasAlerts = false;
        JPanel alertsPanel = new JPanel();
        alertsPanel.setLayout(new BoxLayout(alertsPanel, BoxLayout.Y_AXIS));
        
        for (Map.Entry<String, BudgetService.BudgetStatus> entry : budgetStatus.entrySet()) {
            if (entry.getValue().getStatus() == BudgetService.BudgetStatus.Status.OVER_BUDGET) {
                hasAlerts = true;
                
                double overspent = entry.getValue().getSpent() - entry.getValue().getLimit();
                String message = String.format("Warning: %s category has exceeded the budget by %.2f %s", 
                        entry.getKey(), overspent, settings.getDefaultCurrency());
                
                JPanel alertItemPanel = new JPanel(new BorderLayout());
                alertItemPanel.add(new JLabel(message), BorderLayout.CENTER);
                
                JButton acknowledgeButton = new JButton("Got it");
                acknowledgeButton.addActionListener(e -> {
                    alertItemPanel.setVisible(false);
                    alertsPanel.revalidate();
                });
                alertItemPanel.add(acknowledgeButton, BorderLayout.EAST);
                
                alertsPanel.add(alertItemPanel);
                alertsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
        if (hasAlerts) {
            JScrollPane scrollPane = new JScrollPane(alertsPanel);
            alertPanel.add(scrollPane, BorderLayout.CENTER);
        } else {
            alertPanel.add(new JLabel("No budget alerts to note."), BorderLayout.CENTER);
        }
        
        alertPanel.revalidate();
        alertPanel.repaint();
    }
} 